<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>로비</title>
<link rel="stylesheet"
      href="${pageContext.request.contextPath}/static/lobby/lobby.css">
</head>
<body>

<header class="top-bar">
  <div class="brand">OMOK</div>

  <div class="auth-box">
    <c:choose>
      <c:when test="${empty sessionScope.loginUserId}">
        <a href="${pageContext.request.contextPath}/login/kakao">
          <img
            src="${pageContext.request.contextPath}/static/kakao_login_medium_narrow.png"
            alt="카카오 로그인"
            style="height:40px; cursor:pointer;"
          />
        </a>
      </c:when>

      <c:otherwise>
        <span class="nickname">
          👤 <c:out value="${sessionScope.loginNickname}" />
        </span>

        <form action="${pageContext.request.contextPath}/logout"
              method="post"
              style="display:inline;">
          <button type="submit" class="btn btn-logout">
            로그아웃
          </button>
        </form>
        <button class="btn" onclick="window.location.href='/user/my'">마이페이지</button> 
      </c:otherwise>
    </c:choose>
  </div>
</header>

<!-- ✅ 추가 wrapper(레이아웃용). 기존 태그/클래스/아이디는 유지 -->
<main class="lobby-layout">

  <!-- 좌측: 방 목록 -->
  <section class="lobby-left">
    <h2>게임 방 목록</h2>
    <p class="subtext">참여할 게임을 선택하세요</p>

    <form action="${pageContext.request.contextPath}/lobby/room/create" method="get">
      <button type="submit"
              <c:if test="${empty sessionScope.loginUserId}">disabled</c:if>
              <c:if test="${empty sessionScope.loginUserId}">onclick="alert('로그인이 필요합니다.'); return false;"</c:if>>
        방 생성
      </button>
    </form>

    <table>
      <thead>
        <tr>
          <th>방 이름</th>
          <th>공개 여부</th>
          <th>게임 타입</th>
          <th>인원</th>
          <th>입장</th>
        </tr>
      </thead>

      <tbody id="room-tbody">
        <tr>
          <td colspan="5">방 목록을 불러오는 중...</td>
        </tr>
      </tbody>
    </table>
  </section>

  <!-- 우측: 로그인 카드(추가) -->
  <aside class="lobby-right">
    <div class="login-card">
      <c:choose>
        <c:when test="${empty sessionScope.loginUserId}">
          <h3>로그인</h3>

          <!-- (디자인용) 기본 로그인 UI -->
          <div class="field">
            <label>이메일</label>
            <input id="login-email" type="text" placeholder="이메일" />
          </div>

          <div class="field">
            <label>비밀번호</label>
            <input id="login-password" type="password" placeholder="비밀번호" />
          </div>

          <button type="button" class="btn-primary" id="btn-login">
            로그인
          </button>

          <div class="divider"><span>OR</span></div>

          <a class="kakao-wide" href="${pageContext.request.contextPath}/login/kakao">
            카카오 로그인
          </a>

          <p class="signup-hint">
            계정이 없으신가요? <a href="${pageContext.request.contextPath}/signup2">회원가입</a>
          </p>
        </c:when>

        <c:otherwise>
          <h3>내 계정</h3>

          <div class="profile-box">
            <div class="profile-name">
              👤 <c:out value="${sessionScope.loginNickname}" />
            </div>
            <div class="profile-sub">환영합니다!</div>
          </div>

          <button type="button" class="btn-primary"
                  onclick="window.location.href='/user/my'">
            마이페이지
          </button>

          <form action="${pageContext.request.contextPath}/logout"
                method="post"
                style="margin-top:10px;">
            <button type="submit" class="btn-secondary">
              로그아웃
            </button>
          </form>
        </c:otherwise>
      </c:choose>
    </div>
  </aside>

</main>

<script>
  const IS_LOGIN = ${not empty sessionScope.loginUserId};
  const CTX = "${pageContext.request.contextPath}";
</script>
<script src="${pageContext.request.contextPath}/static/lobby/lobby.js"></script>

</body>
</html>
