package com.vysh.subairoma.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.SharedPrefKeys;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Vishal on 6/17/2017.
 */

public class ActivityOTPVerification extends AppCompatActivity implements View.OnClickListener {
    final String apiOTP = "/twiliosender.php";
    final String apiURLHelper = "/saveuser.php";
    final String apiURLMigrant = "/savemigrant.php";

    @BindView(R.id.btnVerify)
    Button btnVerify;
    @BindView(R.id.btnChangeNumber)
    Button btnChangeNumber;
    @BindView(R.id.rlRoot)
    RelativeLayout rootLayout;

    String apiUserRegister;
    String name, phoneNumber, age, gender;
    int uType;
    Boolean isSupervised = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ButterKnife.bind(this);
        btnVerify.setOnClickListener(this);
        btnChangeNumber.setOnClickListener(this);

        //Sending the OTP To the mentioned number;
        //sendOTP();

        Intent intent = getIntent();
        name = intent.getStringExtra("name");
        phoneNumber = intent.getStringExtra("phoneNumber");
        age = intent.getStringExtra("age");
        gender = intent.getStringExtra("gender");
        uType = intent.getIntExtra("userType", -10);
        if (uType == 1) {
            apiUserRegister = ApplicationClass.getInstance().getAPIROOT() + apiURLMigrant;
            isSupervised = intent.getBooleanExtra("isSupervised", false);
        } else
            apiUserRegister = ApplicationClass.getInstance().getAPIROOT() + apiURLHelper;

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnVerify:
                //Validate the OTP Here
                registerUser(apiUserRegister);
                /*
                Intent intent = new Intent(ActivityOTPVerification.this, ActivityMigrantList.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                */
                break;
            case R.id.btnChangeNumber:
                Intent intent1 = new Intent(ActivityOTPVerification.this, ActivityRegister.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent1);
                break;
        }
    }

    private void sendOTP() {
        String api = apiUserRegister = ApplicationClass.getInstance().getAPIROOT() + apiOTP;
        final ProgressDialog progressDialog = new ProgressDialog(ActivityOTPVerification.this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Sending OTP...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        StringRequest saveRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Log.d("mylog", "response : " + response);
                parseResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                String err = error.toString();
                Log.d("mylog", "error : " + err);
                if (!err.isEmpty() && err.contains("TimeoutError"))
                    showSnackbar("Failed to connect to server :(");
                else
                    showSnackbar(error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                long otpCode = generateOTP();
                HashMap<String, String> params = new HashMap<>();
                params.put("otp", otpCode + "");
                params.put("phone_number", phoneNumber);
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(ActivityOTPVerification.this);
        saveRequest.setShouldCache(false);
        queue.add(saveRequest);
    }

    private long generateOTP() {
        return 1;
    }

    private void registerUser(String api) {
        Log.d("mylog", "API called: " + api);
        final ProgressDialog progressDialog = new ProgressDialog(ActivityOTPVerification.this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Registering...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        StringRequest saveRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Log.d("mylog", "response : " + response);
                parseResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                String err = error.toString();
                Log.d("mylog", "error : " + err);
                if (!err.isEmpty() && err.contains("TimeoutError"))
                    showSnackbar("Failed to connect to server :(");
                else
                    showSnackbar(error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("full_name", name);
                params.put("phone_number", phoneNumber);
                params.put("age", age);
                params.put("gender", gender);

                //Flow will not enter these two unless a migrant is being registered
                if (uType == 1 && !isSupervised) {
                    params.put("user_id", "-1");
                } else if (isSupervised)
                    params.put("user_id", ApplicationClass.getInstance().getUserId() + "");
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(ActivityOTPVerification.this);
        saveRequest.setShouldCache(false);
        queue.add(saveRequest);
    }

    public void showSnackbar(String msg) {
        Snackbar snack = Snackbar.make(rootLayout, msg, Snackbar.LENGTH_LONG);
        View view = snack.getView();
        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= 17)
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        snack.show();
    }

    private void parseResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            Boolean error = jsonResponse.getBoolean("error");
            if (!error) {
                SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (uType == 1) {
                    ApplicationClass.getInstance().setUserId(-1);
                    editor.putString(SharedPrefKeys.userType, "migrant");
                    int mig_id = jsonResponse.getInt("migrant_id");
                    Log.d("mylog", "Migrant ID: " + mig_id);
                    ApplicationClass.getInstance().setMigrantId(mig_id);
                    editor.putInt(SharedPrefKeys.userId, mig_id);
                    editor.commit();
                    new SQLDatabaseHelper(ActivityOTPVerification.this).insertResponseTableData(gender, -2, -1, mig_id, "mg_sex");
                    //Do Next Step Now
                    Intent intent = new Intent(ActivityOTPVerification.this, ActivityMigrantList.class);
                    intent.putExtra("migrantmode", true);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                    return;
                }
                //Means the registered person was Helper
                else {
                    //startOTPActivity();
                    int user_id = jsonResponse.getInt("user_id");
                    Log.d("mylog", "Saving user ID: " + user_id);
                    ApplicationClass.getInstance().setUserId(user_id);
                    //Parse Other responses and save in SharedPref
                    editor.putString(SharedPrefKeys.userName, name);
                    editor.putInt(SharedPrefKeys.userId, user_id);
                    editor.putString(SharedPrefKeys.userPhone, phoneNumber);
                    editor.putString(SharedPrefKeys.userSex, gender);
                    editor.putString(SharedPrefKeys.userAge, age);
                    editor.putString(SharedPrefKeys.userType, "helper");
                    editor.commit();

                    Intent intent = new Intent(ActivityOTPVerification.this, ActivityRegister.class);
                    intent.putExtra("migrantmode", true);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            } else {
                String message = jsonResponse.getString("message");
                showSnackbar(message);
            }
        } catch (JSONException e) {
            Log.d("mylog", "Error in parsing: " + e.getMessage());
        }
    }
}
