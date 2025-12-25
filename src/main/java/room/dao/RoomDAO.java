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

	/**
	* 비공개 방 비밀번호 검증
	*
	* @param roomId   방 ID
	* @param inputPwd 사용자가 입력한 비밀번호 (평문)
	*
	* @return true  - 비밀번호 일치
	*         false - 비밀번호 불일치 또는 존재하지 않는 방
	*
	* 동작 규칙
	*  - 공개방(isPublic="0")은 호출 대상이 아님
	*  - 비공개방(isPublic="1")에 대해서만 room_pwd와 비교
	*/
	boolean matchRoomPassword(String roomId, String inputPwd) throws Exception;

	/**
	 * 방이 비공개방(비밀번호가 설정된 방)인지 여부 조
	 *
	 * @param roomId 조회할 방 ID
	 */
	boolean isPrivateRoom(String roomId) throws Exception;

}