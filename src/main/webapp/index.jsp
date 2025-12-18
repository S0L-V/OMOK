<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="javax.naming.InitialContext" %>
<%@ page import="javax.sql.DataSource" %>
<%@ page import="java.sql.Connection" %>
<html>
<head>
    <title>OMOK - DB Test</title>
</head>
<body>
<h1>OMOK 프로젝트</h1>

<h2>DB Connection Test</h2>
<%
    try {
        out.println("1. InitialContext 생성 중...<br>");
        InitialContext ctx = new InitialContext();
        out.println("✅ InitialContext 생성 성공<br><br>");

        out.println("2. JNDI Lookup: java:/comp/env/jdbc/oracle<br>");
        DataSource ds = (DataSource)ctx.lookup("java:/comp/env/jdbc/oracle");
        out.println("✅ DataSource 획득 성공<br><br>");

        out.println("3. DB 연결 시도...<br>");
        Connection conn = ds.getConnection();
        out.println("<h3 style='color:green;'>✅ DB 연결 성공!</h3>");
        out.println("Connection: " + conn + "<br>");
        out.println("URL: " + conn.getMetaData().getURL() + "<br>");
        out.println("Username: " + conn.getMetaData().getUserName() + "<br>");
        conn.close();

    } catch (Exception e) {
        out.println("<h3 style='color:red;'>❌ DB 연결 실패</h3>");
        out.println("<strong>Error:</strong> " + e.getMessage() + "<br><br>");
        out.println("<pre style='background:#f5f5f5;padding:10px;'>");
        e.printStackTrace(new java.io.PrintWriter(out));
        out.println("</pre>");
    }
%>

<hr>
<a href="/omok/friend/list">Friend API Test</a>
</body>
</html>