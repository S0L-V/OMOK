package login.vo;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserVo {

	private String id;
	private String email;
	private String pwd;
	private Timestamp createdAt;
	private String loginType;

}