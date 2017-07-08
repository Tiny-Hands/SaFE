package com.vysh.subairoma;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;

/**
 * Created by Vishal on 6/12/2017.
 */

public class ActivitySplash extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (Exception ex) {
                    Log.d("mylog", "Sleeping exception");
                } finally {
                    SQLDatabaseHelper dbHelper = new SQLDatabaseHelper(ActivitySplash.this);
                    dbHelper.getWritableDatabase();
                    Intent intent = new Intent(ActivitySplash.this, ActivityRegister.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        }).start();
    }
}
