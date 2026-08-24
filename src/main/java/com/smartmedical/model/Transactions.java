package com.smartmedical.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

// ─── Sale ────────────────────────────────────────────────────────────────────
class Sale {
    private int saleId;
    private int customerId;
    private String customerShopName;
    private int salesmanId;
    private String salesmanName;
    private String billNo;
    private Date saleDate;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal gst;
    private BigDecimal netTotal;
    private String paymentStatus;
    private Timestamp createdAt;
    private int createdBy;
    private List<SaleItem> items;

    public Sale() {}

    public int getSaleId()                           { return saleId; }
    public void setSaleId(int saleId)                { this.saleId = saleId; }
    public int getCustomerId()                       { return customerId; }
    public void setCustomerId(int customerId)        { this.customerId = customerId; }
    public String getCustomerShopName()              { return customerShopName; }
    public void setCustomerShopName(String s)        { this.customerShopName = s; }
    public int getSalesmanId()                       { return salesmanId; }
    public void setSalesmanId(int salesmanId)        { this.salesmanId = salesmanId; }
    public String getSalesmanName()                  { return salesmanName; }
    public void setSalesmanName(String s)            { this.salesmanName = s; }
    public String getBillNo()                        { return billNo; }
    public void setBillNo(String billNo)             { this.billNo = billNo; }
    public Date getSaleDate()                        { return saleDate; }
    public void setSaleDate(Date saleDate)           { this.saleDate = saleDate; }
    public BigDecimal getSubtotal()                  { return subtotal; }
    public void setSubtotal(BigDecimal subtotal)     { this.subtotal = subtotal; }
    public BigDecimal getDiscount()                  { return discount; }
    public void setDiscount(BigDecimal discount)     { this.discount = discount; }
    public BigDecimal getGst()                       { return gst; }
    public void setGst(BigDecimal gst)               { this.gst = gst; }
    public BigDecimal getNetTotal()                  { return netTotal; }
    public void setNetTotal(BigDecimal netTotal)     { this.netTotal = netTotal; }
    public String getPaymentStatus()                 { return paymentStatus; }
    public void setPaymentStatus(String s)           { this.paymentStatus = s; }
    public Timestamp getCreatedAt()                  { return createdAt; }
    public void setCreatedAt(Timestamp t)            { this.createdAt = t; }
    public int getCreatedBy()                        { return createdBy; }
    public void setCreatedBy(int c)                  { this.createdBy = c; }
    public List<SaleItem> getItems()                 { return items; }
    public void setItems(List<SaleItem> items)       { this.items = items; }
}

// ─── SaleItem ─────────────────────────────────────────────────────────────────
class SaleItem {
    private int itemId;
    private int saleId;
    private int medicineId;
    private String medicineName;
    private int batchId;
    private String batchNo;
    private int qtySold;
    private BigDecimal mrp;
    private BigDecimal purchaseRate;
    private BigDecimal discount;
    private BigDecimal gstPct;
    private BigDecimal amount;

    public SaleItem() {}

    public int getItemId()                           { return itemId; }
    public void setItemId(int itemId)                { this.itemId = itemId; }
    public int getSaleId()                           { return saleId; }
    public void setSaleId(int saleId)                { this.saleId = saleId; }
    public int getMedicineId()                       { return medicineId; }
    public void setMedicineId(int medicineId)        { this.medicineId = medicineId; }
    public String getMedicineName()                  { return medicineName; }
    public void setMedicineName(String n)            { this.medicineName = n; }
    public int getBatchId()                          { return batchId; }
    public void setBatchId(int batchId)              { this.batchId = batchId; }
    public String getBatchNo()                       { return batchNo; }
    public void setBatchNo(String b)                 { this.batchNo = b; }
    public int getQtySold()                          { return qtySold; }
    public void setQtySold(int qtySold)              { this.qtySold = qtySold; }
    public BigDecimal getMrp()                       { return mrp; }
    public void setMrp(BigDecimal mrp)               { this.mrp = mrp; }
    public BigDecimal getPurchaseRate()              { return purchaseRate; }
    public void setPurchaseRate(BigDecimal r)        { this.purchaseRate = r; }
    public BigDecimal getDiscount()                  { return discount; }
    public void setDiscount(BigDecimal discount)     { this.discount = discount; }
    public BigDecimal getGstPct()                    { return gstPct; }
    public void setGstPct(BigDecimal gstPct)         { this.gstPct = gstPct; }
    public BigDecimal getAmount()                    { return amount; }
    public void setAmount(BigDecimal amount)         { this.amount = amount; }

    /** Profit per item = (MRP - purchaseRate) * qty - discount */
    public BigDecimal getProfit() {
        BigDecimal base = mrp.subtract(purchaseRate)
                .multiply(BigDecimal.valueOf(qtySold));
        return base.subtract(discount == null ? BigDecimal.ZERO : discount);
    }
}

// ─── Purchase ─────────────────────────────────────────────────────────────────
class Purchase {
    private int purchaseId;
    private int supplierId;
    private String supplierName;
    private String invoiceNo;
    private Date purchaseDate;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private List<PurchaseItem> items;

    public Purchase() {}

    public int getPurchaseId()                           { return purchaseId; }
    public void setPurchaseId(int p)                     { this.purchaseId = p; }
    public int getSupplierId()                           { return supplierId; }
    public void setSupplierId(int s)                     { this.supplierId = s; }
    public String getSupplierName()                      { return supplierName; }
    public void setSupplierName(String s)                { this.supplierName = s; }
    public String getInvoiceNo()                         { return invoiceNo; }
    public void setInvoiceNo(String i)                   { this.invoiceNo = i; }
    public Date getPurchaseDate()                        { return purchaseDate; }
    public void setPurchaseDate(Date d)                  { this.purchaseDate = d; }
    public BigDecimal getTotalAmount()                   { return totalAmount; }
    public void setTotalAmount(BigDecimal t)             { this.totalAmount = t; }
    public String getPaymentStatus()                     { return paymentStatus; }
    public void setPaymentStatus(String s)               { this.paymentStatus = s; }
    public List<PurchaseItem> getItems()                 { return items; }
    public void setItems(List<PurchaseItem> items)       { this.items = items; }
}

// ─── PurchaseItem ─────────────────────────────────────────────────────────────
class PurchaseItem {
    private int itemId;
    private int purchaseId;
    private int medicineId;
    private String medicineName;
    private int batchId;
    private String batchNo;
    private int qty;
    private BigDecimal rate;
    private BigDecimal mrp;
    private BigDecimal gstPct;
    private BigDecimal amount;

    public PurchaseItem() {}

    public int getItemId()                   { return itemId; }
    public void setItemId(int i)             { this.itemId = i; }
    public int getPurchaseId()               { return purchaseId; }
    public void setPurchaseId(int p)         { this.purchaseId = p; }
    public int getMedicineId()               { return medicineId; }
    public void setMedicineId(int m)         { this.medicineId = m; }
    public String getMedicineName()          { return medicineName; }
    public void setMedicineName(String n)    { this.medicineName = n; }
    public int getBatchId()                  { return batchId; }
    public void setBatchId(int b)            { this.batchId = b; }
    public String getBatchNo()               { return batchNo; }
    public void setBatchNo(String b)         { this.batchNo = b; }
    public int getQty()                      { return qty; }
    public void setQty(int q)                { this.qty = q; }
    public BigDecimal getRate()              { return rate; }
    public void setRate(BigDecimal r)        { this.rate = r; }
    public BigDecimal getMrp()               { return mrp; }
    public void setMrp(BigDecimal m)         { this.mrp = m; }
    public BigDecimal getGstPct()            { return gstPct; }
    public void setGstPct(BigDecimal g)      { this.gstPct = g; }
    public BigDecimal getAmount()            { return amount; }
    public void setAmount(BigDecimal a)      { this.amount = a; }
}

// ─── Payment ──────────────────────────────────────────────────────────────────
class Payment {
    private int paymentId;
    private char partyType;  // 'C' or 'S'
    private int partyId;
    private String partyName;
    private String paymentMode;
    private BigDecimal amount;
    private String referenceNo;
    private Date paymentDate;
    private Integer saleId;
    private Integer purchaseId;
    private String chequeStatus;

    public Payment() {}

    public int getPaymentId()                        { return paymentId; }
    public void setPaymentId(int p)                  { this.paymentId = p; }
    public char getPartyType()                       { return partyType; }
    public void setPartyType(char p)                 { this.partyType = p; }
    public int getPartyId()                          { return partyId; }
    public void setPartyId(int p)                    { this.partyId = p; }
    public String getPartyName()                     { return partyName; }
    public void setPartyName(String n)               { this.partyName = n; }
    public String getPaymentMode()                   { return paymentMode; }
    public void setPaymentMode(String m)             { this.paymentMode = m; }
    public BigDecimal getAmount()                    { return amount; }
    public void setAmount(BigDecimal a)              { this.amount = a; }
    public String getReferenceNo()                   { return referenceNo; }
    public void setReferenceNo(String r)             { this.referenceNo = r; }
    public Date getPaymentDate()                     { return paymentDate; }
    public void setPaymentDate(Date d)               { this.paymentDate = d; }
    public Integer getSaleId()                       { return saleId; }
    public void setSaleId(Integer s)                 { this.saleId = s; }
    public Integer getPurchaseId()                   { return purchaseId; }
    public void setPurchaseId(Integer p)             { this.purchaseId = p; }
    public String getChequeStatus()                  { return chequeStatus; }
    public void setChequeStatus(String c)            { this.chequeStatus = c; }
}
