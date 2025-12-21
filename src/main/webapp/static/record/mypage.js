// 전역 변수
let currentUserId = null;
let userStats = null;
let currentRankingType = 'winRate';

// 페이지 로드 시
window.addEventListener('DOMContentLoaded', function () {
    // ✅ currentUserId는 이미 JSP에서 설정됨
    if (!currentUserId) {
        alert('로그인이 필요합니다.');
        location.href = contextPath + '/login.jsp';
        return;
    }

    loadUserStats();
    loadFriendRequests();
    loadFriends();
});

// ========== 유저 통계 로드 ==========
function loadUserStats() {
    fetch(`${contextPath}/record/stats/${currentUserId}`)
        .then(response => response.json())
        .then(result => {
            if (result.success) {
                userStats = result.data;
                displayUserStats(userStats);

                document.getElementById('loading').style.display = 'none';
                document.getElementById('mypageContent').style.display = 'block';
            } else {
                alert('사용자 정보를 불러올 수 없습니다.');
                location.href = contextPath + '/login.jsp';
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('서버 오류가 발생했습니다.');
        });
}

// 유저 통계 표시
function displayUserStats(stats) {
    // 프로필
    document.getElementById('profileAvatar').textContent =
        stats.nickname.charAt(0).toUpperCase();
    document.getElementById('profileNickname').textContent = stats.nickname;
    document.getElementById('profileUserId').textContent = `ID: ${stats.userId}`;

    // 통계
    const totalGames = stats.totalWin + stats.totalLose + stats.totalDraw;
    document.getElementById('statTotalGames').textContent = totalGames;
    document.getElementById('statWins').textContent = stats.totalWin;
    document.getElementById('statLosses').textContent = stats.totalLose;
    document.getElementById('statDraws').textContent = stats.totalDraw;
    document.getElementById('statWinRate').textContent = stats.winRate.toFixed(1) + '%';

    // 연승/연패 표시
    const streakValue = stats.currentStreak;
    const streakText = streakValue > 0 ? `${streakValue}연승` :
        streakValue < 0 ? `${Math.abs(streakValue)}연패` : '0';
    document.getElementById('statCurrentStreak').textContent = streakText;

    document.getElementById('statBestStreak').textContent = stats.maxWinStreak;
    document.getElementById('statCoin').textContent = stats.coin.toLocaleString();
}

// ========== 친구 요청 로드 ==========
function loadFriendRequests() {
    fetch(`${contextPath}/friend/pending`)
        .then(response => response.json())
        .then(result => {
            if (result.success) {
                displayFriendRequests(result.data);
            }
        })
        .catch(error => console.error('Error:', error));
}

// 친구 요청 표시
function displayFriendRequests(requests) {
    const section = document.getElementById('friendRequestsSection');
    const list = document.getElementById('friendRequestsList');
    const count = document.getElementById('requestCount');

    if (!requests || requests.length === 0) {
        section.style.display = 'none';
        return;
    }

    section.style.display = 'block';
    count.textContent = requests.length;

    list.innerHTML = requests.map(request => `
        <div class="friend-request-item" id="request-${request.friendId}">
            <div class="request-user-info">
                <div class="request-avatar">
                    ${request.nickname.charAt(0).toUpperCase()}
                </div>
                <div class="request-details">
                    <div class="request-nickname">${escapeHtml(request.nickname)}</div>
                    <div class="request-stats">
                        ${request.totalWin}승 ${request.totalLose}패 | 승률 ${request.winRate.toFixed(1)}%
                    </div>
                </div>
            </div>
            <div class="request-actions">
                <button class="btn-accept" onclick="acceptFriendRequest('${request.friendId}')">수락</button>
                <button class="btn-reject" onclick="rejectFriendRequest('${request.friendId}')">거절</button>
            </div>
        </div>
    `).join('');
}

// 친구 요청 수락
function acceptFriendRequest(friendId) {
    if (!confirm('친구 요청을 수락하시겠습니까?')) {
        return;
    }

    fetch(`${contextPath}/friend/accept`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({friendId: friendId})
    })
        .then(response => response.json())
        .then(result => {
            if (result.success) {
                alert('친구 요청을 수락했습니다.');
                loadFriendRequests();
                loadFriends();
            } else {
                alert(result.message || '친구 요청 수락에 실패했습니다.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('서버 오류가 발생했습니다.');
        });
}

// 친구 요청 거절
function rejectFriendRequest(friendId) {
    if (!confirm('친구 요청을 거절하시겠습니까?')) {
        return;
    }

    fetch(`${contextPath}/friend/remove`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({friendId: friendId})
    })
        .then(response => response.json())
        .then(result => {
            if (result.success) {
                alert('친구 요청을 거절했습니다.');
                loadFriendRequests();
            } else {
                alert(result.message || '친구 요청 거절에 실패했습니다.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('서버 오류가 발생했습니다.');
        });
}

// ========== 친구 목록 로드 ==========
function loadFriends() {
    fetch(`${contextPath}/friend/list`)
        .then(response => response.json())
        .then(result => {
            if (result.success) {
                displayFriends(result.data);
            }
        })
        .catch(error => console.error('Error:', error));
}

// 친구 목록 표시
function displayFriends(friends) {
    const list = document.getElementById('friendsList');
    const count = document.getElementById('friendCount');

    if (!friends || friends.length === 0) {
        list.innerHTML = '<div class="empty-message">아직 친구가 없습니다</div>';
        count.textContent = '0';
        return;
    }

    count.textContent = friends.length;

    list.innerHTML = friends.map(friend => `
        <div class="friend-card">
            <div class="friend-avatar">
                ${friend.nickname.charAt(0).toUpperCase()}
            </div>
            <div class="friend-nickname">${escapeHtml(friend.nickname)}</div>
            <div class="friend-stats">
                코인: ${friend.coin ? friend.coin.toLocaleString() : 0}<br>
                ${friend.totalWin || 0}승 ${friend.totalLose || 0}패<br>
                승률: ${friend.winRate ? friend.winRate.toFixed(1) : 0}%
            </div>
            <div class="friend-actions">
                <button class="btn-remove" onclick="removeFriend('${friend.friendId}')">삭제</button>
            </div>
        </div>
    `).join('');
}

// 친구 삭제
function removeFriend(friendId) {
    if (!confirm('정말 친구를 삭제하시겠습니까?')) {
        return;
    }

    fetch(`${contextPath}/friend/remove`, {
        method: 'DELETE',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({friendId: friendId})
    })
        .then(response => response.json())
        .then(result => {
            if (result.success) {
                alert('친구를 삭제했습니다.');
                loadFriends();
            } else {
                alert(result.message || '친구 삭제에 실패했습니다.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('서버 오류가 발생했습니다.');
        });
}

// ========== 친구 검색 ==========
function searchFriend() {
    const searchInput = document.getElementById('searchInput');
    const nickname = searchInput.value.trim();

    if (!nickname) {
        alert('닉네임을 입력해주세요.');
        return;
    }

    // TODO: 닉네임으로 사용자 검색 API 필요
    // 임시로 userId로 검색
    fetch(`${contextPath}/record/stats/${nickname}`)
        .then(response => response.json())
        .then(result => {
            if (result.success) {
                displaySearchResult(result.data);
            } else {
                displaySearchResult(null);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            displaySearchResult(null);
        });
}

// 검색 결과 표시
function displaySearchResult(user) {
    const modal = document.getElementById('searchModal');
    const result = document.getElementById('searchResult');

    if (!user) {
        result.innerHTML = '<div class="error-message">사용자를 찾을 수 없습니다.</div>';
    } else if (user.userId === currentUserId) {
        result.innerHTML = '<div class="error-message">자기 자신에게는 친구 요청을 보낼 수 없습니다.</div>';
    } else {
        result.innerHTML = `
            <div class="search-user-card">
                <div class="search-user-info">
                    <div class="search-avatar">
                        ${user.nickname.charAt(0).toUpperCase()}
                    </div>
                    <div class="search-details">
                        <div class="search-nickname">${escapeHtml(user.nickname)}</div>
                        <div class="search-stats">
                            ${user.totalWin}승 ${user.totalLose}패 | 승률 ${user.winRate.toFixed(1)}%
                        </div>
                    </div>
                </div>
                <button class="btn-send-request" onclick="sendFriendRequest('${user.userId}')">
                    친구 요청
                </button>
            </div>
        `;
    }

    modal.style.display = 'block';
}

// 친구 요청 보내기
function sendFriendRequest(friendId) {
    fetch(`${contextPath}/friend/request`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({friendId: friendId})
    })
        .then(response => response.json())
        .then(result => {
            if (result.success) {
                alert('친구 요청을 보냈습니다.');
                closeSearchModal();
                document.getElementById('searchInput').value = '';
            } else {
                alert(result.message || '친구 요청에 실패했습니다.');
            }
        })
        .catch(error => {
            console.error('Error:', error);
            alert('서버 오류가 발생했습니다.');
        });
}

// 검색 모달 닫기
function closeSearchModal() {
    document.getElementById('searchModal').style.display = 'none';
}

// ========== 탭 전환 ==========
function switchTab(tabName) {
    // 모든 탭 버튼 비활성화
    document.querySelectorAll('.tab-btn').forEach(btn => {
        btn.classList.remove('active');
    });

    // 모든 탭 컨텐츠 숨기기
    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.remove('active');
    });

    // 선택된 탭 활성화
    event.target.classList.add('active');
    document.getElementById(tabName + 'Tab').classList.add('active');

    // 데이터 로드
    if (tabName === 'history') {
        loadGameHistory();
    } else if (tabName === 'ranking') {
        loadRanking(currentRankingType);
    }
}

// ========== 게임 기록 로드 ==========
function loadGameHistory() {
    fetch(`${contextPath}/record/history/${currentUserId}`)
        .then(response => response.json())
        .then(result => {
            if (result.success) {
                displayGameHistory(result.data);
            }
        })
        .catch(error => console.error('Error:', error));
}

// 게임 기록 표시
function displayGameHistory(games) {
    const tbody = document.getElementById('gameHistoryBody');

    if (!games || games.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" class="empty-message">게임 기록이 없습니다</td></tr>';
        return;
    }

    tbody.innerHTML = games.map(game => {
        const resultClass = game.gameResult === 'W' ? 'win' :
            game.gameResult === 'L' ? 'lose' : 'draw';
        const resultText = game.gameResult === 'W' ? '승리' :
            game.gameResult === 'L' ? '패배' : '무승부';
        const stoneColor = game.stoneColor === '1' ? '흑돌' : '백돌';
        const playType = game.playType === '1' ? '개인전' : '팀전';

        return `
            <tr>
                <td>${formatDateTime(game.finishedAt)}</td>
                <td>${escapeHtml(game.roomId)}</td>
                <td>${stoneColor}</td>
                <td><span class="result-badge ${resultClass}">${resultText}</span></td>
                <td>${playType}</td>
            </tr>
        `;
    }).join('');
}

// ========== 랭킹 로드 ==========
function loadRanking(type) {
    currentRankingType = type;

    // 필터 버튼 활성화
    document.querySelectorAll('.filter-btn').forEach(btn => {
        btn.classList.remove('active');
    });
    event.target.classList.add('active');

    // TODO: 랭킹 API 필요 (임시로 더미 데이터)
    const dummyRanking = [
        {
            userId: 'user-001',
            nickname: '프로게이머',
            totalWin: 150,
            totalLose: 50,
            totalDraw: 10,
            winRate: 71.4,
            maxWinStreak: 15
        },
        {
            userId: 'user-002',
            nickname: '오목왕',
            totalWin: 120,
            totalLose: 60,
            totalDraw: 5,
            winRate: 64.9,
            maxWinStreak: 12
        },
        {
            userId: 'user-003',
            nickname: '연승중',
            totalWin: 100,
            totalLose: 70,
            totalDraw: 8,
            winRate: 56.2,
            maxWinStreak: 20
        }
    ];

    displayRanking(dummyRanking);
}

// 랭킹 표시
function displayRanking(rankings) {
    const tbody = document.getElementById('rankingBody');

    if (!rankings || rankings.length === 0) {
        tbody.innerHTML = '<tr><td colspan="7" class="empty-message">랭킹 정보가 없습니다</td></tr>';
        return;
    }

    tbody.innerHTML = rankings.map((user, index) => {
        const rank = index + 1;
        const rankClass = rank === 1 ? 'gold' : rank === 2 ? 'silver' : rank === 3 ? 'bronze' : '';
        const rankBadge = rank <= 3 ?
            `<span class="rank-badge ${rankClass}">${rank}</span>` :
            rank;

        return `
            <tr>
                <td>${rankBadge}</td>
                <td>${escapeHtml(user.nickname)}</td>
                <td>${user.winRate.toFixed(1)}%</td>
                <td>${user.totalWin}</td>
                <td>${user.totalLose}</td>
                <td>${user.totalDraw}</td>
                <td>${user.maxWinStreak}</td>
            </tr>
        `;
    }).join('');
}

// ========== 유틸리티 함수 ==========
function formatDateTime(timestamp) {
    if (!timestamp) return '-';
    const date = new Date(timestamp);
    return date.toLocaleDateString('ko-KR') + ' ' +
        date.toLocaleTimeString('ko-KR', {hour: '2-digit', minute: '2-digit'});
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Enter 키로 검색
document.addEventListener('DOMContentLoaded', function () {
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('keypress', function (e) {
            if (e.key === 'Enter') {
                searchFriend();
            }
        });
    }
});