package com.vysh.subairoma;

import android.app.Application;

/**
 * Created by Vishal on 6/28/2017.
 */

public class ApplicationClass extends Application {

    private int userId, migrantId;
    private static ApplicationClass mInstance;

    public static ApplicationClass getInstance() {
        return mInstance;
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
        mInstance = this;
    }
}
