package com.vysh.subairoma.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.vysh.subairoma.dialogs.DialogAnswersVerification;
import com.vysh.subairoma.dialogs.DialogCountryChooser;
import com.vysh.subairoma.imageHelpers.ImageEncoder;
import com.vysh.subairoma.utils.CustomTextView;
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
    @BindView(R.id.ivHam)
    ImageView ivHam;
    @BindView(R.id.drawerLayout)
    DrawerLayout drawerLayout;
    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.rlInfoScreen)
    RelativeLayout rlInstruction;
    @BindView(R.id.rlInfo1)
    RelativeLayout rlInfo1;
    @BindView(R.id.tv2)
    TextView tvinfo2;
    @BindView(R.id.tv3)
    TextView tvInfo3;
    @BindView(R.id.tv2desc)
    TextView tvDesc2;
    @BindView(R.id.tv3desc)
    TextView tvDesc3;
    @BindView(R.id.llinfo2)
    LinearLayout llinfo2;
    @BindView(R.id.llinfo3)
    LinearLayout llinfo3;
    @BindView(R.id.btnFirst)
    TextView btnFirstInfo;
    NavigationView navView;

    CustomTextView tvName, tvPhone, tvNavCounty;
    ImageView ivUserAvatar;
    int infoCount = 0;

    public String migName, migPhone, migrantGender, countryName, countryId, countryStatus, countryBlacklist;
    Intent intent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tile_chooser);
        ButterKnife.bind(this);
        navView = findViewById(R.id.nav_view);
        intent = getIntent();
        getRequiredData(intent);
        if (isInitialUse()) {
            showInformationOverlay();
        }
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
                if (countryName.length() < 2) {
                    Toast.makeText(ActivityTileChooser.this, getResources().getString(R.string.select_country), Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent1 = new Intent(ActivityTileChooser.this, ActivityTileHome.class);
                intent1.putExtras(intent.getExtras());
                intent1.putExtra("tiletype", "fep");
                startActivity(intent1);
            }
        });
        tileType2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (countryName.length() < 2) {
                    Toast.makeText(ActivityTileChooser.this, getResources().getString(R.string.select_country), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!checkIfVerifiedAnswers()) {
                   /* Toast.makeText(ActivityTileChooser.this, R.string.section_locked, Toast.LENGTH_SHORT).show();
                    return;*/
                    goToNextSection();
                    return;
                }
                Intent intent1 = new Intent(ActivityTileChooser.this, ActivityTileHome.class);
                intent1.putExtras(intent.getExtras());
                intent1.putExtra("tiletype", "gas");
                startActivity(intent1);
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
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
        setUpNavigationButtons();
    }

    private void showInformationOverlay() {
        rlInfo1.setVisibility(View.VISIBLE);
        tvinfo2.setVisibility(View.INVISIBLE);
        tvInfo3.setVisibility(View.INVISIBLE);
        tvDesc2.setVisibility(View.INVISIBLE);
        tvDesc3.setVisibility(View.INVISIBLE);
        llinfo2.setVisibility(View.INVISIBLE);
        llinfo3.setVisibility(View.INVISIBLE);
        btnFirstInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (infoCount) {
                    case 0:
                        infoCount++;
                        rlInfo1.setVisibility(View.INVISIBLE);
                        tvinfo2.setVisibility(View.VISIBLE);
                        tvDesc2.setVisibility(View.VISIBLE);
                        llinfo2.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        infoCount++;
                        tvinfo2.setVisibility(View.INVISIBLE);
                        tvDesc2.setVisibility(View.INVISIBLE);
                        llinfo2.setVisibility(View.INVISIBLE);

                        tvInfo3.setVisibility(View.VISIBLE);
                        tvDesc3.setVisibility(View.VISIBLE);
                        llinfo3.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        infoCount++;
                        rlInstruction.setVisibility(View.GONE);

                        SharedPreferences sp = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putBoolean(SharedPrefKeys.initialuser, false);
                        editor.apply();
                        Toast.makeText(ActivityTileChooser.this, getResources().getString(R.string.welcome_info), Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    private boolean isInitialUse() {
        SharedPreferences sp = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE);
        boolean isInitial = sp.getBoolean(SharedPrefKeys.initialuser, true);
        return isInitial;
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

    private void setUpNavigationButtons() {
        ivHam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        SharedPreferences sp = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE);
        String uname = sp.getString(SharedPrefKeys.userName, "");
        String uage = sp.getString(SharedPrefKeys.userAge, "");
        String unumber = sp.getString(SharedPrefKeys.userPhone, "");
        String uimg = sp.getString(SharedPrefKeys.userImg, "");

        View view = navView.getHeaderView(0);
        if (view == null)
            Log.d("mylog", "Header view Null");
        if (navView == null)
            Log.d("mylog", "Nav View Null");
        tvName = view.findViewById(R.id.tvMName);
        tvNavCounty = view.findViewById(R.id.tvMCountry);
        tvPhone = view.findViewById(R.id.tvMPhone);

        ivUserAvatar = view.findViewById(R.id.ivUserAva);

        tvName.setText(uname);
        tvNavCounty.setText(unumber);
        tvPhone.setText(uage);
        if (uimg != null && uimg.length() > 10)
            ivUserAvatar.setImageBitmap(ImageEncoder.decodeFromBase64(uimg));
        else
            ivUserAvatar.setImageResource(R.drawable.ic_male);

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_profile:
                        Intent intent = new Intent(ActivityTileChooser.this, ActivityProfileEdit.class);
                        intent.putExtra("userType", 1);
                        startActivity(intent);
                        break;
                    case R.id.nav_addmigrants:
                        Intent intentMig = new Intent(ActivityTileChooser.this, ActivityRegister.class);
                        intentMig.putExtra("migrantmode", true);
                        drawerLayout.closeDrawer(GravityCompat.END);
                        startActivity(intentMig);
                        break;
                    case R.id.delete_migrant:
                        deleteMigrant();
                        break;
                    case R.id.nav_about:
                        Intent intentAbout = new Intent(ActivityTileChooser.this, ActivityAboutUs.class);
                        drawerLayout.closeDrawer(GravityCompat.END);
                        startActivity(intentAbout);
                        break;
                    case R.id.nav_contact:
                        Intent intentContact = new Intent(ActivityTileChooser.this, ActivityAboutUs.class);
                        intentContact.putExtra("contact", true);
                        drawerLayout.closeDrawer(GravityCompat.END);
                        startActivity(intentContact);
                        break;
                    case R.id.nav_faq:
                        Intent intentFaq = new Intent(ActivityTileChooser.this, ActivityAboutUs.class);
                        intentFaq.putExtra("faq", true);
                        drawerLayout.closeDrawer(GravityCompat.END);
                        startActivity(intentFaq);
                        break;
                }
                return false;
            }
        });
    }

    private void deleteMigrant() {
        new SQLDatabaseHelper(ActivityTileChooser.this).insertMigrantDeletion(ApplicationClass.getInstance().getMigrantId()
                , ApplicationClass.getInstance().getUserId(), System.currentTimeMillis() + "");

        // showing snack bar with Undo option
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                Intent deleteMig = new Intent(ActivityTileChooser.this, ActivityMigrantList.class);
                deleteMig.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                drawerLayout.closeDrawer(GravityCompat.END);
                startActivity(deleteMig);
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                new SQLDatabaseHelper(ActivityTileChooser.this).insertMigrantDeletion(ApplicationClass.getInstance().getMigrantId(),
                        ApplicationClass.getInstance().getUserId(), "");
            }
        });
        builder.setMessage(R.string.mig_delete_confirmation);
        builder.show();
        builder.setCancelable(false);
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
