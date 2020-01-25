package com.vysh.subairoma;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.login.widget.LoginButton;
import com.flurry.android.FlurryAgent;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.activities.ActivityMigrantList;
import com.vysh.subairoma.activities.ActivityRegister;
import com.vysh.subairoma.imageHelpers.ImageResizer;
import com.vysh.subairoma.imageHelpers.ImageSaveHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;

public class ActivityRegisterMigrant extends AppCompatActivity {
    final String apiURLMigrant = "/savesafemigrant.php";
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
    String gender;
    private final int REQUEST_SELECT_FILE = 1;
    private final int REQUEST_TAKE_PIC = 0;
    String pathToImage, encodedImage = " ";
    final static int PERMISSION_ALL = 2;
    final static String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_migrant_add);

        ButterKnife.bind(this);
        Log.d("mylog", "Register Activity for Migrants");
        FlurryAgent.logEvent("register_migrant");
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateData()) {
                    HashMap<String, String> params = new HashMap<>();
                    params.put("full_name", etName.getText().toString());
                    params.put("phone_number", etNumber.getText().toString());
                    params.put("age", etAge.getText().toString());
                    gender = "male";
                    if (rbFemale.isChecked())
                        gender = "female";
                    params.put("gender", gender);
                    params.put("user_id", ApplicationClass.getInstance().getSafeUserId() + "");
                    Log.d("mylog", "Using User ID: " + ApplicationClass.getInstance().getSafeUserId());
                    params.put("user_img", encodedImage);
                    saveMigrant(params);
                }
            }
        });
        btnAlreadyRegistered.setVisibility(GONE);
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

    private void saveMigrant(HashMap params) {
        final ProgressDialog progressDialog = new ProgressDialog(ActivityRegisterMigrant.this);
        //progressDialog.setTitle("Please wait");
        progressDialog.setMessage(getResources().getString(R.string.registering));
        progressDialog.setCancelable(false);
        progressDialog.show();
        String api = ApplicationClass.getInstance().getAPIROOT() + apiURLMigrant;
        StringRequest saveRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("mylog", "response : " + response);
                progressDialog.dismiss();
                try {
                    JSONObject jsonObj = new JSONObject(response);
                    boolean error = jsonObj.getBoolean("error");
                    if (error)
                        Toast.makeText(ActivityRegisterMigrant.this, jsonObj.getString("message"), Toast.LENGTH_SHORT).show();
                    else {
                        parseResponse(response);
                    }
                } catch (JSONException e) {
                    //Toast.makeText(ActivityRegisterMigrant.this, getString(R.string.), Toast.LENGTH_SHORT).show();
                    Log.d("mylog", "Exceptions: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(ActivityRegisterMigrant.this, getString(R.string.server_noconnect), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                SharedPreferences sp = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE);
                Log.d("mylog", "putting authheader " + sp.getString(SharedPrefKeys.token, ""));
                headers.put("Authorization", sp.getString(SharedPrefKeys.token, ""));
                return headers;
            }
        };
        saveRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = Volley.newRequestQueue(ActivityRegisterMigrant.this);
        requestQueue.add(saveRequest);
    }

    private void parseResponse(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            Boolean error = jsonObject.getBoolean("error");
            if (!error) {
                int mig_id = jsonObject.getInt("migrant_id");
                Calendar cal = Calendar.getInstance();
                String time = cal.getTimeInMillis() + "";
                SQLDatabaseHelper.getInstance(ActivityRegisterMigrant.this).insertResponseTableData(gender, SharedPrefKeys.questionGender, -1, mig_id, "mg_sex", time);
                SQLDatabaseHelper.getInstance(ActivityRegisterMigrant.this).insertMigrants(mig_id, etName.getText().toString(),
                        Integer.parseInt(etAge.getText().toString()), etNumber.getText().toString(), gender, ApplicationClass.getInstance().getSafeUserId(), encodedImage, 0);
                Intent intent = new Intent(ActivityRegisterMigrant.this, ActivityMigrantList.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            } else {
                String message = jsonObject.getString("message");
                Toast.makeText(ActivityRegisterMigrant.this, message, Toast.LENGTH_LONG).show();
            }
        } catch (JSONException e) {
            Log.d("mylog", "Error in parsing migrant result: " + e.toString());
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
            }
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
                Uri photoURI = FileProvider.getUriForFile(ActivityRegisterMigrant.this,
                        "com.vysh.subairoma.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PIC);
            }

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
}
