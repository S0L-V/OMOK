package room.dao;

import java.util.List;

import room.dto.RoomPlayerDTO;

public interface RoomPlayerDAO {

	List<RoomPlayerDTO> getPlayerList(String roomId) throws Exception;

	boolean enterIfAbsent(String roomId, String userId) throws Exception;

	void exit(String roomId, String userId) throws Exception;

	public int countActivePlayers(String roomId) throws Exception;

	/**
	 * 게임 시작 시 방에 있는 플레이어 상태를 IN_GAME(1)으로 변경
	 * @param roomId 방 ID
	 * @return 변경된 행 수
	 */
	int updatePlayersToInGame(String roomId) throws Exception;

}