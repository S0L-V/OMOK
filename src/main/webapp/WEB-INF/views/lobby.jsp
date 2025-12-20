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
          👤 ${sessionScope.loginNickname}
        </span>
        <form action="${pageContext.request.contextPath}/logout"
              method="post"
              style="display:inline;">
          <button type="submit" class="btn btn-logout">
            로그아웃
          </button>
        </form>
      </c:otherwise>
    </c:choose>
  </div>
</header>

<h2>🎮 오목 로비</h2>

<form action="${pageContext.request.contextPath}/lobby/room/create" method="get">
  <button type="submit"
          <c:if test="${empty sessionScope.loginUserId}">disabled</c:if>
          <c:if test="${empty sessionScope.loginUserId}">onclick="alert('로그인이 필요합니다.'); return false;"</c:if>>
    방 생성
  </button>
</form>

<br/>

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
	<c:choose>
		<c:when test="${empty roomList}">
			<tr>
				<td colspan="5">현재 생성된 방이 없습니다.</td>
			</tr>
		</c:when>

		<c:otherwise>
			<c:forEach var="room" items="${roomList}">
				<tr>
					<td>${room.roomName}</td>

					<td>
						<c:choose>
							<c:when test="${room.isPublic == 1}">공개</c:when>
							<c:otherwise>비공개 🔒</c:otherwise>
						</c:choose>
					</td>

					<td>
						<c:choose>
							<c:when test="${room.playType == 0}">개인전</c:when>
							<c:otherwise>팀전</c:otherwise>
						</c:choose>
					</td>

					<td>
						${room.currentUserCnt} / ${room.totalUserCnt}
					</td>

					<td>
					  <form action="${pageContext.request.contextPath}/room/enter" method="post">
						 <input type="hidden" name="roomId" value="${room.id}" />
						  <button type="submit"
						          <c:if test="${empty sessionScope.loginUserId}">disabled</c:if>
						          <c:if test="${empty sessionScope.loginUserId}">onclick="alert('로그인이 필요합니다.'); return false;"</c:if>>
						    입장
						  </button>
					  </form>
					</td>
				</tr>
			</c:forEach>
		</c:otherwise>
	</c:choose>
	</tbody>
</table>
<script src="${pageContext.request.contextPath}/static/lobby/lobby.js"></script>
</body>
</html>
