package record.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import record.dto.GameResultDTO;
import util.DB;

public class GameResultDAOImpl implements GameResultDAO {

	@Override
	public void insertGameResultBatch(List<GameResultDTO> gameResults, Connection connection) throws SQLException {

		String query = """
				INSERT INTO game_result
				(id, game_id, room_id, user_id, stone_color, game_result, play_type, finished_at)
				VALUES (?, ?, ?, ?, ?, ?, ?, SYSDATE)
			""";

		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			for (GameResultDTO result : gameResults) {
				pstmt.setString(1, result.getId());
				pstmt.setString(2, result.getGameId());
				pstmt.setString(3, result.getUserId());
				pstmt.setString(4, result.getStoneColor());
				pstmt.setString(5, result.getGameResult());
				pstmt.setString(6, result.getPlayType());
				pstmt.setString(7, result.getPlayType());

				pstmt.addBatch();
			}

			pstmt.executeBatch();
			System.out.println("[DAO] 게임 결과: " + gameResults.size() + "건 저장 완료");
		} catch (Exception e) {
			System.out.println("[DAO] insertGameResultBatch 실패");
			throw new SQLException("게임 결과 저장 실패," + e);
		}
	}

	@Override
	public List<GameResultDTO> selectGameResultByUser(String userId, Integer limit) throws SQLException {
		String query = """
				SELECT id, game_id, room_id, user_id, stone_color,
						game_result, play_type, finished_at
				FROM game_result
				WHERE user_id = ?
				ORDER BY finished_at DESC
			""" + (limit != null ? " FETCH FIRST ? ROWS ONLY" : "");

		List<GameResultDTO> list = new ArrayList<>();

		try (Connection conn = DB.getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setString(1, userId);
			if (limit != null) {
				pstmt.setInt(2, limit);
			}

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					list.add(mapRow(rs));
				}
			}

			System.out.println("[DAO] " + userId + " 게임 이력 " + list.size() + "건 조회");
		} catch (Exception e) {
			System.out.println("[DAO] selectGameResultByUser 실패");
			throw new SQLException("게임 이력 조회 실패", e);
		}

		return list;
	}

	@Override
	public boolean existsByGameId(String gameId, Connection conn) throws SQLException {
		String query = """
				SELECT COUNT(*) FROM game_result WHERE game_id = ?
			""";

		try (PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setString(1, gameId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getInt(1) > 0;
				}
			} catch (Exception e) {
				throw new SQLException("게임 ID 존재 여부 확인 실패", e);
			}
		}

		return false;
	}

	private GameResultDTO mapRow(ResultSet rs) throws SQLException {
		return GameResultDTO.builder()
			.id(rs.getString("id"))
			.gameId(rs.getString("game_id"))
			.roomId(rs.getString("room_id"))
			.userId(rs.getString("user_id"))
			.stoneColor(rs.getString("stone_color"))
			.gameResult(rs.getString("game_result"))
			.playType(rs.getString("play_type"))
			.finishedAt(rs.getTimestamp("finished_at"))
			.build();
	}
}
