package com.vysh.subairoma.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.facebook.ProfileTracker;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.flurry.android.FlurryAgent;
import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.SharedPrefKeys;
import com.vysh.subairoma.imageHelpers.ImageEncoder;
import com.vysh.subairoma.imageHelpers.ImageResizer;
import com.vysh.subairoma.imageHelpers.ImageRotator;
import com.vysh.subairoma.imageHelpers.ImageSaveHelper;
import com.vysh.subairoma.models.MigrantModel;
import com.vysh.subairoma.utils.CustomTextView;
import com.vysh.subairoma.utils.InternetConnectionChecker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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

    private final int REQUEST_SELECT_FILE = 1;
    private final int REQUEST_TAKE_PIC = 0;
    String pathToImage, encodedImage = "";
    final static int PERMISSION_ALL = 2;
    final static String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};

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
    @BindView(R.id.login_button_edit)
    LoginButton loginButton;
    @BindView(R.id.login_button)
    LoginButton loginButtonToHide;
    @BindView(R.id.tvOR)
    TextView tvOr;

    CallbackManager callbackManager;

    String userType = "";
    ProfileTracker mProfileTracker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        userType = getIntent().getStringExtra("userType");

        //tvHint.setText("Edit Details");
        btnAlreadyRegistered.setVisibility(GONE);
        btnNext.setText(R.string.save);
        btnNext.setOnClickListener(this);
        if (userType.equalsIgnoreCase(SharedPrefKeys.migrantUser)) {
            tvTitle.setText(getResources().getString(R.string.edit_migrant));
            FlurryAgent.logEvent("migrant_edit_mode");
            if (ApplicationClass.getInstance().getUserId() != -1) {
                loginButton.setVisibility(GONE);
                loginButtonToHide.setVisibility(GONE);
                tvOr.setVisibility(GONE);
            } else
                setUpFBLogin();
            getData();
        } else if (userType.equalsIgnoreCase(SharedPrefKeys.helperUser)) {
            FlurryAgent.logEvent("helper_edit_mode");
            tvTitle.setText(getResources().getString(R.string.edit_profile));
            setUpUserData();
            setUpFBLogin();
        }
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
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
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
                Uri photoURI = FileProvider.getUriForFile(ActivityProfileEdit.this,
                        "com.vysh.subairoma.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PIC);
            }

        }
    }

    private void setUpUserData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE);
        etNumber.setText(sharedPreferences.getString(SharedPrefKeys.userPhone, ""));
        etName.setText(sharedPreferences.getString(SharedPrefKeys.userName, ""));
        String sex = sharedPreferences.getString(SharedPrefKeys.userSex, "");
        String age = sharedPreferences.getString(SharedPrefKeys.userAge, "");
        String img = sharedPreferences.getString(SharedPrefKeys.userImg, "");
        if (img.length() > 10) {
            ivRegister.setImageBitmap(ImageEncoder.decodeFromBase64(img));
            encodedImage = img;
        }
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
                if (Profile.getCurrentProfile() == null) {
                    mProfileTracker = new ProfileTracker() {
                        @Override
                        protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                            mProfileTracker.stopTracking();
                            //get data here
                            Log.d("mylog", "In profile Tracker, User ID: " + Profile.getCurrentProfile().getId());
                            addFbIDToUID(Profile.getCurrentProfile().getId());
                        }
                    };
                    // no need to call startTracking() on mProfileTracker
                    // because it is called by its constructor, internally.
                } else {
                    Log.d("mylog", "Successful, User ID: " + Profile.getCurrentProfile().getId());
                    addFbIDToUID(Profile.getCurrentProfile().getId());
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

    private void addFbIDToUID(String id) {
        final String fbId = id;
        String api = ApplicationClass.getInstance().getAPIROOT() + apiaddFbId;
        final ProgressDialog progressDialog = new ProgressDialog(ActivityProfileEdit.this);
        progressDialog.setMessage(getResources().getString(R.string.adding_fb_acc));
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
                        showSnackbar(getResources().getString(R.string.fb_connected));

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
                if (userType.equalsIgnoreCase(SharedPrefKeys.migrantUser)) {
                    params.put("user_type", userType + "");
                    params.put("uid", ApplicationClass.getInstance().getMigrantId() + "");
                } else {
                    params.put("user_type", userType + "");
                    params.put("uid", ApplicationClass.getInstance().getUserId() + "");
                }
                for (Object obj : params.keySet()) {
                    Log.d("mylog", "KEY: " + obj + " VALUE: " + params.get(obj));
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
        RequestQueue queue = Volley.newRequestQueue(ActivityProfileEdit.this);
        checkRequest.setShouldCache(false);
        queue.add(checkRequest);
    }

    private void getData() {
        //Get Data
        MigrantModel migrantModel = new SQLDatabaseHelper(ActivityProfileEdit.this).getMigrantDetails();
        etName.setText(migrantModel.getMigrantName());
        etNumber.setText(migrantModel.getMigrantPhone());
        if (migrantModel.getMigImg() != null && migrantModel.getMigImg().length() > 10) {
            ivRegister.setImageBitmap(ImageEncoder.decodeFromBase64(migrantModel.getMigImg()));
            encodedImage = migrantModel.getMigImg();
        }
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("mylog", "On Activity Result");
        if (resultCode == RESULT_OK) {
            Log.d("mylog", "result is ok with request code : " + requestCode);
            if (requestCode == REQUEST_TAKE_PIC) {
                Log.d("mylog", "request was camera");
                Bitmap picTaken = getPic();
                picTaken = ImageRotator.getBitmapRotatedByDegree(picTaken, 90);
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

    private void encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        byte[] byte_arr = bytes.toByteArray();
        encodedImage = Base64.encodeToString(byte_arr, 0);
    }

    private Bitmap getPic() {
        Bitmap bitmap = BitmapFactory.decodeFile(pathToImage);
        return ImageResizer.calculateInSampleSize(bitmap);
    }

    private void updateUser() {
        String API = "";
        int id = -2;
        if (userType.equalsIgnoreCase(SharedPrefKeys.helperUser)) {
            API = ApplicationClass.getInstance().getAPIROOT() + apiUpdateUser;
            id = ApplicationClass.getInstance().getUserId();
        } else if (userType.equalsIgnoreCase(SharedPrefKeys.migrantUser)) {
            API = ApplicationClass.getInstance().getAPIROOT() + apiUpdateMigrant;
            id = ApplicationClass.getInstance().getMigrantId();
        }
        String name = etName.getText().toString();
        String age = etAge.getText().toString();
        String number = etNumber.getText().toString();
        String sex = "";
        if (rbFemale.isChecked()) sex = "female";
        else if (rbMale.isChecked()) sex = "male";
        if (InternetConnectionChecker.isNetworkConnected(ActivityProfileEdit.this))
            sendToServer(API, id, name, age, number, sex);
        else {
            if (userType.equalsIgnoreCase(SharedPrefKeys.helperUser)) {
                //Save the new user info in SharedPrefs
                SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(SharedPrefKeys.userName, name);
                editor.putString(SharedPrefKeys.userPhone, number);
                editor.putString(SharedPrefKeys.userSex, sex);
                editor.putString(SharedPrefKeys.userAge, age);
                editor.putString(SharedPrefKeys.userImg, encodedImage);
                editor.putString(SharedPrefKeys.userType, "helper");
                editor.commit();
            } else {
                showSnackbar(getString(R.string.server_noconnect));
            }
        }
    }

    private void sendToServer(String API, final int id, final String name, final String age,
                              final String number, final String sex) {
        final ProgressDialog progressDialog = new ProgressDialog(ActivityProfileEdit.this);
        //progressDialog.setTitle("Please wait");
        progressDialog.setMessage(getResources().getString(R.string.updating));
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
                    if (!error) {
                        if (userType.equalsIgnoreCase(SharedPrefKeys.helperUser)) {
                            //Save the new user info in SharedPrefs
                            SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(SharedPrefKeys.userName, name);
                            editor.putString(SharedPrefKeys.userPhone, number);
                            editor.putString(SharedPrefKeys.userSex, sex);
                            editor.putString(SharedPrefKeys.userAge, age);
                            editor.putString(SharedPrefKeys.userImg, encodedImage);
                            editor.putString(SharedPrefKeys.userType, "helper");
                            editor.commit();
                        } else {
                            new SQLDatabaseHelper(ActivityProfileEdit.this).
                                    insertMigrants(id, name, Integer.parseInt(age), number, sex, ApplicationClass.getInstance().getUserId(),
                                            encodedImage, -1);
                        }
                        Intent intent = new Intent(ActivityProfileEdit.this, ActivityMigrantList.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        showSnackbar(getResources().getString(R.string.failed_user_update));
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
                    showSnackbar(getResources().getString(R.string.server_noconnect));
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
                params.put("user_img", encodedImage);
                if (userType.equalsIgnoreCase(SharedPrefKeys.helperUser)) {
                    params.put("user_id", id + "");
                } else if (userType.equalsIgnoreCase(SharedPrefKeys.migrantUser)) {
                    params.put("migrant_id", id + "");
                    params.put("user_id", ApplicationClass.getInstance().getUserId() + "");
                }
                for (Object obj : params.keySet()) {
                    Log.d("mylog", "Key: " + obj + " Val: " + params.get(obj));
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