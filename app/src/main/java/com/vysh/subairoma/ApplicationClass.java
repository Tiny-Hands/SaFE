package com.vysh.subairoma;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Vishal on 6/28/2017.
 */

public class ApplicationClass extends Application {

    ///private final String APIROOT = "http://192.168.1.25";
    private final String appTOKEN = "aswqssaadwz1234fs3sqwawf456v";
    private final String APIROOT = "http://subairoma-mysql.azurewebsites.net";
    private int userId = -1, migrantId = -1;
    private String userType;
    private static ApplicationClass mInstance;

    public static ApplicationClass getInstance() {
        return mInstance;
    }

    public String getAPIROOT() {
        return APIROOT;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
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

    public String getAppToken() {
        return appTOKEN;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        mInstance = this;
        new FlurryAgent.Builder()
                .withLogEnabled(true)
                .build(this, "2W5WGBPYKS7ZXN2Y684D");
    }
}
