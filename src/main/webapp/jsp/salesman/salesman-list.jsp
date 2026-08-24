<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html><html lang="en">
<head><meta charset="UTF-8"><title>Salesmen — Smart Medical ERP</title></head>
<body>
<%@ include file="/jsp/includes/navbar.jsp" %>
  <div class="topbar">
    <div class="topbar-title">Salesman Management</div>
    <div class="topbar-actions">
      <a href="${pageContext.request.contextPath}/salesmen/routes" class="btn btn-purple">🗺️ Routes</a>
      <a href="${pageContext.request.contextPath}/salesmen/report" class="btn btn-info">📊 Report</a>
      <button class="btn btn-primary" onclick="document.getElementById('addModal').classList.add('open')">+ Add Salesman</button>
    </div>
  </div>
  <div class="content">
    <div class="card">
      <div class="table-wrap">
        <table>
          <thead><tr><th>#</th><th>Name</th><th>Phone</th><th>Commission %</th><th>Status</th><th>Actions</th></tr></thead>
          <tbody>
            <c:forEach var="s" items="${salesmen}" varStatus="st">
              <tr>
                <td class="td-muted">${st.count}</td>
                <td><strong>${s.name}</strong></td>
                <td class="td-mono">${s.phone}</td>
                <td class="td-mono">${s.commissionPct}%</td>
                <td><span class="badge badge-success">Active</span></td>
                <td><button class="btn btn-sm btn-danger" onclick="deleteSalesman(${s.salesmanId},'${s.name}')">Deactivate</button></td>
              </tr>
            </c:forEach>
            <c:if test="${empty salesmen}"><tr><td colspan="6" style="text-align:center;color:var(--text-3);padding:28px;">No salesmen found.</td></tr></c:if>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</div>

<div class="modal-overlay" id="addModal">
  <div class="modal">
    <div class="modal-header"><div class="modal-title">Add Salesman</div><button class="modal-close" onclick="document.getElementById('addModal').classList.remove('open')">×</button></div>
    <div class="modal-body">
      <div class="form-group"><label class="form-label">Name *</label><input id="smName"></div>
      <div class="form-group" style="margin-top:12px;"><label class="form-label">Phone</label><input id="smPhone"></div>
      <div class="form-group" style="margin-top:12px;"><label class="form-label">Commission %</label><input id="smComm" type="number" step="0.01" value="0"></div>
      <div id="smMsg" style="margin-top:10px;"></div>
    </div>
    <div class="modal-footer">
      <button class="btn btn-outline" onclick="document.getElementById('addModal').classList.remove('open')">Cancel</button>
      <button class="btn btn-primary" onclick="saveSalesman()">Save</button>
    </div>
  </div>
</div>
<script>
const CTX='${pageContext.request.contextPath}';
async function saveSalesman(){
  const name=document.getElementById('smName').value.trim();
  if(!name){showMsg('smMsg','Name required','error');return;}
  const body=new URLSearchParams({name,phone:document.getElementById('smPhone').value,commissionPct:document.getElementById('smComm').value});
  const res=await fetch(CTX+'/salesmen/add',{method:'POST',body});
  const data=await res.json();
  if(data.success)location.reload();else showMsg('smMsg',data.message,'error');
}
async function deleteSalesman(id,name){
  if(!confirm('Deactivate: '+name+'?'))return;
  const res=await fetch(CTX+'/salesmen/delete',{method:'POST',body:new URLSearchParams({salesmanId:id})});
  const data=await res.json();
  if(data.success)location.reload();else alert(data.message);
}
function showMsg(id,msg,type){const el=document.getElementById(id);el.className='alert alert-'+type;el.textContent=msg;}
</script>
</body></html>