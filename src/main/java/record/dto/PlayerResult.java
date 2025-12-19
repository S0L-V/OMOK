package record.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PlayerResult {
	private String userId;
	private String stoneColor;
	private String gameResult;
}
