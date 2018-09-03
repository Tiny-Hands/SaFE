package com.vysh.subairoma.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
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
import com.vysh.subairoma.dialogs.DialogAnswersVerification;
import com.vysh.subairoma.dialogs.DialogCountryChooser;
import com.vysh.subairoma.utils.PercentHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Vishal on 8/25/2018.
 */

public class ActivityTileChooser extends AppCompatActivity {
    private final String saveAPI = "/saveresponse.php";
    private final String saveFeedbackAPI = "/savefeedbackresponse.php";

    @BindView(R.id.tvMigNumber)
    TextView tvMigNumber;
    @BindView(R.id.tvMigrantName)
    TextView tvMigName;
    @BindView(R.id.tvCountryName)
    TextView tvCountry;
    @BindView(R.id.tvPercentComplete1)
    TextView tvPercentComp1;
    @BindView(R.id.tvPercentComplete2)
    TextView tvPercentComp2;
    @BindView(R.id.ivImpContacts)
    ImageView ivImpContacts;
    @BindView(R.id.ivCountry)
    ImageView ivSelectCountry;
    @BindView(R.id.tiletype1)
    ImageView tileType1;
    @BindView(R.id.tiletype2)
    ImageView tileType2;
    @BindView(R.id.btnNextSections)
    ImageButton btnNext;
    @BindView(R.id.progressSection1)
    ProgressBar progressBar1;
    @BindView(R.id.progressSection2)
    ProgressBar progressBar2;
    @BindView(R.id.llTravel)
    LinearLayout llTravel;

    String migName, migPhone, migrantGender, countryName, countryId, countryStatus, countryBlacklist;
    Intent intent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tile_chooser);
        ButterKnife.bind(this);
        intent = getIntent();
        getRequiredData(intent);
        tvMigName.setText(migName);
        tvMigNumber.setText(migPhone);
        tvCountry.setText(countryName);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNextSection();
            }
        });
        tileType1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent1 = new Intent(ActivityTileChooser.this, ActivityTileHome.class);
                intent1.putExtras(intent.getExtras());
                intent1.putExtra("tiletype", "fep");
                startActivity(intent1);
            }
        });
        tileType2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkIfVerifiedAnswers()) {
                    Toast.makeText(ActivityTileChooser.this, R.string.section_locked, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent1 = new Intent(ActivityTileChooser.this, ActivityTileHome.class);
                intent1.putExtras(intent.getExtras());
                intent1.putExtra("tiletype", "gas");
                startActivity(intent1);
            }
        });
        ivSelectCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCountryChooser();
            }
        });
        ivImpContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityTileChooser.this, ActivityImportantContacts.class);
                intent.putExtra("countryId", countryId);
                intent.putExtra("section", checkIfVerifiedAnswers());
                startActivity(intent);
            }
        });

        hideIfIndia();
        setPercentCompletion();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        countryName = intent.getStringExtra("countryName");
        countryStatus = intent.getStringExtra("countryStatus");
        countryId = intent.getStringExtra("countryId");
        countryBlacklist = intent.getStringExtra("countryBlacklist");
        Log.d("mylog", "In new Intent");
    }

    private void hideIfIndia() {
        if (countryId.equalsIgnoreCase("in")) {
            llTravel.setVisibility(View.GONE);
            btnNext.setVisibility(View.GONE);
        }
    }

    private void getRequiredData(Intent intent) {
        migName = intent.getStringExtra("migrantName");
        migrantGender = intent.getStringExtra("migrantGender");
        migPhone = intent.getStringExtra("migrantPhone");
        countryName = intent.getStringExtra("countryName");
        countryStatus = intent.getStringExtra("countryStatus");
        countryId = intent.getStringExtra("countryId");
        countryBlacklist = intent.getStringExtra("countryBlacklist");
    }

    private void setPercentCompletion() {
        if (countryId.equalsIgnoreCase("in")) {
            Log.d("mylog", "Getting GIS Tiles");
            String percentComp = PercentHelper.getPercentCompleteBySection(ActivityTileChooser.this,
                    ApplicationClass.getInstance().getMigrantId(), "GIS");
            tvPercentComp1.setText(percentComp + "% " + getResources().getString(R.string.complete));
            if (Integer.parseInt(percentComp) > 0) {
                progressBar1.setVisibility(View.VISIBLE);
                progressBar1.setProgress(Integer.parseInt(percentComp));
            }
        } else {
            String percentComp = PercentHelper.getPercentCompleteBySection(ActivityTileChooser.this,
                    ApplicationClass.getInstance().getMigrantId(), "FEP");
            tvPercentComp1.setText(percentComp + "% " + getResources().getString(R.string.complete));
            if (Integer.parseInt(percentComp) > 0) {
                progressBar1.setVisibility(View.VISIBLE);
                progressBar1.setProgress(Integer.parseInt(percentComp));
            }
            if (checkIfVerifiedAnswers()) {
                //Get percent Complete of GAS
                String percentComp2 = PercentHelper.getPercentCompleteBySection(ActivityTileChooser.this,
                        ApplicationClass.getInstance().getMigrantId(), "GAS");
                if (Integer.parseInt(percentComp2) > 0) {
                    progressBar2.setVisibility(View.VISIBLE);
                    progressBar2.setProgress(Integer.parseInt(percentComp2));
                }
                tvPercentComp2.setText(percentComp2 + "% " + getResources().getString(R.string.complete));
                tileType2.setImageResource(R.drawable.ic_traveltile);
            }
        }
    }

    private void showCountryChooser() {
        DialogCountryChooser dialog = DialogCountryChooser.newInstance();
        dialog.setMigrantName(tvMigName.getText().toString());
        dialog.show(getSupportFragmentManager(), "tag");
    }

    private boolean checkIfVerifiedAnswers() {
        String verified = new SQLDatabaseHelper(ActivityTileChooser.this).getResponse(ApplicationClass.getInstance().getMigrantId(),
                "mg_verified_answers");
//        String isFeedbackSaved = new SQLDatabaseHelper(ActivityTileChooser.this).getResponse(ApplicationClass.getInstance().getMigrantId(),
//                "mg_feedback_saved");
        if (verified.equalsIgnoreCase("true"))
            return true;
        return false;
    }

    public void goToNextSection() {
        if (!checkIfVerifiedAnswers())
            new DialogAnswersVerification().show(getSupportFragmentManager(), "dialog");
        else {
            Toast.makeText(ActivityTileChooser.this, "Completed", Toast.LENGTH_SHORT).show();
        }
        getAllResponses();
    }

    private void getAllResponses() {
        int migId = ApplicationClass.getInstance().getMigrantId();
        if (migId > 0) {
            ArrayList<HashMap> allParams = new SQLDatabaseHelper(ActivityTileChooser.this)
                    .getAllResponse(migId);
            for (int i = 0; i < allParams.size(); i++) {
                //Log.d("mylog", "Saving to server: " + i);
                saveResponseToServer(allParams.get(i), 1);
            }
        } else
            Log.d("mylog", "MigID: " + migId + " Not saving to server");
    }

    private void saveResponseToServer(final HashMap<String, String> fParams, int responseType) {
        String api;
        if (responseType == 1)
            api = ApplicationClass.getInstance().getAPIROOT() + saveAPI;
        else
            api = ApplicationClass.getInstance().getAPIROOT() + saveFeedbackAPI;
        final String fapi = api;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("mylog", "Response: " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String err = error.toString();
                if (!err.isEmpty() && err.contains("NoConnection")) {
                    //showSnackbar("Response cannot be saved at the moment, please check your Intenet connection.");
                    Log.d("mylog", "Response cannot be saved at the moment, please check your Intenet connection.");
                } else
                    Log.d("mylog", "Error saving response: " + err + " \n For: " + fapi);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                for (Object key : fParams.keySet()) {
                    Log.d("mylog", "Key Values: " + key + ": " + fParams.get(key));
                }
                return fParams;
            }
        };
        Log.d("mylog", "Calling: " + api);
        RequestQueue queue = Volley.newRequestQueue(ActivityTileChooser.this);
        queue.add(stringRequest);
    }
}
