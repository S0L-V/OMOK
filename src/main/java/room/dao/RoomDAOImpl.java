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
		} catch (Exception e) {
			e.printStackTrace();
		}

		return list;
	}

	@Override
	public RoomDTO createRoom(String hostUserId, String roomName, String roomPwd, String isPublic, String playType)
		throws Exception {

		String id = Uuid.generate();
		int totalUserCnt = playType.equals("0") ? 2 : 4;

		String query = """
			INSERT INTO room
			    (id, host_user_id, room_name, room_pwd, is_public, play_type, current_user_cnt, total_user_cnt)
			VALUES
			    (?, ?, ?, ?, ?, ?, 0, ?)
			""";

		try (
			Connection conn = DB.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(query)) {
			pstmt.setString(1, id);
			pstmt.setString(2, hostUserId);
			pstmt.setString(3, roomName);
			pstmt.setString(5, isPublic);
			pstmt.setString(6, playType);
			pstmt.setInt(7, totalUserCnt);

			if (isPublic.equals("0")) {
				pstmt.setNull(4, Types.VARCHAR);
			} else {
				pstmt.setString(4, roomPwd);
			}

			pstmt.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return RoomDTO.builder()
			.id(id)
			.hostUserId(hostUserId)
			.roomName(roomName)
			.isPublic(isPublic)
			.playType(playType)
			.totalUserCnt(totalUserCnt)
			.currentUserCnt(0)
			.build();
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
