package game.multi.dao;

public interface MultiPlayerDAO {
	boolean isMember(String roomId, String userId) throws Exception;
	
	/**
	 * 게임 시작 시 방에 있는 플레이어 상태를 IN_GAME(1)으로 변경
	 * @param roomId
	 * @return 변경된 행 수
	 * @throws Exception
	 */
	int updatePlayersToRoom(String roomId) throws Exception;
}
