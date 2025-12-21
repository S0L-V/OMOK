package jwtLogin.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SignupRequestDTO {
	private String email;
	private String password;
	private String nickname;

}
