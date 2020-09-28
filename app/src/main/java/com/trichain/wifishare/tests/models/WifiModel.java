package com.trichain.wifishare.tests.models;

public class WifiModel {
    String ssid;
    String uuid;
    String password;
    String security;
    Double lat;
    Double longt;

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
}
