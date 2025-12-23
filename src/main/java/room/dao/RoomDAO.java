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

	/**
	 * 방의 host userId 조회
	 * @param roomId 방 ID
	 * @return host userId (없으면 null)
	 */
	String getHostUserId(String roomId) throws Exception;

	/**
	* roomId로 방장 userId 조회
	* @param roomId 방 ID
	* @return hostUserId (없으면 null)
	*/
	String findHostUserIdByRoomId(String roomId) throws Exception;

}