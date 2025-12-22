package friend.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import friend.dto.FriendDTO;
import util.DB;

public class FriendDAOImpl implements FriendDAO {

	@Override
	public int createRequest(String requesterId, String receiverId) throws SQLException {
		String query = """
				INSERT INTO friend (id, user_id, friend_id, status, created_at)
				VALUES (?,?,?,'PENDING',SYSDATE)
			""";

		try (Connection conn = DB.getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setString(1, UUID.randomUUID().toString());
			pstmt.setString(2, requesterId);
			pstmt.setString(3, receiverId);

			return pstmt.executeUpdate();
		} catch (Exception e) {
			throw new SQLException("createRequest 실패", e);
		}
	}

	@Override
	public int acceptRequest(String requesterId, String receiverId) throws SQLException {
		String query = """
				UPDATE friend
				SET status = 'ACCEPTED'
				WHERE user_id = ? AND friend_id = ? AND status = 'PENDING'
			""";

		try (Connection conn = DB.getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setString(1, requesterId);
			pstmt.setString(2, receiverId);

			return pstmt.executeUpdate();
		} catch (Exception e) {
			throw new SQLException("acceptRequest 실패", e);
		}
	}

	@Override
	public int deleteRelation(String userId, String friendId) throws SQLException {
		String query = """
				DELETE FROM friend
				WHERE (user_id = ? AND friend_id = ?)
				  OR (user_id = ? AND friend_id = ?)
			""";

		try (Connection conn = DB.getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setString(1, userId);
			pstmt.setString(2, friendId);
			pstmt.setString(3, friendId);
			pstmt.setString(4, userId);

			return pstmt.executeUpdate();
		} catch (Exception e) {
			throw new SQLException("deleteRelation 실패", e);
		}
	}

	@Override
	public List<FriendDTO> findAcceptedFriends(String userId) throws SQLException {
		// CASE 문을 제거하고 OR 조건으로 두 가지 경우를 모두 처리
		String query = """
			    SELECT 
			        f.id, 
			        f.user_id, 
			        f.friend_id, 
			        f.status, 
			        f.created_at,
			        u.nickname,
			        u.total_win,
			        u.total_lose,
			        u.win_rate
			    FROM friend f
			    INNER JOIN user_info u ON (
			        (f.user_id = ? AND u.user_id = f.friend_id) OR
			        (f.friend_id = ? AND u.user_id = f.user_id)
			    )
			    WHERE f.status = 'ACCEPTED'
			""";

		List<FriendDTO> list = new ArrayList<>();

		try (Connection conn = DB.getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setString(1, userId);
			pstmt.setString(2, userId);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					list.add(FriendDTO.builder()
						.id(rs.getString("id"))
						.userId(rs.getString("user_id"))
						.friendId(rs.getString("friend_id"))
						.status(rs.getString("status"))
						.createdAt(rs.getTimestamp("created_at"))
						.nickname(rs.getString("nickname"))
						.totalWin(rs.getInt("total_win"))
						.totalLose(rs.getInt("total_lose"))
						.winRate(rs.getDouble("win_rate"))
						.build());
				}
			}
		} catch (Exception e) {
			throw new SQLException("findAcceptedFriends 실패", e);
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

		try (Connection conn = DB.getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setString(1, status);
			pstmt.setString(2, userId);
			pstmt.setString(3, friendId);

			return pstmt.executeUpdate();
		} catch (Exception e) {
			throw new SQLException("updateStatus 실패", e);
		}
	}

	@Override
	public FriendDTO findRelation(String userId, String friendId) throws SQLException {
		String query = """
				SELECT id, user_id, friend_id, status, created_at
				FROM friend
				WHERE user_id = ? AND friend_id = ?
			""";

		try (Connection conn = DB.getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setString(1, userId);
			pstmt.setString(2, friendId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return mapRow(rs);
				}
			}
		} catch (Exception e) {
			throw new SQLException("findRelation 실패", e);
		}

		return null;
	}

	@Override
	public List<FriendDTO> findPendingRequests(String userId) throws SQLException {
		// user_info 테이블과 JOIN하여 요청자의 정보를 함께 가져오기
		String query = """
			    SELECT 
			        f.id, 
			        f.user_id, 
			        f.friend_id, 
			        f.status, 
			        f.created_at,
			        u.nickname,
			        u.total_win,
			        u.total_lose,
			        u.win_rate
			    FROM friend f
			    INNER JOIN user_info u ON f.user_id = u.user_id
			    WHERE f.friend_id = ? AND f.status = 'PENDING'
			""";

		List<FriendDTO> list = new ArrayList<>();

		try (Connection conn = DB.getConnection();
			 PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setString(1, userId);

			try (ResultSet rs = pstmt.executeQuery()) {
				while (rs.next()) {
					list.add(FriendDTO.builder()
						.id(rs.getString("id"))
						.userId(rs.getString("user_id"))
						.friendId(rs.getString("friend_id"))
						.status(rs.getString("status"))
						.createdAt(rs.getTimestamp("created_at"))
						.nickname(rs.getString("nickname"))          // 추가
						.totalWin(rs.getInt("total_win"))            // 추가
						.totalLose(rs.getInt("total_lose"))          // 추가
						.winRate(rs.getDouble("win_rate"))           // 추가
						.build());
				}
			}

			System.out.println("[DAO] findPendingRequests: " + list.size() + "건");

		} catch (Exception e) {
			System.err.println("[DAO] findPendingRequests 실패!");
			throw new SQLException("findPendingRequests 실패", e);
		}

		return list;
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
