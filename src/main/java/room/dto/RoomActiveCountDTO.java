package room.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoomActiveCountDTO {
	private String roomId;
	private int activeCount;
}
