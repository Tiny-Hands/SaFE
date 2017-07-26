package com.vysh.subairoma;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Vishal on 6/17/2017.
 */

public class ActivityOTPVerification extends AppCompatActivity implements View.OnClickListener {
    @BindView(R.id.btnVerify)
    Button btnVerify;
    @BindView(R.id.btnChangeNumber)
    Button btnChangeNumber;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ButterKnife.bind(this);
        btnVerify.setOnClickListener(this);
        btnChangeNumber.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnVerify:
                Intent intent = new Intent(ActivityOTPVerification.this, ActivityMigrantList.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                break;
            case R.id.btnChangeNumber:
                Intent intent1 = new Intent(ActivityOTPVerification.this, ActivityRegister.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent1);
                break;
        }
    }
}
