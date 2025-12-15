package game.dto;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class GamePlayerDTO {

	private String id;
	private String gameId;
	private String userId;
	private String stoneColor;
	private String isWinner;
	private Timestamp joindAt;
}
