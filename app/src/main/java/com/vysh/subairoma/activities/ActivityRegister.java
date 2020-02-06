package com.vysh.subairoma.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.SharedPrefKeys;
import com.vysh.subairoma.models.MigrantModel;
import com.vysh.subairoma.utils.InternetConnectionChecker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Vishal on 6/15/2017.
 */

public class ActivityRegister extends AppCompatActivity {
    final int G_SIGN_IN = 10334;
    final String checkFbId = "/checkfbuid.php";
    final String apiSaveUser = "/savesafeuser.php";
    final String apiGetAllResponses = "/getallresponses.php";
    private final String APIGETMIG = "/getsafemigrants.php";
    private int gotDetailCount = 0, migrantCount = 0;

    public CallbackManager callbackManager;
    ProfileTracker mProfileTracker;
    RequestQueue queue;

    @BindView(R.id.fb_login_button)
    LoginButton loginButton;

    @BindView(R.id.google_login_button)
    SignInButton signInButton;

    @BindView(R.id.btnFb)
    Button fbBtn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        queue = Volley.newRequestQueue(ActivityRegister.this);
        //Setting View
        setContentView(R.layout.activity_social_login);

        ButterKnife.bind(this);
        //Setting up Google SignIn
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        //GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);\
        TextView tvG = (TextView) signInButton.getChildAt(0);
        tvG.setText(R.string.google_continue);
        signInButton.setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, G_SIGN_IN);
        });


        //Setting Up Facebook Login
        fbBtn.setOnClickListener(v -> {
            loginButton.performClick();
        });
        callbackManager = CallbackManager.Factory.create();
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                if (Profile.getCurrentProfile() == null) {
                    mProfileTracker = new ProfileTracker() {
                        @Override
                        protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                            mProfileTracker.stopTracking();
                            //get data here
                            GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject object, GraphResponse response) {
                                    //Do Safe Login Here and Get Details if User Already Exists, Check Using Email
                                    Log.d("mylog", "Res FB: " + object.toString());
                                    try {
                                        saveSafeUser(object.getString("id"), object.getString("name"), object.getString("email"), "facebook");
                                    } catch (JSONException e) {
                                        Log.d("mylog", "Error parsing fb res: " + e.toString());
                                    }
                                }
                            });
                            Bundle parameters = new Bundle();
                            parameters.putString("fields", "id,name,email");
                            request.setParameters(parameters);
                            request.setParameters(parameters);
                            request.executeAsync();
                        }
                    };
                } else {
                    GraphRequest request = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            //Do Safe Login Here and Get Details if User Already Exists, Check Using Email
                            Log.d("mylog", "Res: " + object.toString());
                            try {
                                saveSafeUser(object.getString("id"), object.getString("name"), object.getString("email"), "facebook");
                            } catch (JSONException e) {
                                Log.d("mylog", "Error parsing fb res 1: " + e.toString());
                            }

                        }
                    });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "id,name,email");
                    request.setParameters(parameters);
                    request.setParameters(parameters);
                    request.executeAsync();
                }
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("mylog", "On Activity Result with Request & Result: " + requestCode + ", " + resultCode);
        if (resultCode == RESULT_OK) {
            if (requestCode == G_SIGN_IN) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                handleSignInResult(task);

            } else
                callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            saveSafeUser(account.getId(), account.getDisplayName(), account.getEmail(), "google");
            // Signed in successfully, show authenticated UI.
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.d("mylog", "Google signInResult:failed code=" + e.getStatusCode());
        }
    }

    private void getLoggedInUserDetails(JSONObject jsonRes) {
        try {
            String userType = jsonRes.getString("user_type");
            int id = jsonRes.getInt("user_id");
            String userName = jsonRes.getString("user_name");
            String userPhone = jsonRes.getString("user_phone");
            String userSex = jsonRes.getString("user_sex");
            String age = jsonRes.getString("user_age");
            String userImg = jsonRes.getString("user_img");
            String token = jsonRes.getString("token");

            SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            //Not saving here as OTP is still not entered, send to OTP Activity and save there
            //editor.putInt(SharedPrefKeys.userId, id);
            editor.putInt(SharedPrefKeys.userId, id);
            ApplicationClass.getInstance().setUserType(userType);
            ApplicationClass.getInstance().setSafeUserId(id);
            if (userType.equalsIgnoreCase(SharedPrefKeys.helperUser)) {
                ApplicationClass.getInstance().setSafeUserId(id);

                //Saving Helper Details and Login Status
                Log.d("mylog", "Saving: " + userName + userPhone + userSex + age);
                editor.putString(SharedPrefKeys.userName, userName);
                editor.putString(SharedPrefKeys.userPhone, userPhone);
                editor.putString(SharedPrefKeys.userSex, userSex);
                editor.putString(SharedPrefKeys.userAge, age);
                editor.putString(SharedPrefKeys.userImg, userImg);
                editor.putString(SharedPrefKeys.userType, "helper");
                Log.d("mylog", "Saving Token: " + token);
                editor.putString(SharedPrefKeys.token, token);
                editor.commit();
            } else if (userType.equalsIgnoreCase(SharedPrefKeys.migrantUser)) {
                ApplicationClass.getInstance().setMigrantId(id);

                //If migrant is already saved and has added a new migrant then don't save again
                if (sharedPreferences.getString(SharedPrefKeys.userName, "").length() >= 1) {
                    return;
                }

                ApplicationClass.getInstance().setMigrantId(id);
                //Saving Migrant ID and Login Status
                editor.putString(SharedPrefKeys.userType, "migrant");
                editor.putString(SharedPrefKeys.userName, userName);
                editor.putString(SharedPrefKeys.userPhone, userPhone);
                editor.putString(SharedPrefKeys.userSex, userSex);
                editor.putString(SharedPrefKeys.userAge, age);
                editor.putString(SharedPrefKeys.userImg, userImg);
                Log.d("mylog", "Saving Token: " + token);
                editor.putString(SharedPrefKeys.token, token);
                editor.commit();
            }
        } catch (JSONException e) {
            Log.d("mylog", "Parsing user details error: " + e.toString());
        }
    }

    private void saveSafeUser(String id, String name, String email, String socialnetwork) {
        String api = ApplicationClass.getInstance().getAPIROOT() + apiSaveUser;
        final ProgressDialog progressDialog = new ProgressDialog(ActivityRegister.this);
        progressDialog.setMessage(getResources().getString(R.string.registering));
        progressDialog.setCancelable(false);
        try {
            progressDialog.show();
        } catch (Exception ex) {
            Log.d("mylog", "Exception in progress list: " + ex.getMessage());
        }
        StringRequest getRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.d("mylog", "Res Server: " + response);
                    progressDialog.dismiss();
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getString("error").equalsIgnoreCase("true")) {
                        Toast.makeText(ActivityRegister.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                    } else {
                        int userId = jsonObject.getInt("user_id");
                        String token = jsonObject.getString("token");
                        String userName = jsonObject.getString("user_name");
                        String userSex = jsonObject.getString("user_sex");
                        String user_age = jsonObject.getString("user_age");
                        String userPhone = jsonObject.getString("user_phone");
                        String userImg = jsonObject.getString("user_img");
                        ApplicationClass.getInstance().setSafeUserId(userId);
                        SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt(SharedPrefKeys.userId, userId);
                        editor.putString(SharedPrefKeys.token, token);
                        editor.putString(SharedPrefKeys.userName, userName);
                        editor.putString(SharedPrefKeys.userSex, userSex);
                        editor.putString(SharedPrefKeys.userAge, user_age);
                        editor.putString(SharedPrefKeys.userPhone, userPhone);
                        editor.putString(SharedPrefKeys.userImg, userImg);
                        editor.commit();

                        getMigrants();
                        //startMigrantistActivity();
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
                        Toast.makeText(ActivityRegister.this, error.toString(), Toast.LENGTH_SHORT).show();
                } catch (Exception ex) {
                    Log.d("mylog", "Error exception: " + ex.toString());
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("id", id);
                params.put("name", name);
                params.put("email", email);
                params.put("social_source", socialnetwork);
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(ActivityRegister.this);
        queue.add(getRequest);
    }

    private void getMigrants() {
        String api = ApplicationClass.getInstance().getAPIROOT() + APIGETMIG;
        final ProgressDialog progressDialog = new ProgressDialog(ActivityRegister.this);
        progressDialog.setMessage(getResources().getString(R.string.getting_mig_details));
        progressDialog.setCancelable(false);
        try {
            progressDialog.show();
        } catch (Exception ex) {
            Log.d("mylog", "Exception in progress list: " + ex.getMessage());
        }
        StringRequest getRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    Log.d("mylog", "REs: " + response);
                    //getAllResponses(ApplicationClass.getInstance().getSafeUserId());
                    parseMigDetailResponse(response);
                    try {
                        progressDialog.dismiss();
                    } catch (Exception ex) {
                        Log.d("mylog", "Exception in progress dis: " + ex.getMessage());
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
                        Toast.makeText(ActivityRegister.this, error.toString(), Toast.LENGTH_SHORT).show();
                } catch (Exception ex) {
                    Log.d("mylog", "Error exception: " + ex.toString());
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                int user_id = ApplicationClass.getInstance().getSafeUserId();
                Log.d("mylog", "User ID: " + user_id);
                params.put("user_id", user_id + "");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE).getString(SharedPrefKeys.token, ""));
                return headers;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(ActivityRegister.this);
        queue.add(getRequest);
    }


    private ArrayList<MigrantModel> parseMigDetailResponse(String response) {
        ArrayList<MigrantModel> migrantModelsTemp = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(response);
            Boolean error = jsonObject.getBoolean("error");
            if (error) {
                //Toast.makeText(ActivityRegister.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
                startMigrantistActivity();
            } else {
                JSONArray migrantJSON = jsonObject.getJSONArray("migrants");
                if (migrantJSON != null) {
                    migrantCount = migrantJSON.length();
                    JSONObject migrantObj;
                    SQLDatabaseHelper dbHelper = SQLDatabaseHelper.getInstance(ActivityRegister.this);
                    int uid = ApplicationClass.getInstance().getSafeUserId();

                    final ProgressDialog pdialog = new ProgressDialog(ActivityRegister.this);
                    //pdialog.setTitle("Setting Up");
                    pdialog.setMessage(getResources().getString(R.string.getting_mig_responses));
                    pdialog.setCancelable(false);
                    try {
                        pdialog.show();
                    } catch (Exception ex) {
                        Log.d("mylog", "Couldn't show dialgo: " + ex.getMessage());
                    }

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
                            //String inactiveDate = migrantObj.getString("inactive_date");
                            //migrantModel.setInactiveDate(inactiveDate);
                            String migImg = migrantObj.getString("user_img");
                            migrantModel.setMigImg(migImg);
                            migrantModel.setUserId(uid);
                            int percentComp = migrantObj.getInt("percent_comp");
                            migrantModel.setPercentComp(percentComp);

                            migrantModelsTemp.add(migrantModel);
                            //Saving in Database
                            dbHelper.insertMigrants(id, name, age, phone, sex, uid, migImg, percentComp);
                            //Getting responses
                            getAllResponses(id, pdialog, i);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.d("mylog", "Error in parsing: " + e.toString());
        }
        return migrantModelsTemp;
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
                        Toast.makeText(ActivityRegister.this, jsonRes.getString("message"), Toast.LENGTH_SHORT).show();
                    } else {
                        getLoggedInUserDetails(jsonRes);
                        getMigrants();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ActivityRegister.this, getString(R.string.cannot_login), Toast.LENGTH_SHORT).show();

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
                    Toast.makeText(ActivityRegister.this, getString(R.string.server_noconnect), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(ActivityRegister.this, error.toString(), Toast.LENGTH_SHORT).show();
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

    public void getAllResponses(final int id, ProgressDialog dialog, int currentMig) {
        String api = ApplicationClass.getInstance().getAPIROOT() + apiGetAllResponses;

        if (!dialog.isShowing())
            dialog.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("mylog", "Responses from migrants: " + response);
                ++gotDetailCount;
                parseAllResponses(response);
                if (gotDetailCount == migrantCount) {
                    startMigrantistActivity();
                    //startOTPActivity(userType, 1);
                    try {
                        dialog.dismiss();
                    } catch (Exception ex) {
                        Log.d("mylog", "Dialog dismissing error or : " + ex.toString());
                    }
                } else if (migrantCount == (currentMig + 1)) {
                    // This means failed to get responses for some migrants
                    try {
                        dialog.dismiss();
                    } catch (Exception ex) {
                        Log.d("mylog", "Dialog dismissing : " + ex.toString());
                    }
                    Toast.makeText(ActivityRegister.this, "Failed to get some responses, please try again", Toast.LENGTH_LONG).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                try {
                    dialog.dismiss();
                } catch (Exception ex) {
                    Log.d("mylog", "Dialog dismissing error or : " + ex.toString());
                }
                Log.d("mylog", "Error getting all responses: " + error.toString());
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("user_id", id + "");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE).getString(SharedPrefKeys.token, ""));
                return headers;
            }
        };

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
                SQLDatabaseHelper dbHelper = SQLDatabaseHelper.getInstance(ActivityRegister.this);
                for (int i = 0; i < responsesJsonArray.length(); i++) {
                    tempResponse = responsesJsonArray.getJSONObject(i);
                    int migrantId = tempResponse.getInt("user_id");
                    int questionId = tempResponse.getInt("question_id");
                    String responseVariable = tempResponse.getString("response_variable");
                    String response = tempResponse.getString("response");
                    String isError = tempResponse.getString("is_error");
                    String sTileId = tempResponse.getString("tile_id");
                    int tileId = -1;
                    if (!sTileId.contains("null") && !sTileId.isEmpty()) {
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
