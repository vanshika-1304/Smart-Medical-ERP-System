<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html><html lang="en">
<head><meta charset="UTF-8"><title>Customer Ledger — Smart Medical ERP</title></head>
<body>
<%@ include file="/jsp/includes/navbar.jsp" %>
  <div class="topbar">
    <div class="topbar-title">Customer Ledger</div>
    <div class="topbar-actions">
      <a href="${pageContext.request.contextPath}/payments/receipt?customerId=${customer.customerId}" class="btn btn-primary">+ Record Receipt</a>
      <a href="${pageContext.request.contextPath}/customers" class="btn btn-outline">← Back</a>
    </div>
  </div>
  <div class="content">
    <div class="card">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;">
        <div>
          <div style="font-size:16px;font-weight:600;">${customer.shopName}</div>
          <div class="td-muted">${customer.ownerName} | ${customer.phone}</div>
        </div>
        <div style="text-align:right;">
          <div class="td-muted" style="font-size:12px;">Outstanding Balance</div>
          <div style="font-size:22px;font-weight:700;font-family:var(--mono);color:${customer.outstandingBalance>0?'var(--danger)':'var(--success)'};">₹${customer.outstandingBalance}</div>
        </div>
      </div>
      <div class="table-wrap">
        <table>
          <thead><tr><th>Date</th><th>Type</th><th>Reference</th><th>Debit (₹)</th><th>Credit (₹)</th><th>Status</th></tr></thead>
          <tbody>
            <c:forEach var="t" items="${ledger}">
              <tr>
                <td class="td-muted">${t.txnDate}</td>
                <td><c:choose>
                  <c:when test="${t.txnType=='BILL'}"><span class="badge badge-danger">BILL</span></c:when>
                  <c:otherwise><span class="badge badge-success">RECEIPT</span></c:otherwise>
                </c:choose></td>
                <td class="td-mono">${t.ref}</td>
                <td class="td-mono fw-600 text-danger"><c:if test="${t.debit>0}">₹${t.debit}</c:if></td>
                <td class="td-mono fw-600 text-success"><c:if test="${t.credit>0}">₹${t.credit}</c:if></td>
                <td><span class="badge badge-info">${t.status}</span></td>
              </tr>
            </c:forEach>
            <c:if test="${empty ledger}"><tr><td colspan="6" style="text-align:center;color:var(--text-3);padding:28px;">No transactions found.</td></tr></c:if>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</div>
</body></html>