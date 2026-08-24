<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html><html lang="en">
<head><meta charset="UTF-8"><title>Customers — Smart Medical ERP</title></head>
<body>
<%@ include file="/jsp/includes/navbar.jsp" %>
  <div class="topbar">
    <div class="topbar-title">Customer Management</div>
    <div class="topbar-actions">
      <a href="${pageContext.request.contextPath}/customers/outstanding" class="btn btn-outline">💰 Outstanding</a>
      <button class="btn btn-primary" onclick="document.getElementById('addModal').classList.add('open')">+ Add Customer</button>
    </div>
  </div>
  <div class="content">
    <div class="card">
      <div style="margin-bottom:14px;">
        <input type="text" id="search" placeholder="Search by shop, owner, phone..." oninput="filterTable(this.value)" style="max-width:340px;">
      </div>
      <div class="table-wrap">
        <table id="custTable">
          <thead><tr><th>#</th><th>Shop Name</th><th>Owner</th><th>Phone</th><th>Route</th><th>Credit Limit</th><th>Outstanding</th><th>Status</th><th>Actions</th></tr></thead>
          <tbody>
            <c:forEach var="c" items="${customers}" varStatus="st">
              <tr>
                <td class="td-muted">${st.count}</td>
                <td><strong>${c.shopName}</strong></td>
                <td>${c.ownerName}</td>
                <td class="td-mono">${c.phone}</td>
                <td><span class="badge badge-info">${c.routeName}</span></td>
                <td class="td-mono">₹${c.creditLimit}</td>
                <td class="td-mono fw-600" style="${c.outstandingBalance>0?'color:var(--danger);':'color:var(--success);'}">₹${c.outstandingBalance}</td>
                <td><c:choose>
                  <c:when test="${c.outstandingBalance>c.creditLimit&&c.creditLimit>0}"><span class="badge badge-danger">Over Limit</span></c:when>
                  <c:when test="${c.outstandingBalance>0}"><span class="badge badge-warning">Due</span></c:when>
                  <c:otherwise><span class="badge badge-success">Clear</span></c:otherwise>
                </c:choose></td>
                <td>
                  <a href="${pageContext.request.contextPath}/customers/ledger?id=${c.customerId}" class="btn btn-sm btn-icon">📒 Ledger</a>
                  <button class="btn btn-sm btn-danger" onclick="deleteCustomer(${c.customerId},'${c.shopName}')">Delete</button>
                </td>
              </tr>
            </c:forEach>
            <c:if test="${empty customers}">
              <tr><td colspan="9" style="text-align:center;color:var(--text-3);padding:28px;">No customers found.</td></tr>
            </c:if>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</div>

<div class="modal-overlay" id="addModal">
  <div class="modal modal-lg">
    <div class="modal-header">
      <div class="modal-title">Add New Customer</div>
      <button class="modal-close" onclick="document.getElementById('addModal').classList.remove('open')">×</button>
    </div>
    <div class="modal-body">
      <div class="form-row">
        <div class="form-group"><label class="form-label">Shop Name *</label><input id="cShop" placeholder="Chemist shop name"></div>
        <div class="form-group"><label class="form-label">Owner Name</label><input id="cOwner" placeholder="Proprietor name"></div>
      </div>
      <div class="form-row">
        <div class="form-group"><label class="form-label">Phone</label><input id="cPhone" placeholder="Mobile number"></div>
        <div class="form-group"><label class="form-label">GSTIN</label><input id="cGstin" placeholder="GST number"></div>
      </div>
      <div class="form-group"><label class="form-label">Address</label><textarea id="cAddress" rows="2" placeholder="Shop address" style="resize:none;"></textarea></div>
      <div class="form-row">
        <div class="form-group"><label class="form-label">Credit Limit (₹)</label><input id="cLimit" type="number" step="100" placeholder="0"></div>
        <div class="form-group"><label class="form-label">Route ID</label><input id="cRoute" type="number" placeholder="Optional"></div>
      </div>
      <div id="modalMsg" style="margin-top:10px;"></div>
    </div>
    <div class="modal-footer">
      <button class="btn btn-outline" onclick="document.getElementById('addModal').classList.remove('open')">Cancel</button>
      <button class="btn btn-primary" onclick="saveCustomer()">Save Customer</button>
    </div>
  </div>
</div>

<script>
const CTX='${pageContext.request.contextPath}';
function filterTable(q){q=q.toLowerCase();document.querySelectorAll('#custTable tbody tr').forEach(tr=>{tr.style.display=tr.textContent.toLowerCase().includes(q)?'':' none';});}
async function saveCustomer(){
  const shopName=document.getElementById('cShop').value.trim();
  if(!shopName){showMsg('modalMsg','Shop name required','error');return;}
  const body=new URLSearchParams({shopName,ownerName:document.getElementById('cOwner').value,phone:document.getElementById('cPhone').value,gstin:document.getElementById('cGstin').value,address:document.getElementById('cAddress').value,creditLimit:document.getElementById('cLimit').value||'0',routeId:document.getElementById('cRoute').value||''});
  const res=await fetch(CTX+'/customers/add',{method:'POST',body});
  const data=await res.json();
  if(data.success)location.reload();else showMsg('modalMsg',data.message,'error');
}
async function deleteCustomer(id,name){
  if(!confirm('Deactivate: '+name+'?'))return;
  const res=await fetch(CTX+'/customers/delete',{method:'POST',body:new URLSearchParams({customerId:id})});
  const data=await res.json();
  if(data.success)location.reload();else alert(data.message);
}
function showMsg(id,msg,type){const el=document.getElementById(id);el.className='alert alert-'+type;el.textContent=msg;}
</script>
</body></html>