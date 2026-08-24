package com.smartmedical.model;

import java.math.BigDecimal;
import java.sql.Date;

public class MedicineBatch {
    private int batchId;
    private int medicineId;
    private String medicineName;  // joined field
    private String batchNo;
    private Date expiryDate;
    private BigDecimal mrp;
    private BigDecimal purchaseRate;
    private int stockQty;
    private int minStockAlert;
    private boolean active;

    public MedicineBatch() {}

    public int getBatchId()                          { return batchId; }
    public void setBatchId(int batchId)              { this.batchId = batchId; }
    public int getMedicineId()                       { return medicineId; }
    public void setMedicineId(int medicineId)        { this.medicineId = medicineId; }
    public String getMedicineName()                  { return medicineName; }
    public void setMedicineName(String n)            { this.medicineName = n; }
    public String getBatchNo()                       { return batchNo; }
    public void setBatchNo(String batchNo)           { this.batchNo = batchNo; }
    public Date getExpiryDate()                      { return expiryDate; }
    public void setExpiryDate(Date expiryDate)       { this.expiryDate = expiryDate; }
    public BigDecimal getMrp()                       { return mrp; }
    public void setMrp(BigDecimal mrp)               { this.mrp = mrp; }
    public BigDecimal getPurchaseRate()              { return purchaseRate; }
    public void setPurchaseRate(BigDecimal r)        { this.purchaseRate = r; }
    public int getStockQty()                         { return stockQty; }
    public void setStockQty(int stockQty)            { this.stockQty = stockQty; }
    public int getMinStockAlert()                    { return minStockAlert; }
    public void setMinStockAlert(int m)              { this.minStockAlert = m; }
    public boolean isActive()                        { return active; }
    public void setActive(boolean active)            { this.active = active; }

    /** Alert status for UI */
    public String getAlertStatus() {
        if (stockQty == 0) return "OUT_OF_STOCK";
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate exp = expiryDate.toLocalDate();
        if (exp.isBefore(today)) return "EXPIRED";
        if (exp.isBefore(today.plusDays(7)))  return "CRITICAL";
        if (exp.isBefore(today.plusDays(30))) return "EXPIRING_SOON";
        if (stockQty < minStockAlert) return "LOW_STOCK";
        return "OK";
    }
}
