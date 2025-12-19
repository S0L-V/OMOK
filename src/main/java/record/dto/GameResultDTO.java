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
public class GameResultDTO {

	private String id; // PK
	private String gameId; // 게임 식별
	private String roomId; // 어떤 방에서 진행됐는지 
	private String userId; // 플레이어 ID
	private String stoneColor; // 흑0 백1
	private String gameResult; // 승0 패1 무2
	private String playType; // 개인전0 팀전1
	private Timestamp finishedAt; // 게임 종료 시간
}
