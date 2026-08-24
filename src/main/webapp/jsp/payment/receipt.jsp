<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html><html lang="en">
<head><meta charset="UTF-8"><title>Record Receipt — Smart Medical ERP</title></head>
<body>
<%@ include file="/jsp/includes/navbar.jsp" %>
  <div class="topbar">
    <div class="topbar-title">Record Customer Receipt</div>
  </div>
  <div class="content">
    <div style="display:grid;grid-template-columns:1fr 1fr;gap:20px;">
      <div class="card">
        <div class="card-header"><div class="card-title">💵 Customer Receipt</div></div>
        <div class="form-group"><label class="form-label">Customer *</label>
          <select id="rcCustomerId" onchange="showOutstanding(this)">
            <option value="">-- Select Customer --</option>
            <c:forEach var="c" items="${customers}">
              <option value="${c.customerId}" data-outstanding="${c.outstandingBalance}">${c.shopName} (₹${c.outstandingBalance} due)</option>
            </c:forEach>
          </select>
          <div id="custOutstanding" style="color:var(--text-3);font-size:12px;margin-top:4px;"></div>
        </div>
        <div class="form-group" style="margin-top:12px;"><label class="form-label">Amount (₹) *</label><input id="rcAmount" type="number" step="0.01" placeholder="Receipt amount"></div>
        <div class="form-group" style="margin-top:12px;"><label class="form-label">Payment Mode *</label>
          <select id="rcMode"><option value="CASH">Cash</option><option value="CHEQUE">Cheque</option><option value="UPI">UPI</option><option value="NEFT">NEFT</option><option value="RTGS">RTGS</option></select>
        </div>
        <div class="form-group" style="margin-top:12px;"><label class="form-label">Reference / Cheque No</label><input id="rcRef" placeholder="UPI txn / cheque no"></div>
        <div class="form-group" style="margin-top:12px;"><label class="form-label">Against Bill (Sale ID) — optional</label><input id="rcSaleId" type="number" placeholder="Sale ID"></div>
        <div id="rcMsg" style="margin-top:12px;"></div>
        <button class="btn btn-primary" style="margin-top:14px;width:100%;" onclick="saveReceipt()">✅ Record Receipt</button>
      </div>
      <div class="card">
        <div class="card-header"><div class="card-title">📊 Quick Outstanding View</div></div>
        <div class="table-wrap">
          <table>
            <thead><tr><th>Customer</th><th>Outstanding</th></tr></thead>
            <tbody>
              <c:forEach var="c" items="${customers}">
                <c:if test="${c.outstandingBalance>0}">
                  <tr>
                    <td>${c.shopName}</td>
                    <td class="td-mono fw-600 text-danger">₹${c.outstandingBalance}</td>
                  </tr>
                </c:if>
              </c:forEach>
            </tbody>
          </table>
        </div>
        <div style="margin-top:14px;display:flex;gap:8px;">
          <a href="${pageContext.request.contextPath}/reports/outstanding?format=pdf" class="btn btn-sm btn-danger">PDF Report</a>
          <a href="${pageContext.request.contextPath}/reports/outstanding?format=excel" class="btn btn-sm btn-success">Excel</a>
        </div>
      </div>
    </div>
  </div>
</div>
<script>
const CTX='${pageContext.request.contextPath}';
function showOutstanding(sel){const opt=sel.options[sel.selectedIndex];if(opt.value)document.getElementById('custOutstanding').textContent='Outstanding: ₹'+parseFloat(opt.dataset.outstanding||0).toFixed(2);}
async function saveReceipt(){
  const customerId=document.getElementById('rcCustomerId').value, amount=document.getElementById('rcAmount').value;
  if(!customerId||!amount||parseFloat(amount)<=0){showMsg('rcMsg','Please fill Customer and Amount','error');return;}
  const body=new URLSearchParams({customerId,amount,paymentMode:document.getElementById('rcMode').value,referenceNo:document.getElementById('rcRef').value,saleId:document.getElementById('rcSaleId').value});
  const res=await fetch(CTX+'/payments/receipt',{method:'POST',body});
  const data=await res.json();
  if(data.success){showMsg('rcMsg','✅ Receipt recorded!','success');setTimeout(()=>location.reload(),1500);}
  else showMsg('rcMsg','❌ '+data.message,'error');
}
function showMsg(id,msg,type){const el=document.getElementById(id);el.className='alert alert-'+type;el.textContent=msg;}
</script>
</body></html>