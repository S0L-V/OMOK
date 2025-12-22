<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>마이페이지</title>
    <style>
        body {
            font-family: 'Noto Sans KR', sans-serif;
            background-color: #f5f7fa;
            margin: 0;
            padding: 0;
        }

        /* 레이아웃: 좌우 2단 분리 */
        .mypage-wrapper {
            display: grid;
            grid-template-columns: 2fr 1fr;
            gap: 20px;
            max-width: 1200px;
            margin: 40px auto;
            padding: 20px;
        }

        /* 공통 카드 스타일 */
        .card {
            background: white;
            border-radius: 12px;
            padding: 25px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
            margin-bottom: 20px;
        }

        .profile-header {
            display: flex;
            align-items: center;
            gap: 20px;
            margin-bottom: 20px;
        }

        .profile-avatar {
            width: 70px;
            height: 70px;
            background: #667eea;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            color: white;
            font-size: 30px;
        }

        .stats-grid {
            display: grid;
            grid-template-columns: repeat(4, 1fr);
            gap: 10px;
            margin-bottom: 20px;
        }

        .stat-item {
            background: #f8f9fa;
            padding: 15px;
            text-align: center;
            border-radius: 8px;
        }

        .stat-val {
            font-weight: bold;
            font-size: 18px;
            color: #333;
        }

        .stat-label {
            font-size: 12px;
            color: #666;
        }

        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 10px;
        }

        th, td {
            padding: 10px;
            text-align: center;
            border-bottom: 1px solid #eee;
            font-size: 14px;
        }

        th {
            background: #f1f3f5;
        }

        /* 오른쪽 영역 스타일 */
        .section-title {
            font-size: 18px;
            font-weight: bold;
            margin-bottom: 15px;
            border-bottom: 2px solid #eee;
            padding-bottom: 10px;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        /* 알림 배지 */
        .badge {
            background: #ff4757;
            color: white;
            border-radius: 10px;
            padding: 2px 8px;
            font-size: 12px;
            font-weight: bold;
            min-width: 20px;
            text-align: center;
        }

        /* 검색창 */
        .search-box {
            display: flex;
            gap: 5px;
            margin-bottom: 20px;
        }

        .search-box input {
            flex: 1;
            padding: 8px;
            border: 1px solid #ddd;
            border-radius: 4px;
        }

        .search-box button {
            padding: 8px 15px;
            background: #333;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
        }

        .search-box button:hover {
            background: #555;
        }

        /* 검색 결과 */
        #searchResult {
            background: #e3f2fd;
            padding: 15px;
            border-radius: 8px;
            margin-bottom: 20px;
            display: none;
        }

        .found-user {
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .btn-request {
            background: #4CAF50;
            color: white;
            border: none;
            padding: 5px 10px;
            border-radius: 4px;
            cursor: pointer;
            font-size: 12px;
        }

        .btn-request:hover {
            background: #45a049;
        }

        /* 친구 목록 */
        .friend-list {
            list-style: none;
            padding: 0;
            margin: 0;
            max-height: 400px;
            overflow-y: auto;
        }

        .friend-item {
            display: flex;
            align-items: center;
            gap: 10px;
            padding: 10px;
            border-bottom: 1px solid #f0f0f0;
        }

        .friend-item:last-child {
            border-bottom: none;
        }

        .f-avatar {
            width: 35px;
            height: 35px;
            background: #ddd;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 14px;
            flex-shrink: 0;
        }

        .f-info {
            flex: 1;
        }

        .f-name {
            font-weight: bold;
            font-size: 14px;
        }

        .f-status {
            font-size: 12px;
            color: #888;
        }

        /* 친구 요청 버튼 */
        .pending-actions {
            display: flex;
            gap: 5px;
            flex-shrink: 0;
        }

        .btn-accept {
            background: #4CAF50;
            color: white;
            border: none;
            padding: 5px 10px;
            border-radius: 4px;
            cursor: pointer;
            font-size: 12px;
        }

        .btn-accept:hover {
            background: #45a049;
        }

        .btn-reject {
            background: #f44336;
            color: white;
            border: none;
            padding: 5px 10px;
            border-radius: 4px;
            cursor: pointer;
            font-size: 12px;
        }

        .btn-reject:hover {
            background: #da190b;
        }

        .btn-back {
            display: block;
            width: 100%;
            padding: 12px;
            background: #555;
            color: white;
            text-align: center;
            text-decoration: none;
            border-radius: 6px;
            margin-top: 20px;
        }

        .btn-back:hover {
            background: #333;
        }

        /* 빈 상태 메시지 */
        .empty-message {
            padding: 10px;
            color: #888;
            text-align: center;
        }
    </style>
</head>
<body>

<div class="mypage-wrapper">
    <!-- 왼쪽 영역 -->
    <div class="left-col">
        <div class="card">
            <div class="profile-header">
                <div class="profile-avatar">👤</div>
                <div>
                    <h2>${userInfo.nickname}</h2>
                    <span style="color: #666; font-size: 14px;">ID: ${userInfo.userId}</span>
                </div>
            </div>

            <div class="stats-grid">
                <div class="stat-item">
                    <div class="stat-label">승률</div>
                    <div class="stat-val">${userInfo.winRate}%</div>
                </div>
                <div class="stat-item">
                    <div class="stat-label">전적</div>
                    <div class="stat-val">${userInfo.totalWin}승 ${userInfo.totalLose}패</div>
                </div>
                <div class="stat-item">
                    <div class="stat-label">최고연승</div>
                    <div class="stat-val">${userInfo.maxWinStreak}</div>
                </div>
                <div class="stat-item">
                    <div class="stat-label">코인</div>
                    <div class="stat-val">${userInfo.coin}</div>
                </div>
            </div>
        </div>

        <div class="card">
            <div class="section-title">🕹️ 최근 전적</div>
            <table>
                <thead>
                <tr>
                    <th>날짜</th>
                    <th>결과</th>
                    <th>돌</th>
                    <th>타입</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${empty gameHistory}">
                        <tr>
                            <td colspan="4">기록이 없습니다.</td>
                        </tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="game" items="${gameHistory}">
                            <tr>
                                <td><fmt:formatDate value="${game.finishedAt}" pattern="MM/dd HH:mm"/></td>
                                <td>
                                    <c:choose>
                                        <c:when test="${game.gameResult eq '0'}"><span
                                                style="color:blue">승리</span></c:when>
                                        <c:when test="${game.gameResult eq '1'}"><span
                                                style="color:red">패배</span></c:when>
                                        <c:otherwise>무승부</c:otherwise>
                                    </c:choose>
                                </td>
                                <td>${game.stoneColor eq '0' ? '⚫' : '⚪'}</td>
                                <td>${game.playType eq '0' ? '개인' : '팀'}</td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>

        <a href="${pageContext.request.contextPath}/lobby" class="btn-back">← 로비로 돌아가기</a>
    </div>

    <!-- 오른쪽 영역 -->
    <div class="right-col">
        <!-- 친구 찾기 -->
        <div class="card">
            <div class="section-title">🔍 친구 찾기</div>
            <div class="search-box">
                <input type="text" id="searchNickname" placeholder="닉네임 입력"
                       onkeypress="if(event.keyCode==13) searchUser()">
                <button onclick="searchUser()">검색</button>
            </div>

            <div id="searchResult">
                <div class="found-user">
                    <div>
                        <span id="foundName" style="font-weight:bold;"></span>
                        <br>
                        <span id="foundStats" style="font-size:12px; color:#666;"></span>
                    </div>
                    <button class="btn-request" onclick="sendFriendRequest()">친구 추가</button>
                    <input type="hidden" id="foundUserId">
                </div>
            </div>
        </div>

        <!-- 받은 친구 요청 -->
        <div class="card">
            <div class="section-title">
                📬 받은 친구 요청
                <span class="badge" id="pendingCount" style="display:none;">0</span>
            </div>
            <ul class="friend-list" id="pendingList">
                <li class="empty-message">대기 중인 요청이 없습니다.</li>
            </ul>
        </div>

        <!-- 내 친구 목록 -->
        <div class="card">
            <div class="section-title">👥 내 친구 (${myFriends.size()})</div>
            <ul class="friend-list">
                <c:choose>
                    <c:when test="${empty myFriends}">
                        <li class="empty-message">등록된 친구가 없습니다.</li>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="friend" items="${myFriends}">
                            <li class="friend-item">
                                <div class="f-avatar">${friend.nickname.charAt(0)}</div>
                                <div class="f-info">
                                    <div class="f-name">${friend.nickname}</div>
                                    <div class="f-status">
                                        승률: ${friend.winRate}% (${friend.totalWin}승 ${friend.totalLose}패)
                                    </div>
                                </div>
                            </li>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </ul>
        </div>
    </div>
</div>

<script>
    const CTX = "${pageContext.request.contextPath}";

    // 페이지 로드 시 대기 중인 친구 요청 불러오기
    window.addEventListener('DOMContentLoaded', function () {
        loadPendingRequests();
    });

    // 1. 대기 중인 친구 요청 조회
    function loadPendingRequests() {
        fetch(CTX + '/friend/pending')
            .then(res => res.json())
            .then(json => {
                if (json.success) {
                    const requests = json.data;
                    updatePendingUI(requests);
                } else {
                    console.error("친구 요청 조회 실패:", json.message);
                }
            })
            .catch(err => {
                console.error("친구 요청 조회 중 오류:", err);
            });
    }

    // 2. 대기 중인 요청 UI 업데이트
    function updatePendingUI(requests) {
        const pendingList = document.getElementById("pendingList");
        const pendingCount = document.getElementById("pendingCount");

        if (requests.length === 0) {
            pendingList.innerHTML = '<li class="empty-message">대기 중인 요청이 없습니다.</li>';
            pendingCount.style.display = "none";
            return;
        }

        // 배지 표시
        pendingCount.textContent = requests.length;
        pendingCount.style.display = "inline-block";

        // 요청 목록 렌더링
        pendingList.innerHTML = '';

        requests.forEach(req => {
            console.log("친구 요청 데이터:", req); // 디버깅용

            const li = document.createElement('li');
            li.className = 'friend-item';

            // DOM 요소 직접 생성
            const avatar = document.createElement('div');
            avatar.className = 'f-avatar';
            avatar.textContent = req.nickname.charAt(0);

            const info = document.createElement('div');
            info.className = 'f-info';

            const name = document.createElement('div');
            name.className = 'f-name';
            name.textContent = req.nickname;

            const status = document.createElement('div');
            status.className = 'f-status';
            status.textContent = `승률: ${req.winRate}% (${req.totalWin}승 ${req.totalLose}패)`;

            info.appendChild(name);
            info.appendChild(status);

            const actions = document.createElement('div');
            actions.className = 'pending-actions';

            const acceptBtn = document.createElement('button');
            acceptBtn.className = 'btn-accept';
            acceptBtn.textContent = '수락';
            acceptBtn.onclick = function () {
                // ✅ req.friendId가 아니라 req.userId 사용!
                acceptRequest(req.userId, req.nickname);
            };

            const rejectBtn = document.createElement('button');
            rejectBtn.className = 'btn-reject';
            rejectBtn.textContent = '거절';
            rejectBtn.onclick = function () {
                // ✅ req.friendId가 아니라 req.userId 사용!
                rejectRequest(req.userId, req.nickname);
            };

            actions.appendChild(acceptBtn);
            actions.appendChild(rejectBtn);

            li.appendChild(avatar);
            li.appendChild(info);
            li.appendChild(actions);

            pendingList.appendChild(li);
        });
    }


    // 3. 친구 요청 수락
    function acceptRequest(requesterId, nickname) {
        if (!confirm(nickname + "님의 친구 요청을 수락하시겠습니까?")) {
            return;
        }

        fetch(CTX + '/friend/accept', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({requesterId: requesterId})
        })
            .then(res => res.json())
            .then(json => {
                if (json.success) {
                    alert("친구 요청을 수락했습니다!");
                    location.reload();  // 페이지 새로고침하여 친구 목록 업데이트
                } else {
                    alert(json.message || "친구 요청 수락 실패");
                }
            })
            .catch(err => {
                console.error(err);
                alert("서버 오류 발생");
            });
    }

    // 4. 친구 요청 거절
    function rejectRequest(requesterId, nickname) {
        if (!confirm(nickname + "님의 친구 요청을 거절하시겠습니까?")) {
            return;
        }

        fetch(CTX + '/friend/reject', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({requesterId: requesterId})
        })
            .then(res => res.json())
            .then(json => {
                if (json.success) {
                    alert("친구 요청을 거절했습니다.");
                    loadPendingRequests();  // 요청 목록만 새로고침
                } else {
                    alert(json.message || "친구 요청 거절 실패");
                }
            })
            .catch(err => {
                console.error(err);
                alert("서버 오류 발생");
            });
    }

    // 5. 유저 검색
    function searchUser() {
        const nickname = document.getElementById("searchNickname").value.trim();
        if (!nickname) {
            alert("닉네임을 입력하세요.");
            return;
        }

        fetch(CTX + '/user/search?nickname=' + encodeURIComponent(nickname))
            .then(res => res.json())
            .then(json => {
                if (json.success) {
                    const user = json.data;
                    document.getElementById("foundName").textContent = user.nickname;
                    document.getElementById("foundStats").textContent = user.totalWin + "승 " + user.totalLose + "패";
                    document.getElementById("foundUserId").value = user.userId;
                    document.getElementById("searchResult").style.display = "block";
                } else {
                    alert(json.message || "사용자를 찾을 수 없습니다.");
                    document.getElementById("searchResult").style.display = "none";
                }
            })
            .catch(err => {
                console.error(err);
                alert("검색 중 오류가 발생했습니다.");
            });
    }

    // 6. 친구 요청 보내기
    function sendFriendRequest() {
        const targetId = document.getElementById("foundUserId").value;
        if (!targetId) return;

        if (!confirm(document.getElementById("foundName").textContent + "님에게 친구 요청을 보내시겠습니까?")) {
            return;
        }

        fetch(CTX + '/friend/request', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({friendId: targetId})
        })
            .then(res => res.json())
            .then(json => {
                if (json.success) {
                    alert("친구 요청을 보냈습니다!");
                    document.getElementById("searchNickname").value = "";
                    document.getElementById("searchResult").style.display = "none";
                } else {
                    alert(json.message || "친구 요청 실패");
                }
            })
            .catch(err => {
                console.error(err);
                alert("서버 오류 발생");
            });
    }
</script>

</body>
</html>
