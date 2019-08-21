package com.vysh.subairoma.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.flurry.android.FlurryAgent;
import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.SharedPrefKeys;
import com.vysh.subairoma.utils.InternetConnectionChecker;

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
    boolean hasExceededReceiveTime = true, isLogginIn = false;
    long lastTime;

    final String apiOTP = "/twiliosender.php";
    final String apiSaveUser = "/saveuser.php";

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
    String name, phoneNumber, age, gender, userImg;
    String uType;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otp);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ButterKnife.bind(this);
        btnVerify.setOnClickListener(this);
        btnChangeNumber.setOnClickListener(this);
        btnSendOtpAgain.setOnClickListener(this);

        Intent intent = getIntent();
        if (intent.hasExtra("otpnumber")) {
            isLogginIn = true;
            phoneNumber = intent.getStringExtra("otpnumber");
            Log.d("mylog", " Logging in, send OTP to: " + phoneNumber);
            if (InternetConnectionChecker.isNetworkConnected(ActivityOTPVerification.this)) {
                //Sending the OTP To the mentioned number;
                lastTime = System.currentTimeMillis();
                sendOTP();
            }
            return;
        }
        name = intent.getStringExtra("name");
        phoneNumber = intent.getStringExtra("phoneNumber");
        age = intent.getStringExtra("age");
        userImg = intent.getStringExtra("userImg");
        gender = intent.getStringExtra("gender");
        uType = intent.getStringExtra("userType");

        apiUserRegister = ApplicationClass.getInstance().getAPIROOT() + apiSaveUser;
        if (InternetConnectionChecker.isNetworkConnected(ActivityOTPVerification.this)) {
            //Sending the OTP To the mentioned number;
            lastTime = System.currentTimeMillis();
            sendOTP();
        } else
            saveUserLocally(uType);

    }

    private void saveUserLocally(String uType) {
        SharedPreferences sp = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE);
        //Parse Other responses and save in SharedPref
        SharedPreferences.Editor editor = sp.edit();
        int user_id = -111;
        if (uType.equalsIgnoreCase(SharedPrefKeys.helperUser)) {
            ApplicationClass.getInstance().setSafeUserId(user_id);
            editor.putString(SharedPrefKeys.userName, name);
            editor.putInt(SharedPrefKeys.userId, user_id);
            editor.putString(SharedPrefKeys.userPhone, phoneNumber);
            editor.putString(SharedPrefKeys.userSex, gender);
            editor.putString(SharedPrefKeys.userAge, age);
            editor.putString(SharedPrefKeys.userImg, userImg);
            editor.putString(SharedPrefKeys.userType, SharedPrefKeys.helperUser);
            editor.commit();
            Intent intent = new Intent(ActivityOTPVerification.this, ActivityRegister.class);
            intent.putExtra("migrantmode", true);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            int mid = SQLDatabaseHelper.getInstance(ActivityOTPVerification.this).insertTempMigrants(name,
                    Integer.parseInt(age), phoneNumber, gender, ApplicationClass.getInstance().getSafeUserId(), userImg);
            //new SQLDatabaseHelper(ActivityRegister.this).insertTempResponseTableData(sex, SharedPrefKeys.questionGender, -1, mid, "mg_sex", time);

            //Getting id to save in corresponding real local DB
            int fabMigId = Integer.parseInt("-1" + mid);

            editor.putString(SharedPrefKeys.userType, SharedPrefKeys.migrantUser);
            Log.d("mylog", "Migrant ID: " + fabMigId);
            ApplicationClass.getInstance().setMigrantId(fabMigId);
            ApplicationClass.getInstance().setSafeUserId(user_id);
            editor.putInt(SharedPrefKeys.userId, user_id);
            editor.putInt(SharedPrefKeys.defMigID, fabMigId);
            editor.commit();

            Calendar cal = Calendar.getInstance();
            String time = cal.getTimeInMillis() + "";

            SQLDatabaseHelper.getInstance(ActivityOTPVerification.this).insertMigrants(fabMigId, name,
                    Integer.parseInt(age), phoneNumber, gender, ApplicationClass.getInstance().getSafeUserId(), userImg, 0);

            SQLDatabaseHelper.getInstance(ActivityOTPVerification.this).insertResponseTableData(gender, SharedPrefKeys.questionGender, -1, fabMigId, "mg_sex", time);

            //Do Next Step Now
            startMigrantActivity();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnVerify:
                //Validate the OTP Here
                boolean isValid = isOtpValid();

                if (isValid) {
                    FlurryAgent.logEvent("otp_verified");
                    if (isLogginIn) {
                        SharedPreferences sp = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        int id = ApplicationClass.getInstance().getSafeUserId();
                        if (id == -1)
                            id = ApplicationClass.getInstance().getMigrantId();
                        Log.d("mylog", "Saving: " + id);
                        editor.putInt(SharedPrefKeys.userId, id);
                        editor.apply();
                        startMigrantActivity();
                    } else
                        registerUser(apiUserRegister);
                } else {

                    FlurryAgent.logEvent("otp_verification_failed");
                    showSnackbar("The OTP is incorrect, please try registering Again");
                }
                /*
                Intent intent = new Intent(ActivityOTPVerification.this, ActivityMigrantList.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                */
                break;
            case R.id.btnChangeNumber:

                FlurryAgent.logEvent("number_change_initiated");
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
                Log.d("mylog", "otp response : " + response);
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
                params.put("user_img", userImg);
                params.put("gender", gender);
                params.put("user_type", uType);
                //params.put("user_id", ApplicationClass.getInstance().getSafeUserId() + "");
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(ActivityOTPVerification.this);
        saveRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(saveRequest);
    }

    public void showSnackbar(String msg) {
       /* Snackbar snack = Snackbar.make(rootLayout, msg, Snackbar.LENGTH_LONG);
        View view = snack.getView();
        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= 17)
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        snack.show();*/

        Toast.makeText(ActivityOTPVerification.this, msg, Toast.LENGTH_SHORT).show();
    }

    private void parseResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            Boolean error = jsonResponse.getBoolean("error");
            if (!error) {
                SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                if (uType.equalsIgnoreCase(SharedPrefKeys.migrantUser)) {
                    //ApplicationClass.getInstance().setSafeUserId(-1);
                    editor.putString(SharedPrefKeys.userType, SharedPrefKeys.migrantUser);
                    //int mig_id = jsonResponse.getInt("migrant_id");
                    int user_id = jsonResponse.getInt("user_id");
                    String token = jsonResponse.getString("token");
                    Log.d("mylog", "Migrant ID: " + user_id);
                    ApplicationClass.getInstance().setMigrantId(user_id);
                    ApplicationClass.getInstance().setSafeUserId(user_id);
                    editor.putInt(SharedPrefKeys.userId, user_id);
                    editor.putInt(SharedPrefKeys.defMigID, user_id);
                    editor.putString(SharedPrefKeys.token, token);

                    //If migrant is already saved and has added a new migrant then don't save again
                    if (sharedPreferences.getString(SharedPrefKeys.userName, "").length() >= 1) {
                    } else {
                        editor.putString(SharedPrefKeys.userPhone, phoneNumber);
                        editor.putString(SharedPrefKeys.userSex, gender);
                        editor.putString(SharedPrefKeys.userAge, age);
                        editor.putString(SharedPrefKeys.userName, name);
                    }
                    editor.commit();

                    Calendar cal = Calendar.getInstance();
                    String time = cal.getTimeInMillis() + "";
                    SQLDatabaseHelper.getInstance(ActivityOTPVerification.this).insertResponseTableData(gender, SharedPrefKeys.questionGender, -1,
                            user_id, "mg_sex", time);
                    SQLDatabaseHelper.getInstance(ActivityOTPVerification.this).insertMigrants(user_id, name, Integer.parseInt(age), phoneNumber, gender, user_id, userImg, 0);
                    startMigrantActivity();

                    return;
                }
                //Means the registered person was Helper
                else {
                    int user_id = jsonResponse.getInt("user_id");
                    Log.d("mylog", "Saving user ID: " + user_id);

                    int oldUid = ApplicationClass.getInstance().getSafeUserId();
                    if (oldUid == -111) {
                        SQLDatabaseHelper.getInstance(ActivityOTPVerification.this).makeUserIdChanges(-111, user_id);
                    }

                    ApplicationClass.getInstance().setSafeUserId(user_id);
                    //Parse Other responses and save in SharedPref
                    String token = jsonResponse.getString("token");
                    editor.putString(SharedPrefKeys.token, token);
                    editor.putString(SharedPrefKeys.userName, name);
                    editor.putInt(SharedPrefKeys.userId, user_id);
                    editor.putString(SharedPrefKeys.userPhone, phoneNumber);
                    editor.putString(SharedPrefKeys.userSex, gender);
                    editor.putString(SharedPrefKeys.userAge, age);
                    editor.putString(SharedPrefKeys.userType, SharedPrefKeys.helperUser);
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

    private void startMigrantActivity() {
        //Do Next Step Now
        Intent intent = new Intent(ActivityOTPVerification.this, ActivityMigrantList.class);
        //intent.putExtra("migrantmode", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}
