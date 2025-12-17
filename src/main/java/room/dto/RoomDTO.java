package room.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class RoomDTO {
	private String id;
	private String hostUserId;
	private String roomName;
	private String roomPwd;
	private int isPublic; // 0: 공개, 1: 비밀방 
	private int playType; // 0: 개인, 1: 팀전 
	private int totalUserCnt;
	private int currentUserCnt;
	private String createdAt;
}
