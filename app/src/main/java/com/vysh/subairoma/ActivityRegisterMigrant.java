package com.vysh.subairoma;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.login.widget.LoginButton;
import com.flurry.android.FlurryAgent;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;

public class ActivityRegisterMigrant extends AppCompatActivity {

    @BindView(R.id.ivRegister)
    ImageView ivRegister;
    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.btnNext)
    Button btnNext;
    @BindView(R.id.tvTitle)
    TextView tvTitle;
    @BindView(R.id.etName)
    EditText etName;
    @BindView(R.id.etAge)
    EditText etAge;
    @BindView(R.id.etNumber)
    EditText etNumber;
    /*@BindView(R.id.tvHint)
    TextView tvHint;*/
    @BindView(R.id.rbMale)
    RadioButton rbMale;
    @BindView(R.id.rbFemale)
    RadioButton rbFemale;
    @BindView(R.id.rlRoot)
    RelativeLayout rootLayout;
    @BindView(R.id.btnAlreadyRegistered)
    Button btnAlreadyRegistered;
    @BindView(R.id.login_button)
    LoginButton loginButton;
    @BindView(R.id.tvOR)
    TextView tvOr;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_migrant_add);

        ButterKnife.bind(this);
        FlurryAgent.logEvent("register_migrant");
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        loginButton.setVisibility(GONE);
        tvOr.setVisibility(GONE);
    }

    private boolean validateData() {
        if (etName.getText().toString().isEmpty() || etName.getText().toString().length() < 5) {
            etName.setError("Name must be more than 5 characters long");
            return false;
        }
        if (etAge.getText().toString().isEmpty() || etAge.getText().toString().length() != 2) {
            etAge.setError("Age must be between 12 - 90");
            return false;
        }
        if (Integer.parseInt(etAge.getText().toString()) < 12 || Integer.parseInt(etAge.getText().toString()) > 90) {
            etAge.setError("Age must be between 12 - 90");
            return false;
        }
        if (etNumber.getText().toString().isEmpty() || etNumber.getText().toString().length() < 10) {
            etNumber.setError("Please enter a valid mobile number");
            return false;
        }
        return true;
    }
}
