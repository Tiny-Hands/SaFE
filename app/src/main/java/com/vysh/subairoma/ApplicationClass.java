package com.vysh.subairoma;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Vishal on 6/28/2017.
 */

public class ApplicationClass extends Application {

    ///private final String APIROOT = "http://192.168.1.25";
    private final String APIROOT = "http://subairoma-mysql.azurewebsites.net";
    private int userId = -1, migrantId = -1;
    private int userType;
    private static ApplicationClass mInstance;

    public static ApplicationClass getInstance() {
        return mInstance;
    }

    public String getAPIROOT() {
        return APIROOT;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getMigrantId() {
        return migrantId;
    }

    public void setMigrantId(int migrantId) {
        this.migrantId = migrantId;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        mInstance = this;
    }
}
