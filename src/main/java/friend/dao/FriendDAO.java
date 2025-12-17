package friend.dao;

import java.sql.SQLException;
import java.util.List;

import friend.dto.FriendDTO;

public interface FriendDAO {

	/**
	 * 친구 요청 생성(status = PENDING)
	 */
	int createRequest(String requesterId, String receiverId) throws SQLException;

	/**
	 * 친구 요청 수락 (PENDING -> ACCEPTED)
	 */
	int acceptRequest(String requesterId, String receiverId) throws SQLException;

	/**
	 * 친구 관계 삭제 (요청/수락된 기록 제거)
	 */
	int deleteRelation(String userId, String friendId) throws SQLException;

	/**
	 * 지정한 사용자의 친구 목록(ACCEPTED 상태, 양방향 포함) 조회 
	 */
	List<FriendDTO> findAcceptedFriends(String userId) throws SQLException;

	/**
	 * 친구 상태 변경(PENDING, ACCEPTED, BLOCKED)
	 */
	int updateStatus(String userId, String friendId, String status) throws SQLException;

	/**
	 * 두 사용자간의 친구 관계 1건 조회 (없으면 null)
	 */
	FriendDTO findRelation(String userId, String friendId) throws SQLException;

}
