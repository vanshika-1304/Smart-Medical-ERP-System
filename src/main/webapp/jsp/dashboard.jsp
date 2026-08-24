<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html><html lang="en">
<head><meta charset="UTF-8"><title>Dashboard — MedERP</title></head>
<body>
<%@ include file="/jsp/includes/navbar.jsp" %>
<div class="topbar">
  <div class="topbar-title">Dashboard</div>
  <div class="topbar-actions">
    <a href="${pageContext.request.contextPath}/billing/new" class="btn btn-primary btn-sm">
      <svg viewBox="0 0 24 24" fill="currentColor"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>
      New Bill
    </a>
  </div>
</div>
<div class="content">
  <c:set var="d" value="${dashboard}"/>
  <div class="stats-grid">
    <div class="stat-card green">
      <div class="stat-label">Today's Sales</div>
      <div class="stat-value">₹<c:out value="${d.todaySales}"/></div>
      <div class="stat-change"><c:out value="${d.todayBills}"/> bills today</div>
    </div>
    <div class="stat-card amber">
      <div class="stat-label">Total Outstanding</div>
      <div class="stat-value">₹<c:out value="${d.totalCustomerOutstanding}"/></div>
      <div class="stat-change neg">Customer dues</div>
    </div>
    <div class="stat-card red">
      <div class="stat-label">Expiry Alerts</div>
      <div class="stat-value"><c:out value="${d.expiryCount}"/></div>
      <div class="stat-change neg">Within 30 days</div>
    </div>
    <div class="stat-card blue">
      <div class="stat-label">Low / Out of Stock</div>
      <div class="stat-value"><c:out value="${d.lowStockCount}"/></div>
      <div class="stat-change neg"><c:out value="${d.outOfStockCount}"/> out of stock</div>
    </div>
  </div>

  <div class="grid-2">
    <div class="card">
      <div class="card-header">
        <div><div class="card-title">Quick Actions</div></div>
      </div>
      <div style="display:flex;flex-direction:column;gap:8px;">
        <a href="${pageContext.request.contextPath}/billing/new" class="btn btn-primary" style="justify-content:center;">
          <svg viewBox="0 0 24 24" fill="currentColor" style="width:14px;height:14px;"><path d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/></svg>
          Create New Bill
        </a>
        <a href="${pageContext.request.contextPath}/payments/receipt" class="btn btn-outline" style="justify-content:center;">💵 Record Customer Receipt</a>
        <a href="${pageContext.request.contextPath}/suppliers/purchase/new" class="btn btn-outline" style="justify-content:center;">🛒 New Purchase Entry</a>
        <a href="${pageContext.request.contextPath}/inventory/expiry" class="btn btn-outline" style="justify-content:center;">⏰ View Expiry Alerts</a>
        <a href="${pageContext.request.contextPath}/customers/outstanding" class="btn btn-outline" style="justify-content:center;">💰 Outstanding Aging</a>
        <a href="${pageContext.request.contextPath}/reports/gst" class="btn btn-outline" style="justify-content:center;">🧾 GST Summary Report</a>
      </div>
    </div>
    <div class="card">
      <div class="card-header">
        <div><div class="card-title">Stock Alerts</div><div class="card-sub">Action required</div></div>
        <a href="${pageContext.request.contextPath}/inventory" class="btn btn-outline btn-sm">View All</a>
      </div>
      <c:if test="${d.outOfStockCount > 0}">
        <div class="alert-strip danger">
          <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/></svg>
          <c:out value="${d.outOfStockCount}"/> medicines are out of stock
        </div>
      </c:if>
      <c:if test="${d.expiryCount > 0}">
        <div class="alert-strip warning">
          <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/></svg>
          <c:out value="${d.expiryCount}"/> batches expiring within 30 days
        </div>
      </c:if>
      <c:if test="${d.lowStockCount > 0}">
        <div class="alert-strip warning">
          <svg viewBox="0 0 24 24" fill="currentColor"><path d="M1 21h22L12 2 1 21zm12-3h-2v-2h2v2zm0-4h-2v-4h2v4z"/></svg>
          <c:out value="${d.lowStockCount}"/> items below minimum stock level
        </div>
      </c:if>
      <c:if test="${d.expiryCount == 0 && d.lowStockCount == 0 && d.outOfStockCount == 0}">
        <div class="alert-strip success">
          <svg viewBox="0 0 24 24" fill="currentColor"><path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"/></svg>
          All stock levels are healthy — no alerts
        </div>
      </c:if>
    </div>
  </div>
</div>
</div>
</body></html>