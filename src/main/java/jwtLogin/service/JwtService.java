package jwtLogin.service;

import jwtLogin.dto.ResponseDTO;

public interface JwtService {

	ResponseDTO signupNormal(String email, String password, String nickname) throws Exception;

	ResponseDTO loginNormal(String email, String password) throws Exception;

	ResponseDTO issueToken(String userId, String email, String loginTypeDb, String nickname);
}
