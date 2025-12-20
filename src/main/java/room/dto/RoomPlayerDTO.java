package room.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RoomPlayerDTO {
	private String id;
	private String roomId;
	private String userId;
	private String stoneColor; // 흑 0, 백 1
	private String joinedAt;
	private String status; // in_room 0 , in_game 1, left 2
	private String nickname;
}
