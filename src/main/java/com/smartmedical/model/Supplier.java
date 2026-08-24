package com.smartmedical.model;

import java.math.BigDecimal;

public class Supplier {
    private int supplierId;
    private String name;
    private String contactNo;
    private String gstin;
    private String address;
    private BigDecimal creditLimit;
    private BigDecimal outstandingBalance;
    private boolean active;

    public Supplier() {}

    public int getSupplierId()                               { return supplierId; }
    public void setSupplierId(int supplierId)                { this.supplierId = supplierId; }
    public String getName()                                  { return name; }
    public void setName(String name)                         { this.name = name; }
    public String getContactNo()                             { return contactNo; }
    public void setContactNo(String contactNo)               { this.contactNo = contactNo; }
    public String getGstin()                                 { return gstin; }
    public void setGstin(String gstin)                       { this.gstin = gstin; }
    public String getAddress()                               { return address; }
    public void setAddress(String address)                   { this.address = address; }
    public BigDecimal getCreditLimit()                       { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit)       { this.creditLimit = creditLimit; }
    public BigDecimal getOutstandingBalance()                { return outstandingBalance; }
    public void setOutstandingBalance(BigDecimal b)          { this.outstandingBalance = b; }
    public boolean isActive()                                { return active; }
    public void setActive(boolean active)                    { this.active = active; }
}
