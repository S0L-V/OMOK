<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>싱글 게임 중</title>
    <style>
        .players { display: flex; gap: 16px; margin: 16px 0; }
        .player {
            display: flex;
            align-items: center;
            gap: 10px;
            border: 1px solid #ccc;
            padding: 10px;
            border-radius: 10px;
            min-width: 180px;
        }
        .avatar { width: 40px; height: 40px; border-radius: 50%; background: #eee; }
        .name { font-weight: bold; }
        .bubble {
            display: none;
            margin-left: 6px;
            padding: 6px 10px;
            border: 1px solid #aaa;
            border-radius: 14px;
            background: #fff;
        }
        .emojiBar { margin-top: 16px; }
        .emojiBar button { font-size: 18px; margin-right: 8px; }
    </style>
</head>
<body>

<h2>게임 중 (이모지 전용)</h2>

<div class="players">
    <div class="player" data-user="Player1">
        <div class="avatar"></div>
        <div><div class="name">Player1</div></div>
        <div class="bubble"></div>
    </div>

    <div class="player" data-user="Player2">
        <div class="avatar"></div>
        <div><div class="name">Player2</div></div>
        <div class="bubble"></div>
    </div>
</div>

<div class="emojiBar">
    <button onclick="sendEmoji('smile')">🙂</button>
    <button onclick="sendEmoji('angry')">😡</button>
    <button onclick="sendEmoji('clap')">👏</button>
</div>

<script>
    window.contextPath = "<%= request.getContextPath() %>";
    const params = new URLSearchParams(location.search);
    window.roomId = params.get("roomId") || "lobby";
</script>

<!-- ws-common 먼저 실행  -->
<script src="<%= request.getContextPath() %>/js/ws-common.js"></script>
<script src="<%= request.getContextPath() %>/js/game.js"></script>

<script src="<%= request.getContextPath() %>/chat/emojiChat.js"></script>
</body>
</html>