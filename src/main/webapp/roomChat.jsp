<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>대기실</title>
    <style>
        .msgArea {
            border: 1px solid #ccc;
            width: 420px;
            height: 260px;
            overflow-y: auto;
            padding: 8px;
            margin-bottom: 10px;
        }
    </style>
</head>
<body>

<h2>대기실 채팅</h2>

<div class="msgArea"></div>
<input type="text" class="content" placeholder="메시지 입력">
<button onclick="sendChat()">보내기</button>

<script>
    // 전역(window)에 박아두기 (room.js에서 사용)
    window.contextPath = "<%= request.getContextPath() %>";
    const params = new URLSearchParams(location.search);
    window.roomId = params.get("roomId") || "lobby";
</script>

<script src="<%= request.getContextPath() %>/js/ws-common.js"></script>
<script src="<%= request.getContextPath() %>/js/room.js"></script>

</body>
</html>
