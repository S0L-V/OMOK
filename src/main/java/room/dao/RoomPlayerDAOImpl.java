package room.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import room.dto.RoomPlayerDTO;
import util.DB;

public class RoomPlayerDAOImpl implements RoomPlayerDAO {

	private static final String STATUS_IN_ROOM = "0"; // 입장
	private static final String STATUS_LEAVE = "2"; // 퇴장

	@Override
	public List<RoomPlayerDTO> getPlayerList(String roomId) throws Exception {
		final String query = """
					SELECT
					    rp.user_id,
					    ui.nickname,
					    rp.stone_color,
					    rp.joined_at,
					    rp.room_id,
					    rp.status
					FROM room_player rp
					JOIN user_info ui
					  ON ui.user_id = rp.user_id
					WHERE rp.room_id = ?
					  AND rp.status = ?
					ORDER BY rp.joined_at ASC
			""";

		try (Connection conn = DB.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setString(1, roomId);
			pstmt.setString(2, STATUS_IN_ROOM);

			try (ResultSet rs = pstmt.executeQuery()) {
				List<RoomPlayerDTO> list = new ArrayList<>();
				while (rs.next()) {
					list.add(mapToRoomPlayer(rs));
				}
				return list;
			}
		}
	}

	/**
	 * 입장 처리
	 * - 이미 존재하면 STATUS='0', JOINED_AT 갱신(재입장 시각), STONE_COLOR는 유지(또는 NULL로 초기화 선택)
	 * - 없다면 새로 INSERT
	 */
	@Override
	public void enterIfAbsent(String roomId, String userId) throws Exception {
		final String query = """
				MERGE INTO room_player t
				USING (
				    SELECT ? AS room_id, ? AS user_id
				    FROM dual
				) s
				ON (
				    t.room_id = s.room_id
				    AND t.user_id = s.user_id
				)
				WHEN MATCHED THEN
				    UPDATE SET
				        t.status = ?,
				        t.joined_at = SYSTIMESTAMP
				WHEN NOT MATCHED THEN
				    INSERT (
				        id,
				        room_id,
				        user_id,
				        stone_color,
				        status
				    )
				    VALUES (
				        SYS_GUID(),
				        s.room_id,
				        s.user_id,
				        NULL,
				        ?
				    )
			""";
		try (Connection conn = DB.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setString(1, roomId);
			pstmt.setString(2, userId);
			pstmt.setString(3, STATUS_IN_ROOM);
			pstmt.setString(4, STATUS_IN_ROOM);
			pstmt.executeUpdate();
		}
	}

	/**
	 * 퇴장 처리 (STATUS='2'으로 변경 + 기록 유지)
	 */
	@Override
	public void exit(String roomId, String userId) throws Exception {
		final String query = """
			UPDATE room_player
			SET status = ?
			WHERE room_id = ?
			  AND user_id = ?
			  AND status = ?
			""";

		try (Connection conn = DB.getConnection();
			PreparedStatement ps = conn.prepareStatement(query)) {

			ps.setString(1, STATUS_LEAVE);
			ps.setString(2, roomId);
			ps.setString(3, userId);
			ps.setString(4, STATUS_IN_ROOM);

			ps.executeUpdate();
		}
	}

	/**
	 * 현재 참가자 수 (STATUS='0')
	 */
	@Override
	public int countActivePlayers(String roomId) throws Exception {
		final String query = """
				SELECT COUNT(*) AS cnt
				FROM room_player
				WHERE room_id = ?
				  AND status = ?
			""";

		try (Connection conn = DB.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setString(1, roomId);
			pstmt.setString(2, STATUS_IN_ROOM);

			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next())
					return rs.getInt("CNT");
				return 0;
			}
		}
	}

	private RoomPlayerDTO mapToRoomPlayer(ResultSet rs) throws SQLException {
		return RoomPlayerDTO.builder()
			.roomId(rs.getString("room_id"))
			.userId(rs.getString("user_id"))
			.stoneColor(rs.getString("stone_color"))
			.joinedAt(rs.getString("joined_at"))
			.status(rs.getString("status"))
			.nickname(rs.getString("nickname"))
			.build();
	}
}
