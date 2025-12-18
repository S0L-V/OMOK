<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>방 생성</title>

<style>
  .wrap { max-width: 520px; margin: 24px auto; padding: 16px; border: 1px solid #ddd; border-radius: 10px; }
  .row { margin: 12px 0; }
  label { display:block; margin-bottom: 6px; }
  input[type="text"], input[type="password"], select { width:100%; padding: 10px; box-sizing: border-box; }
  .btns { display:flex; gap: 8px; margin-top: 16px; }
</style>

<script>
  function togglePwd(show) {
    const box = document.getElementById("pwdBox");
    const input = document.getElementById("roomPwd");
    box.style.display = show ? "block" : "none";
    input.required = show;
    if (!show) input.value = "";
  }
</script>

</head>
<body>

<div class="wrap">
  <h2>방 생성</h2>
  <form action="${pageContext.request.contextPath}/room/create" method="post">
      
      <!-- 세션 로그인 유저 id를 hostUserId로 전달 TODO: value 수정 필요 ${sessionScope.loginUser.id}-->
      <input type="hidden" name="hostUserId" value="u-001" />

      <div class="row">
        <label>방 이름</label>
        <input type="text" name="roomName" maxlength="30" required />
      </div>

      <div class="row">
        <label>공개 여부</label>
        <label>
          <input type="radio" name="isPublic" value="1" checked onclick="togglePwd(false)" />
          공개
        </label>
        <label>
          <input type="radio" name="isPublic" value="0" onclick="togglePwd(true)" />
          비공개 🔒
        </label>
      </div>

      <div class="row" id="pwdBox" style="display:none;">
        <label>방 비밀번호</label>
        <input type="password" id="roomPwd" name="roomPwd" maxlength="20" />
      </div>

      <div class="row">
        <label>게임 타입</label>
        <label><input type="radio" name="playType" value="0" checked /> 개인전</label>
        <label><input type="radio" name="playType" value="1" /> 팀전</label>
      </div>

      <div class="btns">
        <button type="submit">생성</button>
        <a href="${pageContext.request.contextPath}/lobby">
          <button type="button">취소</button>
        </a>
      </div>
  </form>
</div>

</body>
</html>
