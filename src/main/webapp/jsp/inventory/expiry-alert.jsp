<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html><html lang="en">
<head><meta charset="UTF-8"><title>Expiry Alerts — Smart Medical ERP</title></head>
<body>
<%@ include file="/jsp/includes/navbar.jsp" %>
  <div class="topbar">
    <div class="topbar-title">Expiry Alerts</div>
    <div class="topbar-actions">
      <form method="get" style="display:flex;gap:8px;align-items:center;">
        <span style="font-size:13px;color:var(--text-3);">Within</span>
        <select name="days" onchange="this.form.submit()" style="width:100px;">
          <option value="7"  ${days==7?'selected':''}>7 days</option>
          <option value="15" ${days==15?'selected':''}>15 days</option>
          <option value="30" ${days==30?'selected':''}>30 days</option>
          <option value="60" ${days==60?'selected':''}>60 days</option>
        </select>
      </form>
    </div>
  </div>
  <div class="content">
    <div class="card">
      <div class="table-wrap">
        <table>
          <thead><tr><th>Medicine</th><th>Batch No</th><th>Expiry Date</th><th>MRP</th><th>Stock Qty</th><th>Status</th></tr></thead>
          <tbody>
            <c:forEach var="b" items="${expiryBatches}">
              <tr style="${b.alertStatus=='CRITICAL'?'background:#fff1f2;':'background:#fffbeb;'}">
                <td><strong>${b.medicineName}</strong></td>
                <td class="td-mono">${b.batchNo}</td>
                <td class="td-mono fw-600">${b.expiryDate}</td>
                <td class="td-mono">₹${b.mrp}</td>
                <td class="td-mono">${b.stockQty}</td>
                <td><c:choose>
                  <c:when test="${b.alertStatus=='CRITICAL'}"><span class="badge badge-danger">⚠️ Critical (7d)</span></c:when>
                  <c:otherwise><span class="badge badge-warning">Expiring Soon</span></c:otherwise>
                </c:choose></td>
              </tr>
            </c:forEach>
            <c:if test="${empty expiryBatches}"><tr><td colspan="6" style="text-align:center;color:var(--success);padding:28px;">✅ No expiry alerts for selected period.</td></tr></c:if>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</div>
</body></html>