package com.vysh.subairoma;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.vysh.subairoma.adapters.MigrantListAdapter;
import com.vysh.subairoma.models.MigrantModel;
import com.vysh.subairoma.volley.VolleyController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Vishal on 6/16/2017.
 */

public class ActivityMigrantList extends AppCompatActivity {
    private final String API = "/getmigrants.php";

    @BindView(R.id.rvMigrants)
    RecyclerView recyclerView;
    @BindView(R.id.btnAddMigrant)
    Button btnAddMigrant;
    @BindView(R.id.rlRoot)
    RelativeLayout rootLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_migrant);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ButterKnife.bind(this);

        getMigrants();
        //setUpRecyclerView(null);
        btnAddMigrant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityMigrantList.this, ActivityRegister.class);
                intent.putExtra("migrantmode", true);
                startActivity(intent);
            }
        });
    }

    private void getMigrants() {
        String api = ApplicationClass.getInstance().getAPIROOT() + API;
        final ProgressDialog progressDialog = new ProgressDialog(ActivityMigrantList.this);
        progressDialog.setTitle("Please wait");
        progressDialog.setMessage("Getting migrants...");
        progressDialog.show();
        StringRequest getRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Log.d("mylog", "response : " + response);
                ArrayList<MigrantModel> migrantModels = parseResponse(response);
                setUpRecyclerView(migrantModels);
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
                params.put("user_id", ApplicationClass.getInstance().getUserId() + "");
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(ActivityMigrantList.this);
        queue.add(getRequest);
    }

    private ArrayList<MigrantModel> parseResponse(String response) {
        ArrayList<MigrantModel> migrantsModels = new ArrayList<>();
        try {
            JSONObject jsonObject = new JSONObject(response);
            Boolean error = jsonObject.getBoolean("error");
            if (error) {
                showSnackbar(jsonObject.getString("message"));
            } else {
                JSONArray migrantJSON = jsonObject.getJSONArray("migrants");
                if (migrantJSON != null) {
                    JSONObject migrantObj;
                    for (int i = 0; i < migrantJSON.length(); i++) {
                        migrantObj = migrantJSON.getJSONObject(i);
                        MigrantModel migrantModel = new MigrantModel();
                        if (migrantObj.has("migrant_id")) {
                            migrantModel.setMigrantId(migrantObj.getInt("migrant_id"));
                            migrantModel.setMigrantName(migrantObj.getString("migrant_name"));
                            migrantModel.setMigrantAge(migrantObj.getInt("migrant_age"));
                            migrantModel.setMigrantSex(migrantObj.getString("migrant_sex"));
                            migrantsModels.add(migrantModel);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            Log.d("mylog", "Error in parsing: " + e.toString());
        }
        return migrantsModels;
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

    private void setUpRecyclerView(ArrayList<MigrantModel> migrantModels) {
        recyclerView.setLayoutManager(new LinearLayoutManager(ActivityMigrantList.this));
        MigrantListAdapter migrantListAdapter = new MigrantListAdapter();
        Log.d("mylog", "Number of migrants: " + migrantModels.size());
        migrantListAdapter.setMigrants(migrantModels);
        recyclerView.setAdapter(migrantListAdapter);
    }
}
