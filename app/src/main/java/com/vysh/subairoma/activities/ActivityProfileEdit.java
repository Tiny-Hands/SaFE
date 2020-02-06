package com.vysh.subairoma.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.util.Base64;
import android.util.Log;
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
import com.facebook.login.widget.LoginButton;
import com.flurry.android.FlurryAgent;
import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.SharedPrefKeys;
import com.vysh.subairoma.imageHelpers.ImageEncoder;
import com.vysh.subairoma.imageHelpers.ImageResizer;
import com.vysh.subairoma.imageHelpers.ImageSaveHelper;
import com.vysh.subairoma.models.MigrantModel;
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
    final String apiUpdateMigrant = "/updatesafemigrant.php";
    final String apiUpdateHelper = "/updatesafeuser.php";

    private final int REQUEST_SELECT_FILE = 1;
    private final int REQUEST_TAKE_PIC = 0;
    String pathToImage, encodedImage = "";
    final static int PERMISSION_ALL = 2;
    final static String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};

    boolean editingHelper = false;
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
    @BindView(R.id.fb_login_button)
    LoginButton loginButton;
    @BindView(R.id.tvOR)
    TextView tvOr;

    CallbackManager callbackManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_migrant_add);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        String uType = intent.getStringExtra("userType");
        if (uType.equalsIgnoreCase(SharedPrefKeys.helperUser)) {
            tvTitle.setText(getResources().getString(R.string.edit_profile));
            FlurryAgent.logEvent("safe_user_edit_mode");
            setUpUserData();
            editingHelper = true;
        } else {
            tvTitle.setText(getResources().getString(R.string.edit_migrant));
            FlurryAgent.logEvent("migrant_edit_mode");
            editingHelper = false;
            getData();
        }
        //tvHint.setText("Edit Details");
        btnAlreadyRegistered.setVisibility(GONE);
        btnNext.setText(R.string.save);
        btnNext.setOnClickListener(this);
        loginButton.setVisibility(GONE);
        tvOr.setVisibility(GONE);
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

    private void saveUserData(String sex) {
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPrefKeys.userAge, etAge.getText().toString());
        editor.putString(SharedPrefKeys.userName, etName.getText().toString());
        editor.putString(SharedPrefKeys.userPhone, etNumber.getText().toString());
        editor.putString(SharedPrefKeys.userSex, sex);
        editor.commit();
    }

    private void getData() {
        //Get Data
        MigrantModel migrantModel = SQLDatabaseHelper.getInstance(ActivityProfileEdit.this).getMigrantDetails();
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

    private void updateUser() {
        int id = -2;
        String sex = "";
        if (rbFemale.isChecked()) sex = "female";
        else if (rbMale.isChecked()) sex = "male";

        String name = etName.getText().toString();
        String age = etAge.getText().toString();
        String number = etNumber.getText().toString();
        if (InternetConnectionChecker.isNetworkConnected(ActivityProfileEdit.this))
            sendToServer(id, name, age, number, sex);
        else {
            showSnackbar(getString(R.string.server_noconnect));
        }
    }

    private void sendToServer(final int id, final String name, final String age,
                              final String number, final String sex) {
        final ProgressDialog progressDialog = new ProgressDialog(ActivityProfileEdit.this);
        //progressDialog.setTitle("Please wait");
        progressDialog.setMessage(getResources().getString(R.string.updating));
        progressDialog.setCancelable(false);
        progressDialog.show();
        String API;
        if (editingHelper)
            API = ApplicationClass.getInstance().getAPIROOT() + apiUpdateHelper;
        else
            API = ApplicationClass.getInstance().getAPIROOT() + apiUpdateMigrant;
        StringRequest saveRequest = new StringRequest(Request.Method.POST, API, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                try {
                    Log.d("mylog", "response : " + response);
                    JSONObject jsonObject = new JSONObject(response);
                    Boolean error = jsonObject.getBoolean("error");
                    if (!error) {
                        if (!editingHelper) {
                            SQLDatabaseHelper.getInstance(ActivityProfileEdit.this).
                                    insertMigrants(id, name, Integer.parseInt(age), number, sex, ApplicationClass.getInstance().getSafeUserId(),
                                            encodedImage, -1);
                        } else
                            saveUserData(sex);
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
                params.put("user_age", age);
                params.put("user_sex", sex);
                params.put("user_img", encodedImage);
                if (editingHelper)
                    params.put("user_id", ApplicationClass.getInstance().getSafeUserId() + "");
                else {
                    params.put("migrant_id", ApplicationClass.getInstance().getMigrantId() + "");
                    params.put("user_id", ApplicationClass.getInstance().getSafeUserId() + "");
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
        saveRequest.setRetryPolicy(new DefaultRetryPolicy(60 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
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
      /*  Snackbar snack = Snackbar.make(rootLayout, msg, Snackbar.LENGTH_LONG);
        View view = snack.getView();
        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        if (Build.VERSION.SDK_INT >= 17)
            tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        snack.show();*/

        Toast.makeText(ActivityProfileEdit.this, msg, Toast.LENGTH_SHORT).show();
    }

}