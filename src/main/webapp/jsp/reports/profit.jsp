<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html><html><head><meta charset="UTF-8"><title>Profit Report</title></head>
<body>
<%@ include file="/jsp/includes/navbar.jsp" %>
<div class="container">
  <div class="card">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;">
      <h2>📈 Profit Report</h2>
      <a href="?from=${from}&to=${to}&format=excel" class="btn" style="background:#2e7d32;color:#fff;">📊 Excel</a>
    </div>
    <form method="get" style="display:flex;gap:12px;margin-bottom:16px;align-items:flex-end;">
      <div><label>From</label><input type="date" name="from" value="${from}" style="width:160px;"></div>
      <div><label>To</label><input type="date" name="to" value="${to}" style="width:160px;"></div>
      <button type="submit" class="btn btn-primary">View</button>
    </form>
    <div style="overflow-x:auto;">
      <table>
        <thead><tr><th>Medicine</th><th>Category</th><th>Qty Sold</th><th>Revenue (₹)</th><th>Gross Profit (₹)</th></tr></thead>
        <tbody>
          <c:set var="totProfit" value="0"/><c:set var="totRev" value="0"/>
          <c:forEach var="p" items="${profitData}">
            <tr>
              <td><strong>${p.medicineName}</strong></td>
              <td><span class="badge badge-blue">${p.category}</span></td>
              <td>${p.totalQty}</td>
              <td>₹${p.totalRevenue}</td>
              <td><strong style="color:#2e7d32;">₹${p.grossProfit}</strong></td>
            </tr>
            <c:set var="totProfit" value="${totProfit + p.grossProfit}"/>
            <c:set var="totRev" value="${totRev + p.totalRevenue}"/>
          </c:forEach>
          <c:if test="${empty profitData}">
            <tr><td colspan="5" style="text-align:center;color:#999;padding:20px;">No data.</td></tr>
          </c:if>
        </tbody>
        <tfoot>
          <tr style="background:#e8f5e9;font-weight:bold;">
            <td colspan="3">TOTAL</td>
            <td>₹${totRev}</td>
            <td style="color:#2e7d32;">₹${totProfit}</td>
          </tr>
        </tfoot>
      </table>
    </div>
  </div>
</div>
</body></html>
