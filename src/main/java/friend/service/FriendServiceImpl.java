package friend.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import friend.dao.FriendDAO;
import friend.dao.FriendDAOImpl;
import friend.dto.FriendDTO;
import util.DB;

public class FriendServiceImpl implements FriendService {

	private final FriendDAO friendDAO;

	public FriendServiceImpl() {
		this.friendDAO = new FriendDAOImpl();
	}

	@Override
	public boolean sendFriendRequest(String requesterId, String receiverId) {
		if (requesterId == null || receiverId == null) {
			return false;
		}

		try {
			// 이미 관계가 있는지 확인 (양방향)
			FriendDTO existing1 = friendDAO.findRelation(requesterId, receiverId);
			FriendDTO existing2 = friendDAO.findRelation(receiverId, requesterId);

			if (existing1 != null || existing2 != null) {
				return false;
			}

			int result = friendDAO.createRequest(requesterId, receiverId);
			return result > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean rejectFriendRequest(String requesterId, String targetId) {
		// friend_request가 아니라 friend 테이블입니다!
		String sql = "DELETE FROM friend WHERE user_id = ? AND friend_id = ? AND status = 'PENDING'";

		try (Connection conn = DB.getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, requesterId);
			pstmt.setString(2, targetId);

			int result = pstmt.executeUpdate();
			return result > 0;

		} catch (Exception e) {
			e.printStackTrace();
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
			return friendDAO.findPendingRequests(userId);
		} catch (SQLException e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
	}

	@Override
	public boolean blockFriend(String userId, String friendId) {
		try {
			int result = friendDAO.updateStatus(userId, friendId, "BLOCKED");
			return result > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}
