package com.vysh.subairoma.utils;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by Vishal on 4/25/2018.
 */

public class InternetConnectionChecker {
    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}
