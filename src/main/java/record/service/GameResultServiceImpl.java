package record.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import login.vo.UserInfoVo;
import record.dao.GameResultDAO;
import record.dao.GameResultDAOImpl;
import record.dao.UserInfoDAO;
import record.dao.UserInfoDAOImpl;
import record.dto.GameResultDTO;
import record.dto.GameResultSaveRequest;
import record.dto.PlayerResult;
import util.DB;

public class GameResultServiceImpl implements GameResultService {

	private final GameResultDAO gameResultDAO;
	private final UserInfoDAO userInfoDAO;

	public GameResultServiceImpl() {
		this.gameResultDAO = new GameResultDAOImpl();
		this.userInfoDAO = new UserInfoDAOImpl();
	}

	@Override
	public boolean saveGameResult(GameResultSaveRequest request) {
		Connection conn = null;

		try {
			// 트렌젝션 시작
			conn = DB.getConnection();
			conn.setAutoCommit(false);

			System.out.println("[Service] 트랜잭션 시작");
			System.out.println("gameId: " + request.getGameId());
			System.out.println("roomId: " + request.getRoomId());
			System.out.println("playType: " + request.getPlayType());
			System.out.println("플레이어 수: " + request.getResults().size());

			// 중복 체크
			if (gameResultDAO.existsByGameId(request.getGameId(), conn)) {
				System.out.println("[Service] 이미 저장된 게임 결과: " + request.getGameId());
				conn.rollback();
				return false;
			}

			// GameResultDTO 리스트 생성
			List<GameResultDTO> gameResults = new ArrayList<>();
			int playerIndex = 0;
			for (PlayerResult player : request.getResults()) {
				playerIndex++;
				System.out.println("   플레이어 " + playerIndex + ":");
				System.out.println("     - userId: " + player.getUserId());
				System.out.println("     - stoneColor: " + player.getStoneColor());
				System.out.println("     - gameResult: " + player.getGameResult());

				GameResultDTO dto = GameResultDTO.builder()
					.id(UUID.randomUUID().toString())
					.gameId(request.getGameId())
					.roomId(request.getRoomId())
					.userId(player.getUserId())
					.stoneColor(player.getStoneColor())
					.gameResult(player.getGameResult())
					.playType(request.getPlayType())
					.build();
				System.out.println("   생성된 DTO - id: " + dto.getId());
				System.out.println("            - gameId: " + dto.getGameId());
				System.out.println("            - roomId: " + dto.getRoomId());
				System.out.println("            - userId: " + dto.getUserId());
				gameResults.add(dto);
			}

			gameResultDAO.insertGameResultBatch(gameResults, conn);
			System.out.println("[Service] 게임 결과 저장 완료");

			for (PlayerResult player : request.getResults()) {
				updatePlayerStats(player, conn);
			}
			System.out.println("[Service] 플레이어 통계 업데이트 완료");

			conn.commit();
			System.out.println("[Service] 트랜젝션 커밋 완료");
			return true;
		} catch (Exception e) {
			e.printStackTrace();

			if (conn != null) {
				try {
					conn.rollback();
					System.out.println("[Service] 트랜젝션 롤백");
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
			return false;
		} finally {
			if (conn != null) {
				try {
					conn.setAutoCommit(true);
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void updatePlayerStats(PlayerResult player, Connection conn) throws SQLException {
		// 현재 통계 조회
		UserInfoVo userInfo = userInfoDAO.getUserInfo(player.getUserId(), conn);

		// 통계 계산
		String result = player.getGameResult();

		if ("W".equals(result)) {
			// 승리
			userInfo.setTotalWin(userInfo.getTotalWin() + 1);

			// 연승 계산
			if (userInfo.getCurrentStreak() >= 0) {
				userInfo.setCurrentStreak(userInfo.getCurrentStreak() + 1);
			} else {
				userInfo.setCurrentStreak(1);
			}

		} else if ("L".equals(result)) {
			// 패배
			userInfo.setTotalLose(userInfo.getTotalLose() + 1);

			// 연패 계산
			if (userInfo.getCurrentStreak() <= 0) {
				userInfo.setCurrentStreak(userInfo.getCurrentStreak() - 1);
			} else {
				userInfo.setCurrentStreak(-1);
			}
		} else if ("D".equals(result)) {
			// 무승부
			userInfo.setTotalDraw(userInfo.getTotalDraw() + 1);
			userInfo.setCurrentStreak(0);
		}

		// 최대 연승 갱신
		if (userInfo.getCurrentStreak() > userInfo.getMaxWinStreak()) {
			userInfo.setMaxWinStreak(userInfo.getCurrentStreak());
		}

		// 승률 계산
		int totalGames = userInfo.getTotalWin() + userInfo.getTotalLose() + userInfo.getTotalDraw();
		double winRate = totalGames > 0 ? ((double)userInfo.getTotalWin() / totalGames) * 100 : 0.0;
		userInfo.setWinRate(Math.round(winRate * 100.0) / 100.0);

		// 마지막 게임 날짜
		userInfo.setLastGameDate(new Timestamp(System.currentTimeMillis()));

		// DB 업데이트
		userInfoDAO.updateUserStats(userInfo, conn);
		System.out.println(player.getUserId() + " 통계 업데이트 완료");
	}

	@Override
	public List<GameResultDTO> getUserGameHistory(String userId, Integer limit) {
		try {
			return gameResultDAO.selectGameResultByUser(userId, limit);
		} catch (SQLException e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	@Override
	public UserInfoVo getUserStats(String userId) {
		try (Connection conn = DB.getConnection()) {
			return userInfoDAO.getUserInfo(userId, conn);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
