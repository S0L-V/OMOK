package game.single.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.websocket.Session;

public class SingleGameServiceImpl implements SingleGameService {

	private static SingleGameServiceImpl instance = new SingleGameServiceImpl();

	public static SingleGameServiceImpl getInstance() {
		return instance;
	}

	private List<Session> players = new ArrayList<>();
	private int turn = 0;
	private int[][] board = new int[15][15];
	private boolean gameOver = false;

	private ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private ScheduledFuture<?> turnTask;

	private int wpass = 0;
	private int bpass = 0;

	@Override
	public void onOpen(Session session) throws Exception {
		if (players.size() >= 2) {
			session.close();
			return;
		}

		players.add(session);
		System.out.println("접속: " + session.getId());
		int color = players.size();

		session.getBasicRemote()
			.sendText("{ \"type\":\"SINGLE_START\", \"color\":" + color + "}");

		if (players.size() == 2) {
			turn = 0;
			broadcastTurn();
			startTurnTimer();
		}
	}

	@Override
	public void onMessage(String msg, Session session) throws Exception {
		if (gameOver)
			return;

		if (msg.contains("SINGLE_GIVEUP")) {
			int loserIdx = players.indexOf(session);
			gameOver = true;

			if (turnTask != null)
				turnTask.cancel(false);

			for (Session s : players) {
				s.getBasicRemote().sendText(
					"{ \"type\":\"SINGLE_GIVEUP\", \"losercolor\":" + (loserIdx + 1) + "}");
			}
			return;
		}

		if (players.get(turn) != session)
			return;

		msg = msg.replaceAll("[^0-9,]", "");
		String[] arr = msg.split(",");
		int x = Integer.parseInt(arr[0]);
		int y = Integer.parseInt(arr[1]);

		if (board[y][x] != 0)
			return;

		int color = turn + 1;
		board[y][x] = color;

		for (Session s : players) {
			s.getBasicRemote().sendText(
				"{ \"type\":\"SINGLE_STONE\", \"x\":" + x +
					", \"y\":" + y +
					", \"color\":" + color + " }");
		}

		int wincolor = checkOmok();
		if (wincolor != 0) {
			gameOver = true;
			if (turnTask != null)
				turnTask.cancel(false);
			for (Session s : players) {
				s.getBasicRemote().sendText(
					"{ \"type\":\"SINGLE_WIN\", \"color\":" + wincolor + " }");
			}
			return;
		}

		turn = 1 - turn;
		startTurnTimer();
		broadcastTurn();
	}

	@Override
	public void onClose() {
		players.clear();
		board = new int[15][15];
		turn = 0;
		gameOver = false;
		System.out.println("연결 종료");
	}

	private void startTurnTimer() {
		if (turnTask != null && !turnTask.isDone())
			turnTask.cancel(false);

		turnTask = scheduler.schedule(() -> {
			if (gameOver)
				return;

			if (turn + 1 == 1)
				bpass++;
			else
				wpass++;

			if (bpass == 3 || wpass == 3) {
				int lose = (bpass == 3 ? 1 : 2);
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
					"{ \"type\":\"delay\", \"delayColor\":" + (turn + 1) + "}");
			} catch (Exception e) {}
		}

		turnTask = scheduler.schedule(() -> {
			turn = 1 - turn;
			broadcastTurn();
			startTurnTimer();
		}, 2, TimeUnit.SECONDS);
	}

	private void broadcastTurn() {
		int color = turn + 1;
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
		return 0; // 승리자 없음 
	}

}
