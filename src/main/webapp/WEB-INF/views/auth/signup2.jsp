<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8" />
  <title>회원가입</title>
</head>
<body>
  <h2>회원가입</h2>

  <div>
    <label>Email</label><br/>
    <input id="email" type="email" placeholder="test@example.com" />
    <button type="button" onclick="sendCode()">인증코드 발송</button>
  </div>
  <br/>

  <div>
    <label>인증코드</label><br/>
    <input id="code" type="text" placeholder="6자리 코드" />
    <button type="button" onclick="verifyCode()">코드 확인</button>
  </div>
  <br/>

  <div>
    <label>Password</label><br/>
    <input id="password" type="password" placeholder="1234" />
  </div>
  <br/>

  <div>
    <label>Nickname</label><br/>
    <input id="nickname" type="text" placeholder="재훈1" />
  </div>
  <br/>

  <button id="signupBtn" type="button" onclick="signup()" disabled>가입하기</button>

  <!-- ✅ 로그인 페이지로 돌아가기 -->
  <button type="button"
          onclick="location.href='<%=request.getContextPath()%>/login2'">
    로그인으로
  </button>

  <pre id="result" style="margin-top:16px;"></pre>

  <script>
    let verified = false; // 화면에서만 쓰는 플래그 (서버는 세션으로 검증)

    function writeResult(resp, text) {
      document.getElementById("result").textContent =
        "HTTP " + resp.status + "\n" + text;
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
      document.getElementById("signupBtn").disabled = true;

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
        } else {
          alert("인증코드 발송 실패. 결과창을 확인하세요.");
        }
      } catch (e) {
        resBox.textContent = "요청 실패: " + e;
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
          document.getElementById("signupBtn").disabled = false;
          alert("이메일 인증 성공! 이제 가입할 수 있습니다.");
        } else {
          verified = false;
          document.getElementById("signupBtn").disabled = true;
          alert("인증 실패. 결과창을 확인하세요.");
        }
      } catch (e) {
        resBox.textContent = "요청 실패: " + e;
      }
    }

    async function signup() {
      const email = document.getElementById("email").value.trim();
      const password = document.getElementById("password").value;
      const nickname = document.getElementById("nickname").value.trim();

      const resBox = document.getElementById("result");
      resBox.textContent = "";

      // 화면 플래그 + (서버에서도 세션으로 최종 검증해야 함)
      if (!verified) {
        alert("이메일 인증부터 완료하세요.");
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


