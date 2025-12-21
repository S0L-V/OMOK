package room.dao;

import java.util.List;

import room.dto.RoomDTO;

public interface RoomDAO {

	List<RoomDTO> getRoomList() throws Exception;

	RoomDTO createRoom(
		String hostUserId,
		String roomName,
		String roomPwd,
		String isPublic,
		String playType) throws Exception;

}