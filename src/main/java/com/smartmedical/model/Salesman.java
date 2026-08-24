package com.smartmedical.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Salesman {
    private int salesmanId;
    private String name;
    private String phone;
    private BigDecimal commissionPct;
    private boolean active;
    private Timestamp createdAt;
    private int createdBy;

    public Salesman() {}

    public int getSalesmanId()                       { return salesmanId; }
    public void setSalesmanId(int s)                 { this.salesmanId = s; }
    public String getName()                          { return name; }
    public void setName(String name)                 { this.name = name; }
    public String getPhone()                         { return phone; }
    public void setPhone(String phone)               { this.phone = phone; }
    public BigDecimal getCommissionPct()             { return commissionPct; }
    public void setCommissionPct(BigDecimal c)       { this.commissionPct = c; }
    public boolean isActive()                        { return active; }
    public void setActive(boolean active)            { this.active = active; }
    public Timestamp getCreatedAt()                  { return createdAt; }
    public void setCreatedAt(Timestamp t)            { this.createdAt = t; }
    public int getCreatedBy()                        { return createdBy; }
    public void setCreatedBy(int c)                  { this.createdBy = c; }
}
