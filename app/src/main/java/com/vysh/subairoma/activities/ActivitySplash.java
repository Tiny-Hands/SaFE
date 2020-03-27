package com.vysh.subairoma.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;
import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.SharedPrefKeys;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;

/**
 * Created by Vishal on 6/12/2017.
 */

public class ActivitySplash extends AppCompatActivity {
    private long startTime, sleepTime = 2000;
    private String lang;

    SharedPreferences sp;

    SQLDatabaseHelper dbHelper;
    Thread sleepThread;

    @BindView(R.id.progressCircle)
    LinearLayout progressBar;
    @BindView(R.id.tvLoadingNep)
    TextView tvLoadingNepali;
    @BindView(R.id.tvLoadingEng)
    TextView tvLoadingEng;
    @BindView(R.id.llLang)
    LinearLayout llLang;
    @BindView(R.id.ibEn)
    ImageButton ibEn;
    @BindView(R.id.ibNp)
    ImageButton ibNp;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE);
        int currVersion = sp.getInt(SharedPrefKeys.dbVersion, -1);
        dbHelper = SQLDatabaseHelper.getInstance(ActivitySplash.this);
        if (dbHelper.getVersion() > currVersion && currVersion != -1) {
            Log.d("mylog", "Greater Version dropping");
            dbHelper.dropDB();
            //Remove User data
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(SharedPrefKeys.userId, -10);
            editor.putString(SharedPrefKeys.userType, "");
            editor.commit();
            finish();
            Intent intent = new Intent(ActivitySplash.this, ActivitySplash.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }
        String setLang = sp.getString(SharedPrefKeys.lang, "");
        if (setLang.equalsIgnoreCase("en")) {
            setLocale("en");
            ApplicationClass.getInstance().setLocale(setLang);
            FlurryAgent.logEvent("locale_english_selection");
        } else {
            setLocale("np");
            ApplicationClass.getInstance().setLocale(setLang);
            FlurryAgent.logEvent("locale_nepal_selection");
        }

        setContentView(R.layout.activity_splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Fabric.with(this, new Crashlytics());
        ButterKnife.bind(this);

        if (!setLang.isEmpty())
            hideLangOptions();
        ibEn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLocale("en");
                lang = "en";
                //hideLangOptions();
                startNextActivity();

                SharedPreferences.Editor editor = sp.edit();
                editor.putString(SharedPrefKeys.lang, "en");
                editor.commit();
            }
        });

        ibNp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLocale("np");
                lang = "np";
                //hideLangOptions();
                startNextActivity();

                SharedPreferences.Editor editor = sp.edit();
                editor.putString(SharedPrefKeys.lang, "np");
                editor.commit();
            }
        });

        startTime = System.currentTimeMillis();
        sleepThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (sleepTime > 0)
                        Thread.sleep(sleepTime);
                } catch (Exception ex) {
                    Log.d("mylog", "Sleeping exception: " + ex.toString());
                } finally {
                    if (!setLang.isEmpty())
                        startNextActivity();
                }
            }
        });
        sleepThread.start();
    }

    private void startNextActivity() {
        if (sp.getInt(SharedPrefKeys.userId, -10) > 0) {
            ApplicationClass.getInstance().setSafeUserId(sp.getInt(SharedPrefKeys.userId, -10));
            Intent intent = new Intent(ActivitySplash.this, ActivityMigrantList.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else {
            Intent intent = new Intent(ActivitySplash.this, ActivityRegister.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        if (lang.equalsIgnoreCase("np"))
            tvLoadingNepali.setVisibility(View.VISIBLE);
        else if (lang.equalsIgnoreCase("en"))
            tvLoadingEng.setVisibility(View.VISIBLE);
    }

    private void startInfoActivity() {
        Intent intent = new Intent(ActivitySplash.this, ActivityInfoSlider.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void hideLangOptions() {
        //showLoading();
        llLang.setVisibility(View.GONE);
    }
}
