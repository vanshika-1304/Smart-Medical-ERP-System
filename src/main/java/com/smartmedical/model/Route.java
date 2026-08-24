package com.smartmedical.model;

import java.sql.Timestamp;

public class Route {
    private int routeId;
    private String routeName;
    private String area;
    private int salesmanId;
    private String salesmanName;
    private boolean active;
    private Timestamp createdAt;
    private int createdBy;

    public Route() {}

    public int getRouteId()                      { return routeId; }
    public void setRouteId(int r)                { this.routeId = r; }
    public String getRouteName()                 { return routeName; }
    public void setRouteName(String r)           { this.routeName = r; }
    public String getArea()                      { return area; }
    public void setArea(String area)             { this.area = area; }
    public int getSalesmanId()                   { return salesmanId; }
    public void setSalesmanId(int s)             { this.salesmanId = s; }
    public String getSalesmanName()              { return salesmanName; }
    public void setSalesmanName(String s)        { this.salesmanName = s; }
    public boolean isActive()                    { return active; }
    public void setActive(boolean active)        { this.active = active; }
    public Timestamp getCreatedAt()              { return createdAt; }
    public void setCreatedAt(Timestamp t)        { this.createdAt = t; }
    public int getCreatedBy()                    { return createdBy; }
    public void setCreatedBy(int c)              { this.createdBy = c; }
}
