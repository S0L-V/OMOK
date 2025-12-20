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

<h2>🎮 오목 로비</h2>

<form action="${pageContext.request.contextPath}/lobby/room/create" method="get">
	<button type="submit">방 생성</button>
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
						<form action="${pageContext.request.contextPath}/room/enter" method="get">
							<input type="hidden" name="roomId" value="${room.id}" />
							<button type="submit">입장</button>
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
