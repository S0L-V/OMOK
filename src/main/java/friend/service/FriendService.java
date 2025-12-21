package friend.service;

import java.util.List;

import friend.dto.FriendDTO;

public interface FriendService {

	/**
	 * 친구 요청 보내기
	 */
	boolean sendFriendRequest(String requesterId, String receiverId);

	/**
	 * 친구 요청 수락
	 */
	boolean acceptFriendRequest(String requesterId, String receiverId);

	/**
	 * 친구 삭제 (또는 요청 거절)
	 */
	boolean removeFriend(String userId, String friendId);

	/**
	 * 내 친구 목록 조회 (ACCEPTED 상태만)
	 */
	List<FriendDTO> getMyFriends(String userId);

	/**
	 * 나에게 온 친구 요청 목록 (PENDING 상태, 내가 friend_Id인 것)
	 */
	List<FriendDTO> getPendingRequests(String userId);

	/**
	 * 친구 차단
	 */
	boolean blockFriend(String userId, String friendId);

	/**
	 * 친구 요청 거절
	 */
	boolean rejectFriendRequest(String requesterId, String targetId);
}
