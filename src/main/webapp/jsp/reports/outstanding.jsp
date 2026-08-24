<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html><html><head><meta charset="UTF-8"><title>Outstanding Report</title></head>
<body>
<%@ include file="/jsp/includes/navbar.jsp" %>
<div class="container">
  <div class="card">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;">
      <h2>💰 Outstanding Aging Report</h2>
      <div style="display:flex;gap:8px;">
        <a href="?format=pdf" class="btn" style="background:#e53935;color:#fff;">📄 PDF</a>
        <a href="?format=excel" class="btn" style="background:#2e7d32;color:#fff;">📊 Excel</a>
      </div>
    </div>
    <div style="overflow-x:auto;">
      <table>
        <thead>
          <tr>
            <th>Customer</th><th>Phone</th>
            <th style="background:#4caf50;">0-30 Days</th>
            <th style="background:#ff9800;">31-60 Days</th>
            <th style="background:#f44336;">61-90 Days</th>
            <th style="background:#b71c1c;">90+ Days</th>
            <th>Total</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="row" items="${aging}">
            <tr>
              <td><strong>${row.shopName}</strong></td>
              <td>${row.phone}</td>
              <td style="background:#f1f8e9;">₹${row.bucket0_30}</td>
              <td style="background:#fff8e1;">₹${row.bucket31_60}</td>
              <td style="background:#fce4ec;">₹${row.bucket61_90}</td>
              <td style="background:#ffebee;font-weight:bold;color:#c62828;">₹${row.bucket90plus}</td>
              <td><strong style="color:#c62828;">₹${row.totalOutstanding}</strong></td>
            </tr>
          </c:forEach>
          <c:if test="${empty aging}">
            <tr><td colspan="7" style="text-align:center;color:#2e7d32;padding:20px;">✅ No outstanding dues.</td></tr>
          </c:if>
        </tbody>
      </table>
    </div>
  </div>
</div>
</body></html>
