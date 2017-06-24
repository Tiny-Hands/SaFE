package com.vysh.subairoma.models;

/**
 * Created by Vishal on 6/24/2017.
 */

public class CountryModel {
    String countryId, countryName, countrySatus, countryBlacklist;

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

    public String getCountrySatus() {
        return countrySatus;
    }

    public void setCountrySatus(String countrySatus) {
        this.countrySatus = countrySatus;
    }

    public String getCountryBlacklist() {
        return countryBlacklist;
    }

    public void setCountryBlacklist(String countryBlacklist) {
        this.countryBlacklist = countryBlacklist;
    }
}
