<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width,initial-scale=1.0">
<title>Smart Medical ERP — Login</title>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@300;400;500;600&family=DM+Mono:wght@400;500&display=swap" rel="stylesheet">
<style>
*,*::before,*::after{box-sizing:border-box;margin:0;padding:0;}
:root{--primary:#1a6b5c;--primary-light:#e8f5f2;--border:#e2e5ec;--text:#1a1d23;--text-2:#5a6072;--text-3:#9299ab;--danger:#d93025;--danger-light:#fdecea;--font:'DM Sans',sans-serif;--radius:10px;--radius-sm:6px;--shadow-lg:0 8px 24px rgba(0,0,0,0.10);}
body{font-family:var(--font);background:#f5f6fa;display:flex;align-items:center;justify-content:center;min-height:100vh;}
.login-wrap{width:400px;}
.login-brand{text-align:center;margin-bottom:32px;}
.brand-icon-lg{width:52px;height:52px;background:var(--primary);border-radius:12px;display:inline-flex;align-items:center;justify-content:center;margin-bottom:12px;}
.brand-icon-lg svg{width:28px;height:28px;fill:white;}
.brand-title{font-size:22px;font-weight:700;color:var(--text);}
.brand-sub{font-size:13px;color:var(--text-3);margin-top:2px;}
.login-card{background:white;border:1px solid var(--border);border-radius:var(--radius);padding:32px;box-shadow:var(--shadow-lg);}
.login-heading{font-size:18px;font-weight:600;margin-bottom:4px;}
.login-sub{font-size:13px;color:var(--text-3);margin-bottom:24px;}
.form-group{display:flex;flex-direction:column;gap:5px;margin-bottom:16px;}
.form-label{font-size:12px;font-weight:500;color:var(--text-2);}
.form-input{padding:10px 12px;border:1px solid var(--border);border-radius:var(--radius-sm);font-size:14px;color:var(--text);font-family:var(--font);outline:none;transition:border-color 0.15s,box-shadow 0.15s;}
.form-input:focus{border-color:var(--primary);box-shadow:0 0 0 3px rgba(26,107,92,0.1);}
.btn-login{width:100%;padding:11px;background:var(--primary);color:white;border:none;border-radius:var(--radius-sm);font-size:14px;font-weight:600;cursor:pointer;font-family:var(--font);transition:background 0.15s;margin-top:4px;}
.btn-login:hover{background:#155748;}
.error-box{background:var(--danger-light);color:#991b1b;border:1px solid #fca5a5;padding:10px 14px;border-radius:var(--radius-sm);font-size:13px;margin-bottom:16px;display:flex;align-items:center;gap:8px;}
.error-box svg{width:15px;height:15px;flex-shrink:0;fill:#d93025;}
.login-footer{text-align:center;margin-top:20px;font-size:12px;color:var(--text-3);}
</style>
</head>
<body>
<div class="login-wrap">
  <div class="login-brand">
    <div class="brand-icon-lg">
      <svg viewBox="0 0 24 24"><path d="M19 3H5a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2V5a2 2 0 00-2-2zm-7 3a1 1 0 110 2 1 1 0 010-2zm-2 4h4v1h-1v5h-2v-5h-1V10z"/></svg>
    </div>
    <div class="brand-title">MedERP</div>
    <div class="brand-sub">Smart Medical Agency System v2.0</div>
  </div>
  <div class="login-card">
    <div class="login-heading">Welcome back</div>
    <div class="login-sub">Sign in to continue to your dashboard</div>
    <% String error = (String) request.getAttribute("error"); %>
    <% if (error != null) { %>
    <div class="error-box">
      <svg viewBox="0 0 24 24"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/></svg>
      <%= error %>
    </div>
    <% } %>
    <form method="POST" action="${pageContext.request.contextPath}/login">
      <div class="form-group">
        <label class="form-label">Username</label>
        <input type="text" name="username" class="form-input" placeholder="Enter your username" required autofocus>
      </div>
      <div class="form-group">
        <label class="form-label">Password</label>
        <input type="password" name="password" class="form-input" placeholder="Enter your password" required>
      </div>
      <button type="submit" class="btn-login">Sign In</button>
    </form>
  </div>
  <div class="login-footer">Smart Medical ERP &copy; 2026 &nbsp;·&nbsp; v2.0</div>
</div>
</body>
</html>