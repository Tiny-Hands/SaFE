package com.vysh.subairoma;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.vysh.subairoma.utils.CustomTextView;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Vishal on 6/15/2017.
 */

public class ActivityRegister extends AppCompatActivity {
    @BindView(R.id.btnNext)
    Button btnNext;
    @BindView(R.id.tvTitle)
    CustomTextView tvTitle;
    @BindView(R.id.etName)
    EditText etName;
    @BindView(R.id.etAge)
    EditText etAge;
    @BindView(R.id.etNumber)
    EditText etNumber;
    @BindView(R.id.tvHint)
    TextView tvHint;

    Boolean userRegistered = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ButterKnife.bind(this);

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!userRegistered) {
                    userRegistered = true;
                    loadMigrantView();
                } else
                    startTilesActivity();
            }
        });
    }

    private void startTilesActivity() {
        Intent intent = new Intent(ActivityRegister.this, ActivityOTPVerification.class);
        startActivity(intent);
    }

    private void loadMigrantView() {
        tvHint.setText("Please enter Migrant's details");
        tvTitle.setText("ADD MIGRANT");
        etName.setHint("Migrant's Name");
        etAge.setHint("Migrant's Age");
        etNumber.setHint("Migrant's Phone Number");
    }
}
