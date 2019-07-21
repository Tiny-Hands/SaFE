package com.vysh.subairoma.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.flurry.android.FlurryAgent;
import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.SharedPrefKeys;
import com.vysh.subairoma.dialogs.DialogLoginOptions;
import com.vysh.subairoma.dialogs.DialogUsertypeChooser;
import com.vysh.subairoma.imageHelpers.ImageResizer;
import com.vysh.subairoma.imageHelpers.ImageSaveHelper;
import com.vysh.subairoma.models.MigrantModel;
import com.vysh.subairoma.utils.InternetConnectionChecker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
    final String apiGetResponses = "/getresponses.php";
    final String apiAlreadyRegistered = "/checkphonenumber.php";
    private final String APIGETMIG = "/getmigrants.php";
    private int gotDetailCount = 0, migrantCount = 0;

    private final int REQUEST_SELECT_FILE = 1;
    private final int REQUEST_TAKE_PIC = 0;
    String pathToImage, encodedImage = " ";
    final static int PERMISSION_ALL = 2;
    final static String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};

    //Usertype = 0 for Helper and 1 for migrant. Order is reversed in Dialogs and Other Methods.
    public String userType;
    Boolean userRegistered = false;
    boolean loggedInFromPhone = false;
    String sex = "male";
    public String entered_phone = "";

    @BindView(R.id.ivRegister)
    ImageView ivRegister;
    @BindView(R.id.btnNext)
    Button btnNext;
    @BindView(R.id.btnBack)
    ImageView btnBack;
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
    RequestQueue queue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        queue = Volley.newRequestQueue(ActivityRegister.this);

        //Checking if already logged in on current device
        if (checkUserExists() && !getIntent().hasExtra("migrantmode")) {
            Log.d("mylog", "Open Activity");
            Intent intent = new Intent(ActivityRegister.this, ActivityMigrantList.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }
        //Setting View
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);

        //Checking if user is registering a new migrant
        if (getIntent().hasExtra("migrantmode")) {
            userRegistered = true;
            Log.d("mylog", "Loading migrant view");
            loadMigrantView();
        }
        setUpComponentListeners();
        callbackManager = CallbackManager.Factory.create();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("mylog", "On Activity Result");
        if (resultCode == RESULT_OK) {
            Log.d("mylog", "result is ok with request code : " + requestCode);
            if (requestCode == REQUEST_TAKE_PIC) {
                Log.d("mylog", "request was camera");
                Bitmap picTaken = getPic();
                //picTaken = ImageRotator.getBitmapRotatedByDegree(picTaken, 90);
                encodeImage(picTaken);
                getPic();
                ivRegister.setImageBitmap(picTaken);
            } else if (requestCode == REQUEST_SELECT_FILE) {
                Log.d("mylog", "request was to select");
                Uri selectedImageUri = data.getData();
                try {
                    Bitmap bitmap = ImageResizer.
                            calculateInSampleSize(MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri));
                    //encodeImage(bitmap);
                    //pathToImage = selectedImageUri.toString();
                    encodeImage(ImageResizer.calculateInSampleSize(bitmap));
                    ivRegister.setImageBitmap(bitmap);
                } catch (IOException ex) {
                    Log.d("mylog", "error getting photo: " + ex.toString());
                }
            } else
                callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_ALL:
                Map<String, Integer> perms = new HashMap<>();
                for (int i = 0; i < grantResults.length; i++) {
                    perms.put(permissions[i], grantResults[i]);
                }
                boolean grantedAll = true;
                for (int i = 0; i < perms.size(); i++) {
                    if (perms.get(PERMISSIONS[i]) != PackageManager.PERMISSION_GRANTED) {
                        grantedAll = false;
                    }
                }
                if (grantedAll)
                    showPicTakerDialog();
        }

    }

    private void encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        byte[] byte_arr = bytes.toByteArray();
        encodedImage = Base64.encodeToString(byte_arr, 0);
    }

    private Bitmap getPic() {
        ExifInterface ei = null;
        Bitmap rotatedBitmap = null;
        Bitmap bitmap = BitmapFactory.decodeFile(pathToImage);
        try {
            ei = new ExifInterface(pathToImage);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_UNDEFINED);
            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotatedBitmap = rotateImage(bitmap, 90);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotatedBitmap = rotateImage(bitmap, 180);
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotatedBitmap = rotateImage(bitmap, 270);
                    break;

                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    rotatedBitmap = bitmap;
            }
        } catch (IOException e) {
            Log.d("mylog", "Error in exif");
        }
        if (rotatedBitmap == null)
            rotatedBitmap = bitmap;
        return ImageResizer.calculateInSampleSize(rotatedBitmap);
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
                matrix, true);
    }

    public void checkUserRegistration(String number) {
        final String pNumber = number;
        String api = ApplicationClass.getInstance().getAPIROOT() + apiAlreadyRegistered;
        final ProgressDialog progressDialog = new ProgressDialog(ActivityRegister.this);
        //progressDialog.setTitle("Please wait");
        progressDialog.setMessage(getResources().getString(R.string.checking_registration));
        progressDialog.show();
        progressDialog.setCancelable(false);
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
                        return;
                    } else {
                        loggedInFromPhone = true;
                        getLoggedInUserDetails(jsonRes);
                        //Gets Migrant Details and Saves in DB regardless of User Type
                        getMigrants();
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

                    Toast.makeText(ActivityRegister.this, getString(R.string.server_noconnect), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(ActivityRegister.this, error.toString(), Toast.LENGTH_SHORT).show();
            }
        }) {
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
        try {
            userType = jsonRes.getString("user_type");
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
            if (!loggedInFromPhone)
                editor.putInt(SharedPrefKeys.userId, id);
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
                    getAllResponses(userType, ApplicationClass.getInstance().getSafeUserId());
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
                int mig_id = ApplicationClass.getInstance().getMigrantId();
                Log.d("mylog", "User ID: " + user_id);
                Log.d("mylog", "Mig ID: " + mig_id);
                params.put("user_id", user_id + "");
                params.put("migrant_id", mig_id + "");
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

    private void setUpComponentListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        ivRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (hasCameraPermissions())
                        showPicTakerDialog();
                    else
                        requestPermissions(PERMISSIONS, PERMISSION_ALL);
                } else
                    showPicTakerDialog();
            }
        });
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

                    FlurryAgent.logEvent("user_registration_initiated");
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

    private boolean hasCameraPermissions() {
        for (String permission : PERMISSIONS) {
            if (checkSelfPermission(permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
        //requestPermissions(CAMERA);
    }

    private void showPicTakerDialog() {
        final CharSequence[] items = {getString(R.string.take_photo),
                getResources().getString(R.string.choose_from_gallery)};
        //final CharSequence[] items = {"Take Photo", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setTitle(getResources().getString(R.string.add_photo));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals(getResources().getString(R.string.take_photo))) {
                    dispatchPictureTakeIntent();
                } else if (items[item].equals(getResources().getString(R.string.choose_from_gallery))) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, getResources().getString(R.string.choose_from_gallery)), REQUEST_SELECT_FILE);
                }
            }
        });
        builder.show();
    }

    private void dispatchPictureTakeIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = ImageSaveHelper.createImageFile();
                pathToImage = photoFile.getAbsolutePath();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Log.d("mylog", "Exception while creating file: " + ex.toString());
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Log.d("mylog", "Photofile not null");
                Uri photoURI = FileProvider.getUriForFile(ActivityRegister.this,
                        "com.vysh.subairoma.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PIC);
            }

        }
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

    public void showDisclaimerAndContinue() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_disclaimer, null);
        TextView msg = view.findViewById(R.id.tvDisclaimerContent);
        builder.setView(view);
        builder.setPositiveButton(getResources().getString(R.string.disclaimer_button), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d("mylog", "User type: " + userType + " User Registered: " + userRegistered.toString());
                if (userType.equalsIgnoreCase(SharedPrefKeys.helperUser)) {
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

                            int mid = SQLDatabaseHelper.getInstance(ActivityRegister.this).insertTempMigrants(etName.getText().toString(),
                                    Integer.parseInt(etAge.getText().toString()), etNumber.getText().toString(), sex, ApplicationClass.getInstance().getSafeUserId(), encodedImage);
                            //SQLDatabaseHelper.getInstance(ActivityRegister.this).insertTempResponseTableData(sex, SharedPrefKeys.questionGender, -1, mid, "mg_sex", time);

                            //Saving in corresponding real local DB
                            int fabMigId = Integer.parseInt("-1" + mid);
                            SQLDatabaseHelper.getInstance(ActivityRegister.this).insertMigrants(fabMigId, etName.getText().toString(),
                                    Integer.parseInt(etAge.getText().toString()), etNumber.getText().toString(), sex, ApplicationClass.getInstance().getSafeUserId(), encodedImage, 0);

                            SQLDatabaseHelper.getInstance(ActivityRegister.this).insertResponseTableData(sex, SharedPrefKeys.questionGender, -1, fabMigId, "mg_sex", time);
                            Intent intent = new Intent(ActivityRegister.this, ActivityMigrantList.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                        //Start Migrant List Activity
                    } else {
                        startOTPActivity(userType, 0);
                    }
                } else {
                    if (userRegistered) {
                        String api = ApplicationClass.getInstance().getAPIROOT() + apiURLMigrant;
                        registerMigrant(api);
                    } else
                        startOTPActivity(userType, 0);
                }
            }
        });

        String text = "";
        if (userType == SharedPrefKeys.migrantUser)
            text = getString(R.string.disclaimerMigrant);
        else
            text = getString(R.string.disclaimerHelper);
        msg.setText(text);
        builder.show();
    }

    private void startOTPActivity(String uType, int otpType) {
        //otpType = 1 if logging in
        if (otpType == 1) {
            if (!loggedInFromPhone) {
                startMigrantistActivity();
                return;
            }
            Intent intent = new Intent(ActivityRegister.this, ActivityOTPVerification.class);
            intent.putExtra("otpnumber", entered_phone);
            startActivity(intent);
            return;
        }
        Intent intent = new Intent(ActivityRegister.this, ActivityOTPVerification.class);
        intent.putExtra("name", etName.getText().toString());
        intent.putExtra("phoneNumber", etNumber.getText().toString());
        intent.putExtra("age", etAge.getText().toString());
        intent.putExtra("gender", sex);
        intent.putExtra("userImg", encodedImage);
        intent.putExtra("userType", uType);
        startActivity(intent);
    }

    private void registerMigrant(String api) {
        final ProgressDialog progressDialog = new ProgressDialog(ActivityRegister.this);
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
                Toast.makeText(ActivityRegister.this, getString(R.string.server_noconnect), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("full_name", etName.getText().toString());
                params.put("phone_number", etNumber.getText().toString());
                params.put("age", etAge.getText().toString());
                params.put("gender", sex);
                params.put("user_img", encodedImage);

                //Flow will not enter these two unless a migrant is being registered
                if (userRegistered) {
                    Log.d("mylog", "User ID setting: " + ApplicationClass.getInstance().getSafeUserId());
                    params.put("user_id", ApplicationClass.getInstance().getSafeUserId() + "");
                }
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
                SQLDatabaseHelper.getInstance(ActivityRegister.this).insertResponseTableData(sex, SharedPrefKeys.questionGender, -1, mig_id, "mg_sex", time);
                SQLDatabaseHelper.getInstance(ActivityRegister.this).insertMigrants(mig_id, etName.getText().toString(),
                        Integer.parseInt(etAge.getText().toString()), etNumber.getText().toString(), sex, ApplicationClass.getInstance().getSafeUserId(), encodedImage, 0);
                Intent intent = new Intent(ActivityRegister.this, ActivityMigrantList.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                String message = jsonObject.getString("message");
                Toast.makeText(ActivityRegister.this, message, Toast.LENGTH_LONG).show();
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
                Toast.makeText(ActivityRegister.this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
            } else {
                JSONArray migrantJSON = jsonObject.getJSONArray("migrants");
                if (migrantJSON != null) {
                    migrantCount = migrantJSON.length();
                    JSONObject migrantObj;
                    SQLDatabaseHelper dbHelper = SQLDatabaseHelper.getInstance(ActivityRegister.this);
                    int uid = ApplicationClass.getInstance().getSafeUserId();
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
                            getAllResponses(userType, id);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.d("mylog", "Error in parsing: " + e.toString());
        }
        return migrantModelsTemp;
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
            if (userId < 0 && InternetConnectionChecker.isNetworkConnected(ActivityRegister.this)) {
                //Just need to save user to server and change the UserId in migrant table to new IDs
                //Migrants will be saved to server on Activity Migrant List;
                Log.d("mylog", "Saving User to Server");
                saveUserToServer();
                return false;
            } else {
                if (type.equalsIgnoreCase(SharedPrefKeys.helperUser)) {
                    Log.d("mylog", "User already exists with ID: " + userId);
                    userType = SharedPrefKeys.helperUser;
                    ApplicationClass.getInstance().setSafeUserId(userId);
                    ApplicationClass.getInstance().setUserType(SharedPrefKeys.helperUser);
                } else {
                    Log.d("mylog", "Migrant already exists, Setting user ID: ");
                    //ApplicationClass.getInstance().setMigrantId(userId);
                    userType = SharedPrefKeys.migrantUser;
                    ApplicationClass.getInstance().setUserType(SharedPrefKeys.migrantUser);
                    ApplicationClass.getInstance().setSafeUserId(userId);
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
                        Toast.makeText(ActivityRegister.this, jsonRes.getString("message"), Toast.LENGTH_SHORT).show();
                    } else {
                        loggedInFromPhone = false;
                        getLoggedInUserDetails(jsonRes);
                        //Gets Migrant Details and Saves in DB regardless of User Type
                        //getMigrantDetails();
                        getMigrants();
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

    public void getAllResponses(final String uType, final int id) {
        String api = ApplicationClass.getInstance().getAPIROOT() + apiGetAllResponses;
        final ProgressDialog pdialog = new ProgressDialog(ActivityRegister.this);
        //pdialog.setTitle("Setting Up");
        pdialog.setMessage(getResources().getString(R.string.getting_mig_responses));
        pdialog.setCancelable(false);
        try {
            pdialog.show();
        } catch (Exception ex) {
            Log.d("mylog", "Couldn't show dialgo: " + ex.getMessage());
        }
        StringRequest stringRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("mylog", "Responses from migrants: " + response);
                ++gotDetailCount;
                parseAllResponses(response);
                if (gotDetailCount == migrantCount)
                    startOTPActivity(userType, 1);
                try {
                    pdialog.dismiss();
                } catch (Exception ex) {
                    Log.d("mylog", "Dialog dismissing error or : " + ex.toString());
                }
                //startMigrantistActivity();
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
                params.put("user_id", id + "");
                if (uType.equalsIgnoreCase(SharedPrefKeys.migrantUser))
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
