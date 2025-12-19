<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8" />
  <title>Login</title>
  <style>
    body {
      margin: 0;
      height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      font-family: Arial, sans-serif;
    }

    /* 이미지 버튼 */
    .kakao-btn {
      border: none;
      background: transparent;
      padding: 0;
      cursor: pointer;
    }

    .kakao-btn img {
      display: block;
      width: 180px;   
      height: auto;
    }

    .kakao-btn:active {
      transform: translateY(1px);
    }
  </style>
</head>
<body>

  <form action="<%=request.getContextPath()%>/login/kakao" method="get">
    <button type="submit" class="kakao-btn" aria-label="카카오 로그인">
      <img src="<%=request.getContextPath()%>/static/kakao_login_medium_narrow.png" alt="카카오 로그인" />
    </button>
  </form>

</body>
</html>
