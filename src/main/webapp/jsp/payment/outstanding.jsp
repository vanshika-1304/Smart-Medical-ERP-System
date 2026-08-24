<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html><html lang="en">
<head><meta charset="UTF-8"><title>Outstanding — Smart Medical ERP</title></head>
<body>
<%@ include file="/jsp/includes/navbar.jsp" %>
  <div class="topbar">
    <div class="topbar-title">Payment Outstanding Aging</div>
    <div class="topbar-actions">
      <a href="${pageContext.request.contextPath}/reports/outstanding?format=pdf" class="btn btn-danger">📄 PDF</a>
      <a href="${pageContext.request.contextPath}/reports/outstanding?format=excel" class="btn btn-success">📊 Excel</a>
    </div>
  </div>
  <div class="content">
    <div class="card">
      <div class="table-wrap">
        <table>
          <thead><tr><th>Customer</th><th>Phone</th><th style="color:var(--success);">0-30 Days</th><th style="color:var(--warning);">31-60 Days</th><th style="color:var(--danger);">61-90 Days</th><th style="color:#991b1b;">90+ Days</th><th>Total</th><th>Action</th></tr></thead>
          <tbody>
            <c:forEach var="row" items="${aging}">
              <tr>
                <td><strong>${row.shopName}</strong></td>
                <td class="td-mono">${row.phone}</td>
                <td class="bucket-0 td-mono">₹${row.bucket0_30}</td>
                <td class="bucket-30 td-mono">₹${row.bucket31_60}</td>
                <td class="bucket-60 td-mono">₹${row.bucket61_90}</td>
                <td class="bucket-90 td-mono">₹${row.bucket90plus}</td>
                <td class="td-mono fw-600 text-danger">₹${row.totalOutstanding}</td>
                <td><a href="${pageContext.request.contextPath}/payments/ledger?customerId=${row.customerId}" class="btn btn-sm btn-icon">Ledger</a></td>
              </tr>
            </c:forEach>
            <c:if test="${empty aging}"><tr><td colspan="8" style="text-align:center;color:var(--success);padding:28px;">✅ No outstanding dues.</td></tr></c:if>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</div>
</body></html>