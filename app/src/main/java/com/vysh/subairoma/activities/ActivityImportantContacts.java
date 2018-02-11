package com.vysh.subairoma.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.SharedPrefKeys;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Vishal on 11/6/2017.
 */

public class ActivityImportantContacts extends AppCompatActivity {
    private final String importantContactsAPI = "/getimportantcontacts.php";

    @BindView(R.id.llNepalEmbassy)
    LinearLayout llNepalEmbassy;
    @BindView(R.id.llContact1)
    LinearLayout llContact1;
    @BindView(R.id.llContact2)
    LinearLayout llContact2;
    @BindView(R.id.llContact3)
    LinearLayout llContact3;
    @BindView(R.id.llContact4)
    LinearLayout llContact4;
    @BindView(R.id.llContact5)
    LinearLayout llContact5;

    @BindView(R.id.tvNepalEmbassy)
    TextView tvNepalEmbassy;
    @BindView(R.id.tvContact1)
    TextView tvContact1;
    @BindView(R.id.tvContact2)
    TextView tvContact2;
    @BindView(R.id.tvContact3)
    TextView tvContact3;
    @BindView(R.id.tvContact4)
    TextView tvContact4;
    @BindView(R.id.tvContact5)
    TextView tvContact5;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_important_contacts);
        ButterKnife.bind(this);
        String countryId = getIntent().getStringExtra("countryId");
        setUpInfo(countryId);
        getContacts();
    }

    public void setUpInfo(String cid) {
        Log.d("mylog", "Received Country Id: " + cid);
        String[] contacts = new SQLDatabaseHelper(this).getImportantContacts(cid);
        if (contacts != null)
            for (int i = 0; i < 4; i++) {
                String currentInfo = contacts[i];
                Log.d("mylog", "Curr Info: " + currentInfo);
                switch (i) {
                    case 0:
                        if (currentInfo != null && !currentInfo.isEmpty()) {
                            llNepalEmbassy.setVisibility(View.VISIBLE);
                            tvNepalEmbassy.setText(currentInfo);
                        }
                        break;
                    case 1:
                        if (currentInfo != null && !currentInfo.isEmpty()) {
                            llContact1.setVisibility(View.VISIBLE);
                            tvContact1.setText(currentInfo);
                        }
                        break;
                    case 2:
                        if (currentInfo != null && !currentInfo.isEmpty()) {
                            llContact2.setVisibility(View.VISIBLE);
                            tvContact2.setText(getResources().getString(R.string.address) + ": " + currentInfo);
                        }
                        break;
                    case 3:
                        if (currentInfo != null && !currentInfo.isEmpty()) {
                            llContact3.setVisibility(View.VISIBLE);
                            tvContact3.setText(getResources().getString(R.string.phone_number) + ": " + currentInfo);
                        }
                        break;
                    case 4:
                        if (currentInfo != null && !currentInfo.isEmpty()) {
                            llContact4.setVisibility(View.VISIBLE);
                            tvContact4.setText(getResources().getString(R.string.email) + ": " + currentInfo);
                        }
                        break;
                    case 5:
                        if (currentInfo != null && !currentInfo.isEmpty()) {
                            llContact5.setVisibility(View.VISIBLE);
                            tvContact5.setText(getResources().getString(R.string.website) + ": " + currentInfo);
                        }
                        break;
                }
            }
    }

    private void getContacts() {
        String api = ApplicationClass.getInstance().getAPIROOT() + importantContactsAPI;
        StringRequest getRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //SAVE RESPONSE IN LOCAL DB
                Log.d("mylog", "Got contacts: " + response);
                parseAndSaveContacts(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("mylog", "Error getting contacts: " + error.toString());
                Toast.makeText(ActivityImportantContacts.this, getString(R.string.server_noconnect), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> fParams = new HashMap<>();
                String lang = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE).getString(SharedPrefKeys.lang, "");
                fParams.put("lang", lang);
                return fParams;
            }
        };
        ;
        RequestQueue queue = Volley.newRequestQueue(ActivityImportantContacts.this);
        queue.add(getRequest);
    }

    private void parseAndSaveContacts(String response) {
        try {
            JSONObject jsonContacts = new JSONObject(response);
            boolean error = jsonContacts.getBoolean("error");
            if (error) {
                Log.d("mylog", "Error getting contacts: " + response);
            } else {
                SQLDatabaseHelper dbHelper = new SQLDatabaseHelper(ActivityImportantContacts.this);
                JSONArray contactsArray = jsonContacts.getJSONArray("contacts");
                for (int i = 0; i < contactsArray.length(); i++) {
                    JSONObject contactsObject = contactsArray.getJSONObject(i);
                    String cid = contactsObject.getString("country_id");
                    String title = contactsObject.getString("title");
                    String description = contactsObject.getString("description");
                    String address = contactsObject.getString("address");
                    String phone = contactsObject.getString("phone");
                    String email = contactsObject.getString("email");
                    String website = contactsObject.getString("website");
                    Log.d("mylog", cid);
                    dbHelper.insertImportantContacts(cid, title, description, address, phone, email, website);
                }
            }
        } catch (JSONException e) {
            Log.d("mylog", "Error parsing contacts: " + e.toString());
        }
    }
}
