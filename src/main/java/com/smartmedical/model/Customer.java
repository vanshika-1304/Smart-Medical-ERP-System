package com.smartmedical.model;

import java.math.BigDecimal;

public class Customer {
    private int customerId;
    private String shopName;
    private String ownerName;
    private String phone;
    private String gstin;
    private String address;
    private int routeId;
    private String routeName; // joined
    private BigDecimal creditLimit;
    private BigDecimal outstandingBalance;
    private boolean active;

    public Customer() {}

    public int getCustomerId()                           { return customerId; }
    public void setCustomerId(int customerId)            { this.customerId = customerId; }
    public String getShopName()                          { return shopName; }
    public void setShopName(String shopName)             { this.shopName = shopName; }
    public String getOwnerName()                         { return ownerName; }
    public void setOwnerName(String ownerName)           { this.ownerName = ownerName; }
    public String getPhone()                             { return phone; }
    public void setPhone(String phone)                   { this.phone = phone; }
    public String getGstin()                             { return gstin; }
    public void setGstin(String gstin)                   { this.gstin = gstin; }
    public String getAddress()                           { return address; }
    public void setAddress(String address)               { this.address = address; }
    public int getRouteId()                              { return routeId; }
    public void setRouteId(int routeId)                  { this.routeId = routeId; }
    public String getRouteName()                         { return routeName; }
    public void setRouteName(String routeName)           { this.routeName = routeName; }
    public BigDecimal getCreditLimit()                   { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit)   { this.creditLimit = creditLimit; }
    public BigDecimal getOutstandingBalance()            { return outstandingBalance; }
    public void setOutstandingBalance(BigDecimal b)      { this.outstandingBalance = b; }
    public boolean isActive()                            { return active; }
    public void setActive(boolean active)                { this.active = active; }

    public boolean isCreditLimitExceeded() {
        return outstandingBalance != null && creditLimit != null
                && outstandingBalance.compareTo(creditLimit) >= 0;
    }
}
