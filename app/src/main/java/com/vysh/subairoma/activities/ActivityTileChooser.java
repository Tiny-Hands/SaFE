package com.vysh.subairoma.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.util.DisplayMetrics;
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
import com.facebook.share.Share;
import com.flurry.android.FlurryAgent;
import com.google.android.material.navigation.NavigationView;
import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.SharedPrefKeys;
import com.vysh.subairoma.dialogs.DialogAnswersVerification;
import com.vysh.subairoma.dialogs.DialogCountryChooser;
import com.vysh.subairoma.dialogs.DialogUsertypeChooser;
import com.vysh.subairoma.imageHelpers.ImageEncoder;
import com.vysh.subairoma.utils.CustomTextView;
import com.vysh.subairoma.utils.PercentHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Vishal on 8/25/2018.
 */

public class ActivityTileChooser extends AppCompatActivity {
    final String userTypeAPI = "/updateusertype.php";
    @BindView(R.id.ivUserAvatar)
    CircleImageView ivMigrantImage;
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
        FlurryAgent.logEvent("tile_type_listing");
        getRequiredData(intent);

        tvMigName.setText(migName);
        tvMigNumber.setText(migPhone);
        tvCountry.setText(countryName);
        tileType1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (countryName.length() < 2) {
                    FlurryAgent.logEvent("fep_without_country_selection");
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
                    goToNextSectionProcess();
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

        String img = SQLDatabaseHelper.getInstance(ActivityTileChooser.this).getMigrantImg(ApplicationClass.getInstance().getMigrantId());
        if (img != null && img.length() > 5)
            ivMigrantImage.setImageBitmap(ImageEncoder.decodeFromBase64(img));
        else if (migrantGender != null && migrantGender.equalsIgnoreCase("female"))
            ivMigrantImage.setImageResource(R.drawable.ic_female);
        ivMigrantImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityTileChooser.this, ActivityProfileEdit.class);
                intent.putExtra("userType", SharedPrefKeys.migrantUser);
                startActivity(intent);
            }
        });
        hideIfIndia();
        setUpNavigationButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setPercentCompletion();
        //For some reason Language changing when back pressed, hence rechecking
        if (getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE).getString(SharedPrefKeys.lang, "").equalsIgnoreCase("en")) {
            setLocale("en");
        } else {
            setLocale("np");
        }
    }

    private void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

    private boolean isInitialUse() {
        SharedPreferences sp = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE);
        return sp.getBoolean(SharedPrefKeys.initialuser, true);
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
        if (uimg.length() > 10)
            ivUserAvatar.setImageBitmap(ImageEncoder.decodeFromBase64(uimg));
        else
            ivUserAvatar.setImageResource(R.drawable.ic_male);

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_profile:
                        Intent intent = new Intent(ActivityTileChooser.this, ActivityProfileEdit.class);
                        intent.putExtra("userType", SharedPrefKeys.helperUser);
                        startActivity(intent);
                        break;
                    case R.id.nav_addmigrants:
                        Intent intentMig = new Intent(ActivityTileChooser.this, ActivityRegisterMigrant.class);
                        drawerLayout.closeDrawer(GravityCompat.END);
                        startActivity(intentMig);
                        break;
                    case R.id.update_usertype:
                        DialogUsertypeChooser chooser = new DialogUsertypeChooser();
                        chooser.show(getFragmentManager(), "userchooser");
                        drawerLayout.closeDrawer(GravityCompat.END);
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
                    case R.id.nav_tutorial:
                        FlurryAgent.logEvent("rewatch_tutorial_selection");
                        drawerLayout.closeDrawer(GravityCompat.END);
                        //rlInstruction.setVisibility(View.VISIBLE);
                        //showInformationOverlay();
                        break;
                }
                return false;
            }
        });
    }

    private void deleteMigrant() {
        SQLDatabaseHelper.getInstance(ActivityTileChooser.this).insertMigrantDeletion(ApplicationClass.getInstance().getMigrantId()
                , ApplicationClass.getInstance().getSafeUserId(), System.currentTimeMillis() + "");

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
                SQLDatabaseHelper.getInstance(ActivityTileChooser.this).insertMigrantDeletion(ApplicationClass.getInstance().getMigrantId(),
                        ApplicationClass.getInstance().getSafeUserId(), "");
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
            Log.d("mylog", "Percent Complete: " + percentComp);

            if (!percentComp.equalsIgnoreCase("NaN") && Integer.parseInt(percentComp) > 0) {
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
        String verified = SQLDatabaseHelper.getInstance(ActivityTileChooser.this).getResponse(ApplicationClass.getInstance().getMigrantId(),
                "mg_verified_answers");
//        String isFeedbackSaved = SQLDatabaseHelper.getInstance(ActivityTileChooser.this).getResponse(ApplicationClass.getInstance().getMigrantId(),
//                "mg_feedback_saved");
        if (verified.equalsIgnoreCase("true"))
            return true;
        return false;
    }

    public void goToNextSectionProcess() {
        FlurryAgent.logEvent("next_section_click");
        if (!checkIfVerifiedAnswers())
            new DialogAnswersVerification().show(getSupportFragmentManager(), "dialog");
        else {
            Toast.makeText(ActivityTileChooser.this, "Completed", Toast.LENGTH_SHORT).show();
        }
        //getAllResponses();
    }

    public void updateUserType(String userType) {
        final ProgressDialog progressDialog = new ProgressDialog(ActivityTileChooser.this);
        //progressDialog.setTitle("Please wait");
        progressDialog.setMessage(getResources().getString(R.string.generic_updating));
        progressDialog.setCancelable(false);
        progressDialog.show();
        final HashMap<String, String> fParams = new HashMap<>();
        fParams.put("user_id", ApplicationClass.getInstance().getSafeUserId() + "");
        fParams.put("user_type", userType);
        String api = ApplicationClass.getInstance().getAPIROOT() + userTypeAPI;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Log.d("mylog", "Country Response: " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String err = error.toString();
                progressDialog.dismiss();
                Toast.makeText(ActivityTileChooser.this, getResources().getString(R.string.failed_user_update), Toast.LENGTH_SHORT).show();
                if (!err.isEmpty() && err.contains("NoConnection")) {
                    //showSnackbar("Response cannot be saved at the moment, please check your Intenet connection.");
                    Log.d("mylog", "couldn't save country");
                }
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return fParams;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();

                String userToken = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE).getString(SharedPrefKeys.token, "");
                headers.put("Authorization", userToken);
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(ActivityTileChooser.this);
        queue.add(stringRequest);

    }
}
