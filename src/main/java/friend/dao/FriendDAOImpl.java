package friend.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import friend.dto.FriendDTO;

public class FriendDAOImpl implements FriendDAO {

	private final DataSource ds;

	public FriendDAOImpl(DataSource ds) {
		this.ds = ds;
	}

	@Override
	public int createRequest(String requesterId, String receiverId) throws SQLException {
		String query = """
				INSERT INTO friend (id, user_id, friend_id, status, created_at)
				VALUES (?,?,?,'PENDING',SYSDATE)
			""";

		try (Connection conn = ds.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setString(1, UUID.randomUUID().toString());
			pstmt.setString(2, requesterId);
			pstmt.setString(3, receiverId);

			return pstmt.executeUpdate();
		}
	}

	@Override
	public int acceptRequest(String requesterId, String receiverId) throws SQLException {
		String query = """
				UPDATE friend
				SET status = 'ACCEPTED'
				WHERE user_id = ? AND friend_id = ? AND status = 'PENDING'
			""";

		try (Connection conn = ds.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setString(1, requesterId);
			pstmt.setString(2, receiverId);

			return pstmt.executeUpdate();
		}
	}

	@Override
	public int deleteRelation(String userId, String friendId) throws SQLException {
		String query = """
				DELETE FROM friend
				WHERE (user_id = ? AND friend_id = ?)
				  OR (user_id = ? AND friend_id = ?)
			""";

		try (Connection conn = ds.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setString(1, userId);
			pstmt.setString(2, friendId);
			pstmt.setString(3, friendId);
			pstmt.setString(4, userId);

			return pstmt.executeUpdate();
		}
	}

	@Override
	public List<FriendDTO> findAcceptedFriends(String userId) throws SQLException {
		String query = """
				SELECT id, user_id, friend_id, status, created_at
				FROM friend
				WHERE status = 'ACCEPTED'
				  AND (user_id = ? OR friend_id = ?)
			""";

		List<FriendDTO> list = new ArrayList<>();

		try (Connection conn = ds.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setString(1, userId);
			pstmt.setString(2, userId);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					list.add(mapRow(rs));
				}
			}
		}

		return list;
	}

	@Override
	public int updateStatus(String userId, String friendId, String status) throws SQLException {
		String query = """
				UPDATE friend
				SET status = ?
				WHERE user_id = ? AND friend_id = ?
			""";

		try (Connection conn = ds.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setString(1, status);
			pstmt.setString(2, userId);
			pstmt.setString(3, friendId);

			return pstmt.executeUpdate();
		}
	}

	@Override
	public FriendDTO findRelation(String userId, String friendId) throws SQLException {
		String query = """
				SELECT id, user_id, friend_id, status, created_at
				FROM friend
				WHERE user_id = ? AND friend_id = ?
			""";

		try (Connection conn = ds.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setString(1, userId);
			pstmt.setString(2, friendId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		}

		return null;
	}

	private FriendDTO mapRow(ResultSet rs) throws SQLException {
		return FriendDTO.builder()
			.id(rs.getString("id"))
			.userId(rs.getString("user_id"))
			.friendId(rs.getString("friend_id"))
			.status(rs.getString("status"))
			.createdAt(rs.getTimestamp("created_at"))
			.build();
	}

}
