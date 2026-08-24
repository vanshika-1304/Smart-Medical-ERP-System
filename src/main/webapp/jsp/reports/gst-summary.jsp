<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html><html><head><meta charset="UTF-8"><title>GST Summary</title></head>
<body>
<%@ include file="/jsp/includes/navbar.jsp" %>
<div class="container">
  <div class="card">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;">
      <h2>🧾 GST Summary (GSTR-1)</h2>
      <a href="?month=${month}&year=${year}&format=excel" class="btn" style="background:#2e7d32;color:#fff;">📊 Export Excel</a>
    </div>
    <form method="get" style="display:flex;gap:12px;margin-bottom:16px;align-items:flex-end;">
      <div><label>Month</label>
        <select name="month" style="width:100px;">
          <c:forEach begin="1" end="12" var="m">
            <option value="${m}" ${m==month?'selected':''}>${m}</option>
          </c:forEach>
        </select>
      </div>
      <div><label>Year</label><input type="number" name="year" value="${year}" style="width:100px;"></div>
      <button type="submit" class="btn btn-primary">View</button>
    </form>
    <table>
      <thead><tr><th>GST Slab</th><th>Total GST</th><th>CGST (50%)</th><th>SGST (50%)</th></tr></thead>
      <tbody>
        <c:set var="totalGst" value="0"/>
        <c:forEach var="g" items="${gstData}">
          <tr>
            <td><strong>${g.slab}%</strong></td>
            <td>₹${g.totalGst}</td>
            <td>₹${g.cgst}</td>
            <td>₹${g.sgst}</td>
          </tr>
          <c:set var="totalGst" value="${totalGst + g.totalGst}"/>
        </c:forEach>
        <c:if test="${empty gstData}">
          <tr><td colspan="4" style="text-align:center;color:#999;padding:20px;">No GST data for selected period.</td></tr>
        </c:if>
      </tbody>
      <tfoot>
        <tr style="background:#e3f2fd;font-weight:bold;">
          <td>TOTAL</td><td>₹${totalGst}</td><td colspan="2"></td>
        </tr>
      </tfoot>
    </table>
    <p style="margin-top:12px;color:#777;font-size:0.85rem;">
      CGST = SGST = GST ÷ 2 (Intra-state transactions per BRD 4.2)
    </p>
  </div>
</div>
</body></html>
