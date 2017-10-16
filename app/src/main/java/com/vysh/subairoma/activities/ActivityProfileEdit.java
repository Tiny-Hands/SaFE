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
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.SharedPrefKeys;
import com.vysh.subairoma.models.MigrantModel;
import com.vysh.subairoma.utils.CustomTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;

/**
 * Created by Vishal on 8/25/2017.
 */

public class ActivityProfileEdit extends AppCompatActivity implements View.OnClickListener {

    final String apiUpdateUser = "/updateuser.php";
    final String apiUpdateMigrant = "/updatemigrant.php";
    final String apiaddFbId = "/savefbid.php";

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
    @BindView(R.id.login_button_edit)
    LoginButton loginButton;
    @BindView(R.id.login_button)
    LoginButton loginButtonToHide;
    @BindView(R.id.tvOR)
    TextView tvOr;

    CallbackManager callbackManager;

    int userType = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        userType = getIntent().getIntExtra("userType", -1);

        //tvHint.setText("Edit Details");
        btnAlreadyRegistered.setVisibility(GONE);
        btnNext.setOnClickListener(this);
        if (userType == 1) {
            tvTitle.setText("EDIT MIGRANT");
            if (ApplicationClass.getInstance().getUserId() != -1) {
                loginButton.setVisibility(GONE);
                loginButtonToHide.setVisibility(GONE);
                tvOr.setVisibility(GONE);
            } else
                setUpFBLogin();
            getData();
        } else if (userType == 0) {
            tvTitle.setText("EDIT PROFILE");
            setUpUserData();
            setUpFBLogin();
        }
    }

    private void setUpUserData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE);
        etNumber.setText(sharedPreferences.getString(SharedPrefKeys.userPhone, ""));
        etName.setText(sharedPreferences.getString(SharedPrefKeys.userName, ""));
        String sex = sharedPreferences.getString(SharedPrefKeys.userSex, "");
        String age = sharedPreferences.getString(SharedPrefKeys.userAge, "");
        etAge.setText(age);
        if (sex.equalsIgnoreCase("male"))
            rbMale.setChecked(true);
        else if (sex.equalsIgnoreCase("female"))
            rbFemale.setChecked(false);
    }

    private void setUpFBLogin() {
        loginButtonToHide.setVisibility(GONE);
        loginButton.setVisibility(View.VISIBLE);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT
                , LinearLayout.LayoutParams.WRAP_CONTENT);
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
        layoutParams.setMargins(0, (int) px, 0, 0);
        loginButton.setReadPermissions("email");
        loginButton.setLayoutParams(layoutParams);
        callbackManager = CallbackManager.Factory.create();
        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("mylog", "Successful, User ID: " + Profile.getCurrentProfile().getId());
                addFbIDToUID(Profile.getCurrentProfile().getId());
            }

            @Override
            public void onCancel() {
                Log.d("mylog", "Canceled");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.d("mylog", "Error: " + exception.toString());
            }
        });
    }

    private void addFbIDToUID(String id) {
        final String fbId = id;
        String api = ApplicationClass.getInstance().getAPIROOT() + apiaddFbId;
        final ProgressDialog progressDialog = new ProgressDialog(ActivityProfileEdit.this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Associating with Facebook Account...");
        progressDialog.show();
        StringRequest checkRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Log.d("mylog", "response : " + response);
                try {
                    JSONObject jsonRes = new JSONObject(response);
                    boolean error = jsonRes.getBoolean("error");
                    String message = jsonRes.getString("message");
                    if (error) {
                        showSnackbar(message);
                    } else
                        showSnackbar("Facebook Account Added Successfully");

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("mylog", "Error in check FB connection: " + e.toString());
                }
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
                params.put("fb_id", fbId);
                if (userType == 1) {
                    params.put("user_type", userType + "");
                    params.put("uid", ApplicationClass.getInstance().getMigrantId() + "");
                } else {
                    int userType = 0;
                    params.put("user_type", userType + "");
                    params.put("uid", ApplicationClass.getInstance().getUserId() + "");
                }
                for (Object obj : params.keySet()) {
                    Log.d("mylog", "KEY: " + obj + " VALUE: " + params.get(obj));
                }
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(ActivityProfileEdit.this);
        checkRequest.setShouldCache(false);
        queue.add(checkRequest);
    }

    private void getData() {
        //Get Data
        MigrantModel migrantModel = new SQLDatabaseHelper(ActivityProfileEdit.this).getMigrantDetails();
        etName.setText(migrantModel.getMigrantName());
        etNumber.setText(migrantModel.getMigrantPhone());
        Log.d("mylog", "Age: " + migrantModel.getMigrantAge());

        //Appending empty string as if it's Int, it's considered resource id
        etAge.setText(migrantModel.getMigrantAge() + "");
        String sex = migrantModel.getMigrantSex();
        if (sex.equalsIgnoreCase("male")) {
            rbMale.setChecked(true);
        } else rbFemale.setChecked(true);
    }

    @Override
    public void onClick(View v) {
        if (validateData()) {
            updateUser();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void updateUser() {
        String API = "";
        int id = -2;
        if (userType == 0) {
            API = ApplicationClass.getInstance().getAPIROOT() + apiUpdateUser;
            id = ApplicationClass.getInstance().getUserId();
        } else if (userType == 1) {
            API = ApplicationClass.getInstance().getAPIROOT() + apiUpdateMigrant;
            id = ApplicationClass.getInstance().getMigrantId();
        }
        String name = etName.getText().toString();
        String age = etAge.getText().toString();
        String number = etNumber.getText().toString();
        String sex = "";
        if (rbFemale.isChecked()) sex = "female";
        else if (rbMale.isChecked()) sex = "male";
        sendToServer(API, id, name, age, number, sex);
    }

    private void sendToServer(String API, final int id, final String name, final String age,
                              final String number, final String sex) {
        final ProgressDialog progressDialog = new ProgressDialog(ActivityProfileEdit.this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Updating...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        StringRequest saveRequest = new StringRequest(Request.Method.POST, API, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                try {
                    Log.d("mylog", "response : " + response);
                    JSONObject jsonObject = new JSONObject(response);
                    Boolean error = jsonObject.getBoolean("error");
                    if(!error){
                        if(userType == 0){
                            //Save the new user info in SharedPrefs
                            SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(SharedPrefKeys.userName, name);
                            editor.putString(SharedPrefKeys.userPhone, number);
                            editor.putString(SharedPrefKeys.userSex, sex);
                            editor.putString(SharedPrefKeys.userAge, age);
                            editor.putString(SharedPrefKeys.userType, "helper");
                            editor.commit();
                        }
                        Intent intent = new Intent(ActivityProfileEdit.this, ActivityMigrantList.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                    else{
                        showSnackbar("Failed to update User");
                    }
                } catch (JSONException e) {
                    Log.d("mylog", "Error updating: " + e.toString());
                }
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
                params.put("phone_number", number);
                params.put("age", age);
                params.put("gender", sex);
                if (userType == 0) {
                    params.put("user_id", id + "");
                } else if (userType == 1) {
                    params.put("migrant_id", id + "");
                    params.put("user_id", ApplicationClass.getInstance().getUserId() + "");
                }
                for(Object obj: params.keySet()){
                    Log.d("mylog", "Key: " + obj + " Val: " + params.get(obj));
                }
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(ActivityProfileEdit.this);
        saveRequest.setShouldCache(false);
        queue.add(saveRequest);
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

    private void showSnackbar(String msg) {
        Snackbar snack = Snackbar.make(rootLayout, msg, Snackbar.LENGTH_LONG);
        View view = snack.getView();
        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= 17)
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        snack.show();
    }

}