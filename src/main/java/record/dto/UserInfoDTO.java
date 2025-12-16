package record.dto;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class UserInfoDTO {

	private String userId;
	private String nickname;
	private Integer totalWin;
	private Integer totalLose;
	private Integer currentStreak;
	private Integer maxWinStreak;
	private Double winRate;
	private Integer coin;
	private Timestamp lastGameDate;
}
