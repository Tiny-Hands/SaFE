package com.vysh.subairoma.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.CallbackManager;
import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.SharedPrefKeys;
import com.vysh.subairoma.dialogs.DialogLoginOptions;
import com.vysh.subairoma.dialogs.DialogUsertypeChooser;
import com.vysh.subairoma.models.MigrantModel;
import com.vysh.subairoma.utils.InternetConnectionChecker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Vishal on 6/15/2017.
 */

public class ActivityRegister extends AppCompatActivity {
    final String checkFbId = "/checkfbuid.php";
    final String apiURLMigrant = "/savemigrant.php";
    //For Helper
    final String apiGetAllResponses = "/getallresponses.php";
    //For Migrant
    final String apiGetResponses = "/getresponses.fphp";
    final String apiAlreadyRegistered = "/checkphonenumber.php";
    private final String APIGETMIG = "/getmigrants.php";
    private int gotDetailCount = 0;

    //Usertype = 0 for Helper and 1 for migrant. Order is reversed in Dialogs and Other Methods.
    public int userType;
    Boolean userRegistered = false;
    String sex = "male";

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
    @BindView(R.id.tvOR)
    TextView tvOr;

    public CallbackManager callbackManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //Checking if already logged in on current device
        if (checkUserExists() && !getIntent().hasExtra("migrantmode")) {
            Intent intent = new Intent(ActivityRegister.this, ActivityMigrantList.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }
        ButterKnife.bind(this);
        setUpComponentListeners();
        if (getIntent().hasExtra("migrantmode")) {
            userRegistered = true;
            Log.d("mylog", "Loading migrant view");
            loadMigrantView();
        }

        callbackManager = CallbackManager.Factory.create();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("mylog", "On Activity Result");
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void checkUserRegistration(String number) {
        final String pNumber = number;
        String api = ApplicationClass.getInstance().getAPIROOT() + apiAlreadyRegistered;
        final ProgressDialog progressDialog = new ProgressDialog(ActivityRegister.this);
        //progressDialog.setTitle("Please wait");
        progressDialog.setMessage(getResources().getString(R.string.checking_registration));
        progressDialog.show();
        StringRequest checkRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Log.d("mylog", "response : " + response);
                try {
                    JSONObject jsonRes = new JSONObject(response);
                    boolean error = jsonRes.getBoolean("error");
                    if (error) {
                        showSnackbar(jsonRes.getString("message"));
                        return;
                    } else {
                        getLoggedInUserDetails(jsonRes);
                        //Gets Migrant Details and Saves in DB regardless of User Type
                        getMigrants();
                        //Getting all the saved responses for the user
                        getAllResponses(userType);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("mylog", "Error in getting user_id: " + e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                String err = error.toString();
                Log.d("mylog", "error : " + err);
                if (!err.isEmpty() && err.contains("TimeoutError"))
                    showSnackbar(getResources().getString(R.string.server_noconnect));
                else
                    showSnackbar(error.toString());
            }
        })

        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("number", pNumber);
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(ActivityRegister.this);
        //checkRequest.setShouldCache(false);
        queue.add(checkRequest);
    }

    private void getLoggedInUserDetails(JSONObject jsonRes) {
        int userType;
        try {
            userType = jsonRes.getInt("user_type");
            int id = jsonRes.getInt("user_id");

            SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (userType == 1) {
                ApplicationClass.getInstance().setUserId(id);
                String userName = jsonRes.getString("user_name");
                String userPhone = jsonRes.getString("user_phone");
                String userSex = jsonRes.getString("user_sex");
                String age = jsonRes.getString("user_age");

                //Saving Helper Details and Login Status
                Log.d("mylog", "Saving: " + userName + userPhone + userSex + age);
                editor.putString(SharedPrefKeys.userName, userName);
                editor.putString(SharedPrefKeys.userPhone, userPhone);
                editor.putString(SharedPrefKeys.userSex, userSex);
                editor.putString(SharedPrefKeys.userAge, age);
                editor.putString(SharedPrefKeys.userType, "helper");
                editor.putInt(SharedPrefKeys.userId, id);
                editor.commit();
            } else if (userType == 0) {
                ApplicationClass.getInstance().setMigrantId(id);

                //Saving Migrant ID and Login Status
                editor.putString(SharedPrefKeys.userType, "migrant");
                editor.putInt(SharedPrefKeys.userId, id);
                editor.commit();
            }
        } catch (JSONException e) {
            Log.d("mylog", "Parsing user details error: " + e.toString());
        }
    }

    private void getMigrants() {
        String api = ApplicationClass.getInstance().getAPIROOT() + APIGETMIG;
        final ProgressDialog progressDialog = new ProgressDialog(ActivityRegister.this);
        progressDialog.setMessage(getResources().getString(R.string.getting_mig_details));
        try {
            progressDialog.show();
        } catch (Exception ex) {
            Log.d("mylog", "Exception in progress list: " + ex.getMessage());
        }
        StringRequest getRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    progressDialog.dismiss();
                } catch (Exception ex) {
                    Log.d("mylog", "Exception in progress dis: " + ex.getMessage());
                }
                try {
                    //boolean firstRun = false;
                    parseMigDetailResponse(response);
                    Log.d("mylog", "response : " + response);
                } catch (Exception ex) {
                    Log.d("mylog", "response exception: " + ex.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    progressDialog.dismiss();
                } catch (Exception ex) {
                    Log.d("mylog", "Exception in progress dis error: " + ex.getMessage());
                }
                try {
                    String err = error.toString();
                    Log.d("mylog", "error : " + err);
                    if (!err.isEmpty() && err.contains("TimeoutError"))
                        Toast.makeText(ActivityRegister.this, getResources().getString(R.string.server_noconnect), Toast.LENGTH_SHORT).show();
                    else if (!err.isEmpty() && err.contains("NoConnection")) {
                        Toast.makeText(ActivityRegister.this, getResources().getString(R.string.server_noconnect), Toast.LENGTH_SHORT).show();
                    } else
                        showSnackbar(error.toString());
                } catch (Exception ex) {
                    Log.d("mylog", "Error exception: " + ex.toString());
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                int user_id = ApplicationClass.getInstance().getUserId();
                int mig_id = ApplicationClass.getInstance().getMigrantId();
                Log.d("mylog", "User ID: " + user_id);
                Log.d("mylog", "Mig ID: " + mig_id);
                params.put("user_id", user_id + "");
                params.put("migrant_id", mig_id + "");
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(ActivityRegister.this);
        queue.add(getRequest);
    }

    private void setUpComponentListeners() {
        rbFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rbMale.setChecked(false);
                sex = "female";
            }
        });
        rbMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rbFemale.setChecked(false);
                sex = "male";
            }
        });
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateData()) {
                    Log.d("mylog", "Saving User");
                    saveUser();
                }
            }
        });
        btnAlreadyRegistered.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegistrationDialog();
            }
        });
    }

    private void showRegistrationDialog() {
        DialogLoginOptions dialogLoginOptions = new DialogLoginOptions();
        dialogLoginOptions.show(getFragmentManager(), "alreadyregistered");
    }

    private void saveUser() {
        if (!userRegistered) {
            //Shows user type selection dialog with next button
            //showDisclaimerDialog();
            new DialogUsertypeChooser().show(getFragmentManager(), "utypechooser");
        } else {
            //Parameter 1 is for registering migrant after reading disclaimer
            if (Integer.parseInt(etAge.getText().toString()) < 18) {
                showErrorDialog();
            } else showDisclaimerAndContinue();
        }
    }

    private void showErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRegister.this, R.style.DialogError);
        View errView = LayoutInflater.from(ActivityRegister.this).inflate(R.layout.dialog_error, null);
        TextView errDesc = errView.findViewById(R.id.tvErrorDescription);
        Button btnClose = errView.findViewById(R.id.btnUnderstood);

        builder.setView(errView);
        builder.setCancelable(false);
        errDesc.setText(getString(R.string.under18_message));

        final AlertDialog dialog = builder.show();
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                showDisclaimerAndContinue();
            }
        });
    }

    private void showDisclaimerAndContinue() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_disclaimer, null);
        TextView msg = view.findViewById(R.id.tvDisclaimerContent);
        builder.setView(view);
        builder.setPositiveButton(getResources().getString(R.string.disclaimer_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (userType == 1) {
                    //If it's a helper user registering a migrant, then no need to redirect to OTPActivity,
                    //But Register and then redirect to migrant list.
                    if (userRegistered) {
                        if (InternetConnectionChecker.isNetworkConnected(ActivityRegister.this)) {
                            String api = ApplicationClass.getInstance().getAPIROOT() + apiURLMigrant;
                            registerMigrant(api);
                        } else {
                            //Save in Temp Database to saveLater
                            Calendar cal = Calendar.getInstance();
                            String time = cal.getTimeInMillis() + "";

                            int mid = new SQLDatabaseHelper(ActivityRegister.this).insertTempMigrants(etName.getText().toString(),
                                    Integer.parseInt(etAge.getText().toString()), etNumber.getText().toString(), sex, ApplicationClass.getInstance().getUserId());
                            //new SQLDatabaseHelper(ActivityRegister.this).insertTempResponseTableData(sex, SharedPrefKeys.questionGender, -1, mid, "mg_sex", time);

                            //Saving in corresponding real local DB
                            int fabMigId = Integer.parseInt("-1" + mid);
                            new SQLDatabaseHelper(ActivityRegister.this).insertMigrants(fabMigId, etName.getText().toString(),
                                    Integer.parseInt(etAge.getText().toString()), etNumber.getText().toString(), sex, ApplicationClass.getInstance().getUserId());

                            new SQLDatabaseHelper(ActivityRegister.this).insertResponseTableData(sex, SharedPrefKeys.questionGender, -1, fabMigId, "mg_sex", time);
                            Intent intent = new Intent(ActivityRegister.this, ActivityMigrantList.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                        //Start Migrant List Activity
                    } else {
                        startOTPActivity(userType);
                    }
                } else if (userType == 0) {
                    startOTPActivity(userType);
                }
            }
        });

        String text = "";
        if (userType == 0)
            text = getString(R.string.disclaimerMigrant);
        else
            text = getString(R.string.disclaimerHelper);
        msg.setText(text);
        builder.show();
    }

    private void startOTPActivity(int uType) {
        //uType = 0 for Migrant, 1 for helper
        Intent intent = new Intent(ActivityRegister.this, ActivityOTPVerification.class);
        intent.putExtra("name", etName.getText().toString());
        intent.putExtra("phoneNumber", etNumber.getText().toString());
        intent.putExtra("age", etAge.getText().toString());
        intent.putExtra("gender", sex);
        intent.putExtra("userType", uType);
        startActivity(intent);
    }

    private void registerMigrant(String api) {
        Log.d("mylog", "API called: " + api);
        final ProgressDialog progressDialog = new ProgressDialog(ActivityRegister.this);
        //progressDialog.setTitle("Please wait");
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
                showSnackbar(getString(R.string.server_noconnect));
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("full_name", etName.getText().toString());
                params.put("phone_number", etNumber.getText().toString());
                params.put("age", etAge.getText().toString());
                params.put("gender", sex);

                //Flow will not enter these two unless a migrant is being registered
                if (userRegistered) {
                    Log.d("mylog", "User ID setting: " + ApplicationClass.getInstance().getUserId());
                    params.put("user_id", ApplicationClass.getInstance().getUserId() + "");
                }
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(ActivityRegister.this);
        //saveRequest.setShouldCache(false);
        saveRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(saveRequest);
    }

    private void parseResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            Boolean error = jsonObject.getBoolean("error");
            if (!error) {
                int mig_id = jsonObject.getInt("migrant_id");
                int user_id = jsonObject.getInt("user_id");
                Calendar cal = Calendar.getInstance();
                String time = cal.getTimeInMillis() + "";
                new SQLDatabaseHelper(ActivityRegister.this).insertResponseTableData(sex, SharedPrefKeys.questionGender, -1, mig_id, "mg_sex", time);
                new SQLDatabaseHelper(ActivityRegister.this).insertMigrants(mig_id, etName.getText().toString(),
                        Integer.parseInt(etAge.getText().toString()), etNumber.getText().toString(), sex, ApplicationClass.getInstance().getUserId());
                Intent intent = new Intent(ActivityRegister.this, ActivityMigrantList.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                String message = jsonObject.getString("message");
                showSnackbar(message);
            }
        } catch (JSONException e) {
            Log.d("mylog", "Error in parsing migrant result: " + e.toString());
        }
    }

    private ArrayList<MigrantModel> parseMigDetailResponse(String response) {
        ArrayList<MigrantModel> migrantModelsTemp = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(response);
            Boolean error = jsonObject.getBoolean("error");
            if (error) {
                showSnackbar(jsonObject.getString("message"));
            } else {
                JSONArray migrantJSON = jsonObject.getJSONArray("migrants");
                if (migrantJSON != null) {
                    JSONObject migrantObj;
                    SQLDatabaseHelper dbHelper = new SQLDatabaseHelper(ActivityRegister.this);
                    int uid = ApplicationClass.getInstance().getUserId();
                    for (int i = 0; i < migrantJSON.length(); i++) {
                        migrantObj = migrantJSON.getJSONObject(i);
                        MigrantModel migrantModel = new MigrantModel();
                        if (migrantObj.has("migrant_id")) {
                            int id = migrantObj.getInt("migrant_id");
                            migrantModel.setMigrantId(id);
                            String name = migrantObj.getString("migrant_name");
                            migrantModel.setMigrantName(name);
                            int age = migrantObj.getInt("migrant_age");
                            migrantModel.setMigrantAge(age);
                            String sex = migrantObj.getString("migrant_sex");
                            migrantModel.setMigrantSex(sex);
                            String phone = migrantObj.getString("migrant_phone");
                            migrantModel.setMigrantPhone(phone);
                            String inactiveDate = migrantObj.getString("inactive_date");
                            migrantModel.setInactiveDate(inactiveDate);
                            migrantModel.setUserId(uid);

                            migrantModelsTemp.add(migrantModel);
                            //Saving in Database
                            dbHelper.insertMigrants(id, name, age, phone, sex, uid);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.d("mylog", "Error in parsing: " + e.toString());
        }
        return migrantModelsTemp;
    }

    public void showSnackbar(String msg) {
        Snackbar snack = Snackbar.make(rootLayout, msg, Snackbar.LENGTH_LONG);
        View view = snack.getView();
        TextView tv = view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= 17)
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        snack.show();
    }

    public void showDisclaimerDialog() {
      /*  if (userType == 0)
            showErrorDialog();
        else*/
        showDisclaimerAndContinue();
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

    private void loadMigrantView() {
        tvOr.setVisibility(View.GONE);
        etNumber.setText("");
        etAge.setText("");
        etName.setText("");
        //tvHint.setText("Please enter Migrant's details");
        Resources res = getResources();
        tvTitle.setText(res.getString(R.string.add_migrant));
        etName.setHint(res.getString(R.string.migrants_name));
        etAge.setHint(res.getString(R.string.migrants_age));
        etNumber.setHint(res.getString(R.string.migrants_phone_number));
        btnAlreadyRegistered.setVisibility(View.INVISIBLE);
    }

    private boolean checkUserExists() {
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE);
        int userId = sharedPreferences.getInt(SharedPrefKeys.userId, -10);
        int migId = sharedPreferences.getInt(SharedPrefKeys.defMigID, -10);
        String type = sharedPreferences.getString(SharedPrefKeys.userType, "");
        Log.d("mylog", "User id: " + userId + " Type: " + type);
        if (userId != -10) {
            if (userId < 0 && InternetConnectionChecker.isNetworkConnected(ActivityRegister.this)) {
                //Just need to save user to server and change the UserId in migrant table to new IDs
                //Migrants will be saved to server on Activity Migrant List;
                Log.d("mylog", "Saving User to Server");
                saveUserToServer();
                return false;
            } else {
                if (type.equalsIgnoreCase("helper")) {
                    Log.d("mylog", "User already exists with ID: " + userId);
                    userType = 1;
                    ApplicationClass.getInstance().setUserId(userId);
                    ApplicationClass.getInstance().setUserType(1);
                } else {
                    Log.d("mylog", "Migrant already exists, Setting user ID: ");
                    //ApplicationClass.getInstance().setMigrantId(userId);
                    userType = 0;
                    ApplicationClass.getInstance().setUserType(0);
                    ApplicationClass.getInstance().setUserId(userId);
                    ApplicationClass.getInstance().setMigrantId(sharedPreferences.getInt(SharedPrefKeys.defMigID, -1));
                }
            }
            return true;
        } else return false;
    }

    private void saveUserToServer() {
        SharedPreferences sp = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE);

        String name = sp.getString(SharedPrefKeys.userName, "");
        String phoneNumber = sp.getString(SharedPrefKeys.userPhone, "");
        String age = sp.getString(SharedPrefKeys.userAge, "");
        String gender = sp.getString(SharedPrefKeys.userSex, "");

        Intent intent = new Intent(ActivityRegister.this, ActivityOTPVerification.class);
        intent.putExtra("name", name);
        intent.putExtra("phoneNumber", phoneNumber);
        intent.putExtra("age", age);
        intent.putExtra("gender", gender);
        intent.putExtra("userType", 1);
        startActivity(intent);
    }

    public void checkIfFBUserExists(String fid) {
        final String fbId = fid;
        String api = ApplicationClass.getInstance().getAPIROOT() + checkFbId;
        final ProgressDialog progressDialog = new ProgressDialog(this);
        //progressDialog.setTitle("Please wait");
        progressDialog.setMessage(getResources().getString(R.string.checking_registration));
        progressDialog.show();
        StringRequest checkRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Log.d("mylog", "response : " + response);
                try {
                    JSONObject jsonRes = new JSONObject(response);
                    boolean error = jsonRes.getBoolean("error");
                    if (error) {
                        showSnackbar(jsonRes.getString("message"));
                    } else {
                        getLoggedInUserDetails(jsonRes);
                        //Gets Migrant Details and Saves in DB regardless of User Type
                        //getMigrantDetails();
                        getMigrants();
                        //Getting all the saved responses for the user
                        getAllResponses(userType);
                    }
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
                    showSnackbar(getResources().getString(R.string.server_noconnect));
                else
                    showSnackbar(error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("fb_id", fbId);
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(this);
        //checkRequest.setShouldCache(false);

        checkRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(checkRequest);
    }

    private void startMigrantistActivity() {
        Intent intent = new Intent(ActivityRegister.this, ActivityMigrantList.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void getAllResponses(final int uType) {
        String api;
        if (uType == 0)
            api = ApplicationClass.getInstance().getAPIROOT() + apiGetAllResponses;
        else
            api = ApplicationClass.getInstance().getAPIROOT() + apiGetResponses;
        final ProgressDialog pdialog = new ProgressDialog(ActivityRegister.this);
        //pdialog.setTitle("Setting Up");
        pdialog.setMessage(getResources().getString(R.string.getting_mig_details));
        try {
            pdialog.show();
        } catch (Exception ex) {

        }
        StringRequest stringRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("mylog", "Responses from migrants: " + response);
                ++gotDetailCount;
                try {
                    pdialog.dismiss();
                } catch (Exception ex) {
                    Log.d("mylog", "Dialog dismissing error or : " + ex.toString());
                }
                parseAllResponses(response);
                startMigrantistActivity();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    pdialog.dismiss();
                } catch (Exception ex) {
                    Log.d("mylog", "Dialog dismissing error or : " + ex.toString());
                }
                Log.d("mylog", "Error getting all responses: " + error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("user_id", ApplicationClass.getInstance().getUserId() + "");
                if (uType == 1)
                    params.put("migrant_id", ApplicationClass.getInstance().getMigrantId() + "");
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(ActivityRegister.this);
        queue.add(stringRequest);
    }

    private void parseAllResponses(String res) {
        JSONObject jsonResponse = null;
        try {
            jsonResponse = new JSONObject(res);
            Boolean error = jsonResponse.getBoolean("error");
            if (!error) {
                Log.d("mylog", "NO error sorting responses");
                JSONArray responsesJsonArray = jsonResponse.getJSONArray("responses");
                JSONObject tempResponse;
                SQLDatabaseHelper dbHelper = new SQLDatabaseHelper(ActivityRegister.this);
                for (int i = 0; i < responsesJsonArray.length(); i++) {
                    tempResponse = responsesJsonArray.getJSONObject(i);
                    int migrantId = tempResponse.getInt("migrant_id");
                    int questionId = tempResponse.getInt("question_id");
                    String responseVariable = tempResponse.getString("response_variable");
                    String response = tempResponse.getString("response");
                    String isError = tempResponse.getString("is_error");
                    String sTileId = tempResponse.getString("tile_id");
                    int tileId = -1;
                    if (!sTileId.contains("null") && !sTileId.isEmpty() && sTileId != null) {
                        tileId = Integer.parseInt(sTileId);
                    }
                    Log.d("mylog", "Inserting Response with Tile ID: " + tileId);
                    dbHelper.insertAllResponses(response, questionId, migrantId, responseVariable, isError, tileId);
                }
            } else {
                Log.d("mylog", "No responses found");
            }
        } catch (JSONException e) {
            Log.d("mylog", "Error in user response: " + e.toString());
        }

    }
}
