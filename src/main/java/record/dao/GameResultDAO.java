package record.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import record.dto.GameResultDTO;

public interface GameResultDAO {

	/**
	 * 게임 결과 일괄 저장
	 */
	void insertGameResultBatch(List<GameResultDTO> gameResults, Connection connection) throws SQLException;

	/**
	 * 특정 사용자의 게임 이력 조회
	 */
	List<GameResultDTO> selectGameResultByUser(String userId, Integer limit) throws SQLException;

	/**
	 * 게임 ID로 중복 체크
	 */
	boolean existsByGameId(String gameId, Connection conn) throws SQLException;

}
