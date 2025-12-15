package friend.dto;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class FriendDTO {
	
	private String id;
	private String userId;
	private String fiendId;
	private String status; // PENDING, ACCEPTED, BLOCKED
	private Timestamp createdAt;
}
