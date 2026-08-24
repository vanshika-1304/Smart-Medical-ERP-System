<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html><html lang="en">
<head><meta charset="UTF-8"><title>Supplier Payment — Smart Medical ERP</title></head>
<body>
<%@ include file="/jsp/includes/navbar.jsp" %>
  <div class="topbar">
    <div class="topbar-title">Record Supplier Payment</div>
  </div>
  <div class="content">
    <div class="card" style="max-width:520px;">
      <div class="card-header"><div class="card-title">💳 Supplier Payment</div></div>
      <div class="form-group"><label class="form-label">Supplier *</label>
        <select id="spSupplierId">
          <option value="">-- Select Supplier --</option>
          <c:forEach var="s" items="${suppliers}"><option value="${s.supplierId}">${s.name} (₹${s.outstandingBalance} due)</option></c:forEach>
        </select>
      </div>
      <div class="form-group" style="margin-top:12px;"><label class="form-label">Amount (₹) *</label><input id="spAmount" type="number" step="0.01" placeholder="Payment amount"></div>
      <div class="form-group" style="margin-top:12px;"><label class="form-label">Payment Mode *</label>
        <select id="spMode"><option value="CASH">Cash</option><option value="CHEQUE">Cheque</option><option value="UPI">UPI</option><option value="NEFT">NEFT</option><option value="RTGS">RTGS</option></select>
      </div>
      <div class="form-group" style="margin-top:12px;"><label class="form-label">Reference / Cheque No</label><input id="spRef" placeholder="Transaction reference"></div>
      <div class="form-group" style="margin-top:12px;"><label class="form-label">Against Purchase ID (optional)</label><input id="spPurchId" type="number" placeholder="Purchase ID"></div>
      <div id="spMsg" style="margin-top:12px;"></div>
      <div style="display:flex;gap:10px;margin-top:16px;">
        <button class="btn btn-primary" onclick="savePayment()">✅ Record Payment</button>
        <a href="${pageContext.request.contextPath}/suppliers" class="btn btn-outline">Cancel</a>
      </div>
    </div>
  </div>
</div>
<script>
const CTX='${pageContext.request.contextPath}';
async function savePayment(){
  const supplierId=document.getElementById('spSupplierId').value, amount=document.getElementById('spAmount').value;
  if(!supplierId||!amount){showMsg('spMsg','Fill Supplier and Amount','error');return;}
  const body=new URLSearchParams({supplierId,amount,paymentMode:document.getElementById('spMode').value,referenceNo:document.getElementById('spRef').value,purchaseId:document.getElementById('spPurchId').value});
  const res=await fetch(CTX+'/payments/supplier-payment',{method:'POST',body});
  const data=await res.json();
  if(data.success){showMsg('spMsg','✅ Payment recorded!','success');setTimeout(()=>location.href=CTX+'/suppliers',1500);}
  else showMsg('spMsg','❌ '+data.message,'error');
}
function showMsg(id,msg,type){const el=document.getElementById(id);el.className='alert alert-'+type;el.textContent=msg;}
</script>
</body></html>