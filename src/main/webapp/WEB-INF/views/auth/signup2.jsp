<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8" />
  <title>회원가입</title>

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

    /* 로그인 페이지랑 같은 느낌의 카드 */
    .container {
      width: 520px;
      max-width: calc(100vw - 40px);
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

    .row {
      display: grid;
      gap: 8px;
      margin-bottom: 14px;
    }

    label {
      font-weight: 800;
      font-size: 12px;
      color: #6b7280;
    }

    .input-line {
      display: grid;
      grid-template-columns: 1fr auto;
      gap: 10px;
      align-items: center;
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

    /* 작은 액션 버튼 (인증코드 발송/확인) */
    .btn-mini {
      padding: 12px 14px;
      border-radius: 12px;
      border: 1px solid #d1d5db;
      background: #ffffff;
      color: #111827;
      font-weight: 900;
      cursor: pointer;
      white-space: nowrap;
    }
    .btn-mini:hover { background: #f9fafb; }
    .btn-mini:active { transform: translateY(1px); }

    /* 상태 메시지 */
    .hint {
      margin-top: 6px;
      font-size: 12px;
      color: #6b7280;
    }
    .hint.ok { color: #0f766e; font-weight: 800; }
    .hint.bad { color: #b91c1c; font-weight: 800; }

    /* 하단 버튼 영역 */
    .btn-row {
      margin-top: 10px;
      display: flex;
      gap: 10px;
    }

    .btn-primary, .btn-secondary {
      flex: 1;
      padding: 13px 14px;
      border-radius: 12px;
      font-weight: 900;
      cursor: pointer;
      border: 1px solid #d1d5db;
      background: #ffffff;
      color: #111827;
    }

    .btn-primary {
      border-color: #111827;
      background: #111827;
      color: #ffffff;
    }

    .btn-primary:disabled {
      opacity: 0.45;
      cursor: not-allowed;
    }

    .btn-secondary:hover { background: #f9fafb; }
    .btn-primary:active,
    .btn-secondary:active { transform: translateY(1px); }

    /* 결과 박스(디버그) - 원하면 숨겨도 됨 */
    pre {
      margin-top: 14px;
      white-space: pre-wrap;
      word-break: break-word;
      background: #f6f6f6;
      padding: 12px;
      border: 1px solid #ddd;
      min-height: 80px;
      border-radius: 12px;
      color: #111827;
    }
  </style>
</head>

<body>
  <div class="container">
    <h2>회원가입</h2>

    <!-- 이메일 + 인증코드 발송 -->
    <div class="row">
      <label for="email">Email</label>
      <div class="input-line">
        <input id="email" type="email" placeholder="test@example.com" />
        <button type="button" class="btn-mini" onclick="sendCode()">인증코드 발송</button>
      </div>
      <div id="emailHint" class="hint">이메일 인증을 먼저 진행하세요.</div>
    </div>

    <!-- 인증코드 + 확인 -->
    <div class="row">
      <label for="code">인증코드</label>
      <div class="input-line">
        <input id="code" type="text" placeholder="6자리 코드" />
        <button type="button" class="btn-mini" onclick="verifyCode()">코드 확인</button>
      </div>
      <div id="codeHint" class="hint">메일로 받은 코드를 입력하세요.</div>
    </div>

    <!-- 비밀번호 -->
    <div class="row">
      <label for="password">Password</label>
      <input id="password" type="password" placeholder="비밀번호를 입력하세요" oninput="checkPwMatch()" />
    </div>

    <!-- ✅ [ADD] 비밀번호 확인 -->
    <div class="row">
      <label for="password2">Password 확인</label>
      <input id="password2" type="password" placeholder="비밀번호를 한 번 더 입력하세요" oninput="checkPwMatch()" />

    </div>

    <!-- 닉네임 -->
    <div class="row">
      <label for="nickname">Nickname</label>
      <input id="nickname" type="text"  />
    </div>

    <!-- 버튼들 -->
    <div class="btn-row">
      <button id="signupBtn" type="button" class="btn-primary" onclick="signup()" disabled>가입하기</button>

      <button type="button"
              class="btn-secondary"
              onclick="location.href='<%=request.getContextPath()%>/login2'">
        로그인으로
      </button>
    </div>

    <pre id="result"></pre>
  </div>

  <script>
    // ✅ 이메일 인증 성공 여부(프론트 플래그)
    let verified = false;

    // ✅ 비밀번호 일치 여부
    let pwMatched = false;

    function writeResult(resp, text) {
      document.getElementById("result").textContent =
        "HTTP " + resp.status + "\n" + text;
    }

    // ✅ 가입 버튼 활성화 조건: (이메일 인증 OK) AND (비번 2개 일치)
    function updateSignupButtonState() {
      const btn = document.getElementById("signupBtn");
      btn.disabled = !(verified && pwMatched);
    }

    // ✅ [ADD] 비밀번호 2번 입력 검증
    function checkPwMatch() {
      const pw1 = document.getElementById("password").value;
      const pw2 = document.getElementById("password2").value;
      const hint = document.getElementById("pwHint");

      // 둘 중 하나라도 아직 입력이 없으면 "안내" 상태
      if (!pw1 || !pw2) {
        pwMatched = false;
        hint.className = "hint";
        hint.textContent = "비밀번호를 2번 입력해 주세요.";
        updateSignupButtonState();
        return;
      }

      if (pw1 === pw2) {
        pwMatched = true;
        hint.className = "hint ok";
        hint.textContent = "비밀번호가 일치합니다.";
      } else {
        pwMatched = false;
        hint.className = "hint bad";
        hint.textContent = "비밀번호가 일치하지 않습니다.";
      }

      updateSignupButtonState();
    }

    async function sendCode() {
      const email = document.getElementById("email").value.trim();
      const resBox = document.getElementById("result");
      resBox.textContent = "";

      if (!email) {
        alert("이메일을 입력하세요.");
        return;
      }

      // 인증 다시 시작하면 가입 버튼 다시 잠금
      verified = false;
      document.getElementById("emailHint").className = "hint";
      document.getElementById("emailHint").textContent = "이메일 인증을 진행 중입니다...";
      document.getElementById("codeHint").className = "hint";
      document.getElementById("codeHint").textContent = "메일로 받은 코드를 입력하세요.";
      updateSignupButtonState();

      try {
        const resp = await fetch("<%=request.getContextPath()%>/email/send-code", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ email })
        });

        const text = await resp.text();
        writeResult(resp, text);

        if (resp.ok) {
          alert("인증코드를 이메일로 보냈습니다. 메일을 확인하세요!");
          document.getElementById("emailHint").className = "hint ok";
          document.getElementById("emailHint").textContent = "인증코드를 발송했습니다. 메일을 확인하세요.";
        } else {
          alert("인증코드 발송 실패. 결과창을 확인하세요.");
          document.getElementById("emailHint").className = "hint bad";
          document.getElementById("emailHint").textContent = "인증코드 발송에 실패했습니다.";
        }
      } catch (e) {
        resBox.textContent = "요청 실패: " + e;
        document.getElementById("emailHint").className = "hint bad";
        document.getElementById("emailHint").textContent = "요청 실패(네트워크/서버)를 확인하세요.";
      }
    }

    async function verifyCode() {
      const email = document.getElementById("email").value.trim();
      const code = document.getElementById("code").value.trim();
      const resBox = document.getElementById("result");
      resBox.textContent = "";

      if (!email) {
        alert("이메일을 입력하세요.");
        return;
      }
      if (!code) {
        alert("인증코드를 입력하세요.");
        return;
      }

      try {
        const resp = await fetch("<%=request.getContextPath()%>/email/verify-code", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ email, code })
        });

        const text = await resp.text();
        writeResult(resp, text);

        if (resp.ok) {
          verified = true;
          alert("이메일 인증 성공! 이제 가입할 수 있습니다.");
          document.getElementById("codeHint").className = "hint ok";
          document.getElementById("codeHint").textContent = "이메일 인증이 완료되었습니다.";
        } else {
          verified = false;
          alert("인증 실패. 결과창을 확인하세요.");
          document.getElementById("codeHint").className = "hint bad";
          document.getElementById("codeHint").textContent = "인증코드가 올바르지 않습니다.";
        }

        updateSignupButtonState();
      } catch (e) {
        resBox.textContent = "요청 실패: " + e;
        verified = false;
        document.getElementById("codeHint").className = "hint bad";
        document.getElementById("codeHint").textContent = "요청 실패(네트워크/서버)를 확인하세요.";
        updateSignupButtonState();
      }
    }

    async function signup() {
      const email = document.getElementById("email").value.trim();
      const password = document.getElementById("password").value;
      const password2 = document.getElementById("password2").value;
      const nickname = document.getElementById("nickname").value.trim();

      const resBox = document.getElementById("result");
      resBox.textContent = "";

      if (!verified) {
        alert("이메일 인증부터 완료하세요.");
        return;
      }

      // ✅ [ADD] 가입 직전에도 한번 더 체크(프론트 우회 방지)
      if (!password || !password2 || password !== password2) {
        alert("비밀번호가 일치하지 않습니다. 다시 확인하세요.");
        return;
      }

      if (!nickname) {
        alert("닉네임을 입력하세요.");
        return;
      }

      try {
        const resp = await fetch("<%=request.getContextPath()%>/nomalSignup", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ email, password, nickname })
        });

        const text = await resp.text();
        writeResult(resp, text);

        if (resp.ok) {
          alert("회원가입 성공! 로그인 페이지로 이동합니다.");
          location.href = "<%=request.getContextPath()%>/login2";
        } else {
          alert("회원가입 실패. 결과창을 확인하세요.");
        }
      } catch (e) {
        resBox.textContent = "요청 실패: " + e;
      }
    }
  </script>
</body>
</html>
