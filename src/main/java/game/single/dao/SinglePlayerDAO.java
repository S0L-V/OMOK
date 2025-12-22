package game.single.dao;

public interface SinglePlayerDAO {
	// 방의 멤버와 session의 userid 일치 확인
	boolean isMember(String roomId, String userId) throws Exception;
	
	/**
	 * 게임 시작 시 방에 있는 플레이어 상태를 IN_GAME(1)으로 변경
	 * @param roomId 방 ID
	 * @return 변경된 행 수
	 */
	int updatePlayersToRoom(String roomId) throws Exception;
}
