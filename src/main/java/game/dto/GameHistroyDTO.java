package game.dto;


import java.sql.Timestamp;

import lombok.Data;

@Data
public class GameHistroyDTO {

	private String gameId;
	private String roomId;
	private String playType;
	private String winnerTeam;
	private Timestamp startedAt;
	private Timestamp finishedAt;
}
