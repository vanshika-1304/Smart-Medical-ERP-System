<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html><html lang="en">
<head><meta charset="UTF-8"><title>Route-wise Report — Smart Medical ERP</title></head>
<body>
<%@ include file="/jsp/includes/navbar.jsp" %>
  <div class="topbar">
    <div class="topbar-title">Route-wise Sales Report</div>
  </div>
  <div class="content">
    <div class="card">
      <form method="get" style="display:flex;gap:10px;align-items:flex-end;flex-wrap:wrap;margin-bottom:16px;">
        <div class="form-group"><label class="form-label">From</label><input type="date" name="from" value="${param.from}" style="width:160px;"></div>
        <div class="form-group"><label class="form-label">To</label><input type="date" name="to" value="${param.to}" style="width:160px;"></div>
        <button type="submit" class="btn btn-primary">View</button>
      </form>
      <div class="table-wrap">
        <table>
          <thead><tr><th>Route</th><th>Area</th><th>Bills</th><th>Total Sales (₹)</th></tr></thead>
          <tbody>
            <c:forEach var="r" items="${reportData}">
              <tr>
                <td><strong>${r.routeName}</strong></td>
                <td><span class="badge badge-purple">${r.area}</span></td>
                <td><span class="badge badge-info">${r.billCount}</span></td>
                <td class="td-mono fw-600">₹${r.totalSales}</td>
              </tr>
            </c:forEach>
            <c:if test="${empty reportData}"><tr><td colspan="4" style="text-align:center;color:var(--text-3);padding:28px;">No data for selected period.</td></tr></c:if>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</div>
</body></html>