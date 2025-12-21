package jwtLogin.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVo {
    private String id;
    private String email;
    private String pwd;
    private String loginType;
    private String nickname;
}
