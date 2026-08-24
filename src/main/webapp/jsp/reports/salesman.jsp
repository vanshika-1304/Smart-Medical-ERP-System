<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html><html><head><meta charset="UTF-8"><title>Salesman Report</title></head>
<body>
<%@ include file="/jsp/includes/navbar.jsp" %>
<div class="container">
  <div class="card">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;">
      <h2>🧑‍💼 Salesman Performance Report</h2>
      <a href="?from=${from}&to=${to}&format=excel" class="btn" style="background:#2e7d32;color:#fff;">📊 Excel</a>
    </div>
    <form method="get" style="display:flex;gap:12px;margin-bottom:16px;align-items:flex-end;">
      <div><label>From</label><input type="date" name="from" value="${from}" style="width:160px;"></div>
      <div><label>To</label><input type="date" name="to" value="${to}" style="width:160px;"></div>
      <button type="submit" class="btn btn-primary">View</button>
    </form>
    <table>
      <thead><tr><th>Salesman</th><th>Bills</th><th>Total Sales (₹)</th><th>Commission %</th><th>Commission (₹)</th></tr></thead>
      <tbody>
        <c:forEach var="s" items="${salesmanData}">
          <tr>
            <td><strong>${s.name}</strong></td>
            <td>${s.billCount}</td>
            <td>₹${s.totalSales}</td>
            <td>${s.commissionPct}%</td>
            <td><strong style="color:#2e7d32;">₹${s.commission}</strong></td>
          </tr>
        </c:forEach>
        <c:if test="${empty salesmanData}">
          <tr><td colspan="5" style="text-align:center;color:#999;padding:20px;">No data.</td></tr>
        </c:if>
      </tbody>
    </table>
  </div>
</div>
</body></html>
