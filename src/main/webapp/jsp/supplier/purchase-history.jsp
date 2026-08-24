<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html><html lang="en">
<head><meta charset="UTF-8"><title>Purchase History — Smart Medical ERP</title></head>
<body>
<%@ include file="/jsp/includes/navbar.jsp" %>
  <div class="topbar">
    <div class="topbar-title">Purchase History</div>
    <div class="topbar-actions">
      <a href="${pageContext.request.contextPath}/suppliers/purchase/new" class="btn btn-primary">+ New Purchase</a>
      <a href="${pageContext.request.contextPath}/payments/supplier-payment?supplierId=${supplier.supplierId}" class="btn btn-outline">💳 Make Payment</a>
      <a href="${pageContext.request.contextPath}/suppliers" class="btn btn-outline">← Back</a>
    </div>
  </div>
  <div class="content">
    <div class="card">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:14px;">
        <div><div style="font-size:16px;font-weight:600;">${supplier.name}</div></div>
        <div>Outstanding: <span class="fw-600 font-mono text-danger">₹${supplier.outstandingBalance}</span></div>
      </div>
      <form method="get" style="display:flex;gap:10px;align-items:flex-end;flex-wrap:wrap;margin-bottom:14px;">
        <input type="hidden" name="id" value="${supplier.supplierId}">
        <div class="form-group"><label class="form-label">From</label><input type="date" name="from" value="${param.from}" style="width:160px;"></div>
        <div class="form-group"><label class="form-label">To</label><input type="date" name="to" value="${param.to}" style="width:160px;"></div>
        <button type="submit" class="btn btn-primary">Filter</button>
      </form>
      <div class="table-wrap">
        <table>
          <thead><tr><th>#</th><th>Invoice No</th><th>Date</th><th>Total Amount</th><th>Status</th></tr></thead>
          <tbody>
            <c:forEach var="p" items="${purchases}" varStatus="st">
              <tr>
                <td class="td-muted">${st.count}</td>
                <td class="td-mono fw-600">${p.invoiceNo}</td>
                <td class="td-muted">${p.purchaseDate}</td>
                <td class="td-mono fw-600">₹${p.totalAmount}</td>
                <td><c:choose>
                  <c:when test="${p.paymentStatus=='PAID'}"><span class="badge badge-success">PAID</span></c:when>
                  <c:when test="${p.paymentStatus=='PARTIAL'}"><span class="badge badge-warning">PARTIAL</span></c:when>
                  <c:otherwise><span class="badge badge-danger">PENDING</span></c:otherwise>
                </c:choose></td>
              </tr>
            </c:forEach>
            <c:if test="${empty purchases}"><tr><td colspan="5" style="text-align:center;color:var(--text-3);padding:28px;">No purchases found.</td></tr></c:if>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</div>
</body></html>