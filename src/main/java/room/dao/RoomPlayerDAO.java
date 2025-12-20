package room.dao;

import java.util.List;

import room.dto.RoomPlayerDTO;

public interface RoomPlayerDAO {

	List<RoomPlayerDTO> getUserList(String roomId) throws Exception;

	void enterIfAbsent(String roomId, String userId) throws Exception;

	void exit(String roomId, String userId) throws Exception;

	public int countActivePlayers(String roomId) throws Exception;

}