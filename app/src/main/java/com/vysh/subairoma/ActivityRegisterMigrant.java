package com.vysh.subairoma;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import com.facebook.login.widget.LoginButton;
import com.flurry.android.FlurryAgent;
import com.vysh.subairoma.activities.ActivityMigrantList;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;

public class ActivityRegisterMigrant extends AppCompatActivity {
    final String apiURLMigrant = "/savemigrant.php";
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_migrant_add);

        ButterKnife.bind(this);
        FlurryAgent.logEvent("register_migrant");
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateData()) {
                    HashMap<String, String> params = new HashMap<>();
                    params.put("full_name", etName.getText().toString());
                    params.put("phone_number", etName.getText().toString());
                    params.put("age", etAge.getText().toString());
                    String gender = "male";
                    if (rbFemale.isChecked())
                        gender = "female";
                    params.put("gender", gender);
                    params.put("user_id", ApplicationClass.getInstance().getSafeUserId() + "");
                    params.put("user_img", "");
                    saveMigrant(params);
                }
            }
        });
        btnAlreadyRegistered.setVisibility(GONE);
        loginButton.setVisibility(GONE);
        tvOr.setVisibility(GONE);
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
                        Intent intent = new Intent(ActivityRegisterMigrant.this, ActivityMigrantList.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                } catch (JSONException e) {
                    Toast.makeText(ActivityRegisterMigrant.this, getString(R.string.failed_user_update), Toast.LENGTH_SHORT).show();
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
                Log.d("mylog", "putting authheader" + sp.getString(SharedPrefKeys.token, ""));
                headers.put("Authorization", sp.getString(SharedPrefKeys.token, ""));
                return headers;
            }
        };
        saveRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue requestQueue = Volley.newRequestQueue(ActivityRegisterMigrant.this);
        requestQueue.add(saveRequest);

    }
}
