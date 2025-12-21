package login.vo;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UserInfoVo {

	private String userId;
	private String nickname;

	private int totalWin;
	private int totalLose;
	private int totalDraw;

	private int currentStreak;
	private int maxWinStreak;

	private double winRate;
	private int coin;

	private Timestamp lastGameDate;

}