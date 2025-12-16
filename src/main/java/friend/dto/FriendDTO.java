package friend.dto;

import java.sql.Timestamp;
import java.util.UUID;

import lombok.Data;

@Data
public class FriendDTO {
	
	private String id; // PK
	private String userId; // 친구 요청을 보낸 사람
	private String fiendId; // 친구 요청을 받은 사람
	private String status; // PENDING, ACCEPTED, BLOCKED
	private Timestamp createdAt; // 친구 요청 시간
}
