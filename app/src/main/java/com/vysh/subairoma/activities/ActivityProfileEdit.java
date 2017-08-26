package com.vysh.subairoma.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
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
import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.R;
import com.vysh.subairoma.utils.CustomTextView;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Vishal on 8/25/2017.
 */

public class ActivityProfileEdit extends AppCompatActivity implements View.OnClickListener {

    final String apiUpdateUser = "/updateuser.php";
    final String apiUpdateMigrant = "/updatemigrant.php";

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
    @BindView(R.id.tvHint)
    TextView tvHint;
    @BindView(R.id.rbMale)
    RadioButton rbMale;
    @BindView(R.id.rbFemale)
    RadioButton rbFemale;
    @BindView(R.id.rlRoot)
    RelativeLayout rootLayout;
    @BindView(R.id.btnAlreadyRegistered)
    Button btnAlreadyRegistered;

    int userType = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ButterKnife.bind(this);
        userType = getIntent().getIntExtra("userType", 0);

        tvHint.setText("Edit Details");
        btnAlreadyRegistered.setVisibility(View.GONE);
        getData();
    }

    private void getData() {
        //Get Data
        etNumber.setText("Saved Number");
        etName.setText("Saved Number");
        etAge.setText("Saved Age");
        tvTitle.setText("PROFILE");
        btnNext.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (validateData()) {
            updateUser();
        }
    }

    private void updateUser() {
        String API = "";
        int id = -2;
        if (userType == 1) {
            API = ApplicationClass.getInstance().getAPIROOT() + apiUpdateUser;
            id = ApplicationClass.getInstance().getUserId();
        } else if (userType == 2) {
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

    private void sendToServer(String API, final int id, final String name, final String age, final String number, final String sex) {
        final ProgressDialog progressDialog = new ProgressDialog(ActivityProfileEdit.this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Updating...");
        progressDialog.setCancelable(false);
        progressDialog.show();
        StringRequest saveRequest = new StringRequest(Request.Method.POST, API, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Intent intent = new Intent(ActivityProfileEdit.this, ActivityMigrantList.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                Log.d("mylog", "response : " + response);
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
                if (userType == 1) {
                    params.put("user_id", id + "");
                } else if (userType == 2) {
                    params.put("migrant_id", id + "");
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