package room.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import util.DB;

public class RoomService {

	private static final String STATUS_IN_ROOM = "0";
	private static final String STATUS_EXIT = "2";

	/**
	 * @return 결과 상태
	 *  - "ROOM_DELETE": 방 삭제
	 *  - "HOST_CHANGE": host 위임 발생
	 *  - "ROOM_EXIT": 그냥 퇴장만 처리
	 */
	public String exitAndHandleHost(String roomId, String userId) throws Exception {

		final String qLockRoom = """
			SELECT id
			FROM room
			WHERE id = ?
			FOR UPDATE
			""";

		final String qExitPlayer = """
			UPDATE room_player
			SET status = ?
			WHERE room_id = ?
			  AND user_id = ?
			  AND status = ?
			""";

		final String qActiveCount = """
			SELECT COUNT(*) AS cnt
			FROM room_player
			WHERE room_id = ?
			  AND status = ?
			""";

		final String qGetHost = """
			SELECT host_user_id AS host_user_id
			FROM room
			WHERE id = ?
			""";

		final String qPickNextHost = """
			SELECT rp.user_id AS user_id
			FROM room_player rp
			WHERE rp.room_id = ?
			  AND rp.status = ?
			ORDER BY rp.joined_at ASC
			FETCH FIRST 1 ROWS ONLY
			""";

		final String qTransferHost = """
			UPDATE room
			SET host_user_id = ?
			WHERE id = ?
			  AND host_user_id = ?
			""";

		final String qDeleteRoomPlayers = """
			DELETE FROM room_player
			WHERE room_id = ?
			""";

		final String qDeleteRoom = """
			DELETE FROM room
			WHERE id = ?
			""";

		try (Connection conn = DB.getConnection()) {
			conn.setAutoCommit(false);

			try {
				// 방 row 잠금 
				try (PreparedStatement pstmt = conn.prepareStatement(qLockRoom)) {
					pstmt.setString(1, roomId);
					try (ResultSet rs = pstmt.executeQuery()) {
						if (!rs.next())
							throw new IllegalArgumentException("room not found: " + roomId);
					}
				}

				// 퇴장 처리
				int updated;
				try (PreparedStatement pstmt = conn.prepareStatement(qExitPlayer)) {
					pstmt.setString(1, STATUS_EXIT);
					pstmt.setString(2, roomId);
					pstmt.setString(3, userId);
					pstmt.setString(4, STATUS_IN_ROOM);
					updated = pstmt.executeUpdate();
				}

				if (updated == 0) {
					conn.commit();
					return "ROOM_EXIT";
				}

				// 퇴장 후 남은 인원 카운트
				int activeCnt = 0;
				try (PreparedStatement pstmt = conn.prepareStatement(qActiveCount)) {
					pstmt.setString(1, roomId);
					pstmt.setString(2, STATUS_IN_ROOM);
					try (ResultSet rs = pstmt.executeQuery()) {
						rs.next();
						activeCnt = rs.getInt("cnt");
					}
				}

				// 방에 아무도 없는 경우 방 삭제
				if (activeCnt == 0) {
					try (PreparedStatement pstmt = conn.prepareStatement(qDeleteRoomPlayers)) {
						pstmt.setString(1, roomId);
						pstmt.executeUpdate();
					}
					try (PreparedStatement pstmt = conn.prepareStatement(qDeleteRoom)) {
						pstmt.setString(1, roomId);
						pstmt.executeUpdate();
					}

					conn.commit();
					return "ROOM_DELETE";
				}

				// host 위임 필요 여부 확인
				String hostUserId = null;
				try (PreparedStatement pstmt = conn.prepareStatement(qGetHost)) {
					pstmt.setString(1, roomId);
					try (ResultSet rs = pstmt.executeQuery()) {
						if (rs.next())
							hostUserId = rs.getString("host_user_id");
					}
				}

				// host 본인이 나가는 경우 host 위임
				if (hostUserId != null && hostUserId.equals(userId)) {
					String nextHostId = null;
					try (PreparedStatement ps = conn.prepareStatement(qPickNextHost)) {
						ps.setString(1, roomId);
						ps.setString(2, STATUS_IN_ROOM);
						try (ResultSet rs = ps.executeQuery()) {
							if (rs.next())
								nextHostId = rs.getString("user_id");
						}
					}

					if (nextHostId != null && !nextHostId.isBlank()) {
						try (PreparedStatement pstmt = conn.prepareStatement(qTransferHost)) {
							pstmt.setString(1, nextHostId);
							pstmt.setString(2, roomId);
							pstmt.setString(3, userId);
							pstmt.executeUpdate();
						}
						conn.commit();
						return "HOST_CHANGE";
					}
				}

				conn.commit();
				return "ROOM_EXIT";

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
}
