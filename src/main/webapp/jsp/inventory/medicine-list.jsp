<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html><html lang="en">
<head><meta charset="UTF-8"><title>Inventory — Smart Medical ERP</title></head>
<body>
<%@ include file="/jsp/includes/navbar.jsp" %>
  <div class="topbar">
    <div class="topbar-title">Medicine Inventory</div>
    <div class="topbar-actions">
      <a href="${pageContext.request.contextPath}/inventory/expiry" class="btn btn-danger">⏰ Expiry</a>
      <a href="${pageContext.request.contextPath}/inventory/low-stock" class="btn btn-warning">📉 Low Stock</a>
      <button class="btn btn-primary" onclick="document.getElementById('addModal').classList.add('open')">+ Add Medicine</button>
    </div>
  </div>
  <div class="content">
    <div class="card">
      <div style="margin-bottom:14px;">
        <input type="text" id="searchBox" placeholder="Search by name, composition, company..." oninput="filterTable(this.value)" style="max-width:380px;">
      </div>
      <div class="table-wrap">
        <table id="medTable">
          <thead><tr><th>Medicine</th><th>Company</th><th>Category</th><th>HSN</th><th>GST%</th><th>Rack</th><th>Batches</th><th>Action</th></tr></thead>
          <tbody>
            <c:forEach var="m" items="${medicines}">
              <tr>
                <td><strong>${m.name}</strong><br><small class="td-muted">${m.composition}</small></td>
                <td>${m.company}</td>
                <td><span class="badge badge-info">${m.category}</span></td>
                <td class="td-mono">${m.hsnCode}</td>
                <td class="td-muted">${m.gstPct}%</td>
                <td class="td-mono">${m.rackLocation}</td>
                <td><button class="btn btn-sm btn-icon" onclick="viewBatches(${m.medicineId},'${m.name}')">View Batches</button></td>
                <td>
                  <button class="btn btn-sm btn-outline" onclick="editMedicine(${m.medicineId})">✏️</button>
                  <button class="btn btn-sm btn-danger" onclick="deleteMedicine(${m.medicineId},'${m.name}')">Delete</button>
                </td>
              </tr>
            </c:forEach>
            <c:if test="${empty medicines}"><tr><td colspan="8" style="text-align:center;color:var(--text-3);padding:28px;">No medicines found.</td></tr></c:if>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</div>

<div class="modal-overlay" id="addModal">
  <div class="modal">
    <div class="modal-header"><div class="modal-title">Add New Medicine</div><button class="modal-close" onclick="document.getElementById('addModal').classList.remove('open')">×</button></div>
    <div class="modal-body">
      <div class="form-row">
        <div class="form-group"><label class="form-label">Name *</label><input id="mName" placeholder="Medicine name"></div>
        <div class="form-group"><label class="form-label">Company</label><input id="mCompany" placeholder="Manufacturer"></div>
      </div>
      <div class="form-row">
        <div class="form-group"><label class="form-label">Category</label><input id="mCategory" placeholder="Tablet/Syrup/etc"></div>
        <div class="form-group"><label class="form-label">HSN Code</label><input id="mHsn" placeholder="HSN code"></div>
      </div>
      <div class="form-group"><label class="form-label">Composition / Salt</label><input id="mComposition" placeholder="Active ingredients"></div>
      <div class="form-row">
        <div class="form-group"><label class="form-label">GST %</label>
          <select id="mGst"><option value="0">0%</option><option value="5">5%</option><option value="12">12%</option><option value="18">18%</option></select>
        </div>
        <div class="form-group"><label class="form-label">Rack Location</label><input id="mRack" placeholder="A1, B2 etc"></div>
      </div>
      <div id="modalMsg" style="margin-top:10px;"></div>
    </div>
    <div class="modal-footer">
      <button class="btn btn-outline" onclick="document.getElementById('addModal').classList.remove('open')">Cancel</button>
      <button class="btn btn-primary" onclick="saveMedicine()">Save Medicine</button>
    </div>
  </div>
</div>

<div class="modal-overlay" id="batchModal">
  <div class="modal modal-lg">
    <div class="modal-header">
      <div class="modal-title" id="batchModalTitle">Batches</div>
      <button class="modal-close" onclick="document.getElementById('batchModal').classList.remove('open')">×</button>
    </div>
    <div class="modal-body">
      <div id="batchContent"></div>
      <details style="margin-top:18px;border:1px solid var(--border);border-radius:var(--radius-sm);padding:12px;">
        <summary style="cursor:pointer;font-weight:600;color:var(--primary);">+ Add New Batch</summary>
        <div style="margin-top:14px;">
          <div class="form-row">
            <div class="form-group"><label class="form-label">Batch No *</label><input id="bBatchNo"></div>
            <div class="form-group"><label class="form-label">Expiry Date *</label><input id="bExpiry" type="date"></div>
          </div>
          <div class="form-row">
            <div class="form-group"><label class="form-label">MRP (₹) *</label><input id="bMrp" type="number" step="0.01"></div>
            <div class="form-group"><label class="form-label">Purchase Rate (₹) *</label><input id="bRate" type="number" step="0.01"></div>
          </div>
          <div class="form-row">
            <div class="form-group"><label class="form-label">Stock Qty *</label><input id="bStock" type="number"></div>
            <div class="form-group"><label class="form-label">Min Stock Alert</label><input id="bMinStock" type="number" value="10"></div>
          </div>
          <button class="btn btn-primary" style="margin-top:10px;" onclick="saveBatch()">Add Batch</button>
        </div>
      </details>
    </div>
  </div>
</div>

<script>
const CTX='${pageContext.request.contextPath}';
let currentMedicineId=null;
function filterTable(q){q=q.toLowerCase();document.querySelectorAll('#medTable tbody tr').forEach(tr=>{tr.style.display=tr.textContent.toLowerCase().includes(q)?'':'none';});}
async function saveMedicine(){
  const name=document.getElementById('mName').value.trim();
  if(!name){showMsg('modalMsg','Name is required','error');return;}
  const body=new URLSearchParams({name,company:document.getElementById('mCompany').value,category:document.getElementById('mCategory').value,hsnCode:document.getElementById('mHsn').value,composition:document.getElementById('mComposition').value,gstPct:document.getElementById('mGst').value,rackLocation:document.getElementById('mRack').value});
  const res=await fetch(CTX+'/inventory/add-medicine',{method:'POST',body});
  const data=await res.json();
  if(data.success)location.reload();else showMsg('modalMsg',data.message,'error');
}
async function viewBatches(medicineId,name){
  currentMedicineId=medicineId;
  document.getElementById('batchModalTitle').textContent='Batches — '+name;
  const res=await fetch(CTX+'/inventory/batches?id='+medicineId);
  const data=await res.json();
  const badges={OUT_OF_STOCK:'badge-danger',CRITICAL:'badge-danger',EXPIRING_SOON:'badge-warning',LOW_STOCK:'badge-warning',EXPIRED:'badge-danger',OK:'badge-success'};
  const labels={OUT_OF_STOCK:'Out of Stock',CRITICAL:'Critical',EXPIRING_SOON:'Expiring Soon',LOW_STOCK:'Low Stock',EXPIRED:'Expired',OK:'OK'};
  if(!data.length){document.getElementById('batchContent').innerHTML='<p class="td-muted">No batches found.</p>';}
  else{
    let html='<div class="table-wrap"><table><thead><tr><th>Batch No</th><th>Expiry</th><th>MRP</th><th>Rate</th><th>Stock</th><th>Status</th></tr></thead><tbody>';
    data.forEach(b=>{html+='<tr><td class="td-mono">'+b.batchNo+'</td><td class="td-mono">'+b.expiryDate+'</td><td class="td-mono">₹'+b.mrp+'</td><td class="td-mono">₹'+b.purchaseRate+'</td><td class="td-mono">'+b.stockQty+'</td><td><span class="badge '+(badges[b.alertStatus]||'badge-neutral')+'">'+( labels[b.alertStatus]||b.alertStatus)+'</span></td></tr>';});
    html+='</tbody></table></div>';
    document.getElementById('batchContent').innerHTML=html;
  }
  document.getElementById('batchModal').classList.add('open');
}
async function saveBatch(){
  const body=new URLSearchParams({medicineId:currentMedicineId,batchNo:document.getElementById('bBatchNo').value,expiryDate:document.getElementById('bExpiry').value,mrp:document.getElementById('bMrp').value,purchaseRate:document.getElementById('bRate').value,stockQty:document.getElementById('bStock').value,minStock:document.getElementById('bMinStock').value});
  const res=await fetch(CTX+'/inventory/add-batch',{method:'POST',body});
  const data=await res.json();
  if(data.success)viewBatches(currentMedicineId,'');else alert(data.message);
}
async function deleteMedicine(id,name){
  if(!confirm('Deactivate: '+name+'?'))return;
  const res=await fetch(CTX+'/inventory/delete',{method:'POST',body:new URLSearchParams({medicineId:id})});
  const data=await res.json();
  if(data.success)location.reload();else alert(data.message);
}
function editMedicine(id){alert('Edit — id='+id);}
function showMsg(id,msg,type){const el=document.getElementById(id);el.className='alert alert-'+type;el.textContent=msg;}
</script>
</body></html>