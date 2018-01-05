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

import com.android.volley.AuthFailureError;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    final String apiGetResponses = "/getresponses.php";
    final String apiAlreadyRegistered = "/checkphonenumber.php";
    private final String apiGetMigrants = "/getmigrants.php";

    //Usertype = 0 for Helper and 1 for migrant. Order is reversed in Dialogs and Other Methods.
    int userType;
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
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Checking Registration...");
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
                        getMigrantDetails();
                    }
                    //Getting all the saved responses for the user
                    getAllResponses(userType);
                    Intent intent = new Intent(ActivityRegister.this, ActivityMigrantList.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
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
                    showSnackbar("Failed to connect to server :(");
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
        checkRequest.setShouldCache(false);
        queue.add(checkRequest);
    }

    private void getLoggedInUserDetails(JSONObject jsonRes) {
        int userType = 0;
        try {
            userType = jsonRes.getInt("user_type");
            int id = jsonRes.getInt("user_id");

            SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (userType == 0) {
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
            } else if (userType == 1) {
                ApplicationClass.getInstance().setUserId(-1);
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
            showUserTypeDialogAndContinue();
        } else {
            //Parameter 1 is for registering migrant after reading disclaimer
            showDisclaimerAndContinue(1);
        }
    }

    private void showDisclaimerAndContinue(final int i) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_disclaimer, null);
        TextView msg = (TextView) view.findViewById(R.id.tvDisclaimerContent);
        builder.setView(view);
        builder.setPositiveButton(getResources().getString(R.string.disclaimer_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (i == 1) {
                    //If it's a helper user, then no need to redirect to OTPActivity,
                    //But Register and then redirect to migrant list.
                    if (userRegistered) {
                        String api = ApplicationClass.getInstance().getAPIROOT() + apiURLMigrant;
                        registerMigrant(api);
                        //Start Migrant List Activity
                    } else {
                        startOTPActivity(1);
                    }
                } else if (i == 2) {
                    startOTPActivity(2);
                }
            }
        });
        msg.setText(getResources().getString(R.string.disclaimer));
        builder.show();
    }

    private void startOTPActivity(int uType) {
        //uType = 1 for Migrant, 2 for helper
        Intent intent = new Intent(ActivityRegister.this, ActivityOTPVerification.class);
        intent.putExtra("name", etName.getText().toString());
        intent.putExtra("phoneNumber", etNumber.getText().toString());
        intent.putExtra("age", etAge.getText().toString());
        intent.putExtra("gender", sex);
        intent.putExtra("userType", uType);
        if (uType == 1)
            intent.putExtra("isSupervised", userRegistered);
        startActivity(intent);
    }

    private void registerMigrant(String api) {
        Log.d("mylog", "API called: " + api);
        final ProgressDialog progressDialog = new ProgressDialog(ActivityRegister.this);
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
        saveRequest.setShouldCache(false);
        queue.add(saveRequest);
    }

    private void parseResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            Boolean error = jsonObject.getBoolean("error");
            if (!error) {
                int mig_id = jsonObject.getInt("migrant_id");
                new SQLDatabaseHelper(ActivityRegister.this).insertResponseTableData(sex, -2, -1, mig_id, "mg_sex");
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

    public void showSnackbar(String msg) {
        Snackbar snack = Snackbar.make(rootLayout, msg, Snackbar.LENGTH_LONG);
        View view = snack.getView();
        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= 17)
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        snack.show();
    }

    private void showUserTypeDialogAndContinue() {
        userType = 0;
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityRegister.this);
        builder.setPositiveButton(getResources().getString(R.string.register), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (userType == 0) {
                    showDisclaimerAndContinue(1);
                } else {
                    showDisclaimerAndContinue(2);
                }
            }
        });
        builder.setSingleChoiceItems(new String[]{getResources().getString(R.string.aspiringmigrant),
                getResources().getString(R.string.helper)}, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("mylog", "Which: " + which);
                userType = which;
            }
        });
        builder.show();
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
        String type = sharedPreferences.getString(SharedPrefKeys.userType, "");
        Log.d("mylog", "User id: " + userId + " Type: " + type);
        if (userId != -10) {
            if (type.equalsIgnoreCase("helper")) {
                Log.d("mylog", "User already exists with ID: " + userId);
                ApplicationClass.getInstance().setUserId(userId);
            } else {
                Log.d("mylog", "Migrant already exists, setting user ID: " + -1);
                ApplicationClass.getInstance().setMigrantId(userId);
                ApplicationClass.getInstance().setUserId(-1);
            }
            return true;
        } else return false;
    }

    public void checkIfFBUserExists(String fid) {
        final String fbId = fid;
        String api = ApplicationClass.getInstance().getAPIROOT() + checkFbId;
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Checking Registration...");
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
                        getMigrantDetails();
                        //Getting all the saved responses for the user
                        getAllResponses(userType);
                        Intent intent = new Intent(ActivityRegister.this, ActivityMigrantList.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
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
                    showSnackbar("Failed to connect to server :(");
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
        checkRequest.setShouldCache(false);
        queue.add(checkRequest);
    }

    public void getMigrantDetails() {
        String api = ApplicationClass.getInstance().getAPIROOT() + apiGetMigrants;
        final ProgressDialog progressDialog = new ProgressDialog(ActivityRegister.this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Getting Migrant Details");
        progressDialog.show();
        StringRequest getRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    progressDialog.dismiss();
                    Log.d("mylog", "response : " + response);
                    JSONObject responseJson = new JSONObject(response);
                    Boolean error = Boolean.parseBoolean(responseJson.getString("error"));
                    if (!error) {
                        JSONArray jsonArray = responseJson.getJSONArray("migrants");
                        if (jsonArray.length() > 0) {
                            JSONObject currMig = jsonArray.getJSONObject(0);
                            int id = currMig.getInt("migrant_id");
                            String migName = currMig.getString("migrant_name");
                            String migSex = currMig.getString("migrant_sex");
                            String migPhone = currMig.getString("migrant_phone");
                            int migAge = currMig.getInt("migrant_age");
                            new SQLDatabaseHelper(ActivityRegister.this).
                                    insertMigrants(id, migName, migAge, migPhone, migSex, ApplicationClass.getInstance().getUserId());
                        }
                    }
                } catch (Exception ex) {
                    Log.d("mylog", "response exception: " + ex.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    progressDialog.dismiss();
                    String err = error.toString();
                    Log.d("mylog", "error : " + err);
                    if (!err.isEmpty() && err.contains("TimeoutError"))
                        showSnackbar("Failed to connect to server :(");
                    else if (!err.isEmpty() && err.contains("NoConnection"))
                        showSnackbar("Please connect to Internet for new Data :(");
                    else
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

    public void getAllResponses(final int uType) {
        String api;
        if (uType == 0)
            api = ApplicationClass.getInstance().getAPIROOT() + apiGetAllResponses;
        else
            api = ApplicationClass.getInstance().getAPIROOT() + apiGetResponses;
        final ProgressDialog pdialog = new ProgressDialog(ActivityRegister.this);
        pdialog.setTitle("Setting Up");
        pdialog.setMessage("Getting Migrant Responses");
        pdialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("mylog", "Responses from migrants: " + response);
                try {
                    pdialog.dismiss();
                } catch (Exception ex) {
                    Log.d("mylog", "Dialog dismissing error or : " + ex.toString());
                }
                parseAllResponses(response);
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
