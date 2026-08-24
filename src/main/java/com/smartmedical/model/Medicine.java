package com.smartmedical.model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class Medicine {
    private int medicineId;
    private String name;
    private String category;
    private String hsnCode;
    private String composition;
    private String company;
    private BigDecimal gstPct;
    private String rackLocation;
    private boolean active;
    private Timestamp createdAt;
    private int createdBy;

    public Medicine() {}

    public int getMedicineId()                   { return medicineId; }
    public void setMedicineId(int medicineId)    { this.medicineId = medicineId; }
    public String getName()                      { return name; }
    public void setName(String name)             { this.name = name; }
    public String getCategory()                  { return category; }
    public void setCategory(String category)     { this.category = category; }
    public String getHsnCode()                   { return hsnCode; }
    public void setHsnCode(String hsnCode)       { this.hsnCode = hsnCode; }
    public String getComposition()               { return composition; }
    public void setComposition(String c)         { this.composition = c; }
    public String getCompany()                   { return company; }
    public void setCompany(String company)       { this.company = company; }
    public BigDecimal getGstPct()                { return gstPct; }
    public void setGstPct(BigDecimal g)          { this.gstPct = g; }
    public String getRackLocation()              { return rackLocation; }
    public void setRackLocation(String r)        { this.rackLocation = r; }
    public boolean isActive()                    { return active; }
    public void setActive(boolean active)        { this.active = active; }
    public Timestamp getCreatedAt()              { return createdAt; }
    public void setCreatedAt(Timestamp t)        { this.createdAt = t; }
    public int getCreatedBy()                    { return createdBy; }
    public void setCreatedBy(int c)              { this.createdBy = c; }
}
