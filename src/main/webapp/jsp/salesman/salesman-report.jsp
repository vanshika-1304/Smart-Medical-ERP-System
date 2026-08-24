<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html><html lang="en">
<head><meta charset="UTF-8"><title>Salesman Report — Smart Medical ERP</title></head>
<body>
<%@ include file="/jsp/includes/navbar.jsp" %>
  <div class="topbar">
    <div class="topbar-title">Salesman Performance Report</div>
    <div class="topbar-actions">
      <a href="${pageContext.request.contextPath}/reports/salesman?from=${from}&to=${to}&format=excel" class="btn btn-success">📊 Excel</a>
    </div>
  </div>
  <div class="content">
    <div class="card">
      <form method="get" style="display:flex;gap:10px;align-items:flex-end;flex-wrap:wrap;margin-bottom:16px;">
        <div class="form-group"><label class="form-label">From</label><input type="date" name="from" value="${from}" style="width:160px;"></div>
        <div class="form-group"><label class="form-label">To</label><input type="date" name="to" value="${to}" style="width:160px;"></div>
        <button type="submit" class="btn btn-primary">View</button>
      </form>
      <div class="table-wrap">
        <table>
          <thead><tr><th>Salesman</th><th>Bills</th><th>Total Sales (₹)</th><th>Commission %</th><th>Commission (₹)</th></tr></thead>
          <tbody>
            <c:forEach var="r" items="${reportData}">
              <tr>
                <td><strong>${r.name}</strong></td>
                <td><span class="badge badge-info">${r.billCount}</span></td>
                <td class="td-mono">₹${r.totalSales}</td>
                <td class="td-muted">${r.commissionPct}%</td>
                <td class="td-mono fw-600 text-success">₹${r.commission}</td>
              </tr>
            </c:forEach>
            <c:if test="${empty reportData}"><tr><td colspan="5" style="text-align:center;color:var(--text-3);padding:28px;">No data for selected period.</td></tr></c:if>
          </tbody>
        </table>
      </div>
      <div style="margin-top:14px;">
        <a href="${pageContext.request.contextPath}/salesmen/route-report?from=${from}&to=${to}" class="btn btn-purple">🗺️ Route-wise Report</a>
      </div>
    </div>
  </div>
</div>
</body></html>