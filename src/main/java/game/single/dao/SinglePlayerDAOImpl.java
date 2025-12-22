package game.single.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.sql.DataSource;

import util.DB;

public class SinglePlayerDAOImpl implements SinglePlayerDAO {
    /**
     * userId가 roomId 방의 멤버인지 확인한다.
     *
     * @param roomId 방 ID
     * @param userId 유저 ID
     * @return 멤버이면 true, 아니면 false
     */
    @Override
    public boolean isMember(String roomId, String userId) throws Exception {
        final String sql =
            "SELECT 1 FROM ROOM_PLAYER WHERE room_id = ? AND user_id = ?";

        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, roomId);
            ps.setString(2, userId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            // 운영에서는 logger 권장
            e.printStackTrace();
            return false;
        }
    }
}
