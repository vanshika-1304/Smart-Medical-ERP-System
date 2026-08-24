<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html><html lang="en">
<head><meta charset="UTF-8"><title>New Bill — MedERP</title></head>
<body>
<%@ include file="/jsp/includes/navbar.jsp" %>
<div class="topbar">
  <div class="topbar-title">Billing & Invoice</div>
  <div class="topbar-actions">
    <a href="${pageContext.request.contextPath}/billing/list" class="btn btn-outline btn-sm">Bill History</a>
  </div>
</div>
<div class="content">
  <div class="tabs">
    <div class="tab active">New Bill</div>
    <div class="tab" onclick="location.href='${pageContext.request.contextPath}/billing/list'">Bill History</div>
  </div>

  <div class="grid-2" style="align-items:start;">
    <!-- Left: Bill Form -->
    <div style="display:flex;flex-direction:column;gap:16px;">
      <div class="card">
        <div class="card-title" style="margin-bottom:14px;">Customer Details</div>
        <div class="form-grid form-grid-2">
          <div class="form-group" style="grid-column:1/-1;">
            <label class="form-label">Customer / Shop Name *</label>
            <select id="customerId" class="form-select" onchange="checkCreditLimit(this)">
              <option value="">Select customer...</option>
              <c:forEach var="c" items="${customers}">
                <option value="${c.customerId}"
                        data-outstanding="${c.outstandingBalance}"
                        data-limit="${c.creditLimit}"
                        data-gstin="${c.gstin}">
                  ${c.shopName} — ${c.ownerName}
                  <c:if test="${c.outstandingBalance > 0}"> (Due: ₹${c.outstandingBalance})</c:if>
                </option>
              </c:forEach>
            </select>
            <div id="creditInfo" style="font-size:12px;color:var(--text-3);margin-top:3px;"></div>
          </div>
          <div class="form-group">
            <label class="form-label">Salesman</label>
            <select id="salesmanId" class="form-select">
              <option value="">-- None --</option>
              <c:forEach var="s" items="${salesmen}">
                <option value="${s.salesmanId}">${s.name}</option>
              </c:forEach>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">Bill Date</label>
            <input type="date" class="form-input" id="billDate" value="<%= java.time.LocalDate.now() %>">
          </div>
        </div>
      </div>

      <div id="creditAlert" style="display:none;">
        <div class="alert-strip danger">
          <svg viewBox="0 0 24 24" fill="currentColor"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/></svg>
          Credit limit exceeded! Billing is blocked for this customer.
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <div class="card-title">Medicine Search</div>
        </div>
        <div class="search-box">
          <svg viewBox="0 0 24 24" fill="currentColor"><path d="M15.5 14h-.79l-.28-.27A6.471 6.471 0 0016 9.5 6.5 6.5 0 109.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z"/></svg>
          <input type="text" id="medicineSearch" class="form-input" placeholder="Type medicine name or composition..." autocomplete="off" oninput="searchMedicine(this.value)" style="padding-left:32px;">
        </div>
        <div id="searchResults" style="border:1px solid var(--border);border-top:none;border-radius:0 0 var(--radius-sm) var(--radius-sm);max-height:220px;overflow-y:auto;display:none;background:white;box-shadow:var(--shadow);"></div>
      </div>

      <div class="card">
        <div class="card-header">
          <div class="card-title">Bill Items</div>
        </div>
        <div style="overflow-x:auto;">
          <table class="bill-items-table" id="billTable">
            <thead>
              <tr><th>#</th><th>Medicine</th><th>Batch / Expiry</th><th>MRP</th><th>Qty</th><th>Disc%</th><th>GST%</th><th>Amount</th><th></th></tr>
            </thead>
            <tbody id="billTbody">
              <tr id="emptyRow"><td colspan="9" style="text-align:center;color:var(--text-3);padding:20px;font-size:13px;">Search and add medicines above</td></tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- Right: Summary -->
    <div style="display:flex;flex-direction:column;gap:16px;">
      <div class="card">
        <div class="card-title" style="margin-bottom:14px;">Bill Summary</div>
        <div class="bill-summary">
          <div class="bill-summary-row"><span>Subtotal</span><span class="td-mono" id="sumSub">₹0.00</span></div>
          <div class="bill-summary-row"><span>Total Discount</span><span class="td-mono" id="sumDisc" style="color:var(--danger);">-₹0.00</span></div>
          <div class="bill-summary-row"><span>CGST</span><span class="td-mono" id="sumCgst">₹0.00</span></div>
          <div class="bill-summary-row"><span>SGST</span><span class="td-mono" id="sumSgst">₹0.00</span></div>
          <div class="bill-summary-row total"><span>Net Payable</span><span class="td-mono" id="sumTotal">₹0.00</span></div>
        </div>
        <div style="display:flex;gap:8px;margin-top:14px;">
          <button class="btn btn-primary" style="flex:1;" id="saveBtn" onclick="saveBill()">
            <svg viewBox="0 0 24 24" fill="currentColor"><path d="M17 3H5c-1.11 0-2 .9-2 2v14c0 1.1.89 2 2 2h14c1.1 0 2-.9 2-2V7l-4-4zm-5 16c-1.66 0-3-1.34-3-3s1.34-3 3-3 3 1.34 3 3-1.34 3-3 3zm3-10H5V5h10v4z"/></svg>
            Save Bill
          </button>
          <button class="btn btn-outline" onclick="clearBill()">Clear</button>
        </div>
        <div id="billMsg" style="margin-top:10px;"></div>
      </div>

      <div class="card">
        <div class="card-title" style="margin-bottom:10px;">GST Slab Breakup</div>
        <table style="width:100%;font-size:12.5px;">
          <thead><tr style="border-bottom:1px solid var(--border);">
            <th style="padding:6px 0;color:var(--text-3);font-weight:600;font-size:11px;text-transform:uppercase;">Slab</th>
            <th style="padding:6px;color:var(--text-3);font-weight:600;font-size:11px;text-transform:uppercase;">Taxable</th>
            <th style="padding:6px;color:var(--text-3);font-weight:600;font-size:11px;text-transform:uppercase;">CGST</th>
            <th style="padding:6px;color:var(--text-3);font-weight:600;font-size:11px;text-transform:uppercase;">SGST</th>
          </tr></thead>
          <tbody id="gstBreakup">
            <tr><td colspan="4" style="padding:10px 0;color:var(--text-3);text-align:center;font-size:12px;">No items added</td></tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</div>

<script>
const CTX = '${pageContext.request.contextPath}';
let billItems = [];
let searchTimer = null;
let gstSlabs = {};

function searchMedicine(q) {
  clearTimeout(searchTimer);
  const box = document.getElementById('searchResults');
  if (q.length < 2) { box.style.display = 'none'; return; }
  searchTimer = setTimeout(async function() {
    const res = await fetch(CTX + '/billing/search-medicine?q=' + encodeURIComponent(q));
    const meds = await res.json();
    if (!meds.length) { box.innerHTML = '<div style="padding:12px 14px;color:var(--text-3);font-size:13px;">No medicines found</div>'; box.style.display='block'; return; }
    var html = meds.map(function(m) {
      var safeName = (m.name||'').replace(/'/g,"&#39;").replace(/"/g,'&quot;');
      return '<div onclick="selectMedicine(' + m.medicineId + ',\'' + safeName + '\',' + m.gstPct + ')"' +
        ' style="padding:11px 14px;cursor:pointer;border-bottom:1px solid var(--border);" ' +
        ' onmouseover="this.style.background=\'#f5f6fa\'" onmouseout="this.style.background=\'\'">' +
        '<div style="font-weight:600;font-size:13.5px;">' + (m.name||'') + '</div>' +
        '<div style="font-size:12px;color:var(--text-3);margin-top:2px;">' + (m.company||'') + ' &nbsp;·&nbsp; ' + (m.composition||'') + ' &nbsp;·&nbsp; GST: ' + m.gstPct + '%</div>' +
        '</div>';
    }).join('');
    box.innerHTML = html;
    box.style.display = 'block';
  }, 250);
}

async function selectMedicine(medicineId, name, gstPct) {
  document.getElementById('searchResults').style.display = 'none';
  document.getElementById('medicineSearch').value = '';
  const res = await fetch(CTX + '/billing/batches?medicineId=' + medicineId);
  const batches = await res.json();
  if (!batches || !batches.length) { showMsg('billMsg', 'No stock available for ' + name, 'warning'); return; }
  const b = batches[0];
  addItemRow(medicineId, name, b.batchId, b.batchNo, b.expiryDate, b.mrp, b.purchaseRate, gstPct, batches);
}

function addItemRow(medicineId, name, batchId, batchNo, expiryDate, mrp, purchaseRate, gstPct, allBatches) {
  var idx = billItems.length;
  billItems.push({medicineId, batchId, qty:1, discount:0, mrp, purchaseRate, gstPct});
  var empty = document.getElementById('emptyRow');
  if (empty) empty.remove();
  var tbody = document.getElementById('billTbody');
  var tr = document.createElement('tr');
  tr.id = 'item_' + idx;
  var batchOpts = allBatches.map(function(b) {
    return '<option value="' + b.batchId + '" data-mrp="' + b.mrp + '" data-pr="' + b.purchaseRate + '"' + (b.batchId === batchId ? ' selected' : '') + '>' + b.batchNo + ' (Exp: ' + b.expiryDate + ') Qty:' + b.stockQty + '</option>';
  }).join('');
  tr.innerHTML = '<td style="color:var(--text-3);font-size:12px;">' + (idx+1) + '</td>' +
    '<td><strong style="font-size:13px;">' + name + '</strong></td>' +
    '<td><select style="width:200px;font-size:12px;" onchange="onBatchChange(' + idx + ',this)">' + batchOpts + '</select></td>' +
    '<td class="td-mono" id="mrp_' + idx + '">₹' + parseFloat(mrp).toFixed(2) + '</td>' +
    '<td><input type="number" min="1" value="1" style="width:60px;" onchange="updateItem(' + idx + ',\'qty\',this.value)"></td>' +
    '<td><input type="number" min="0" max="100" value="0" style="width:60px;" onchange="updateItem(' + idx + ',\'discount\',this.value)"></td>' +
    '<td class="td-mono" style="color:var(--text-3);">' + gstPct + '%</td>' +
    '<td class="td-mono" id="amt_' + idx + '" style="font-weight:600;color:var(--primary);">₹' + parseFloat(mrp).toFixed(2) + '</td>' +
    '<td><button class="remove-row" onclick="removeItem(' + idx + ')">×</button></td>';
  tbody.appendChild(tr);
  calcTotals();
}

function onBatchChange(idx, sel) {
  var opt = sel.options[sel.selectedIndex];
  billItems[idx].batchId = parseInt(sel.value);
  billItems[idx].mrp = parseFloat(opt.dataset.mrp);
  billItems[idx].purchaseRate = parseFloat(opt.dataset.pr);
  document.getElementById('mrp_' + idx).textContent = '₹' + billItems[idx].mrp.toFixed(2);
  calcTotals();
}

function updateItem(idx, field, val) {
  billItems[idx][field] = parseFloat(val) || 0;
  calcTotals();
}

function removeItem(idx) {
  billItems[idx] = null;
  var row = document.getElementById('item_' + idx);
  if (row) row.remove();
  calcTotals();
  if (billItems.filter(Boolean).length === 0) {
    document.getElementById('billTbody').innerHTML = '<tr id="emptyRow"><td colspan="9" style="text-align:center;color:var(--text-3);padding:20px;font-size:13px;">Search and add medicines above</td></tr>';
  }
}

function calcTotals() {
  var subtotal = 0, totalDisc = 0, totalGst = 0;
  gstSlabs = {};
  billItems.forEach(function(item, idx) {
    if (!item) return;
    var base = item.mrp * item.qty;
    var discAmt = base * item.discount / 100;
    var taxable = base - discAmt;
    var gstAmt = taxable * item.gstPct / 100;
    var lineAmt = taxable + gstAmt;
    subtotal += taxable;
    totalDisc += discAmt;
    totalGst += gstAmt;
    if (!gstSlabs[item.gstPct]) gstSlabs[item.gstPct] = {taxable:0, gst:0};
    gstSlabs[item.gstPct].taxable += taxable;
    gstSlabs[item.gstPct].gst += gstAmt;
    var amtEl = document.getElementById('amt_' + idx);
    if (amtEl) amtEl.textContent = '₹' + lineAmt.toFixed(2);
  });
  var netTotal = subtotal + totalGst;
  document.getElementById('sumSub').textContent = '₹' + subtotal.toFixed(2);
  document.getElementById('sumDisc').textContent = '-₹' + totalDisc.toFixed(2);
  document.getElementById('sumCgst').textContent = '₹' + (totalGst/2).toFixed(2);
  document.getElementById('sumSgst').textContent = '₹' + (totalGst/2).toFixed(2);
  document.getElementById('sumTotal').textContent = '₹' + netTotal.toFixed(2);
  // GST Breakup table
  var slabHtml = Object.entries(gstSlabs).map(function(e) {
    return '<tr style="border-bottom:1px solid var(--border);"><td style="padding:6px 0;" class="td-mono">' + e[0] + '%</td>' +
      '<td style="padding:6px;" class="td-mono">₹' + e[1].taxable.toFixed(2) + '</td>' +
      '<td style="padding:6px;" class="td-mono">₹' + (e[1].gst/2).toFixed(2) + '</td>' +
      '<td style="padding:6px;" class="td-mono">₹' + (e[1].gst/2).toFixed(2) + '</td></tr>';
  }).join('');
  document.getElementById('gstBreakup').innerHTML = slabHtml || '<tr><td colspan="4" style="padding:10px 0;color:var(--text-3);text-align:center;font-size:12px;">No items added</td></tr>';
}

function checkCreditLimit(sel) {
  var opt = sel.options[sel.selectedIndex];
  if (!opt.value) return;
  var outstanding = parseFloat(opt.dataset.outstanding) || 0;
  var limit = parseFloat(opt.dataset.limit) || 0;
  var info = document.getElementById('creditInfo');
  if (limit > 0) {
    var pct = Math.round(outstanding / limit * 100);
    info.textContent = 'Outstanding: ₹' + outstanding.toFixed(2) + ' / Credit Limit: ₹' + limit.toFixed(2) + ' (' + pct + '% used)';
  } else {
    info.textContent = outstanding > 0 ? 'Outstanding: ₹' + outstanding.toFixed(2) : '';
  }
  var alertEl = document.getElementById('creditAlert');
  var saveBtn = document.getElementById('saveBtn');
  if (limit > 0 && outstanding >= limit) {
    alertEl.style.display = 'block';
    saveBtn.disabled = true;
    saveBtn.style.opacity = '0.5';
  } else {
    alertEl.style.display = 'none';
    saveBtn.disabled = false;
    saveBtn.style.opacity = '1';
  }
}

async function saveBill() {
  var customerId = document.getElementById('customerId').value;
  if (!customerId) { showMsg('billMsg', 'Please select a customer', 'warning'); return; }
  var active = billItems.filter(Boolean);
  if (!active.length) { showMsg('billMsg', 'Add at least one medicine', 'warning'); return; }
  var payload = new URLSearchParams();
  payload.append('customerId', customerId);
  payload.append('salesmanId', document.getElementById('salesmanId').value);
  payload.append('items', JSON.stringify(active.map(function(i) {
    return {medicineId:i.medicineId, batchId:i.batchId, qty:i.qty, discount:i.discount};
  })));
  var res = await fetch(CTX + '/billing/create', {method:'POST', body:payload});
  var data = await res.json();
  if (data.success) {
    showMsg('billMsg', 'Bill saved successfully! Sale ID: ' + data.data.saleId, 'success');
    clearBill();
  } else {
    showMsg('billMsg', data.message, 'danger');
  }
}

function clearBill() {
  billItems = [];
  document.getElementById('billTbody').innerHTML = '<tr id="emptyRow"><td colspan="9" style="text-align:center;color:var(--text-3);padding:20px;font-size:13px;">Search and add medicines above</td></tr>';
  document.getElementById('customerId').value = '';
  document.getElementById('salesmanId').value = '';
  document.getElementById('creditInfo').textContent = '';
  document.getElementById('creditAlert').style.display = 'none';
  document.getElementById('saveBtn').disabled = false;
  document.getElementById('saveBtn').style.opacity = '1';
  calcTotals();
}

function showMsg(id, msg, type) {
  var el = document.getElementById(id);
  var colors = {success:'var(--success-light)',danger:'var(--danger-light)',warning:'var(--warning-light)'};
  var textColors = {success:'#166534',danger:'#991b1b',warning:'#92400e'};
  el.style.cssText = 'padding:10px 14px;border-radius:6px;font-size:13px;background:' + (colors[type]||colors.warning) + ';color:' + (textColors[type]||textColors.warning) + ';';
  el.textContent = msg;
  if (type === 'success') setTimeout(function(){ el.textContent=''; el.style.cssText=''; }, 4000);
}

document.addEventListener('click', function(e) {
  if (!e.target.closest('#medicineSearch') && !e.target.closest('#searchResults'))
    document.getElementById('searchResults').style.display = 'none';
});
</script>
</body></html>