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
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.flurry.android.FlurryAgent;
import com.google.android.material.navigation.NavigationView;
import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.SharedPrefKeys;
import com.vysh.subairoma.dialogs.DialogAnswersVerification;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.adapters.TileAdapter;
import com.vysh.subairoma.imageHelpers.ImageEncoder;
import com.vysh.subairoma.models.MigrantModel;
import com.vysh.subairoma.models.TilesModel;
import com.vysh.subairoma.utils.CustomTextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Vishal on 6/12/2017.
 */

public class ActivityTileHome extends AppCompatActivity {
    final String apiURLMigrantPercent = "/updatepercentcomplete.php";
    ArrayList<TilesModel> tiles;
    public String migName, countryName, countryId, migGender = "", migPhone;
    public int blacklist, status, totalResponseCount = 0, currentSavedCount = 0;
    int[] tileIcons;
    TileAdapter tileAdapter;
    public static Boolean finalSection, showIndia;
    String uname, unumber, uage, uimg, uavatar, tileType, userToken;
    public String section;

    @BindView(R.id.ivUserAvatar)
    CircleImageView ivMigrantImage;
    @BindView(R.id.rvTiles)
    RecyclerView rvTiles;
    @BindView(R.id.rlRoot)
    RelativeLayout rootLayout;
    @BindView(R.id.tvMigrantName)
    TextView tvMigrantName;
    @BindView(R.id.tvMigNumber)
    TextView tvCountry;
    @BindView(R.id.btnNext)
    Button btnNext;
    @BindView(R.id.tvPercent)
    TextView tvPercent;
    @BindView(R.id.progressPercent)
    ProgressBar progressPercent;
    //@BindView(R.id.btnImportantContacts)
    //CardView btnImportantContacts;
    //@BindView(R.id.ivAvatar)
    //ImageView ivAvatar;
    @BindView(R.id.ivHam)
    ImageView ivHam;
    @BindView(R.id.nsv)
    NestedScrollView nsv;
    @BindView(R.id.drawerLayout)
    DrawerLayout drawerLayout;
    @BindView(R.id.btnBack)
    ImageView btnBack;
    NavigationView navView;

    CustomTextView tvName, tvPhone, tvNavCounty;
    ImageView ivUserAvatar;
    RequestQueue queue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tile_home);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ButterKnife.bind(this);
        userToken = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE).getString(SharedPrefKeys.token, "");
        queue = Volley.newRequestQueue(ActivityTileHome.this);
        countryId = getIntent().getStringExtra("countryId");
        migName = getIntent().getStringExtra("migrantName");
        migGender = getIntent().getStringExtra("migrantGender");
        migPhone = getIntent().getStringExtra("migrantPhone");
        countryName = getIntent().getStringExtra("countryName");
        status = getIntent().getIntExtra("countryStatus", -1);
        blacklist = getIntent().getIntExtra("countryBlacklist", -1);
        tileType = getIntent().getStringExtra("tiletype");
        section = tileType.toUpperCase();
        Log.d("mylog", "Tile Type: " + tileType);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        navView = findViewById(R.id.nav_view);
        getUserDetails();
        setMigDetails();
        setUpNavigationButtons();
        setUpRecyclerView();
        FlurryAgent.logEvent("tiles_listing_created");
    }

    @Override
    protected void onStop() {
        totalResponseCount = 0;
        currentSavedCount = 0;
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        finalSection = checkIfVerifiedAnswers();
        setUpListeners();
        //setUpRecyclerView();
        //getAllResponses();
        //getAllFeedbackResponses();
        float percentComp = getPercentComplete();
        if (percentComp > 99.0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(ActivityTileHome.this);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    goToNextSectionProcess();
                }
            });
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.setTitle(R.string.complete);
            if (showIndia || finalSection)
                builder.setMessage(R.string.section_complete_message);
            else
                builder.setMessage(R.string.complete_message);
            builder.show();
            builder.setCancelable(false);
        }
        //saveMigPercent(percentComp);
        if (tileAdapter != null)
            tileAdapter.notifyDataSetChanged();
        FlurryAgent.logEvent("tiles_listing_resumed");
    }

    private void saveMigPercent(float percentComp) {
        RequestQueue queue = Volley.newRequestQueue(ActivityTileHome.this);
        String api = ApplicationClass.getInstance().getAPIROOT() + apiURLMigrantPercent;

        StringRequest saveRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("mylog", "response : " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String err = error.toString();
                Log.d("mylog", "error : " + err);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("migrant_id", ApplicationClass.getInstance().getMigrantId() + "");
                params.put("user_id", ApplicationClass.getInstance().getSafeUserId() + "");
                params.put("percent_complete", percentComp + "");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", userToken);
                return headers;
            }

        };
        saveRequest.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(saveRequest);
    }

    public void goToNextSectionProcess() {
        FlurryAgent.logEvent("next_section_click");
        if (!checkIfVerifiedAnswers())
            new DialogAnswersVerification().show(getSupportFragmentManager(), "dialog");
        else {
            Intent intent = new Intent(this, ActivityFeedback.class);
            intent.putExtra("countryId", countryId);
            intent.putExtra("migrantName", migName);
            intent.putExtra("countryName", countryName);
            intent.putExtra("countryStatus", status);
            intent.putExtra("countryBlacklist", blacklist);
            intent.putExtra("section", section);
            startActivity(intent);
        }
        //getAllResponses();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        countryId = intent.getStringExtra("countryId");
        countryName = intent.getStringExtra("countryName");
        if (intent.hasExtra("status"))
            status = intent.getIntExtra("status", 0);
        if (intent.hasExtra("blacklist"))
            blacklist = intent.getIntExtra("blacklist", 0);
        if (intent.hasExtra("tiletype")) {
            tileType = intent.getStringExtra("tiletype");
            section = tileType.toUpperCase();

            setUpRecyclerView();
        }

        tvCountry.setText(countryName);
        if (countryId.equalsIgnoreCase("in")) {
            //GET GIS TILES
            Log.d("mylog", "Received Country is India");
            showIndia = true;
        } else {
            Log.d("mylog", "Setting show India to False");
            showIndia = false;
        }
        Log.d("mylog", "new CID: " + countryId);
    }

    private void setMigDetails() {
        if (countryId.equalsIgnoreCase("in")) {
            //GET GIS TILES
            Log.d("mylog", "Received Country is India");
            showIndia = true;
        } else {
            Log.d("mylog", "Setting show India to False");
            showIndia = false;
        }

        tvMigrantName.setText(migName.toUpperCase());
        tvCountry.setText(countryName.toUpperCase());
        tvCountry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("mylog", "Country change required");
               /* DialogCountryChooser dialog = DialogCountryChooser.newInstance();
                dialog.setMigrantName(migName.toUpperCase());
                dialog.show(getSupportFragmentManager(), "countrydialog");*/
            }
        });
        if (status == 1) {
            tvCountry.setTextColor(getResources().getColor(R.color.colorNeutral));
        }

        if (blacklist == 1) {
            tvCountry.setTextColor(getResources().getColor(R.color.colorError));
        }
        String img = SQLDatabaseHelper.getInstance(ActivityTileHome.this).getMigrantImg(ApplicationClass.getInstance().getMigrantId());
        if (img != null && img.length() > 5)
            ivMigrantImage.setImageBitmap(ImageEncoder.decodeFromBase64(img));
        else if (migGender != null && migGender.equalsIgnoreCase("female"))
            ivMigrantImage.setImageResource(R.drawable.ic_female);
        ivMigrantImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityTileHome.this, ActivityProfileEdit.class);
                intent.putExtra("userType", SharedPrefKeys.migrantUser);
                startActivity(intent);
            }
        });
    }

    private void getUserDetails() {
        SharedPreferences sp = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE);
        uname = sp.getString(SharedPrefKeys.userName, "");
        uage = sp.getString(SharedPrefKeys.userAge, "");
        unumber = sp.getString(SharedPrefKeys.userPhone, "");
        uimg = sp.getString(SharedPrefKeys.userImg, "");
    }

    private void setUpNavigationButtons() {
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
    }

    private float getPercentComplete() {
        SQLDatabaseHelper dbHelper = SQLDatabaseHelper.getInstance(ActivityTileHome.this);
        float totalPercent = 0f;
        int tilesCount = tiles.size();
        for (int i = 0; i < tilesCount; i++) {
            float perComplete = dbHelper.getPercentComplete(ApplicationClass.getInstance().getMigrantId(), tiles.get(i).getTileId());
            tiles.get(i).setPercentComplete(perComplete);
            Log.d("mylog", "Setting percent complete: " + perComplete);
            totalPercent += perComplete;
        }
        float percent = totalPercent / tilesCount;
        DecimalFormat decimalFormat = new DecimalFormat("##");
        tvPercent.setVisibility(View.VISIBLE);
        tvPercent.setText(decimalFormat.format(percent) + "%");
        dbHelper.insertPercentComp(ApplicationClass.getInstance().getMigrantId(), (int) percent);
        progressPercent.setVisibility(View.VISIBLE);
        progressPercent.setProgress((int) percent);
        rvTiles.getAdapter().notifyDataSetChanged();
        saveMigPercent(percent);
        return percent;
    }

    private boolean checkIfVerifiedAnswers() {
        String verified = SQLDatabaseHelper.getInstance(ActivityTileHome.this).getResponse(ApplicationClass.getInstance().getMigrantId(),
                "mg_verified_answers");
        String isFeedbackSaved = SQLDatabaseHelper.getInstance(ActivityTileHome.this).getResponse(ApplicationClass.getInstance().getMigrantId(),
                "mg_feedback_saved");
        Log.d("mylog", "VERIFIED ans: " + verified + " SAVED Feedback: " + isFeedbackSaved);
        if (verified.equalsIgnoreCase("true") && isFeedbackSaved.equalsIgnoreCase("true")) {
            if (showIndia) {
                section = "GIS";
            }
            return true;
        } else if (verified.equalsIgnoreCase("true")) {
            Intent intent = new Intent(ActivityTileHome.this, ActivityFeedback.class);
            intent.putExtra("countryId", this.countryId);
            intent.putExtra("migrantName", this.migName);
            intent.putExtra("countryName", this.countryName);
            intent.putExtra("countryStatus", this.status);
            intent.putExtra("countryBlacklist", this.blacklist);
            if (showIndia) {
                intent.putExtra("section", "GIS");
                section = "GIS";
            }
            startActivity(intent);
            return false;
        } else {
            if (showIndia) {
                section = "GIS";
            }
        }

        return false;
    }

    private void setUpListeners() {
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToNextSection();
            }
        });

        ivHam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });
        navView.getMenu().findItem(R.id.nav_tutorial).setVisible(false);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_profile:
                        Intent intent = new Intent(ActivityTileHome.this, ActivityProfileEdit.class);
                        intent.putExtra("userType", SharedPrefKeys.helperUser);
                        startActivity(intent);
                        break;
                    case R.id.nav_addmigrants:
                        Intent intentMig = new Intent(ActivityTileHome.this, ActivityRegister.class);
                        intentMig.putExtra("migrantmode", true);
                        drawerLayout.closeDrawer(GravityCompat.END);
                        startActivity(intentMig);
                        break;
                    case R.id.delete_migrant:
                        deleteMigrant();
                        break;
                    case R.id.nav_about:
                        Intent intentAbout = new Intent(ActivityTileHome.this, ActivityAboutUs.class);
                        drawerLayout.closeDrawer(GravityCompat.END);
                        startActivity(intentAbout);
                        break;
                    case R.id.nav_contact:
                        Intent intentContact = new Intent(ActivityTileHome.this, ActivityAboutUs.class);
                        intentContact.putExtra("contact", true);
                        drawerLayout.closeDrawer(GravityCompat.END);
                        startActivity(intentContact);
                        break;
                    case R.id.nav_faq:
                        Intent intentFaq = new Intent(ActivityTileHome.this, ActivityAboutUs.class);
                        intentFaq.putExtra("faq", true);
                        drawerLayout.closeDrawer(GravityCompat.END);
                        startActivity(intentFaq);
                        break;
                    case R.id.nav_lang:
                        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityTileHome.this);
                        builder.setTitle("Change Language");
                        builder.setPositiveButton("English", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setLocale("en");
                                ApplicationClass.getInstance().setLocale("en");
                                SharedPreferences sp = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString(SharedPrefKeys.lang, "en");
                                editor.commit();
                                recreate();
                            }
                        });
                        builder.setNegativeButton("नेपाली", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setLocale("np");
                                ApplicationClass.getInstance().setLocale("np");
                                SharedPreferences sp = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString(SharedPrefKeys.lang, "np");
                                editor.commit();
                                recreate();
                            }
                        });
                        builder.show();
                        break;
                }
                return false;
            }
        });
    }

    private void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

    public void goToNextSection() {
        if (!finalSection)
            new DialogAnswersVerification().show(getSupportFragmentManager(), "dialog");
        else {
            //Toast.makeText(ActivityTileHome.this, "Completed", Toast.LENGTH_SHORT).show();
            showSnackbar("The steps are Completed");
        }
        //getAllResponses();
    }

    private void deleteMigrant() {
        SQLDatabaseHelper.getInstance(ActivityTileHome.this).insertMigrantDeletion(ApplicationClass.getInstance().getMigrantId()
                , ApplicationClass.getInstance().getSafeUserId(), System.currentTimeMillis() + "");

        // showing snack bar with Undo option
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                Intent deleteMig = new Intent(ActivityTileHome.this, ActivityMigrantList.class);
                deleteMig.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                drawerLayout.closeDrawer(GravityCompat.END);
                startActivity(deleteMig);
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                SQLDatabaseHelper.getInstance(ActivityTileHome.this).insertMigrantDeletion(ApplicationClass.getInstance().getMigrantId(),
                        ApplicationClass.getInstance().getSafeUserId(), "");
            }
        });
        builder.setMessage(R.string.mig_delete_confirmation);
        builder.show();
        builder.setCancelable(false);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        finalSection = false;
        //if (ApplicationClass.getInstance().getSafeUserId() == -1)
        super.onBackPressed();
       /* else {
            Intent intent = new Intent(ActivityTileHome.this, ActivityMigrantList.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }*/
    }

    private void showSnackbar(String msg) {
       /* Snackbar snack = Snackbar.make(rootLayout, msg, Snackbar.LENGTH_LONG);
        View view = snack.getView();
        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        snack.show();*/

        Toast.makeText(ActivityTileHome.this, msg, Toast.LENGTH_SHORT).show();
    }

    private void setUpRecyclerView() {
        if (showIndia) {
            tiles = SQLDatabaseHelper.getInstance(ActivityTileHome.this).getTiles("GIS");
            //tilesGAS = new ArrayList<>();
        } else if (tileType.equalsIgnoreCase("fep")) {
            tiles = SQLDatabaseHelper.getInstance(ActivityTileHome.this).getTiles("FEP");
            //tilesGAS = new ArrayList<>();
        } else {
            tiles = SQLDatabaseHelper.getInstance(ActivityTileHome.this).getTiles("GAS");
        }
        setUpAdapter();
    }

    public void setUpAdapter() {
        tileAdapter = new TileAdapter(tiles, tileIcons, ActivityTileHome.this, countryId);
       /* for (int i = 0; i < tilesGAS.size(); i++) {
            tiles.add(i, tilesGAS.get(i));
        }
        finalSection = true;
        */
        btnNext.setVisibility(View.GONE);
        rvTiles.setLayoutManager(new GridLayoutManager(this, 2));
        rvTiles.setAdapter(tileAdapter);
        //float y = rvTiles.getChildAt(0).getY();
        rvTiles.smoothScrollToPosition(0);
        nsv.scrollTo(0, 0);
    }
}