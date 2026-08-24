<%@ page contentType="text/html;charset=UTF-8" isErrorPage="true" %>
<!DOCTYPE html><html><head><meta charset="UTF-8"><title>500 - Server Error</title></head>
<body>
<%@ include file="/jsp/includes/navbar.jsp" %>
<div class="container" style="text-align:center;padding-top:60px;">
  <div style="font-size:5rem;">⚠️</div>
  <h2 style="color:#f57c00;margin:16px 0;">500 — Internal Server Error</h2>
  <p style="color:#666;">Something went wrong. Please try again or contact admin.</p>
  <% if (exception != null) { %>
    <div style="background:#fff3e0;border-radius:5px;padding:12px;margin:16px auto;max-width:600px;text-align:left;font-size:0.85rem;color:#e65100;">
      <strong>Error:</strong> <%= exception.getMessage() %>
    </div>
  <% } %>
  <a href="${pageContext.request.contextPath}/dashboard" class="btn btn-primary" style="margin-top:16px;display:inline-block;">Go to Dashboard</a>
</div>
</body></html>
