package room.dao;

import java.util.List;

import room.dto.RoomPlayerDTO;

public interface RoomPlayerDAO {

	List<RoomPlayerDTO> getPlayerList(String roomId) throws Exception;

	boolean enterIfAbsent(String roomId, String userId) throws Exception;

	void exit(String roomId, String userId) throws Exception;

	public int countActivePlayers(String roomId) throws Exception;

}