package friend.dto;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendDTO {

	private String id; // PK
	private String userId; // 친구 요청을 보낸 사람
	private String friendId; // 친구 요청을 받은 사람
	private String status; // PENDING, ACCEPTED, BLOCKED
	private Timestamp createdAt; // 친구 요청 시간

	// 추가: 화면 표시용 필드
	private String nickname;      // 친구의 닉네임
	private Integer totalWin;     // 친구의 승수
	private Integer totalLose;    // 친구의 패수
	private Double winRate;       // 친구의 승률
}
