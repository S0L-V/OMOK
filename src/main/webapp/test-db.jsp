<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="javax.naming.InitialContext" %>
<%@ page import="javax.sql.DataSource" %>
<%@ page import="java.sql.Connection" %>
<html>
<head>
    <title>DB Connection Test</title>
</head>
<body>
<h1>JNDI DB Connection Test</h1>
<%
    try {
        out.println("1. InitialContext 생성 시도...<br>");
        InitialContext ctx = new InitialContext();
        out.println("2. InitialContext 생성 성공!<br>");

        out.println("3. JNDI Lookup 시도: java:/comp/env/jdbc/oracle<br>");
        DataSource ds = (DataSource)ctx.lookup("java:/comp/env/jdbc/oracle");
        out.println("4. DataSource 획득 성공!<br>");

        out.println("5. DB 연결 시도...<br>");
        Connection conn = ds.getConnection();
        out.println("<h2 style='color:green;'>✅ DB 연결 성공!</h2>");
        out.println("Connection: " + conn + "<br>");
        out.println("Catalog: " + conn.getCatalog() + "<br>");
        conn.close();
        out.println("6. Connection 닫기 성공!<br>");

    } catch (Exception e) {
        out.println("<h2 style='color:red;'>❌ DB 연결 실패!</h2>");
        out.println("Error: " + e.getMessage() + "<br>");
        out.println("<pre>");
        e.printStackTrace(new java.io.PrintWriter(out));
        out.println("</pre>");
    }
%>
</body>
</html>