package friend.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import friend.dto.FriendDTO;

public class FriendDAO {

	/**
	 * 친구 요청을 생성 (status = PENDING)
	 * @param userId 
	 * @param friendId
	 * @return insert 된 행 수 (성공시 1)
	 */
	public int insertFriendRequest(String userId, String friendId) throws SQLException {

		return 1;
	}

	/**
	 * 친구 요청을 수락 (PENDING -> ACCEPTED)
	 * @param userId
	 * @param friendId
	 * @return 업데이트 된 행 수 (성공시 1)
	 */
	public int acceptFriendRequest(String userId, String friendId) throws SQLException {
		return 1;
	}

	/**
	 * 친구 관계를 삭제함
	 * @param userId
	 * @param friendId
	 * @return 삭제된 행 수
	 */
	public int deleteFriend(String userId, String friendId) throws SQLException {
		return 1;
	}

	/**
	 * 지정한 사용자의 친구 목록(ACCEPTED 상태)을 조회 (양방향)
	 * @param userId 조회할 사용자 ID
	 * @return 수락된 친구 관계 리스트
	 */
	public List<FriendDTO> findAcceptedFriendsByUserId(String userId) throws SQLException {
		return new ArrayList<>();
	}

	/**
	 * 친구 관계의 상태를 변경
	 * @param userId
	 * @param friendId
	 * @param status (PENDING, ACCEPTED, BLOCKED)
	 * @return
	 */
	public int updateFriendStatus(String userId, String friendId, String status) throws SQLException {

		return 1;
	}
}
