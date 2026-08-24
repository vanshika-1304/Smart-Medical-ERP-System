<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html><html><head><meta charset="UTF-8"><title>Monthly Sales</title></head>
<body>
<%@ include file="/jsp/includes/navbar.jsp" %>
<div class="container">
  <div class="card">
    <h2 style="margin-bottom:16px;">📆 Monthly Sales Report</h2>
    <form method="get" style="display:flex;gap:12px;margin-bottom:20px;align-items:flex-end;">
      <div><label>Month</label>
        <select name="month" style="width:130px;">
          <c:forEach begin="1" end="12" var="m">
            <option value="${m}" ${m==month?'selected':''}>${m}</option>
          </c:forEach>
        </select>
      </div>
      <div><label>Year</label><input type="number" name="year" value="${year}" style="width:100px;"></div>
      <button type="submit" class="btn btn-primary">View</button>
    </form>
    <c:if test="${not empty summary}">
      <div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(180px,1fr));gap:16px;">
        <div class="card" style="text-align:center;border:2px solid #1a73e8;">
          <div style="font-size:1.8rem;font-weight:bold;color:#1a73e8;">₹${summary.totalSales}</div>
          <div style="color:#666;font-size:0.9rem;">Total Sales</div>
        </div>
        <div class="card" style="text-align:center;border:2px solid #2e7d32;">
          <div style="font-size:1.8rem;font-weight:bold;color:#2e7d32;">${summary.billCount}</div>
          <div style="color:#666;font-size:0.9rem;">Total Bills</div>
        </div>
        <div class="card" style="text-align:center;border:2px solid #f57c00;">
          <div style="font-size:1.8rem;font-weight:bold;color:#f57c00;">₹${summary.avgBill}</div>
          <div style="color:#666;font-size:0.9rem;">Avg Bill Value</div>
        </div>
        <div class="card" style="text-align:center;border:2px solid #7b1fa2;">
          <div style="font-size:1.8rem;font-weight:bold;color:#7b1fa2;">₹${summary.totalGst}</div>
          <div style="color:#666;font-size:0.9rem;">Total GST</div>
        </div>
        <div class="card" style="text-align:center;border:2px solid #c62828;">
          <div style="font-size:1.8rem;font-weight:bold;color:#c62828;">₹${summary.totalDiscount}</div>
          <div style="color:#666;font-size:0.9rem;">Total Discount</div>
        </div>
      </div>
    </c:if>
  </div>
</div>
</body></html>
