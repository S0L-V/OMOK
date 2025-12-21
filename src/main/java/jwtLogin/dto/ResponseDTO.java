package jwtLogin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseDTO {

	private String accessToken;
	private User user;

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@Builder
	public static class User {

		private String id;
		private String email;
		private String nickname;
		private String loginType;

	}
}
