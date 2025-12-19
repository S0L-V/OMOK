package room.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RoomDTO {
	private String id;
	private String hostUserId;
	private String roomName;
	private String roomPwd;
	private String isPublic; // 0: 공개, 1: 비밀방 
	private String playType; // 0: 개인, 1: 팀전 
	private int totalUserCnt;
	private int currentUserCnt;
	private String createdAt;
}
