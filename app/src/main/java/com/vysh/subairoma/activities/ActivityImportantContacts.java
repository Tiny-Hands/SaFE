package com.vysh.subairoma.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.vysh.subairoma.adapters.ImportantContactsAdatper;
import com.vysh.subairoma.models.ImportantContactsModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Vishal on 11/6/2017.
 */

public class ActivityImportantContacts extends AppCompatActivity {
    private final String importantContactsAPI = "/getimportantcontacts.php";
    private final String importantContactsDefaultAPI = "/getimportantcontactsdefault.php";

    boolean finalSec;
    @BindView(R.id.rvContacts)
    RecyclerView rvContacts;
    @BindView(R.id.btnBack)
    ImageView btnBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_important_contacts);
        ButterKnife.bind(this);
        String countryId = getIntent().getStringExtra("countryId");
        finalSec = getIntent().getBooleanExtra("section", false);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        setUpInfo(countryId);
        getContacts(1);
        getContacts(2);
    }

    public void setUpInfo(String cid) {
        Log.d("mylog", "Received Country Id: " + cid);
        ArrayList<ImportantContactsModel> contactsModels = SQLDatabaseHelper.getInstance(this).getImportantContacts(cid);
        ArrayList<ImportantContactsModel> defaultContactsModels = SQLDatabaseHelper.getInstance(this).getDefaultImportantContacts();
        rvContacts.setLayoutManager(new LinearLayoutManager(this));
        if (finalSec) {
            contactsModels.addAll(defaultContactsModels);
            rvContacts.setAdapter(new ImportantContactsAdatper(contactsModels));
        } else {
            defaultContactsModels.addAll(contactsModels);
            rvContacts.setAdapter(new ImportantContactsAdatper(defaultContactsModels));
        }
    }

    private void getContacts(int type) {
        String api;
        if (type == 1)
            api = ApplicationClass.getInstance().getAPIROOT() + importantContactsAPI;
        else
            api = ApplicationClass.getInstance().getAPIROOT() + importantContactsDefaultAPI;
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
                SQLDatabaseHelper dbHelper = SQLDatabaseHelper.getInstance(ActivityImportantContacts.this);
                JSONArray contactsArray = jsonContacts.getJSONArray("contacts");
                for (int i = 0; i < contactsArray.length(); i++) {
                    JSONObject contactsObject = contactsArray.getJSONObject(i);
                    String cid;
                    if (contactsObject.has("country_id"))
                        cid = contactsObject.getString("country_id");
                    else
                        cid = "default";

                    int contactId;
                    if (contactsObject.has("contact_id"))
                        contactId = contactsObject.getInt("contact_id");
                    else
                        contactId = contactsObject.getInt("id");

                    String title = contactsObject.getString("title");
                    String description = contactsObject.getString("description");
                    String address = contactsObject.getString("address");
                    String phone = contactsObject.getString("phone");
                    String email = contactsObject.getString("email");
                    String website = contactsObject.getString("website");
                    Log.d("mylog", "Current CID: " + cid);
                    if (cid.equalsIgnoreCase("default")) {
                        dbHelper.insertImportantContactsDefault(contactId, title, description, address, phone, email, website);
                        Log.d("mylog", "Inserting Default: " + title);
                    } else
                        dbHelper.insertImportantContacts(contactId, cid, title, description, address, phone, email, website);
                }
            }
        } catch (JSONException e) {
            Log.d("mylog", "Error parsing contacts: " + e.toString());
        }
    }
}
