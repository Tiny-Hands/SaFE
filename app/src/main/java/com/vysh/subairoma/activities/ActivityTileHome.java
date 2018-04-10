package com.vysh.subairoma.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.vysh.subairoma.SharedPrefKeys;
import com.vysh.subairoma.dialogs.DialogAnswersVerification;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.adapters.TileAdapter;
import com.vysh.subairoma.models.TilesModel;
import com.vysh.subairoma.utils.CustomTextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Vishal on 6/12/2017.
 */

public class ActivityTileHome extends AppCompatActivity {
    private final String saveAPI = "/saveresponse.php";
    private final String saveFeedbackAPI = "/savefeedbackresponse.php";
    ArrayList<TilesModel> tiles, tilesGAS;
    public String migName, countryName, countryId, migGender = "", migPhone;
    public int blacklist, status;
    int[] tileIcons;
    TileAdapter tileAdapter;
    public static Boolean finalSection, showIndia;

    @BindView(R.id.rvTiles)
    RecyclerView rvTiles;
    @BindView(R.id.rlRoot)
    RelativeLayout rootLayout;
    @BindView(R.id.tvMigrantName)
    TextView tvMigrantName;
    @BindView(R.id.tvCountry)
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
    NavigationView navView;

    CustomTextView tvName, tvPhone, tvNavCounty;
    ImageView ivUserAvatar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tile_home);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ButterKnife.bind(this);

        countryId = getIntent().getStringExtra("countryId");
        migName = getIntent().getStringExtra("migrantName");
        migGender = getIntent().getStringExtra("migrantGender");
        migPhone = getIntent().getStringExtra("migrantPhone");
        countryName = getIntent().getStringExtra("countryName");
        status = getIntent().getIntExtra("countryStatus", -1);
        blacklist = getIntent().getIntExtra("countryBlacklist", -1);
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


        navView = findViewById(R.id.nav_view);
        setUpNavigationButtons();
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

        tvName.setText(migName);
        tvNavCounty.setText(countryName);
        tvPhone.setText(migPhone);
        if (migGender != null) {
            if (migGender.equals("male"))
                ivUserAvatar.setImageResource(R.drawable.ic_male);
            else
                ivUserAvatar.setImageResource(R.drawable.ic_female);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        finalSection = false;
        setUpListeners();
        setUpRecyclerView();
        getAllResponses();
        getAllFeedbackResponses();
        calculatePercentComplete();
    }

    private void calculatePercentComplete() {
        SQLDatabaseHelper dbHelper = new SQLDatabaseHelper(ActivityTileHome.this);
        int answeredQuestions = dbHelper.getAllResponseCount(ApplicationClass.getInstance().getMigrantId());
        int count = 0;
        for (int i = 0; i < tiles.size(); i++) {
            int tempCount = dbHelper.getNoRedFlagQuestionsCount(tiles.get(i).getTileId());
            int answersCount = dbHelper.getTileResponse(ApplicationClass.getInstance().getMigrantId(), tiles.get(i).getTileId());
            count += tempCount;
            tiles.get(i).setPercentComplete(((float) answersCount / (float) tempCount) * 100);
            Log.d("mylog", "Temp count: " + tempCount);

        }
       /* for (int i = 0; i < tilesGAS.size(); i++) {
            int tempCount = dbHelper.getNoRedFlagQuestionsCount(tilesGAS.get(i).getTileId());
            count += tempCount;
            Log.d("mylog", "Temp count GAS: " + tempCount);
        }*/
        float percent = ((float) answeredQuestions / (float) count) * 100;
        DecimalFormat decimalFormat = new DecimalFormat("##");
        tvPercent.setText(decimalFormat.format(percent)+"%");
        progressPercent.setProgress((int) percent);
        Log.d("mylog", "total count: " + count);
        Log.d("mylog", "answered count: " + answeredQuestions);
        rvTiles.getAdapter().notifyDataSetChanged();
    }

    private boolean checkIfVerifiedAnswers() {
        String verified = new SQLDatabaseHelper(ActivityTileHome.this).getResponse(ApplicationClass.getInstance().getMigrantId(),
                "mg_verified_answers");
        String isFeedbackSaved = new SQLDatabaseHelper(ActivityTileHome.this).getResponse(ApplicationClass.getInstance().getMigrantId(),
                "mg_feedback_saved");
        if (verified.equalsIgnoreCase("true") && isFeedbackSaved.equalsIgnoreCase("true")) {
            return true;
        } else if (verified.equalsIgnoreCase("true")) {
            Intent intent = new Intent(ActivityTileHome.this, ActivityFeedback.class);
            intent.putExtra("countryId", this.countryId);
            intent.putExtra("migrantName", this.migName);
            intent.putExtra("countryName", this.countryName);
            intent.putExtra("countryStatus", this.status);
            intent.putExtra("countryBlacklist", this.blacklist);
            startActivity(intent);
            return false;
        }
        return false;
    }

    private void getAllFeedbackResponses() {
        int migId = ApplicationClass.getInstance().getMigrantId();
        SQLDatabaseHelper sqlDatabaseHelper = new SQLDatabaseHelper(ActivityTileHome.this);
        ArrayList<HashMap> responses = sqlDatabaseHelper.getAllFeedbackResponses(migId);
        for (int i = 0; i < responses.size(); i++) {
            saveResponseToServer(responses.get(i), 2);
        }
    }

    private void setUpListeners() {
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!finalSection)
                    new DialogAnswersVerification().show(getSupportFragmentManager(), "dialog");
                else {
                    //Toast.makeText(ActivityTileHome.this, "Completed", Toast.LENGTH_SHORT).show();
                    showSnackbar("The steps are Completed");
                }
                getAllResponses();
            }
        });

        ivHam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.END);
            }
        });

        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.nav_profile:
                        Intent intent = new Intent(ActivityTileHome.this, ActivityProfileEdit.class);
                        intent.putExtra("userType", 1);
                        startActivity(intent);
                        break;
                }
                return false;
            }
        });

        /*ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityTileHome.this, ActivityProfileEdit.class);
                intent.putExtra("userType", 1);
                startActivity(intent);
            }
        });*/

        /*btnImportantContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent impIntent = new Intent(ActivityTileHome.this, ActivityImportantContacts.class);
                impIntent.putExtra("countryId", countryId);
                startActivity(impIntent);
            }
        });*/
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        finalSection = false;
        if (ApplicationClass.getInstance().getUserId() == -1)
            super.onBackPressed();
        else {
            Intent intent = new Intent(ActivityTileHome.this, ActivityMigrantList.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    private void showSnackbar(String msg) {
        Snackbar snack = Snackbar.make(rootLayout, msg, Snackbar.LENGTH_LONG);
        View view = snack.getView();
        TextView tv = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        tv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        snack.show();
    }

    private void setUpRecyclerView() {
        Log.d("mylog", "Received Country ID: " + countryId + " And showIndia: " + showIndia);
        if (showIndia) {
            tiles = new SQLDatabaseHelper(ActivityTileHome.this).getTiles("GIS");
            tilesGAS = new ArrayList<>();
        } else {
            tiles = new SQLDatabaseHelper(ActivityTileHome.this).getTiles("FEP");
            tilesGAS = new SQLDatabaseHelper(ActivityTileHome.this).getTiles("GAS");
        }
        if (checkIfVerifiedAnswers()) {
            setUpGasSections();
            return;
        } else {
            int afterFEP = tiles.size();
            rvTiles.setLayoutManager(new GridLayoutManager(this, 2));
            tiles.addAll(tilesGAS);
            tileAdapter = new TileAdapter(tiles, afterFEP, tileIcons, ActivityTileHome.this, countryId);
            rvTiles.setAdapter(tileAdapter);
        }
    }

    public void setUpGasSections() {
        tileAdapter = new TileAdapter(tiles, tilesGAS.size(), tileIcons, ActivityTileHome.this, countryId);
        for (int i = 0; i < tilesGAS.size(); i++) {
            tiles.add(i, tilesGAS.get(i));
        }
        finalSection = true;
        btnNext.setVisibility(View.GONE);
        rvTiles.setLayoutManager(new GridLayoutManager(this, 2));
        rvTiles.setAdapter(tileAdapter);
        //float y = rvTiles.getChildAt(0).getY();
        rvTiles.smoothScrollToPosition(0);
        nsv.scrollTo(0, 0);
    }

    private void getAllResponses() {
        ArrayList<HashMap> allParams = new SQLDatabaseHelper(ActivityTileHome.this)
                .getAllResponse(ApplicationClass.getInstance().getMigrantId());
        for (int i = 0; i < allParams.size(); i++) {
            //Log.d("mylog", "Saving to server: " + i);
            saveResponseToServer(allParams.get(i), 1);
        }
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
                    Log.d("mylog", key + ": " + fParams.get(key));
                }
                return fParams;
            }
        };
        Log.d("mylog", "Calling: " + api);
        RequestQueue queue = Volley.newRequestQueue(ActivityTileHome.this);
        queue.add(stringRequest);
    }

}