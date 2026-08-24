<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html><html lang="en">
<head><meta charset="UTF-8"><title>Bills — Smart Medical ERP</title></head>
<body>
<%@ include file="/jsp/includes/navbar.jsp" %>
  <div class="topbar">
    <div class="topbar-title">Sales Bills</div>
    <div class="topbar-actions">
      <a href="${pageContext.request.contextPath}/billing/new" class="btn btn-primary">+ New Bill</a>
    </div>
  </div>
  <div class="content">
    <div class="card">
      <form method="get" style="display:flex;gap:10px;align-items:flex-end;flex-wrap:wrap;margin-bottom:16px;">
        <div class="form-group"><label class="form-label">From</label><input type="date" name="from" value="${param.from}" style="width:160px;"></div>
        <div class="form-group"><label class="form-label">To</label><input type="date" name="to" value="${param.to}" style="width:160px;"></div>
        <button type="submit" class="btn btn-primary">🔍 Filter</button>
        <a href="${pageContext.request.contextPath}/reports/daily?format=pdf&date=${param.from}" class="btn btn-danger">📄 PDF</a>
        <a href="${pageContext.request.contextPath}/reports/daily?format=excel&date=${param.from}" class="btn btn-success">📊 Excel</a>
      </form>
      <div class="table-wrap">
        <table>
          <thead><tr><th>#</th><th>Bill No</th><th>Date</th><th>Customer</th><th>Salesman</th><th>Net Total</th><th>Status</th><th>Action</th></tr></thead>
          <tbody>
            <c:forEach var="s" items="${sales}" varStatus="st">
              <tr>
                <td class="td-muted">${st.count}</td>
                <td class="td-mono fw-600">${s.billNo}</td>
                <td class="td-muted">${s.saleDate}</td>
                <td><strong>${s.shopName}</strong></td>
                <td class="td-muted">${s.salesmanName}</td>
                <td class="td-mono fw-600">₹${s.netTotal}</td>
                <td><c:choose>
                  <c:when test="${s.paymentStatus=='PAID'}"><span class="badge badge-success">PAID</span></c:when>
                  <c:when test="${s.paymentStatus=='PARTIAL'}"><span class="badge badge-warning">PARTIAL</span></c:when>
                  <c:otherwise><span class="badge badge-danger">PENDING</span></c:otherwise>
                </c:choose></td>
                <td><button class="btn btn-sm btn-danger" onclick="processReturn(${s.saleId},'${s.billNo}')">↩ Return</button></td>
              </tr>
            </c:forEach>
            <c:if test="${empty sales}">
              <tr><td colspan="8" style="text-align:center;color:var(--text-3);padding:28px;">No bills found for selected date range.</td></tr>
            </c:if>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</div>
<script>
const CTX='${pageContext.request.contextPath}';
async function processReturn(saleId,billNo){
  if(!confirm('Process return for Bill '+billNo+'? Stock will be restored.'))return;
  const res=await fetch(CTX+'/billing/return',{method:'POST',body:new URLSearchParams({saleId})});
  const data=await res.json();
  alert(data.message);
  if(data.success)location.reload();
}
</script>
</body></html>