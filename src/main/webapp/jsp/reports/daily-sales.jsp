<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html><html lang="en">
<head><meta charset="UTF-8"><title>Daily Sales Report</title></head>
<body>
<%@ include file="/jsp/includes/navbar.jsp" %>
<div class="container">
  <div class="card">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;">
      <h2>📅 Daily Sales Report</h2>
      <div style="display:flex;gap:8px;">
        <a href="?date=${selectedDate}&format=pdf" class="btn" style="background:#e53935;color:#fff;">📄 PDF</a>
        <a href="?date=${selectedDate}&format=excel" class="btn" style="background:#2e7d32;color:#fff;">📊 Excel</a>
      </div>
    </div>
    <form method="get" style="display:flex;gap:12px;margin-bottom:16px;align-items:flex-end;">
      <div><label>Date</label><input type="date" name="date" value="${selectedDate}" style="width:180px;"></div>
      <button type="submit" class="btn btn-primary">View</button>
    </form>
    <div style="overflow-x:auto;">
      <table>
        <thead><tr><th>#</th><th>Bill No</th><th>Customer</th><th>Subtotal</th><th>Discount</th><th>GST</th><th>Net Total</th><th>Status</th></tr></thead>
        <tbody>
          <c:set var="total" value="0"/>
          <c:forEach var="s" items="${sales}" varStatus="st">
            <tr>
              <td>${st.count}</td><td><strong>${s.billNo}</strong></td><td>${s.shopName}</td>
              <td>₹${s.subtotal}</td><td>₹${s.discount}</td><td>₹${s.gst}</td>
              <td><strong>₹${s.netTotal}</strong></td>
              <td><c:choose>
                <c:when test="${s.paymentStatus=='PAID'}"><span class="badge badge-green">PAID</span></c:when>
                <c:when test="${s.paymentStatus=='PARTIAL'}"><span class="badge badge-amber">PARTIAL</span></c:when>
                <c:otherwise><span class="badge badge-red">PENDING</span></c:otherwise>
              </c:choose></td>
            </tr>
            <c:set var="total" value="${total + s.netTotal}"/>
          </c:forEach>
          <c:if test="${empty sales}">
            <tr><td colspan="8" style="text-align:center;color:#999;padding:20px;">No bills for ${selectedDate}.</td></tr>
          </c:if>
        </tbody>
        <tfoot>
          <tr style="background:#e3f2fd;font-weight:bold;">
            <td colspan="6">Total Bills: ${fn:length(sales)}</td>
            <td>₹${total}</td><td></td>
          </tr>
        </tfoot>
      </table>
    </div>
  </div>
</div>
</body></html>
