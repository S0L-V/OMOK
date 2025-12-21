package record.service;

import java.util.List;

import login.vo.UserInfoVo;
import record.dto.GameResultDTO;
import record.dto.GameResultSaveRequest;

public interface GameResultService {

	/**
	 * 게임 결과 저장 + 플레이어 통계 업데이트
	 */
	boolean saveGameResult(GameResultSaveRequest request);

	/**
	 * 사용자 게임 이력 조회
	 */
	List<GameResultDTO> getUserGameHistory(String userId, Integer limit);

	/**
	 * 사용자 통계 조회
	 */
	UserInfoVo getUserStats(String userId);
}
