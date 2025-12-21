package jwtLogin.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class EmailSendCodeRequestDTO {
	private String email;

}