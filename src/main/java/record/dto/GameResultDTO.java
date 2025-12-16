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
	private String stoneColor; // 흑1 백2
	private String gameResult; // W승 L패 D무
	private String playerType; // 개인전1 팀전2
	private Timestamp finishedAt; // 게임 종료 시간
}
