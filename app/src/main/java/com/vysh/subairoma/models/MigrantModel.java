package com.vysh.subairoma.models;

/**
 * Created by Vishal on 6/28/2017.
 */

public class MigrantModel {
    String migrantName, migrantPhone, migrantSex;
    int migrantAge, migrantId, userId;

    public int getMigrantId() {
        return migrantId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setMigrantId(int migrantId) {
        this.migrantId = migrantId;
    }

    public String getMigrantName() {
        return migrantName;
    }

    public void setMigrantName(String migrantName) {
        this.migrantName = migrantName;
    }

    public String getMigrantPhone() {
        return migrantPhone;
    }

    public void setMigrantPhone(String migrantPhone) {
        this.migrantPhone = migrantPhone;
    }

    public String getMigrantSex() {
        return migrantSex;
    }

    public void setMigrantSex(String migrantSex) {
        this.migrantSex = migrantSex;
    }

    public int getMigrantAge() {
        return migrantAge;
    }

    public void setMigrantAge(int migrantAge) {
        this.migrantAge = migrantAge;
    }
}
