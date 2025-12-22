package game.single.service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.websocket.Session;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class SingleGameServiceImpl implements SingleGameService {

//	private static SingleGameServiceImpl instance = new SingleGameServiceImpl();

//	public static SingleGameServiceImpl getInstance() {
//		return instance;
//	}
	private final ScheduledExecutorService scheduler;

	private List<Session> players = new ArrayList<>();
	private AtomicInteger turn = new AtomicInteger(0);
	private int[][] board = new int[15][15];
	private AtomicBoolean gameOver = new AtomicBoolean(false);
	private final Gson gson = new Gson();

	private final String roomId;
	private final java.util.Map<Session, String> sessionToUserId = new java.util.concurrent.ConcurrentHashMap<>();

	private ScheduledFuture<?> turnTask;

	private int wpass = 0;
	private int bpass = 0;

	public SingleGameServiceImpl(String roomId, ScheduledExecutorService scheduler) {
	    this.roomId = roomId;
	    this.scheduler = scheduler;
	}
	
	@Override
	public synchronized void onOpen(Session session, String userId) throws Exception {
		if (players.size() >= 2) {
	        session.close();
	        return;
	    }

	    // 1) 먼저 등록
	    players.add(session);
	    sessionToUserId.put(session, userId);

	    System.out.println("접속: " + session.getId());

	    // 2) 아직 2명 안 찼으면 WAIT 브로드캐스트
	    if (players.size() < 2) {

	        String jsonStr =
	            "{ \"type\":\"SINGLE_WAIT\", " +
	            "\"msg\":\"다른 플레이어를 기다리는 중입니다... (" + players.size() + "/2)\" }";

	        for (Session s : players) {
	            try {
	                s.getBasicRemote().sendText(jsonStr);
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	        return;
	    }

	    // 3) 2명 찼으면 START를 각각 보내기(각자 색 다르게!)
	    for (int i = 0; i < players.size(); i++) {
	        Session s = players.get(i);
	        int color = i + 1; // 1=흑, 2=백
	        s.getBasicRemote().sendText("{ \"type\":\"SINGLE_START\", \"color\":" + color + " }");
	    }

	    // 4) 게임 시작
	    turn.set(0);
	    bpass = 0;
	    wpass = 0;
	    gameOver.set(false);

	    broadcastTurn();
	    startTurnTimer();
	}

	@Override
	public synchronized void onMessage(String msg, Session session) throws Exception {
		if (gameOver.get())
			return;
		
		JsonObject json = gson.fromJson(msg, JsonObject.class);

		if (msg.contains("SINGLE_GIVEUP")) {
			int loserIdx = players.indexOf(session);
			int winnerIdx = 1 - loserIdx;

			String loserId = sessionToUserId.get(players.get(loserIdx));
			String winnerId = sessionToUserId.get(players.get(winnerIdx));

			// null 방어(혹시 저장 안 된 경우)
			if (winnerId != null && loserId != null) {
				saveGameResultToAPI(roomId, winnerId, winnerIdx, loserId, loserIdx);
			}

			gameOver.set(true);

			if (turnTask != null)
				turnTask.cancel(false);

			for (Session s : players) {
				s.getBasicRemote().sendText(
					"{ \"type\":\"SINGLE_GIVEUP\", \"losercolor\":" + (loserIdx + 1) + "}");
			}
			return;
		}

		if (players.get(turn.get()) != session)
			return;
		
		if (!json.has("x") || !json.has("y")) {
			return;
		}

		int x = json.get("x").getAsInt();
		int y = json.get("y").getAsInt();

        // 좌표 범위 체크
        if (x < 0 || x >= 15 || y < 0 || y >= 15) {
        	return;
        }
        
		if (board[y][x] != 0) {
			return;
		}
		int color = turn.get() + 1;
		board[y][x] = color;

		for (Session s : players) {
			s.getBasicRemote().sendText(
				"{ \"type\":\"SINGLE_STONE\", \"x\":" + x +
					", \"y\":" + y +
					", \"color\":" + color + " }");
		}

		int wincolor = checkOmok();
		if (wincolor != 0) {
			int winnerIdx = wincolor - 1;
			int loserIdx = 1 - winnerIdx;

			String winnerId = sessionToUserId.get(players.get(winnerIdx));
			String loserId = sessionToUserId.get(players.get(loserIdx));

			if (winnerId != null && loserId != null) {
				saveGameResultToAPI(roomId, winnerId, winnerIdx, loserId, loserIdx);
			}

			gameOver.set(true);
			if (turnTask != null)
				turnTask.cancel(false);
			for (Session s : players) {
				s.getBasicRemote().sendText(
					"{ \"type\":\"SINGLE_WIN\", \"color\":" + wincolor + " }");
			}
			return;
		}

		turn.set(1 - turn.get());
		startTurnTimer();
		broadcastTurn();
	}

	@Override
	public synchronized void onClose(Session session) {
		players.remove(session);
		sessionToUserId.remove(session);

	    // 남은 사람이 없으면 게임 리셋(혹은 게임 종료 처리)
	    if (players.isEmpty()) {
	        board = new int[15][15];
	        turn.set(0);
	        gameOver.set(false);
	        bpass = 0;
	        wpass = 0;
	        if (turnTask != null) turnTask.cancel(false);
	    }
	    System.out.println("연결 종료: " + session.getId());
	}

	private synchronized void startTurnTimer() {
		if (turnTask != null && !turnTask.isDone())
			turnTask.cancel(false);

		turnTask = scheduler.schedule(() -> {
			if (gameOver.get())
				return;

			if (turn.get() + 1 == 1)
				bpass++;
			else
				wpass++;

			if (bpass == 3 || wpass == 3) {
				int lose = (bpass == 3 ? 1 : 2);
				int loserIdx = lose - 1;
				int winnerIdx = 1 - loserIdx;

				String loserId = sessionToUserId.get(players.get(loserIdx));
				String winnerId = sessionToUserId.get(players.get(winnerIdx));

				if (winnerId != null && loserId != null) {
					saveGameResultToAPI(roomId, winnerId, winnerIdx, loserId, loserIdx);
				}

				for (Session s : players) {
					try {
						s.getBasicRemote().sendText(
							"{ \"type\":\"SINGLE_GIVEUP\", \"losercolor\":" + lose + "}");
					} catch (Exception e) {}
				}
				return;
			}

			turnDelay();
		}, 15, TimeUnit.SECONDS);
	}

	private void turnDelay() {
		for (Session s : players) {
			try {
				s.getBasicRemote().sendText(
					"{ \"type\":\"delay\", \"delayColor\":" + (turn.get() + 1) + "}");
			} catch (Exception e) {}
		}

		turnTask = scheduler.schedule(() -> {
			turn.set(1 - turn.get());
			broadcastTurn();
			startTurnTimer();
		}, 2, TimeUnit.SECONDS);
	}

	private void broadcastTurn() {
		int color = turn.get() + 1;
		for (Session s : players) {
			try {
				s.getBasicRemote().sendText(
					"{ \"type\":\"SINGLE_TURN\", \"color\":" + color +
						", \"bpass\":" + bpass +
						", \"wpass\":" + wpass +
						", \"time\":15 }");
			} catch (Exception e) {}
		}
	}
	
	public void destroy() {
        if (turnTask != null) {
            turnTask.cancel(false);
        }
    }
	
	private void saveGameResultToAPI(String roomId, String winnerId, int winnerIdx, String loserId, int loserIdx) {
        try {
        	String gameId = UUID.randomUUID().toString();

            int winnerStoneColor = winnerIdx + 1; // 0→1(흑), 1→2(백)
            int loserStoneColor  = loserIdx + 1;
            
            String json = String.format("""
                {
                  "gameId": "%s",
                  "roomId": "%s",
                  "playType": "0",
                  "results": [
                    {
                      "userId": "%s",
                      "stoneColor": "%d",
                      "gameResult": "W"
                    },
                    {
                      "userId": "%s",
                      "stoneColor": "%d",
                      "gameResult": "L"
                    }
                  ]
                }
                """,
                gameId,
                roomId,
                winnerId,
                winnerStoneColor,
                loserId,
                loserStoneColor
            );
            
            // 2. HTTP POST 요청
            URL url = new URL("http://localhost:8089/record/save");  // :dart: 네 Endpoint!
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            
            // 3. JSON 전송
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes("UTF-8"));
            }
            
            // 4. 응답 확인
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                System.out.println(":o: 게임 결과 저장 성공 gameId=" + gameId);
            } else {
                System.err.println(":x: 게임 결과 저장 실패: " + responseCode);
            }
            
        } catch (Exception e) {
            System.err.println(":x: 게임 결과 API 호출 실패");
            e.printStackTrace();
            // 게임은 계속 진행 (저장 실패해도 게임 종료는 알림)
        }
    }

	private int checkOmok() {
		int[][] directions = {
			{0, 1},
			{1, 0},
			{1, 1},
			{1, -1}
		};

		for (int row = 0; row < 15; row++) {
			for (int col = 0; col < 15; col++) {
				int stone = board[row][col];

				if (stone == 0) {
					continue;
				}

				for (int[] dir : directions) {
					int cnt = 1;
					for (int step = 1; step < 5; step++) {
						int newRow = row + dir[0] * step;
						int newCol = col + dir[1] * step;

						if (newRow < 0 || newRow >= 15 || newCol < 0 || newCol >= 15) {
							break;
						}

						if (board[newRow][newCol] == stone) {
							cnt++;
						} else {
							break;
						}
					}

					if (cnt >= 5) {
						return stone;
					}
				}
			}
		}
		return 0; 
	}

}
