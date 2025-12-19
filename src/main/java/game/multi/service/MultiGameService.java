package game.multi.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.websocket.Session;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class MultiGameService {

	// 세션별 Player 정보를 관리하기 위한 맵 (세션 ID -> Player 객체)
	private static Map<String, Player> playersMap = new ConcurrentHashMap<>();

	// 순서 유지를 위한 리스트 (0,1,2,3번 자리)
	private static List<Session> sessionList = Collections.synchronizedList(new ArrayList<>());

	private static int turn = 0; // 0, 1, 2, 3 인덱스
	private static int[][] board = new int[15][15];
	private static boolean gameOver = false;
	private static Gson gson = new Gson(); // GSON 객체

	// 타이머 관련
	private static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private static ScheduledFuture<?> turnTask;

	// Dispatcher에게 전달할 작업 단위
	public static record SendJob(Session target, String text) {}

	/**
	 * 입장 처리
	 * @param session
	 * @return
	 */
	public synchronized List<SendJob> handleOpen(Session session) {
		List<SendJob> out = new ArrayList<>();

		if (sessionList.size() >= 4) {
			out.add(createErrorJob(session, "ROOM_FULL", "방이 꽉 찼습니다."));
			return out;
		}

		// 리스트에 세션 등록
		sessionList.add(session);
		System.out.println("입장: " + session.getId() + " (현재 " + sessionList.size() + "/4명)");

		// 4명이 안 찼으면 대기 메시지 전송
		if (sessionList.size() < 4) {
			JsonObject waitJson = new JsonObject();
			waitJson.addProperty("type", "GAME_OVER");
			waitJson.addProperty("msg", "플레이어 대기 중... (" + sessionList.size() + "/4)");

			JsonObject j = new JsonObject();
			j.addProperty("type", "MULTI_WAIT");
			j.addProperty("msg", "다른 플레이어를 기다리는 중입니다... (" + sessionList.size() + "/4)");
			out.add(new SendJob(session, gson.toJson(j)));

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

				// Player 객체 생성 및 저장
				Player p = new Player(s, i, team, color);
				playersMap.put(s.getId(), p);

				// 시작 메시지 JSON 생성
				JsonObject startJson = new JsonObject();
				startJson.addProperty("type", "GAME_START");
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

		// GSON으로 파싱
		try {
			JsonObject json = gson.fromJson(msg, JsonObject.class);

			// 기권 처리
			if (json.has("type") && "MULTI_GIVEUP".equals(json.get("type").getAsString())) {
				endGame(out, "기권을 눌렀다네");
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

			// 턴 검사 (현재 턴 세션 === 보낸 세션)
			if (!sessionList.get(turn).equals(session)) {
				return out; // 내 자례 아님
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
			out.add(new SendJob(null, gson.toJson(updateJson)));

			// 승리 체크
			int winColor = checkOmok();
			if (winColor != 0) {
				gameOver = true;
				if (turnTask != null) {
					turnTask.cancel(true);
				}

				JsonObject winJson = new JsonObject();
				winJson.addProperty("type", "iswin");
				winJson.addProperty("color", winColor);
				out.add(new SendJob(null, gson.toJson(winJson)));
				return out;
			}

			// 턴 넘기기
			nextTurn();
			out.addAll(broadcastTurn());
			startTurnTimer();

		} catch (Exception e) {
			e.printStackTrace(); // 서버 콘솔에서 에러 확인
		}
		return out;
	}

	/**
	 * 종료 처리
	 * @param session
	 * @return
	 */
	public synchronized List<SendJob> handleClose(Session session) {
		List<SendJob> out = new ArrayList<>();

		// 나간 사람 제거
		boolean removed = sessionList.remove(session);
		playersMap.remove(session.getId());

		System.out.println("퇴장: " + session.getId() + " (남은 인원: " + sessionList.size() + ")");

		// 게임 중이었는데 누가 나가면 게임 종료
		if (removed && !gameOver && sessionList.size() > 0 && sessionList.size() < 4) {
			// 4명이 다 찬 상태에서 줄어든 경우에만 에러 처리 (게임 시작 전 퇴장은 무관)
			// 하지만 간단하게 구현하기 위해, 
			// "게임이 이미 시작된 상태(board에 돌이 있거나 등)"를 체크하면 좋음.
			// 여기서는 간단히 리셋.
			endGame(out, "플레이어 퇴장으로 게임이 종료되었습니다.");

			// 상태 초기화
			board = new int[15][15];
			turn = 0;
			if (turnTask != null)
				turnTask.cancel(false);
			// sessionList는 유지하되, 게임을 다시 시작하려면 4명을 다시 채워야 함
		}

		// 초기화
		if (sessionList.isEmpty()) {
			board = new int[15][15];
			turn = 0;
		}

		return out;
	}

	// ---------------------------------------------------------------
	// 보조 메서드들
	// ---------------------------------------------------------------	

	private void nextTurn() {
		turn = (turn + 1) % 4;
	}

	private void endGame(List<SendJob> out, String msg) {
		gameOver = true;
		JsonObject json = new JsonObject();
		json.addProperty("type", "gameover");
		json.addProperty("msg", msg);
		out.add(new SendJob(null, gson.toJson(json)));
	}

	// 턴 알림 생성
	private List<SendJob> broadcastTurn() {
		int color = (turn % 2 == 0) ? 1 : 2;
		JsonObject turnJson = new JsonObject();
		turnJson.addProperty("type", "MULTI_TURN");
		turnJson.addProperty("color", color);
		turnJson.addProperty("time", 15);
		turnJson.addProperty("turnIdx", turn); // 현재 턴인 사람의 slot 번호 (0~3)

		// target이 null이면 전체 전송 (MultiWebSocket에서 처리)
		return List.of(new SendJob(null, gson.toJson(turnJson)));
	}

	// 에러 메시지 생성 헬퍼
	private SendJob createErrorJob(Session session, String code, String msg) {
		JsonObject json = new JsonObject();
		json.addProperty("type", "error");
		json.addProperty("code", code);
		json.addProperty("msg", msg);
		return new SendJob(session, gson.toJson(json));
	}

	// 타이머 시작
	private void startTurnTimer() {
		if (turnTask != null && !turnTask.isDone()) {
			turnTask.cancel(false);
		}
		turnTask = scheduler.schedule(() -> {
			// 시간 초과 로직\
			synchronized (MultiGameService.this) {
				if (gameOver) {
					return;
				}
				System.out.println("시간 초과! 차례가 넘어갑니다.");
				nextTurn();
				try {
					// ※ 서비스에서 직접 소켓 전송을 하려면 
					// WebSocket endpoint와 연결된 콜백 구조가 필요하나,
					// 여기서는 로직상 턴만 넘기고, 다음 사람이 돌을 둘 때 갱신되게 하거나
					// 별도 스레드에서 전송 로직을 구현해야 함. (복잡도 방지를 위해 생략)
				} catch (Exception e) {}
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
		return 0; // 승리자 없음
	}
}