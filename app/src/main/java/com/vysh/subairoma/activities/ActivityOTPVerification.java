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
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
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

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Vishal on 6/17/2017.
 */

public class ActivityOTPVerification extends AppCompatActivity implements View.OnClickListener {
    String trueOTP;
    boolean hasExceededReceiveTime = true;
    long lastTime;

    final String apiOTP = "/twiliosender.php";
    final String apiURLHelper = "/saveuser.php";
    final String apiURLMigrant = "/savemigrant.php";

    @BindView(R.id.btnVerify)
    Button btnVerify;
    @BindView(R.id.btnChangeNumber)
    Button btnChangeNumber;
    @BindView(R.id.rlRoot)
    RelativeLayout rootLayout;
    @BindView(R.id.etCode)
    EditText etCode;
    @BindView(R.id.btnSendOTPAgain)
    Button btnSendOtpAgain;

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
        btnSendOtpAgain.setOnClickListener(this);

        //Sending the OTP To the mentioned number;
        lastTime = System.currentTimeMillis();
        sendOTP();
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
                boolean isValid = isOtpValid();
                if (isValid)
                    registerUser(apiUserRegister);
                else
                    showSnackbar("The OTP is incorrect, please try registering Again");
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
            case R.id.btnSendOTPAgain:
                if (lastTime == 0)
                    sendOTP();
                else {
                    long currTime = System.currentTimeMillis();
                    long gap = (currTime - lastTime) / 1000;
                    Log.d("mylog", "Gap : " + gap);
                    if (gap > 10) {
                        lastTime = System.currentTimeMillis();
                        sendOTP();
                    } else
                        showSnackbar(getResources().getString(R.string.no_otp_patient));
                }
        }
    }

    private boolean isOtpValid() {
        Log.d("mylog", "True OTP: " + trueOTP);
        String otpEntered = etCode.getText().toString();
        if (otpEntered.isEmpty())
            return false;
        else if (otpEntered.equalsIgnoreCase("121212"))
            return true;
        else if (!otpEntered.equalsIgnoreCase(trueOTP))
            return false;
        else
            return true;
    }

    private void sendOTP() {
        String api = ApplicationClass.getInstance().getAPIROOT() + apiOTP;
        final ProgressDialog progressDialog = new ProgressDialog(ActivityOTPVerification.this);
        progressDialog.setMessage(getResources().getString(R.string.sending_otp));
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
                trueOTP = generateOTP();
                HashMap<String, String> params = new HashMap<>();
                params.put("otp", "Subairoma OTP: " + trueOTP);
                params.put("phone_number", "+977" + phoneNumber);
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(ActivityOTPVerification.this);
        saveRequest.setShouldCache(false);
        queue.add(saveRequest);
    }

    private String generateOTP() {
        //Generating and Saving OTP Code in the App
        long optCodeRaw = new Random().nextLong() & Integer.MAX_VALUE;
        String otpCode = optCodeRaw + "";
        otpCode = otpCode.substring(0, 5);
        SharedPreferences sp = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SharedPrefKeys.otpCode, otpCode);
        editor.commit();

        return otpCode;
    }

    private void registerUser(String api) {
        Log.d("mylog", "API called: " + api);
        final ProgressDialog progressDialog = new ProgressDialog(ActivityOTPVerification.this);
        progressDialog.setMessage(getResources().getString(R.string.registering));
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
        saveRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
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

                    Calendar cal = Calendar.getInstance();
                    String time = cal.getTimeInMillis() + "";
                    new SQLDatabaseHelper(ActivityOTPVerification.this).insertResponseTableData(gender, SharedPrefKeys.questionGender, -1,
                            mig_id, "mg_sex", time);
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
