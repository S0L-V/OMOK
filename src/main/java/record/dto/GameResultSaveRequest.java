package record.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GameResultSaveRequest {
	private String gameId;
	private String roomId;
	private String playType;
	private List<PlayerResult> results;
}
