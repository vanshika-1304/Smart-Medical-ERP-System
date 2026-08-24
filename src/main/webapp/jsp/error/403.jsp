<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html><html><head><meta charset="UTF-8"><title>403 - Access Denied</title></head>
<body>
<%@ include file="/jsp/includes/navbar.jsp" %>
<div class="container" style="text-align:center;padding-top:60px;">
  <div style="font-size:5rem;">🚫</div>
  <h2 style="color:#c62828;margin:16px 0;">403 — Access Denied</h2>
  <p style="color:#666;">You don't have permission to access this page.</p>
  <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-primary" style="margin-top:20px;display:inline-block;">Go to Dashboard</a>
</div>
</body></html>
