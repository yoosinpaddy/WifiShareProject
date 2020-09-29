package com.trichain.wifishare.model;

import java.io.Serializable;

public class Country implements Serializable {
    private String countryName;
    private Integer usersNumCount;

    public Country(String countryName, Integer usersNumCount) {
        this.countryName = countryName;
        this.usersNumCount = usersNumCount;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public Integer getUsersNumCount() {
        return usersNumCount;
    }

    public void setUsersNumCount(Integer usersNumCount) {
        this.usersNumCount = usersNumCount;
    }
}
