package login.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LoginKakaoUserInfoDTO {
	private String userId;
	private String email;
	private String nickname;
	private String loginType; // 0 카카오 1 일반
}
