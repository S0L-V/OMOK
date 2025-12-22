<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>방 생성</title>

<style>
  /* ===== Base ===== */
  * { box-sizing: border-box; }
  body { margin: 0; font-family: system-ui, -apple-system, Segoe UI, Roboto, "Noto Sans KR", Arial, sans-serif; background:#f5f7fb; color:#111827; }

  /* ===== Top bar (added wrapper only) ===== */
  .top-bar {
    height: 92px;
    background: #0b1424;
    color: #fff;
    display: flex;
    align-items: center;
    padding: 0 28px;
  }
  .brand {
    font-size: 28px;
    font-weight: 900;
    letter-spacing: 0.5px;
  }

  /* ===== Layout ===== */
  .page {
    max-width: 980px;
    margin: 0 auto;
    padding: 34px 22px 60px;
  }

  .back-row {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 16px;
    color: #374151;
    font-weight: 700;
  }
  .back-row a {
    text-decoration: none;
    color: inherit;
    display: inline-flex;
    align-items: center;
    gap: 10px;
    padding: 8px 10px;
    border-radius: 10px;
  }
  .back-row a:hover { background: rgba(17,24,39,0.04); }

  .wrap {
    max-width: 760px;       
    margin: 0 auto;
    padding: 36px;
    border: 1px solid #d9dee7;
    border-radius: 0;         
    background: #fff;
  }

  /* 제목 */
  .wrap h2 {
    margin: 0 0 26px;
    font-size: 22px;
    font-weight: 800;
  }

  /* ===== Rows ===== */
  .row { margin: 18px 0; }
  .row > label {
    display: block;
    margin-bottom: 10px;
    font-weight: 800;
    color: #374151;
  }

  input[type="text"], input[type="password"], select {
    width: 100%;
    padding: 16px 16px;
    border: 1px solid #d1d5db;
    background: #fff;
    font-size: 15px;
    outline: none;
  }
  input[type="text"]::placeholder, input[type="password"]::placeholder { color: #9ca3af; }

  input[type="text"]:focus, input[type="password"]:focus, select:focus {
    border-color: #111827;
  }

  /* ===== Radio groups (기존 태그 유지: label 안에 input) ===== */
  .row label label {
    display: inline-flex;
    align-items: center;
    gap: 10px;
    margin-right: 18px;
    font-weight: 700;
    color: #374151;
  }

  input[type="radio"] {
    width: 18px;
    height: 18px;
    accent-color: #111827;
    cursor: pointer;
  }

  /* 비밀번호 영역 */
  #pwdBox { margin-top: 10px; }

  /* ===== Buttons (기존 .btns 유지) ===== */
  .btns {
    display: flex;
    gap: 12px;
    margin-top: 26px;
  }

  .btns button {
    flex: 1;
    padding: 16px 0;
    font-size: 16px;
    font-weight: 900;
    border: 1px solid #d1d5db;
    background: #fff;
    cursor: pointer;
  }

  .btns button[type="submit"] {
    border-color: #0b1424;
    background: #0b1424;
    color: #fff;
  }

  .btns button[type="submit"]:hover { filter: brightness(0.96); }
  .btns button[type="button"]:hover { background: #f9fafb; }

  .btns a { flex: 1; text-decoration: none; }
  .btns a button { width: 100%; }

  @media (max-width: 720px) {
    .wrap { padding: 22px; }
    .btns { flex-direction: column; }
  }
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

<header class="top-bar">
  <div class="brand">OMOK</div>
</header>

<div class="page">
  <div class="back-row">
    <a href="${pageContext.request.contextPath}/lobby">
      <span aria-hidden="true">←</span>
      <span>뒤로가기</span>
    </a>
  </div>

  <div class="wrap">
    <h2>방 생성</h2>

    <form action="${pageContext.request.contextPath}/room/create" method="post">
      <!-- 세션 로그인 유저 id를 hostUserId로 전달 TODO: value 수정 필요 ${sessionScope.loginUser.id}-->
      <input type="hidden" name="hostUserId" value="u-001" />

      <div class="row">
        <label>방 이름</label>
        <input type="text" name="roomName" maxlength="30" required placeholder="방 이름을 입력하세요" />
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
        <input type="password" id="roomPwd" name="roomPwd" maxlength="20" placeholder="비밀번호를 입력하세요" />
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
</div>

</body>
</html>
