<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html><html lang="en">
<head><meta charset="UTF-8"><title>New Purchase — Smart Medical ERP</title></head>
<body>
<%@ include file="/jsp/includes/navbar.jsp" %>
  <div class="topbar">
    <div class="topbar-title">Record New Purchase</div>
  </div>
  <div class="content">
    <div class="card">
      <div class="form-row">
        <div class="form-group"><label class="form-label">Supplier *</label>
          <select id="supplierId" required>
            <option value="">-- Select Supplier --</option>
            <c:forEach var="s" items="${suppliers}"><option value="${s.supplierId}">${s.name}</option></c:forEach>
          </select>
        </div>
        <div class="form-group"><label class="form-label">Invoice No *</label><input id="invoiceNo" placeholder="Supplier invoice number"></div>
        <div class="form-group"><label class="form-label">Purchase Date</label><input id="purchaseDate" type="date" value="<%= java.time.LocalDate.now() %>"></div>
      </div>
      <div style="margin-top:16px;">
        <label class="form-label">Add Medicine</label>
        <div class="search-wrap">
          <input type="text" id="medSearch" placeholder="Search medicine to add..." autocomplete="off" oninput="searchMed(this.value)">
          <div class="search-results" id="medResults"></div>
        </div>
      </div>
      <div style="margin-top:16px;">
        <div class="table-wrap">
          <table>
            <thead><tr><th>#</th><th>Medicine</th><th>Batch No</th><th>Expiry Date</th><th>Qty</th><th>Rate (₹)</th><th>MRP (₹)</th><th>GST%</th><th>Amount</th><th></th></tr></thead>
            <tbody id="purchaseItems">
              <tr id="emptyRow"><td colspan="10" style="text-align:center;color:var(--text-3);padding:24px;">No items added.</td></tr>
            </tbody>
            <tfoot><tr><td colspan="8" class="fw-600">TOTAL:</td><td id="totalDisplay" class="td-mono fw-600">₹0.00</td><td></td></tr></tfoot>
          </table>
        </div>
      </div>
      <div style="display:flex;gap:10px;margin-top:16px;">
        <button class="btn btn-primary" onclick="savePurchase()">💾 Save Purchase</button>
        <a href="${pageContext.request.contextPath}/suppliers" class="btn btn-outline">Cancel</a>
      </div>
      <div id="msg" style="margin-top:12px;"></div>
    </div>
  </div>
</div>
<script>
const CTX='${pageContext.request.contextPath}';
let purchItems=[],searchTimer;
async function searchMed(q){
  clearTimeout(searchTimer);const box=document.getElementById('medResults');
  if(q.length<2){box.style.display='none';return;}
  searchTimer=setTimeout(async()=>{
    const res=await fetch(CTX+'/billing/search-medicine?q='+encodeURIComponent(q));
    const meds=await res.json();
    if(!meds.length){box.style.display='none';return;}
    box.innerHTML=meds.map(m=>'<div class="search-item" onclick="addPurchaseItem('+m.medicineId+',\''+m.name.replace(/\'/g,"&#39;")+'\',' +m.gstPct+')"><strong>'+m.name+'</strong> — '+(m.company||'')+' <small class="td-muted">'+(m.composition||'')+'</small></div>').join('');
    box.style.display='block';
  },250);
}
function addPurchaseItem(medicineId,name,gstPct){
  document.getElementById('medResults').style.display='none';document.getElementById('medSearch').value='';
  const idx=purchItems.length;purchItems.push({medicineId,name,gstPct,qty:1,rate:0,mrp:0,batchNo:'',expiryDate:'',minStock:10});
  document.getElementById('emptyRow')?.remove();
  const tbody=document.getElementById('purchaseItems');const tr=document.createElement('tr');tr.id='pitem_'+idx;
  tr.innerHTML='<td class="td-muted">'+(idx+1)+'</td><td><strong>'+name+'</strong></td>'+
    '<td><input placeholder="Batch no" style="width:110px;" onchange="updatePItem('+idx+',\'batchNo\',this.value)"></td>'+
    '<td><input type="date" style="width:140px;" onchange="updatePItem('+idx+',\'expiryDate\',this.value)"></td>'+
    '<td><input type="number" min="1" value="1" style="width:60px;" onchange="updatePItem('+idx+',\'qty\',this.value);calcTotal()"></td>'+
    '<td><input type="number" step="0.01" placeholder="0.00" style="width:80px;" onchange="updatePItem('+idx+',\'rate\',this.value);calcTotal()"></td>'+
    '<td><input type="number" step="0.01" placeholder="0.00" style="width:80px;" onchange="updatePItem('+idx+',\'mrp\',this.value)"></td>'+
    '<td class="td-muted">'+gstPct+'%</td>'+
    '<td id="pamt_'+idx+'" class="td-mono">₹0.00</td>'+
    '<td><button class="btn btn-sm btn-danger" onclick="removePItem('+idx+')">✕</button></td>';
  tbody.appendChild(tr);
}
function updatePItem(idx,field,val){purchItems[idx][field]=field==='batchNo'||field==='expiryDate'?val:(parseFloat(val)||0);calcTotal();}
function removePItem(idx){purchItems[idx]=null;document.getElementById('pitem_'+idx)?.remove();calcTotal();}
function calcTotal(){let total=0;purchItems.forEach((item,idx)=>{if(!item)return;const amt=item.qty*item.rate;total+=amt;const el=document.getElementById('pamt_'+idx);if(el)el.textContent='₹'+amt.toFixed(2);});document.getElementById('totalDisplay').textContent='₹'+total.toFixed(2);}
async function savePurchase(){
  const supplierId=document.getElementById('supplierId').value,invoiceNo=document.getElementById('invoiceNo').value.trim();
  if(!supplierId){showMsg('msg','Select supplier','error');return;}
  if(!invoiceNo){showMsg('msg','Enter invoice number','error');return;}
  const activeItems=purchItems.filter(Boolean);
  if(!activeItems.length){showMsg('msg','Add at least one item','error');return;}
  for(const item of activeItems){if(!item.batchNo||!item.expiryDate||!item.rate||!item.mrp){showMsg('msg','Fill all fields for each item','error');return;}}
  const body=new URLSearchParams({supplierId,invoiceNo,items:JSON.stringify(activeItems)});
  const res=await fetch(CTX+'/suppliers/purchase/create',{method:'POST',body});
  const data=await res.json();
  if(data.success){showMsg('msg','✅ Purchase saved! Stock updated.','success');purchItems=[];document.getElementById('purchaseItems').innerHTML='<tr id="emptyRow"><td colspan="10" style="text-align:center;color:var(--text-3);padding:24px;">No items added.</td></tr>';calcTotal();}
  else showMsg('msg','❌ '+data.message,'error');
}
function showMsg(id,msg,type){const el=document.getElementById(id);el.className='alert alert-'+type;el.textContent=msg;}
document.addEventListener('click',e=>{if(!e.target.closest('#medSearch'))document.getElementById('medResults').style.display='none';});
</script>
</body></html>