<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<link rel="preconnect" href="https://fonts.googleapis.com">
<link href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@300;400;500;600&family=DM+Mono:wght@400;500&display=swap" rel="stylesheet">
<style>
*,*::before,*::after{box-sizing:border-box;margin:0;padding:0;}
:root{
  --primary:#1a6b5c;--primary-light:#e8f5f2;--primary-mid:#2e9e85;
  --accent:#f0a500;--accent-light:#fff8e6;
  --danger:#d93025;--danger-light:#fdecea;
  --warning:#f59e0b;--warning-light:#fffbeb;
  --success:#16a34a;--success-light:#f0fdf4;
  --info:#0369a1;--info-light:#f0f9ff;
  --sidebar-w:240px;--bg:#f5f6fa;--surface:#ffffff;
  --border:#e2e5ec;--text:#1a1d23;--text-2:#5a6072;--text-3:#9299ab;
  --shadow-sm:0 1px 3px rgba(0,0,0,0.06),0 1px 2px rgba(0,0,0,0.04);
  --shadow:0 4px 12px rgba(0,0,0,0.08);--shadow-lg:0 8px 24px rgba(0,0,0,0.10);
  --radius:10px;--radius-sm:6px;
  --font:'DM Sans',sans-serif;--mono:'DM Mono',monospace;
}
body{font-family:var(--font);background:var(--bg);color:var(--text);display:flex;min-height:100vh;font-size:14px;}
#sidebar{width:var(--sidebar-w);background:var(--surface);border-right:1px solid var(--border);display:flex;flex-direction:column;position:fixed;top:0;left:0;height:100vh;z-index:100;}
.sidebar-brand{padding:20px 20px 16px;border-bottom:1px solid var(--border);}
.brand-logo{display:flex;align-items:center;gap:10px;}
.brand-icon{width:34px;height:34px;background:var(--primary);border-radius:8px;display:flex;align-items:center;justify-content:center;flex-shrink:0;}
.brand-icon svg{width:18px;height:18px;fill:white;}
.brand-name{font-size:15px;font-weight:600;color:var(--text);line-height:1.2;}
.brand-sub{font-size:10px;color:var(--text-3);font-weight:400;letter-spacing:0.04em;text-transform:uppercase;}
.sidebar-nav{flex:1;overflow-y:auto;padding:12px 10px;}
.nav-section-label{font-size:10px;font-weight:600;text-transform:uppercase;letter-spacing:0.08em;color:var(--text-3);padding:10px 10px 6px;margin-top:4px;}
.nav-item{display:flex;align-items:center;gap:10px;padding:9px 10px;border-radius:var(--radius-sm);cursor:pointer;color:var(--text-2);font-size:13.5px;font-weight:500;transition:all 0.15s;text-decoration:none;margin-bottom:1px;}
.nav-item svg{width:16px;height:16px;flex-shrink:0;opacity:0.7;}
.nav-item:hover{background:var(--bg);color:var(--text);}
.nav-item:hover svg{opacity:1;}
.nav-item.active{background:var(--primary-light);color:var(--primary);}
.nav-item.active svg{opacity:1;fill:var(--primary);}
.nav-badge{margin-left:auto;background:var(--danger);color:white;font-size:10px;font-weight:600;padding:1px 6px;border-radius:20px;}
.nav-badge.warn{background:var(--warning);}
.sidebar-footer{padding:14px 16px;border-top:1px solid var(--border);display:flex;align-items:center;gap:10px;}
.user-avatar{width:32px;height:32px;border-radius:50%;background:var(--primary);display:flex;align-items:center;justify-content:center;color:white;font-size:12px;font-weight:600;flex-shrink:0;}
.user-info{flex:1;min-width:0;}
.user-name{font-size:13px;font-weight:600;color:var(--text);}
.user-role{font-size:11px;color:var(--text-3);}
#main{margin-left:var(--sidebar-w);flex:1;display:flex;flex-direction:column;min-height:100vh;}
.topbar{background:var(--surface);border-bottom:1px solid var(--border);padding:0 28px;height:60px;display:flex;align-items:center;gap:16px;position:sticky;top:0;z-index:50;}
.topbar-title{font-size:17px;font-weight:600;color:var(--text);flex:1;}
.topbar-actions{display:flex;gap:8px;align-items:center;}
.btn{display:inline-flex;align-items:center;gap:6px;padding:8px 16px;border-radius:var(--radius-sm);font-size:13px;font-weight:500;cursor:pointer;border:1px solid transparent;transition:all 0.15s;font-family:var(--font);text-decoration:none;}
.btn svg{width:14px;height:14px;}
.btn-primary{background:var(--primary);color:white;}.btn-primary:hover{background:#155748;}
.btn-outline{background:white;color:var(--text);border-color:var(--border);}.btn-outline:hover{background:var(--bg);}
.btn-danger{background:var(--danger);color:white;}
.btn-warning{background:var(--warning);color:white;}
.btn-success{background:var(--success);color:white;}
.btn-sm{padding:5px 12px;font-size:12px;}
.content{padding:28px;flex:1;}
.card{background:var(--surface);border:1px solid var(--border);border-radius:var(--radius);padding:20px;}
.card-header{display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;}
.card-title{font-size:14px;font-weight:600;color:var(--text);}
.card-sub{font-size:12px;color:var(--text-3);margin-top:2px;}
.stats-grid{display:grid;grid-template-columns:repeat(4,1fr);gap:16px;margin-bottom:24px;}
.stat-card{background:var(--surface);border:1px solid var(--border);border-radius:var(--radius);padding:20px;display:flex;flex-direction:column;gap:4px;position:relative;overflow:hidden;}
.stat-card::before{content:'';position:absolute;top:0;left:0;right:0;height:3px;}
.stat-card.green::before{background:var(--primary);}
.stat-card.amber::before{background:var(--accent);}
.stat-card.red::before{background:var(--danger);}
.stat-card.blue::before{background:var(--info);}
.stat-label{font-size:11.5px;font-weight:500;color:var(--text-3);text-transform:uppercase;letter-spacing:0.04em;}
.stat-value{font-size:26px;font-weight:600;color:var(--text);font-family:var(--mono);line-height:1.2;margin-top:4px;}
.stat-change{font-size:12px;color:var(--success);display:flex;align-items:center;gap:3px;margin-top:4px;}
.stat-change.neg{color:var(--danger);}
.table-wrap{overflow-x:auto;border-radius:var(--radius);border:1px solid var(--border);}
table{width:100%;border-collapse:collapse;}
thead th{background:var(--bg);font-size:11.5px;font-weight:600;text-transform:uppercase;letter-spacing:0.04em;color:var(--text-3);padding:10px 14px;text-align:left;border-bottom:1px solid var(--border);white-space:nowrap;}
tbody tr{border-bottom:1px solid var(--border);transition:background 0.1s;}
tbody tr:last-child{border-bottom:none;}
tbody tr:hover{background:var(--bg);}
tbody td{padding:11px 14px;font-size:13.5px;color:var(--text);}
.td-muted{color:var(--text-2);font-size:12.5px;}
.td-mono{font-family:var(--mono);font-size:12.5px;}
.badge{display:inline-flex;align-items:center;gap:4px;padding:3px 9px;border-radius:20px;font-size:11.5px;font-weight:500;}
.badge-success{background:var(--success-light);color:var(--success);}
.badge-danger{background:var(--danger-light);color:var(--danger);}
.badge-warning{background:var(--warning-light);color:var(--warning);}
.badge-info{background:var(--info-light);color:var(--info);}
.badge-neutral{background:var(--bg);color:var(--text-2);}
.badge-primary{background:var(--primary-light);color:var(--primary);}
.form-grid{display:grid;gap:14px;}
.form-grid-2{grid-template-columns:1fr 1fr;}
.form-grid-3{grid-template-columns:1fr 1fr 1fr;}
.form-group{display:flex;flex-direction:column;gap:5px;}
.form-label{font-size:12px;font-weight:500;color:var(--text-2);}
.form-input,.form-select,.form-textarea{padding:8px 12px;border:1px solid var(--border);border-radius:var(--radius-sm);font-size:13.5px;color:var(--text);font-family:var(--font);background:white;transition:border-color 0.15s,box-shadow 0.15s;outline:none;}
.form-input:focus,.form-select:focus,.form-textarea:focus{border-color:var(--primary);box-shadow:0 0 0 3px rgba(26,107,92,0.1);}
.form-textarea{resize:vertical;min-height:80px;}
.search-box{position:relative;}
.search-box svg{position:absolute;left:10px;top:50%;transform:translateY(-50%);width:14px;height:14px;opacity:0.4;}
.search-box input{padding-left:32px;}
.grid-2{display:grid;grid-template-columns:1fr 1fr;gap:20px;}
.grid-3{display:grid;grid-template-columns:1fr 1fr 1fr;gap:20px;}
.modal-overlay{display:none;position:fixed;inset:0;background:rgba(0,0,0,0.4);z-index:200;align-items:center;justify-content:center;}
.modal-overlay.open{display:flex;}
.modal{background:var(--surface);border-radius:var(--radius);width:560px;max-width:95vw;max-height:90vh;overflow-y:auto;box-shadow:var(--shadow-lg);animation:modalIn 0.2s ease;}
.modal-lg{width:780px;}
@keyframes modalIn{from{opacity:0;transform:scale(0.96);}to{opacity:1;transform:scale(1);}}
.modal-header{padding:20px 24px 16px;border-bottom:1px solid var(--border);display:flex;align-items:center;justify-content:space-between;}
.modal-title{font-size:16px;font-weight:600;}
.modal-close{cursor:pointer;color:var(--text-3);padding:4px;border-radius:4px;border:none;background:none;font-size:18px;line-height:1;}
.modal-close:hover{background:var(--bg);color:var(--text);}
.modal-body{padding:24px;}
.modal-footer{padding:16px 24px;border-top:1px solid var(--border);display:flex;justify-content:flex-end;gap:10px;}
.alert-strip{display:flex;align-items:center;gap:10px;padding:10px 16px;border-radius:var(--radius-sm);font-size:13px;margin-bottom:16px;}
.alert-strip.warning{background:var(--warning-light);color:#92400e;border:1px solid #fde68a;}
.alert-strip.danger{background:var(--danger-light);color:#991b1b;border:1px solid #fca5a5;}
.alert-strip.success{background:var(--success-light);color:#166534;border:1px solid #bbf7d0;}
.alert-strip svg{width:15px;height:15px;flex-shrink:0;}
.bill-items-table{width:100%;border-collapse:collapse;margin-bottom:12px;}
.bill-items-table th{background:var(--bg);font-size:11px;font-weight:600;text-transform:uppercase;letter-spacing:0.04em;color:var(--text-3);padding:8px 10px;text-align:left;border-bottom:1px solid var(--border);}
.bill-items-table td{padding:8px 10px;border-bottom:1px solid var(--border);font-size:13px;}
.bill-items-table input,.bill-items-table select{width:100%;padding:5px 8px;border:1px solid var(--border);border-radius:4px;font-size:13px;font-family:var(--font);}
.bill-items-table input:focus,.bill-items-table select:focus{outline:none;border-color:var(--primary);}
.remove-row{background:none;border:none;cursor:pointer;color:var(--danger);font-size:16px;padding:2px 6px;border-radius:4px;}
.remove-row:hover{background:var(--danger-light);}
.bill-summary{background:var(--bg);border-radius:var(--radius-sm);padding:14px 16px;}
.bill-summary-row{display:flex;justify-content:space-between;padding:4px 0;font-size:13px;color:var(--text-2);}
.bill-summary-row.total{font-size:15px;font-weight:600;color:var(--text);border-top:1px solid var(--border);padding-top:10px;margin-top:6px;}
.progress-bar{height:6px;background:var(--border);border-radius:3px;overflow:hidden;}
.progress-fill{height:100%;border-radius:3px;transition:width 0.4s ease;}
.tabs{display:flex;border-bottom:1px solid var(--border);margin-bottom:20px;gap:0;}
.tab{padding:10px 18px;cursor:pointer;font-size:13.5px;font-weight:500;color:var(--text-3);border-bottom:2px solid transparent;margin-bottom:-1px;transition:all 0.15s;}
.tab:hover{color:var(--text);}
.tab.active{color:var(--primary);border-bottom-color:var(--primary);}
.payment-mode-btn{padding:8px 14px;border:1px solid var(--border);border-radius:var(--radius-sm);cursor:pointer;font-size:13px;font-weight:500;background:white;color:var(--text-2);transition:all 0.15s;}
.payment-mode-btn.active{background:var(--primary-light);border-color:var(--primary);color:var(--primary);}
::-webkit-scrollbar{width:5px;height:5px;}
::-webkit-scrollbar-track{background:transparent;}
::-webkit-scrollbar-thumb{background:var(--border);border-radius:10px;}
.divider{border:none;border-top:1px solid var(--border);margin:16px 0;}
@media(max-width:1100px){.stats-grid{grid-template-columns:repeat(2,1fr);},.form-grid-3{grid-template-columns:1fr 1fr;}}
</style>

<aside id="sidebar">
  <div class="sidebar-brand">
    <div class="brand-logo">
      <div class="brand-icon">
        <svg viewBox="0 0 24 24"><path d="M19 3H5a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2V5a2 2 0 00-2-2zm-7 3a1 1 0 110 2 1 1 0 010-2zm-2 4h4v1h-1v5h-2v-5h-1V10z"/></svg>
      </div>
      <div>
        <div class="brand-name">MedERP</div>
        <div class="brand-sub">Smart Medical System</div>
      </div>
    </div>
  </div>
  <nav class="sidebar-nav">
    <div class="nav-section-label">Main</div>
    <a class="nav-item ${pageContext.request.servletPath.contains('dashboard') ? 'active' : ''}"
       href="${pageContext.request.contextPath}/dashboard">
      <svg viewBox="0 0 24 24" fill="currentColor"><path d="M3 13h8V3H3v10zm0 8h8v-6H3v6zm10 0h8V11h-8v10zm0-18v6h8V3h-8z"/></svg>
      Dashboard
    </a>
    <a class="nav-item ${pageContext.request.servletPath.contains('billing') ? 'active' : ''}"
       href="${pageContext.request.contextPath}/billing">
      <svg viewBox="0 0 24 24" fill="currentColor"><path d="M14 2H6a2 2 0 00-2 2v16a2 2 0 002 2h12a2 2 0 002-2V8l-6-6zm-1 1.5L18.5 9H13V3.5zM8 18v-1h8v1H8zm0-3v-1h8v1H8zm0-3V11h5v1H8z"/></svg>
      Billing & Invoice
    </a>
    <div class="nav-section-label">Management</div>
    <a class="nav-item ${pageContext.request.servletPath.contains('inventory') ? 'active' : ''}"
       href="${pageContext.request.contextPath}/inventory">
      <svg viewBox="0 0 24 24" fill="currentColor"><path d="M20 7l-8-4-8 4v10l8 4 8-4V7zm-8 2L6.5 7 12 4.18 17.5 7 12 9zM4 8.83l7 3.5V20l-7-3.5V8.83zm9 11.17V12.33l7-3.5V16.5l-7 3.5z"/></svg>
      Inventory
    </a>
    <a class="nav-item ${pageContext.request.servletPath.contains('customers') ? 'active' : ''}"
       href="${pageContext.request.contextPath}/customers">
      <svg viewBox="0 0 24 24" fill="currentColor"><path d="M16 11c1.66 0 2.99-1.34 2.99-3S17.66 5 16 5c-1.66 0-3 1.34-3 3s1.34 3 3 3zm-8 0c1.66 0 2.99-1.34 2.99-3S9.66 5 8 5C6.34 5 5 6.34 5 8s1.34 3 3 3zm0 2c-2.33 0-7 1.17-7 3.5V19h14v-2.5c0-2.33-4.67-3.5-7-3.5zm8 0c-.29 0-.62.02-.97.05 1.16.84 1.97 1.97 1.97 3.45V19h6v-2.5c0-2.33-4.67-3.5-7-3.5z"/></svg>
      Customers
    </a>
    <a class="nav-item ${pageContext.request.servletPath.contains('suppliers') ? 'active' : ''}"
       href="${pageContext.request.contextPath}/suppliers">
      <svg viewBox="0 0 24 24" fill="currentColor"><path d="M20 8h-3V4H3c-1.1 0-2 .9-2 2v11h2c0 1.66 1.34 3 3 3s3-1.34 3-3h6c0 1.66 1.34 3 3 3s3-1.34 3-3h2v-5l-3-4zM6 18.5c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zm13.5-9l1.96 2.5H17V9.5h2.5zm-1.5 9c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5z"/></svg>
      Supplier & Purchase
    </a>
    <a class="nav-item ${pageContext.request.servletPath.contains('payments') ? 'active' : ''}"
       href="${pageContext.request.contextPath}/payments">
      <svg viewBox="0 0 24 24" fill="currentColor"><path d="M20 4H4c-1.11 0-1.99.89-1.99 2L2 18c0 1.11.89 2 2 2h16c1.11 0 2-.89 2-2V6c0-1.11-.89-2-2-2zm0 14H4v-6h16v6zm0-10H4V6h16v2z"/></svg>
      Payments
    </a>
    <a class="nav-item ${pageContext.request.servletPath.contains('salesmen') ? 'active' : ''}"
       href="${pageContext.request.contextPath}/salesmen">
      <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/></svg>
      Salesmen & Routes
    </a>
    <div class="nav-section-label">Analytics</div>
    <a class="nav-item ${pageContext.request.servletPath.contains('reports') ? 'active' : ''}"
       href="${pageContext.request.contextPath}/reports/daily">
      <svg viewBox="0 0 24 24" fill="currentColor"><path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zM9 17H7v-7h2v7zm4 0h-2V7h2v10zm4 0h-2v-4h2v4z"/></svg>
      Reports
    </a>
    <c:if test="${sessionScope.loggedInUser.role == 'ADMIN'}">
    <a class="nav-item ${pageContext.request.servletPath.contains('users') ? 'active' : ''}"
       href="${pageContext.request.contextPath}/users">
      <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4zm0 4a3 3 0 110 6 3 3 0 010-6zm0 14c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08s5.97 1.09 6 3.08C16.71 17.72 14.5 19 12 19z"/></svg>
      Users
    </a>
    </c:if>
  </nav>
  <div class="sidebar-footer">
    <div class="user-avatar">${sessionScope.loggedInUser.username.substring(0,2).toUpperCase()}</div>
    <div class="user-info">
      <div class="user-name">${sessionScope.loggedInUser.username}</div>
      <div class="user-role">${sessionScope.loggedInUser.role}</div>
    </div>
    <a href="${pageContext.request.contextPath}/logout" style="color:var(--text-3);text-decoration:none;padding:4px;" title="Logout">
      <svg viewBox="0 0 24 24" fill="currentColor" style="width:16px;height:16px;"><path d="M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z"/></svg>
    </a>
  </div>
</aside>
<div id="main">
<script>
function openModal(id){document.getElementById(id).classList.add('open');}
function closeModal(id){document.getElementById(id).classList.remove('open');}
function switchTab(el,contentId){
  var tabs=el.parentElement.querySelectorAll('.tab');
  tabs.forEach(function(t){t.classList.remove('active');});
  el.classList.add('active');
  var sib=el.parentElement.nextElementSibling;
  while(sib){sib.style.display='none';sib=sib.nextElementSibling;}
  var target=document.getElementById(contentId);
  if(target)target.style.display='';
}
function toggleMode(btn){
  btn.closest('div').querySelectorAll('.payment-mode-btn').forEach(function(b){b.classList.remove('active');});
  btn.classList.add('active');
}
document.addEventListener('click',function(e){
  document.querySelectorAll('.modal-overlay').forEach(function(o){
    if(e.target===o)o.classList.remove('open');
  });
});
</script>