package room.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import room.dto.RoomDTO;
import util.DB;
import util.Uuid;

public class RoomDAOImpl implements RoomDAO {

	@Override
	public List<RoomDTO> getRoomList() throws Exception {
		String query = """
			SELECT
			    id,
			    host_user_id,
			    room_name,
			    is_public,
			    play_type,
			    total_user_cnt,
			    current_user_cnt,
			   	created_at
			FROM room
			""";

		List<RoomDTO> list = new ArrayList<>();

		try (
			Connection conn = DB.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(query);
			ResultSet rs = pstmt.executeQuery()) {
			while (rs.next()) {
				list.add(mapToRoom(rs));
			}
		}

		return list;
	}

	@Override
	public RoomDTO createRoom(String hostUserId, String roomName, String roomPwd, String isPublic, String playType)
		throws Exception {

		int totalUserCnt = playType != null && playType.equals("0") ? 2 : 4;

		final String qLockRoomTable = "LOCK TABLE room IN EXCLUSIVE MODE";

		final String qFindExistingByHost = """
			SELECT
			    id,
			    host_user_id,
			    room_name,
			    is_public,
			    play_type,
			    total_user_cnt,
			    current_user_cnt,
			    created_at
			FROM room
			WHERE host_user_id = ?
			FETCH FIRST 1 ROWS ONLY
			""";

		final String qInsert = """
			INSERT INTO room
			    (id, host_user_id, room_name, room_pwd, is_public, play_type, current_user_cnt, total_user_cnt)
			VALUES
			    (?, ?, ?, ?, ?, ?, 0, ?)
			""";

		try (Connection conn = DB.getConnection()) {
			conn.setAutoCommit(false);

			try {
				try (PreparedStatement lockStmt = conn.prepareStatement(qLockRoomTable)) {
					lockStmt.execute();
				}

				// 이미 내가 방장인 방이 있으면 새로 만들지 않고 기존 방 반환
				RoomDTO existing = null;
				try (PreparedStatement ps = conn.prepareStatement(qFindExistingByHost)) {
					ps.setString(1, hostUserId);
					try (ResultSet rs = ps.executeQuery()) {
						if (rs.next()) {
							existing = mapToRoom(rs);
							// mapToRoom에 없는 값들 보강
							existing.setRoomName(rs.getString("room_name"));
							existing.setCreatedAt(rs.getString("created_at"));
						}
					}
				}

				if (existing != null) {
					conn.rollback();
					return existing;
				}

				String id = Uuid.generate();

				try (PreparedStatement pstmt = conn.prepareStatement(qInsert)) {
					pstmt.setString(1, id);
					pstmt.setString(2, hostUserId);
					pstmt.setString(3, roomName);

					// isPublic: 0=공개(비번 null), 1=비밀(비번 필수)
					if (isPublic != null && isPublic.equals("0")) {
						pstmt.setNull(4, Types.VARCHAR);
					} else {
						pstmt.setString(4, roomPwd);
					}

					pstmt.setString(5, isPublic);
					pstmt.setString(6, playType);
					pstmt.setInt(7, totalUserCnt);

					pstmt.executeUpdate();
				}

				conn.commit();

				return RoomDTO.builder()
					.id(id)
					.hostUserId(hostUserId)
					.roomName(roomName)
					.isPublic(isPublic)
					.playType(playType)
					.totalUserCnt(totalUserCnt)
					.currentUserCnt(0)
					.build();

			} catch (Exception e) {
				try {
					conn.rollback();
				} catch (Exception ignored) {}
				throw e;
			} finally {
				try {
					conn.setAutoCommit(true);
				} catch (Exception ignored) {}
			}
		}
	}

	@Override
	public String getHostUserId(String roomId) throws Exception {

		String sql = """
				SELECT host_user_id
				FROM room
				WHERE id = ?
			""";

		try (Connection conn = DB.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, roomId);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					return rs.getString("host_user_id");
				}
				return null;
			}
		}
	}

	@Override
	public String findHostUserIdByRoomId(String roomId) throws Exception {

		String sql = """
			    SELECT host_user_id
			    FROM room
			    WHERE id = ?
			""";

		try (
			Connection conn = DB.getConnection();
			PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, roomId);

			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getString("host_user_id");
				}
				return null;
			}
		}
	}

	private RoomDTO mapToRoom(ResultSet rs) throws SQLException {
		return RoomDTO.builder()
			.id(rs.getString("id"))
			.hostUserId(rs.getString("host_user_id"))
			.roomName(rs.getString("room_name"))
			.isPublic(rs.getString("is_public"))
			.playType(rs.getString("play_type"))
			.totalUserCnt(rs.getInt("total_user_cnt"))
			.currentUserCnt(rs.getInt("current_user_cnt"))
			.build();
	}
}
