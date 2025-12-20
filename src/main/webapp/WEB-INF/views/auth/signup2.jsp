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

  <button type="button" onclick="signup()">가입하기</button>

  <!-- ✅ 로그인 페이지로 돌아가기 -->
  <button type="button"
          onclick="location.href='<%=request.getContextPath()%>/login2'">
    로그인으로
  </button>

  <pre id="result" style="margin-top:16px;"></pre>

  <script>
    async function signup() {
      const email = document.getElementById("email").value.trim();
      const password = document.getElementById("password").value;
      const nickname = document.getElementById("nickname").value.trim();

      const resBox = document.getElementById("result");
      resBox.textContent = "";

      try {
        const resp = await fetch("<%=request.getContextPath()%>/nomalSignup", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ email, password, nickname })
        });

        const text = await resp.text();
        resBox.textContent = "HTTP " + resp.status + "\n" + text;

        if (resp.ok) {
          alert("회원가입 성공! 로그인 페이지로 이동합니다.");
          location.href = "<%=request.getContextPath()%>/login2";
        }
      } catch (e) {
        resBox.textContent = "요청 실패: " + e;
      }
    }
  </script>
</body>
</html>


