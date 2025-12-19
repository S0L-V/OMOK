package record.dao;

import java.util.ArrayList;
import java.util.List;

import record.dto.GameResultDTO;

public class GameResultDAO {

	/**
	 * 게임 결과 일괄 저장(한 게임의 모든 플레이어)
	 * @param gameResultList 게임 결과 리스트
	 * @return 성공 여부
	 */
	public int insertGameResultBatch(List<GameResultDTO> gameResultList) {

		return 0;
	}

	/**
	 * 특정 사용자의 게임 이력 조회
	 * @param userId 사용자ID
	 * @param limit 조회 개수(null 이면 전체)
	 * @return 게임 이력 리스
	 */
	public List<GameResultDTO> selectGameResultByUser(String userId, Integer limit) {

		return new ArrayList<>();
	}

}
