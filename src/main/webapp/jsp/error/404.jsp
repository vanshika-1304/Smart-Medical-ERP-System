<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html><html><head><meta charset="UTF-8"><title>404 - Not Found</title></head>
<body>
<%@ include file="/jsp/includes/navbar.jsp" %>
<div class="container" style="text-align:center;padding-top:60px;">
  <div style="font-size:5rem;">🔍</div>
  <h2 style="color:#1a73e8;margin:16px 0;">404 — Page Not Found</h2>
  <p style="color:#666;">The page you're looking for doesn't exist.</p>
  <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-primary" style="margin-top:20px;display:inline-block;">Go to Dashboard</a>
</div>
</body></html>
