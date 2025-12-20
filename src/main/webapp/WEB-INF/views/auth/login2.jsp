<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8" />
  <title>로그인</title>
  <style>
    body {
      margin: 0;
      height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      font-family: Arial, sans-serif;
    }
    .container {
      width: 320px;
    }
    label {
      display: block;
      margin-bottom: 6px;
      font-weight: bold;
    }
    input {
      width: 100%;
      box-sizing: border-box;
      padding: 10px;
      font-size: 14px;
    }
    .btn-row {
      margin-top: 14px;
      display: flex;
      gap: 8px;
    }
    button {
      flex: 1;
      padding: 10px;
      cursor: pointer;
    }
    pre {
      margin-top: 14px;
      white-space: pre-wrap;
      word-break: break-word;
      background: #f6f6f6;
      padding: 10px;
      border: 1px solid #ddd;
      min-height: 60px;
    }
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
      <!-- ✅ onclick에서 login() 호출 -->
      <button type="button" onclick="login()">로그인</button>

      <!-- ✅ 회원가입 페이지로 이동 -->
      <button type="button"
              onclick="location.href='<%=request.getContextPath()%>/signup2'">
        회원가입
      </button>
    </div>

    <pre id="result"></pre>
  </div>

  <script>
    // ✅ 전역 함수로 선언해야 onclick에서 찾을 수 있음
    async function login() {
      const email = document.getElementById("email").value.trim();
      const password = document.getElementById("password").value;

      const resBox = document.getElementById("result");
      resBox.textContent = "";

      // 간단한 클라이언트 검증
      if (!email || !password) {
        resBox.textContent = "email/password를 입력하세요.";
        return;
      }

      try {
        const resp = await fetch("<%=request.getContextPath()%>/noamlLogin", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ email, password })
        });

        const text = await resp.text();
        resBox.textContent = "HTTP " + resp.status + "\n" + text;

        if (resp.ok) {
          // ✅ 로그인 성공 → 로비로 이동 (요구한 주소로 고정)
          window.location.href = "http://localhost:8089/lobby";
        }
      } catch (e) {
        resBox.textContent = "요청 실패: " + e;
      }
    }
  </script>
</body>
</html>
