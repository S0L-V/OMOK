package jwtLogin.dto;

import lombok.Data;

@Data
public class EmailVerifyCodeRequestDTO {

	private String email;
	private String code;

}