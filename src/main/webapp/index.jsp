<%@ page contentType="text/html; charset=UTF-8" %>
<%
    // 서버 시작 시 자동으로 로비로 리다이렉트
    response.sendRedirect(request.getContextPath() + "/lobby");
%>
