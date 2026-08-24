<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html><html lang="en">
<head><meta charset="UTF-8"><title>Low Stock — Smart Medical ERP</title></head>
<body>
<%@ include file="/jsp/includes/navbar.jsp" %>
  <div class="topbar">
    <div class="topbar-title">Low Stock Alerts</div>
  </div>
  <div class="content">
    <div class="card">
      <div class="table-wrap">
        <table>
          <thead><tr><th>Medicine</th><th>Batch No</th><th>Expiry</th><th>MRP</th><th>Stock Qty</th><th>Min Alert</th><th>Status</th></tr></thead>
          <tbody>
            <c:forEach var="b" items="${lowStockBatches}">
              <tr style="${b.stockQty==0?'background:#fff1f2;font-weight:600;':'background:#fffbeb;'}">
                <td><strong>${b.medicineName}</strong></td>
                <td class="td-mono">${b.batchNo}</td>
                <td class="td-mono">${b.expiryDate}</td>
                <td class="td-mono">₹${b.mrp}</td>
                <td class="td-mono">${b.stockQty}</td>
                <td class="td-mono">${b.minStockAlert}</td>
                <td><c:choose>
                  <c:when test="${b.stockQty==0}"><span class="badge badge-danger">Out of Stock</span></c:when>
                  <c:otherwise><span class="badge badge-warning">Low Stock</span></c:otherwise>
                </c:choose></td>
              </tr>
            </c:forEach>
            <c:if test="${empty lowStockBatches}"><tr><td colspan="7" style="text-align:center;color:var(--success);padding:28px;">✅ All stock levels are adequate.</td></tr></c:if>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</div>
</body></html>