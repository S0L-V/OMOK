<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8" />
  <title>로그인</title>
  <style>
    * { box-sizing: border-box; }
    html, body { height: 100%; }

    body {
      margin: 0;
      height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      font-family: system-ui, -apple-system, Segoe UI, Roboto, "Noto Sans KR", Arial, sans-serif;
      background: #f5f7fb;
      color: #111827;
    }

    .container {
      width: 420px;
      background: #ffffff;
      border: 1px solid #e5e7eb;
      border-radius: 14px;
      padding: 26px;
      box-shadow: 0 1px 0 rgba(17,24,39,0.03);
    }

    h2 {
      margin: 0 0 18px;
      font-size: 20px;
      font-weight: 900;
      color: #111827;
    }

    label {
      display: block;
      margin-bottom: 8px;
      font-weight: 800;
      font-size: 12px;
      color: #6b7280;
    }

    input {
      width: 100%;
      padding: 14px 14px;
      font-size: 14px;
      border: 1px solid #d1d5db;
      border-radius: 12px;
      outline: none;
      background: #fff;
    }

    input:focus {
      border-color: #111827;
      box-shadow: 0 0 0 3px rgba(17,24,39,0.08);
    }

    .btn-row {
      margin-top: 16px;
      display: flex;
      gap: 10px;
    }

    button {
      flex: 1;
      padding: 13px 14px;
      cursor: pointer;
      border-radius: 12px;
      font-weight: 900;
      border: 1px solid #d1d5db;
      background: #ffffff;
      color: #111827;
    }

    .btn-row button:first-child {
      border-color: #111827;
      background: #111827;
      color: #ffffff;
    }

    button:active { transform: translateY(1px); }

    /* =========================
       ✅ [MOD] 카카오 버튼: 캡처처럼 "노란 가로 버튼 + 텍스트"
       - 폭: btn-row 전체와 동일 (100%)
       ========================= */
    .kakao-wrap {
      margin-top: 14px;
      width: 100%;
    }
    .kakao-wrap form {
      width: 100%;
      margin: 0;
    }

    .kakao-wide {
      width: 100%;
      height: 50px;                     /* 캡처처럼 낮고 길게 */
      border-radius: 12px;
      border: 1px solid rgba(0,0,0,0.08);
      background: #FEE500;
      color: #111827;
      font-weight: 900;
      font-size: 14px;
      cursor: pointer;

      display: flex;
      align-items: center;
      justify-content: center;
      gap: 8px;
    }

    .kakao-wide:hover {
      filter: brightness(0.98);
    }

    .kakao-wide:active {
      transform: translateY(1px);
    }

    /* (선택) 아이콘 넣고 싶으면 사용 */
    .kakao-icon {
      width: 18px;
      height: 18px;
      display: inline-block;
      border-radius: 50%;
      background: rgba(0,0,0,0.18);
    }

    /* ✅ [MOD] 상태(pre) 제거 */
    pre#result { display: none; }
  </style>
</head>

<body>
  <div class="container">
    <h2>로그인</h2>

    <div>
      <label for="email">Email</label>
      <input id="email" type="email" placeholder="test@example.com" />
    </div>

    <br/>

    <div>
      <label for="password">Password</label>
      <input id="password" type="password" placeholder="1234" />
    </div>

    <div class="btn-row">
      <button type="button" onclick="login()">로그인</button>
      <button type="button" onclick="location.href='<%=request.getContextPath()%>/signup2'">회원가입</button>
    </div>

    <!-- ✅ 카카오: 캡처처럼 텍스트 버튼 -->
    <div class="kakao-wrap">
      <form action="<%=request.getContextPath()%>/login/kakao" method="get">
        <button type="submit" class="kakao-wide" aria-label="카카오 로그인">
          <!-- 아이콘 필요 없으면 아래 span 삭제해도 됨 -->
          <!-- <span class="kakao-icon"></span> -->
          카카오로 로그인
        </button>
      </form>
    </div>

    <!-- 숨김 처리(디버그용으로만 남김) -->
    <pre id="result"></pre>
  </div>

  <script>
    async function login() {
      const email = document.getElementById("email").value.trim();
      const password = document.getElementById("password").value;

      const resBox = document.getElementById("result");
      resBox.textContent = "";

      if (!email || !password) return;

      try {
        const resp = await fetch("<%=request.getContextPath()%>/noamlLogin", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ email, password })
        });

        const text = await resp.text();
        resBox.textContent = "HTTP " + resp.status + "\n" + text;

        if (resp.ok) {
          window.location.href = "http://localhost:8089/lobby";
        }
      } catch (e) {
        resBox.textContent = "요청 실패: " + e;
      }
    }
  </script>
</body>
</html>



