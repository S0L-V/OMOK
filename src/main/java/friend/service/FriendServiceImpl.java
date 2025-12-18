package friend.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import friend.dao.FriendDAO;
import friend.dao.FriendDAOImpl;
import friend.dto.FriendDTO;

public class FriendServiceImpl implements FriendService {

	private final FriendDAO friendDAO;

	public FriendServiceImpl() {
		this.friendDAO = new FriendDAOImpl();
	}

	@Override
	public boolean sendFriendRequest(String requesterId, String receiverId) {
		if (requesterId == null || requesterId == null) {
			return false;
		}

		try {
			// 이미 관계가 있는지 확인 (양방향)
			FriendDTO existing1 = friendDAO.findRelation(requesterId, receiverId);
			FriendDTO existing2 = friendDAO.findRelation(receiverId, requesterId);

			if (existing1 != null && existing2 != null) {
				return false;
			}

			int result = friendDAO.createRequest(requesterId, receiverId);
			return result > 0;
		} catch (SQLException e) {
			e.getErrorCode();
			return false;
		}
	}

	@Override
	public boolean acceptFriendRequest(String requesterId, String receiverId) {
		try {
			// 요청자가 보낸 PENDING 요청만 수락 가능
			int result = friendDAO.acceptRequest(requesterId, receiverId);
			return result > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean removeFriend(String userId, String friendId) {
		try {
			int result = friendDAO.deleteRelation(userId, friendId);
			return result > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public List<FriendDTO> getMyFriends(String userId) {
		try {
			return friendDAO.findAcceptedFriends(userId);
		} catch (SQLException e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	@Override
	public List<FriendDTO> getPendingRequests(String userId) {
		try {
			// 전체 ACCEPTED 친구 목록을 가져온 뒤, 필터링
			List<FriendDTO> allRelations = friendDAO.findAcceptedFriends(userId);

			// PENDING 상태이면서, 내가 friend_id인 것만 필터링
			return allRelations.stream()
				.filter(f -> "PENDING".equals(f.getStatus()) && userId.equals(f.getFriendId()))
				.collect(Collectors.toList());
		} catch (SQLException e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	@Override
	public boolean blockFriend(String userId, String fiendId) {
		try {
			int result = friendDAO.updateStatus(userId, fiendId, "BLOCKED");
			return result > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}
