package record.dto;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {

	private String userId; // 사용자 ID (PK)
	private String nickname; // 게임 내 닉네임
	private Integer totalWin; // 총 승리 횟수
	private Integer totalLose; // 총 패배 횟수
	private Integer totalDraw; // 총 무승부 횟수
	private Integer currentStreak; // 현재 연승(양수) / 연패 (음수)
	private Integer maxWinStreak; // 역대 최대 연승 기록
	private Double winRate; // 승률 
	private Integer coin; // 보유 게임 포인트
	private Timestamp lastGameDate; // 마지막 게임 플레이 일시
}
