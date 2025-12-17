package friend.dao;

import java.sql.SQLException;
import java.util.List;

import friend.dto.FriendDTO;

public class FriendDAOImpl implements FriendDAO {

	@Override
	public int createRequest(String requesterId, String receiverId) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int acceptRequest(String requesterId, String receiverId) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int deleteRelation(String userId, String friendId) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public List<FriendDTO> findAcceptedFriends(String userId) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int updateStatus(String userId, String friendId, String status) throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public FriendDTO findRelation(String userId, String friendId) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
