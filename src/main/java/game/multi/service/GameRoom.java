package game.multi.service;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.websocket.Session;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import game.multi.service.MultiGameService.SendJob;

// 하나의 게임 방을 나타내는 객체
public class GameRoom {
    
    private final String roomId;
    
	// 세션별 Player 정보를 관리하기 위한 맵 (세션 ID -> Player 객체)
    private final Map<String, Player> playersMap = new ConcurrentHashMap<>();
    
	// 순서 유지를 위한 리스트 (0,1,2,3번 자리)
    private final List<Session> sessionList = Collections.synchronizedList(new ArrayList<>());
    
    // 게임 상태 변수들 (static 제거)
    private int turn = 0;
    private int[][] board = new int[15][15];
    private boolean gameOver = false;
    private final Gson gson = new Gson();
    
    // 타이머 관련
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> turnTask;

    public GameRoom(String roomId) {
        this.roomId = roomId;
    }
    
    private final Map<Session, String> tempUserIdMap = new ConcurrentHashMap<>();

	/**
	 * 입장 처리
	 * @param session
	 * @param userId 
	 * @return
	 */
	public synchronized List<SendJob> handleOpen(Session session, String userId) {
		List<SendJob> out = new ArrayList<>();
		
		tempUserIdMap.put(session, userId);
		
		// 게임 시작 전이라면 끊긴 세션 정리
		if (sessionList.size() < 4) {
			sessionList.removeIf(s -> !s.isOpen());
		}

		if (sessionList.size() >= 4) {
			out.add(createErrorJob(session, "ROOM_FULL", "방이 꽉 찼습니다."));
			return out;
		}

		// 리스트에 세션 등록
		sessionList.add(session);
		System.out.println("입장: " + session.getId() + " (현재 " + sessionList.size() + "/4명)");

		// 4명이 안 찼으면 대기 메시지 전송
		if (sessionList.size() < 4) {
			JsonObject j = new JsonObject();
			j.addProperty("type", "MULTI_WAIT");
			j.addProperty("msg", "다른 플레이어를 기다리는 중입니다... (" + sessionList.size() + "/4)");
			
			String jsonStr = gson.toJson(j);
			
			// 리스트에 있는 모든 session에게 개별 전송
			for (Session s : sessionList) {
				out.add(new SendJob(s, jsonStr));
			}
			
			return out;
		}

		// 4명 입장 완료 시 게임 시작
		if (sessionList.size() == 4) {
			System.out.println("4명 접속 완료! 게임을 시작합니다.");

			board = new int[15][15];
			gameOver = false;

			Collections.shuffle(sessionList); // 랜덤 섞기

			// 플레이어 객체 생성 및 시작 메시지 전송
			for (int i = 0; i < 4; i++) {
				Session s = sessionList.get(i);
				int color = (i % 2 == 0) ? 1 : 2; // 0,2=흑 / 1,3=백
				int team = (i % 2 == 0) ? 0 : 1;
				String uId = tempUserIdMap.get(s);

				// Player 객체 생성
				Player p = new Player(s, uId, i, team, color);
				playersMap.put(s.getId(), p);

				// 시작 메시지 JSON 생성
				JsonObject startJson = new JsonObject();
				startJson.addProperty("type", "GAME_MULTI_START");
				startJson.addProperty("color", color);
				startJson.addProperty("slot", i); // 본인의 슬롯 번호(0~3)

				out.add(new SendJob(s, gson.toJson(startJson)));
			}

			// 첫 턴 알림
			turn = 0;
			out.addAll(broadcastTurn());
			startTurnTimer();
		}

		return out;
	}

	/**
	 * 메시지 처리
	 * @param session
	 * @param msg
	 * @return
	 */
	public synchronized List<SendJob> handleMessage(Session session, String msg) {
		List<SendJob> out = new ArrayList<>();
		if (gameOver) {
			return out;
		}

		try {
			JsonObject json = gson.fromJson(msg, JsonObject.class);

			// 기권 처리
			if (json.has("type") && "MULTI_GIVEUP".equals(json.get("type").getAsString())) {
				Player p = playersMap.get(session.getId());
				if (p != null && p.isAlive()) {
					p.disconnect(); // 사망 처리
					
					// 팀 전멸 체크
					if (isTeamDead(p.getTeam())) {
						int winTeam = (p.getTeam() == 0) ? 1 : 0;
						saveGameResult(winTeam);
						endGame(out, (p.getTeam() == 0 ? "흑돌" : "백돌") + " 팀 전원 기권패!");
					} else {
						// 1:2 상황으로 진행
						JsonObject notice = new JsonObject();
						notice.addProperty("type", "MULTI_WAIT");
						notice.addProperty("msg", "플레이어가 기권했습니다. 남은 팀원이 이어받습니다.");
						addBroadcastJobs(out, gson.toJson(notice));
						
						// 턴 상태 갱신
						out.addAll(broadcastTurn());
						startTurnTimer();
					}
				}
				return out;
			}

			// 좌표 처리
			if (!json.has("x") || !json.has("y")) {
				return out;
			}
			int x = json.get("x").getAsInt();
			int y = json.get("y").getAsInt();

			// 유효성 검사
			if (x < 0 || x >= 15 || y < 0 || y >= 15) {
				return out;
			}

			if (board[y][x] != 0) {
				return out;
			}

			// 턴 검사 (현재 턴의 실제 수행자가 맞는지 확인)
			int actorIdx = getActorIndex(turn);
			if (actorIdx == -1) { // 수행자 없음
				return out;
			}
			
			Session activeSession = sessionList.get(actorIdx);
			if (!activeSession.equals(session)) {
				return out; // 내 차례 아님
			}

			// 착수
			int color = (turn % 2 == 0) ? 1 : 2;
			board[y][x] = color;

			// 업데이트 전송 (전체)
			JsonObject updateJson = new JsonObject();
			updateJson.addProperty("type", "MULTI_STONE");
			updateJson.addProperty("x", x);
			updateJson.addProperty("y", y);
			updateJson.addProperty("color", color);
			// 현재 방에 있는 sessionList 사람들에게만 전송
			addBroadcastJobs(out, gson.toJson(updateJson));

			// 승리 체크
			int winColor = checkOmok();
			if (winColor != 0) {
				gameOver = true;
				if (turnTask != null) {
					turnTask.cancel(true);
				}
				
				int winTeam = (winColor == 1) ? 0 : 1;
				saveGameResult(winTeam);
				
				JsonObject winJson = new JsonObject();
				winJson.addProperty("type", "MULTI_WIN");
				winJson.addProperty("color", winColor);
				addBroadcastJobs(out, gson.toJson(winJson));
				return out;
			}

			// 턴 넘기기
			nextTurn();
			out.addAll(broadcastTurn());
			startTurnTimer();

		} catch (Exception e) {
			e.printStackTrace();
		}
		return out;
	}
    

	/**
	 * 퇴장 처리
	 * @param session
	 * @return
	 */
	public synchronized List<SendJob> handleClose(Session session) {
		tempUserIdMap.remove(session);
		List<SendJob> out = new ArrayList<>();
		
		if (gameOver) {
			sessionList.remove(session);
			playersMap.remove(session.getId());
			
			// 마지막 사람까지 다 나가면 방 초기화
			if (sessionList.isEmpty()) {
				boardInitializer();
				System.out.println("게임 종료 후 초기화 완료");
			}
			return out;
		}
		
		// 게임 중일 때는 '사망' 처리함 (인덱스 유지를 위해)
		if (sessionList.size() == 4) {
			Player p = playersMap.get(session.getId());
			if (p != null && p.isAlive()) {
				p.disconnect();
				System.out.println("게임 중 퇴장: " + session.getId());
				
				if (isTeamDead(p.getTeam())) {
					endGame(out, "상대 팀 전원 퇴장으로 승리!");
					// 게임 종료 후 정리
					sessionList.clear();
					playersMap.clear();
				} else {
					// 1명만 나감 -> 게임 계속
					JsonObject notice = new JsonObject();
					notice.addProperty("type", "MULTI_WAIT");
					notice.addProperty("msg", "플레이어가 나갔습니다. 남은 팀원이 이어받습니다.");
					addBroadcastJobs(out, gson.toJson(notice));
					
					out.addAll(broadcastTurn());
					startTurnTimer();
				}
			}
		} else {
			// 대기 중일 떄는 그냥 삭제
			sessionList.remove(session);
			playersMap.remove(session.getId());
			System.out.println("대기 중 퇴장 남은 인원: " + sessionList.size());
			
			JsonObject j = new JsonObject();
			j.addProperty("type", "MULTI_WAIT");
			j.addProperty("msg", "플레이어 퇴장. 현재 (" + sessionList.size() + "/4)");
			addBroadcastJobs(out, gson.toJson(j));
		}
		
		// 모두 나갔으면 초기화
		if (sessionList.isEmpty()) {
			boardInitializer();
		}
		return out;
	}
    
    /**
     * 방이 삭제될 때 스케줄러(타이머)를 강제로 종료하여 메모리 누수를 방지함
     */
    public void destroy() {
        // 1. 현재 돌고 있는 타이머 태스크 취소
        if (turnTask != null) {
            turnTask.cancel(true);
        }
        
        // 2. 스케줄러 스레드 종료 (메모리 누수 방지)
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown();
            try {
                // 혹시 안 꺼지면 1초 정도 기다려줌
                if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
            }
        }
    }
    
    
	// ---------------------------------------------------------------
	// 보조 메서드
	// ---------------------------------------------------------------	

	private void nextTurn() {
		turn = (turn + 1) % 4;
	}
	
    public boolean isEmpty() {
        return sessionList.isEmpty();
    }

	private void endGame(List<SendJob> out, String msg) {
		gameOver = true;
		JsonObject json = new JsonObject();
		json.addProperty("type", "GAME_OVER");
		json.addProperty("msg", msg);
		out.add(new SendJob(null, gson.toJson(json)));
	}
	
	// 방 초기화
	private void boardInitializer() {
		board = new int[15][15];
		turn = 0;
		gameOver = false;
		if (turnTask != null) {
			turnTask.cancel(false);
		}
	}

	// 턴 알림 생성
	// 실제 둘 사람(Actor)을 계산해서 보냄
	private List<SendJob> broadcastTurn() {
		int color = (turn % 2 == 0) ? 1 : 2;
		
		// 현재 턴의 주인이 죽었으면 대리인(같은 팀) 찾음
		int actorIdx = getActorIndex(turn);
		
		JsonObject turnJson = new JsonObject();
		turnJson.addProperty("type", "MULTI_TURN");
		turnJson.addProperty("color", color);
		turnJson.addProperty("time", 15);
		turnJson.addProperty("turnIdx", actorIdx); 

		List<SendJob> jobs = new ArrayList<>();
		addBroadcastJobs(jobs, gson.toJson(turnJson));
		return jobs;
	}
	
	// 특정 인덱스의 플레이어 가져오기
	private Player getPlayerAt(int index) {
		if (index < 0 || index >= sessionList.size()) {
			return null;
		}
		
		try {
			return playersMap.get(sessionList.get(index).getId());
		} catch (Exception e) {
			return null;
		}
	}
	
	// 현재 턴을 수행해야 할 사람 찾기
	private int getActorIndex(int turnIndex) {
		Player p = getPlayerAt(turnIndex);
		
		// 본인이 살아있으면 본인
		if (p != null && p.isAlive()) {
			return turnIndex;
		}
		
		// 본인이 죽었으면 같은 팀원 (0<->2, 1<->3)
		int teammateIndex = (turnIndex + 2) % 4;
		Player t = getPlayerAt(teammateIndex);
		if (t != null && t.isAlive()) {
			return teammateIndex;
		}
		
		return -1; // 둘 다 죽음 (게임 종료)
	}
	
	private boolean isTeamDead(int team) {
		int idx1 = (team == 0) ? 0 : 1;
		int idx2 = idx1 + 2;
		Player p1 = getPlayerAt(idx1);
		Player p2 = getPlayerAt(idx2);
		
		// 접속 전이거나(null) 죽었으면(!alive) 죽은 것으로 간주
		boolean p1Dead = (p1 == null || !p1.isAlive());
		boolean p2Dead = (p2 == null || !p2.isAlive());
		return p1Dead && p2Dead;
	}
	
	// 안전한 전체 전송 헬퍼 (방 내부 인원에게만 전송)
	private void addBroadcastJobs(List<SendJob> out, String json) {
		for (Session s : sessionList) {
			if (s.isOpen()) {
				out.add(new SendJob(s, json));
			}
		}
	}

	// 에러 메시지 생성 헬퍼
	private SendJob createErrorJob(Session session, String code, String msg) {
		JsonObject json = new JsonObject();
		json.addProperty("type", "error");
		json.addProperty("code", code);
		json.addProperty("msg", msg);
		return new SendJob(session, gson.toJson(json));
	}
	
	// 게임 결과 API 저장 메서드
	private void saveGameResult(int winTeam) {
		try {
			String gameId = UUID.randomUUID().toString();
			StringBuilder resultsJson = new StringBuilder();
			
			boolean first = true;
			for (Player p : playersMap.values()) {
				if (!first) {
					resultsJson.append(",");
				}
				first = false;
				
				String result = (p.getTeam() == winTeam) ? "W" : "L";
				
				resultsJson.append(String.format("""
	                    {
	                      "userId": "%s",
	                      "stoneColor": "%d",
	                      "gameResult": "%s"
	                    }
	                    """, p.getUserId(), p.getColor(), result));
	        }
			String jsonPayload = String.format("""
	                {
	                  "gameId": "%s",
	                  "roomId": "%s",
	                  "playType": "1", 
	                  "results": [ %s ]
	                }
	                """, gameId, roomId, resultsJson.toString()); // playType="1" (Multi)
			
			// API 전송
            URL url = new URL("http://localhost:8089/record/save"); 
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonPayload.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            System.out.println("게임 결과 저장 완료 (" + code + ")");
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("게임 결과 저장 실패");
        }
    }

	// 타이머 시작
	private void startTurnTimer() {
		// 기존 타이머가 돌고 있다면 취소
		if (turnTask != null && !turnTask.isDone()) {
			turnTask.cancel(false);
		}
		
		// 현재 턴 번호 기억
		final int currentTurn = turn;
		
		turnTask = scheduler.schedule(() -> {
			synchronized (this) {
				if (gameOver || turn != currentTurn) {
					return;
				}
				
				System.out.println("시간 초과! 차례가 넘어갑니다.");
				nextTurn();
				
				// 변경된 턴 정보를 모든 클라이언트에게 직접 전송
				List<SendJob> jobs = broadcastTurn();
				if (!jobs.isEmpty()) {
					String msg = jobs.get(0).text();
					
					for (Session s : sessionList) {
						if (s.isOpen()) {
							try {
								s.getBasicRemote().sendText(msg);
							} catch (Exception e) {
								e.printStackTrace();
							}
 						}
					}
				}
				// 타이머 재시작
				startTurnTimer();
			}
		}, 15, TimeUnit.SECONDS);
	}

	// 승리 판별 로직
	public int checkOmok() {
		int[][] directions = {{0, 1}, {1, 0}, {1, 1}, {1, -1}};

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