package com.trichain.wifishare.model;

public class WifiModel {
    String ssid;
    String uuid;
    String password;
    String security;
    Double lat;
    Double longt;
    Integer level;
    boolean isFree;
    boolean isSecured;
    boolean isConnected;

    public WifiModel() {
    }

    public WifiModel(String ssid) {
        this.ssid = ssid;
        isFree=true;
        isConnected=false;
        isSecured=true;
    }
    public WifiModel(String ssid, String uuid, String password, String security) {
        this.ssid = ssid;
        this.uuid = uuid;
        this.password = password;
        this.security = security;
    }

    public WifiModel(String ssid, String uuid, String password, String security, Double lat, Double longt) {
        this.ssid = ssid;
        this.uuid = uuid;
        this.password = password;
        this.security = security;
        this.lat = lat;
        this.longt = longt;
    }

    public WifiModel(String ssid, String uuid, String password, String security, Double lat, Double longt, boolean isFree, boolean isSecured) {
        this.ssid = ssid;
        this.uuid = uuid;
        this.password = password;
        this.security = security;
        this.lat = lat;
        this.longt = longt;
        this.isFree = isFree;
        this.isSecured = isSecured;
    }


    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSecurity() {
        return security;
    }

    public void setSecurity(String security) {
        this.security = security;
    }

    public Double getLat() {
        if (lat==null){
            return (double)0;
        }
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLongt() {
        if (longt==null){
            return (double)0;
        }
        return longt;
    }

    public void setLongt(Double longt) {
        this.longt = longt;
    }

    public boolean isFree() {
        return isFree;
    }

    public void setFree(boolean free) {
        isFree = free;
    }

    public boolean isSecured() {
        return isSecured;
    }

    public void setSecured(boolean secured) {
        isSecured = secured;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }

    @Override
    public String toString() {
        return "WifiModel{" +
                "ssid='" + ssid + '\'' +
                ", uuid='" + uuid + '\'' +
                ", password='" + password + '\'' +
                ", security='" + security + '\'' +
                ", lat=" + lat +
                ", longt=" + longt +
                '}';
    }

}
