<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<%@ taglib uri="jakarta.tags.functions" prefix="fn" %>
<!DOCTYPE html><html><head><meta charset="UTF-8"><title>User Management</title></head>
<body>
<%@ include file="/jsp/includes/navbar.jsp" %>
<div class="container">
  <div class="card">
    <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;">
      <h2>👤 User Management (Admin Only)</h2>
      <button class="btn btn-primary" onclick="showAddModal()">+ Add User</button>
    </div>
    <table>
      <thead><tr><th>#</th><th>Username</th><th>Role</th><th>Last Login</th><th>Status</th><th>Actions</th></tr></thead>
      <tbody>
        <c:forEach var="u" items="${users}" varStatus="st">
          <tr>
            <td>${st.count}</td>
            <td><strong>${u.username}</strong></td>
            <td>
              <c:choose>
                <c:when test="${u.role=='ADMIN'}"><span class="badge badge-red">ADMIN</span></c:when>
                <c:when test="${u.role=='STAFF'}"><span class="badge badge-blue">STAFF</span></c:when>
                <c:when test="${u.role=='SALESMAN'}"><span class="badge badge-amber">SALESMAN</span></c:when>
                <c:otherwise><span class="badge badge-green">OWNER</span></c:otherwise>
              </c:choose>
            </td>
            <td>${u.lastLogin}</td>
            <td>${u.active ? '<span class="badge badge-green">Active</span>' : '<span class="badge badge-red">Inactive</span>'}</td>
            <td>
              <button class="btn btn-sm btn-primary" onclick="resetPass(${u.userId},'${u.username}')">🔑 Reset</button>
              <c:if test="${u.userId != sessionScope.loggedInUser.userId}">
                <button class="btn btn-sm btn-danger" onclick="deactivateUser(${u.userId},'${u.username}')">🗑️</button>
              </c:if>
            </td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>
</div>

<div id="addModal" style="display:none;position:fixed;top:0;left:0;right:0;bottom:0;
  background:rgba(0,0,0,0.5);z-index:1000;align-items:center;justify-content:center;">
  <div style="background:#fff;border-radius:8px;padding:24px;width:400px;">
    <h3>Add New User</h3>
    <div><label>Username *</label><input id="uName" placeholder="Login username"></div>
    <div><label>Password *</label><input id="uPass" type="password" placeholder="Initial password"></div>
    <div><label>Role *</label>
      <select id="uRole">
        <option value="STAFF">Staff / Billing</option>
        <option value="SALESMAN">Salesman</option>
        <option value="OWNER">Owner</option>
        <option value="ADMIN">Admin</option>
      </select>
    </div>
    <div id="uMsg" style="margin:10px 0;"></div>
    <div style="display:flex;gap:10px;margin-top:16px;">
      <button class="btn btn-primary" onclick="saveUser()">Create</button>
      <button class="btn" style="background:#eee;" onclick="document.getElementById('addModal').style.display='none'">Cancel</button>
    </div>
  </div>
</div>

<script>
const CTX = '${pageContext.request.contextPath}';
function showAddModal(){document.getElementById('addModal').style.display='flex';}
async function saveUser(){
  const username=document.getElementById('uName').value.trim();
  const password=document.getElementById('uPass').value;
  if(!username||!password){showMsg('uMsg','All fields required','error');return;}
  const body=new URLSearchParams({username,password,role:document.getElementById('uRole').value});
  const res=await fetch(CTX+'/users/add',{method:'POST',body});
  const data=await res.json();
  if(data.success)location.reload();else showMsg('uMsg',data.message,'error');
}
async function resetPass(id,name){
  const np=prompt('New password for '+name+':');
  if(!np||np.length<6){alert('Password must be at least 6 characters');return;}
  const res=await fetch(CTX+'/users/reset-password',{method:'POST',body:new URLSearchParams({userId:id,newPassword:np})});
  const data=await res.json();
  alert(data.message);
}
async function deactivateUser(id,name){
  if(!confirm('Deactivate user: '+name+'?'))return;
  const res=await fetch(CTX+'/users/deactivate',{method:'POST',body:new URLSearchParams({userId:id})});
  const data=await res.json();
  if(data.success)location.reload();else alert(data.message);
}
function showMsg(id,msg,type){const el=document.getElementById(id);el.className='alert alert-'+type;el.textContent=msg;}
</script>
</body></html>
