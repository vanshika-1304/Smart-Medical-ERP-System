<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html><html lang="en">
<head><meta charset="UTF-8"><title>Routes — Smart Medical ERP</title></head>
<body>
<%@ include file="/jsp/includes/navbar.jsp" %>
  <div class="topbar">
    <div class="topbar-title">Route Management</div>
    <div class="topbar-actions">
      <button class="btn btn-primary" onclick="document.getElementById('addModal').classList.add('open')">+ Add Route</button>
    </div>
  </div>
  <div class="content">
    <div class="card">
      <div class="table-wrap">
        <table>
          <thead><tr><th>#</th><th>Route Name</th><th>Area</th><th>Assigned Salesman</th><th>Actions</th></tr></thead>
          <tbody>
            <c:forEach var="r" items="${routes}" varStatus="st">
              <tr>
                <td class="td-muted">${st.count}</td>
                <td><strong>${r.routeName}</strong></td>
                <td>${r.area}</td>
                <td><span class="badge badge-info">${r.salesmanName}</span></td>
                <td><button class="btn btn-sm btn-danger" onclick="deleteRoute(${r.routeId},'${r.routeName}')">Delete</button></td>
              </tr>
            </c:forEach>
            <c:if test="${empty routes}"><tr><td colspan="5" style="text-align:center;color:var(--text-3);padding:28px;">No routes defined.</td></tr></c:if>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</div>

<div class="modal-overlay" id="addModal">
  <div class="modal">
    <div class="modal-header"><div class="modal-title">Add Route</div><button class="modal-close" onclick="document.getElementById('addModal').classList.remove('open')">×</button></div>
    <div class="modal-body">
      <div class="form-group"><label class="form-label">Route Name *</label><input id="rName"></div>
      <div class="form-group" style="margin-top:12px;"><label class="form-label">Area</label><input id="rArea" placeholder="e.g. North Mumbai, Andheri West"></div>
      <div class="form-group" style="margin-top:12px;"><label class="form-label">Assign Salesman</label>
        <select id="rSalesmanId">
          <option value="">-- None --</option>
          <c:forEach var="s" items="${salesmen}"><option value="${s.salesmanId}">${s.name}</option></c:forEach>
        </select>
      </div>
      <div id="rMsg" style="margin-top:10px;"></div>
    </div>
    <div class="modal-footer">
      <button class="btn btn-outline" onclick="document.getElementById('addModal').classList.remove('open')">Cancel</button>
      <button class="btn btn-primary" onclick="saveRoute()">Save Route</button>
    </div>
  </div>
</div>
<script>
const CTX='${pageContext.request.contextPath}';
async function saveRoute(){
  const name=document.getElementById('rName').value.trim();
  if(!name){showMsg('rMsg','Name required','error');return;}
  const body=new URLSearchParams({routeName:name,area:document.getElementById('rArea').value,salesmanId:document.getElementById('rSalesmanId').value});
  const res=await fetch(CTX+'/salesmen/routes/add',{method:'POST',body});
  const data=await res.json();
  if(data.success)location.reload();else showMsg('rMsg',data.message,'error');
}
async function deleteRoute(id,name){
  if(!confirm('Deactivate route: '+name+'?'))return;
  const res=await fetch(CTX+'/salesmen/routes/delete',{method:'POST',body:new URLSearchParams({routeId:id})});
  const data=await res.json();
  if(data.success)location.reload();else alert(data.message);
}
function showMsg(id,msg,type){const el=document.getElementById(id);el.className='alert alert-'+type;el.textContent=msg;}
</script>
</body></html>