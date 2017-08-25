package com.vysh.subairoma.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vysh.subairoma.R;
import com.vysh.subairoma.utils.CustomTextView;

import butterknife.BindView;

/**
 * Created by Vishal on 8/25/2017.
 */

public class ActivityProfileEdit extends AppCompatActivity implements View.OnClickListener {

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
    @BindView(R.id.rbMale)
    RadioButton rbMale;
    @BindView(R.id.rbFemale)
    RadioButton rbFemale;
    @BindView(R.id.rlRoot)
    RelativeLayout rootLayout;
    @BindView(R.id.btnAlreadyRegistered)
    Button btnAlreadyRegistered;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        tvHint.setText("Edit Details");
        btnAlreadyRegistered.setVisibility(View.GONE);
        getData();
    }

    private void getData() {
        //Get Data
        etNumber.setText("Saved Number");
        etName.setText("Saved Number");
        etAge.setText("Saved Age");
        tvTitle.setText("PROFILE");
        btnNext.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

    }
}
