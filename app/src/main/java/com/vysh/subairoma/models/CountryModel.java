package com.vysh.subairoma.models;

/**
 * Created by Vishal on 6/24/2017.
 */

public class CountryModel {
    String countryId, countryName;
    int countrySatus, countryBlacklist, order;

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getCountryId() {
        return countryId;
    }

    public void setCountryId(String countryId) {
        this.countryId = countryId;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public int getCountrySatus() {
        return countrySatus;
    }

    public void setCountrySatus(int countrySatus) {
        this.countrySatus = countrySatus;
    }

    public int getCountryBlacklist() {
        return countryBlacklist;
    }

    public void setCountryBlacklist(int countryBlacklist) {
        this.countryBlacklist = countryBlacklist;
    }
}
