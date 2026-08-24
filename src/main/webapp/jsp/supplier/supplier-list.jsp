<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html><html lang="en">
<head><meta charset="UTF-8"><title>Suppliers</title></head>
<body>
<%@ include file="/jsp/includes/navbar.jsp" %>
<div class="container">
  <div class="card">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;">
      <h2>🏭 Suppliers</h2>
      <div style="display:flex;gap:8px;">
        <a href="${pageContext.request.contextPath}/suppliers/purchase/new" class="btn btn-primary">+ New Purchase</a>
        <button class="btn" style="background:#eee;" onclick="showAddModal()">+ Add Supplier</button>
      </div>
    </div>
    <div style="overflow-x:auto;">
      <table>
        <thead><tr><th>#</th><th>Supplier</th><th>Phone</th><th>GSTIN</th><th>Credit Limit</th><th>Outstanding</th><th>Actions</th></tr></thead>
        <tbody>
          <c:forEach var="s" items="${suppliers}" varStatus="st">
            <tr>
              <td>${st.count}</td>
              <td><strong>${s.name}</strong><br><small style="color:#777;">${s.address}</small></td>
              <td>${s.contactNo}</td>
              <td>${s.gstin}</td>
              <td>₹${s.creditLimit}</td>
              <td><strong style="color:${s.outstandingBalance > 0 ? '#c62828' : '#2e7d32'};">₹${s.outstandingBalance}</strong></td>
              <td>
                <a href="${pageContext.request.contextPath}/suppliers/purchase/history?id=${s.supplierId}"
                   class="btn btn-sm" style="background:#e3f2fd;color:#1565c0;">History</a>
                <button class="btn btn-sm btn-danger" onclick="deleteSupplier(${s.supplierId},'${s.name}')">🗑️</button>
              </td>
            </tr>
          </c:forEach>
          <c:if test="${empty suppliers}">
            <tr><td colspan="7" style="text-align:center;color:#999;padding:20px;">No suppliers found.</td></tr>
          </c:if>
        </tbody>
      </table>
    </div>
  </div>
</div>

<!-- Add Supplier Modal -->
<div id="addModal" style="display:none;position:fixed;top:0;left:0;right:0;bottom:0;
  background:rgba(0,0,0,0.5);z-index:1000;align-items:center;justify-content:center;">
  <div style="background:#fff;border-radius:8px;padding:24px;width:520px;">
    <h3>Add Supplier</h3>
    <div class="form-row">
      <div><label>Name *</label><input id="sName" placeholder="Company/Distributor name"></div>
      <div><label>Phone</label><input id="sPhone"></div>
    </div>
    <div class="form-row">
      <div><label>GSTIN</label><input id="sGstin"></div>
      <div><label>Credit Limit (₹)</label><input id="sLimit" type="number" value="0"></div>
    </div>
    <div><label>Address</label><textarea id="sAddress" rows="2" style="resize:none;"></textarea></div>
    <div id="modalMsg" style="margin:10px 0;"></div>
    <div style="display:flex;gap:10px;margin-top:16px;">
      <button class="btn btn-primary" onclick="saveSupplier()">Save</button>
      <button class="btn" style="background:#eee;" onclick="document.getElementById('addModal').style.display='none'">Cancel</button>
    </div>
  </div>
</div>

<script>
const CTX = '${pageContext.request.contextPath}';
function showAddModal() { document.getElementById('addModal').style.display = 'flex'; }
async function saveSupplier() {
  const name = document.getElementById('sName').value.trim();
  if (!name) { showMsg('modalMsg','Name required','error'); return; }
  const body = new URLSearchParams({ name,
    contactNo: document.getElementById('sPhone').value,
    gstin: document.getElementById('sGstin').value,
    creditLimit: document.getElementById('sLimit').value,
    address: document.getElementById('sAddress').value });
  const res = await fetch(CTX+'/suppliers/add',{method:'POST',body});
  const data = await res.json();
  if (data.success) location.reload(); else showMsg('modalMsg',data.message,'error');
}
async function deleteSupplier(id, name) {
  if (!confirm('Deactivate: ' + name + '?')) return;
  const res = await fetch(CTX+'/suppliers/delete',{method:'POST',body:new URLSearchParams({supplierId:id})});
  const data = await res.json();
  if (data.success) location.reload(); else alert(data.message);
}
function showMsg(id,msg,type){ const el=document.getElementById(id); el.className='alert alert-'+type; el.textContent=msg; }
</script>
</body></html>
