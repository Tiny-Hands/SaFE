package com.vysh.subairoma.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import com.vysh.subairoma.imageHelpers.ImageEncoder;
import com.vysh.subairoma.models.TilesModel;
import com.vysh.subairoma.utils.CustomTextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Vishal on 6/12/2017.
 */

public class ActivityTileHome extends AppCompatActivity {
    private final String saveAPI = "/saveresponse.php";
    private final String saveFeedbackAPI = "/savefeedbackresponse.php";
    ArrayList<TilesModel> tiles;
    public String migName, countryName, countryId, migGender = "", migPhone;
    public int blacklist, status;
    int[] tileIcons;
    TileAdapter tileAdapter;
    public static Boolean finalSection, showIndia, initialStep = false;
    String uname, unumber, uage, uimg, uavatar, tileType, userToken;

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        finalSection = checkIfVerifiedAnswers();
        setUpListeners();
        setUpRecyclerView();
        getAllResponses();
        getAllFeedbackResponses();
        getPercentComplete();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        countryId = intent.getStringExtra("countryId");
        countryName = intent.getStringExtra("countryName");
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
        String img = new SQLDatabaseHelper(ActivityTileHome.this).getMigrantImg(ApplicationClass.getInstance().getMigrantId());
        if (img != null && img.length() > 5)
            ivMigrantImage.setImageBitmap(ImageEncoder.decodeFromBase64(img));
        else if (migGender != null && migGender.equalsIgnoreCase("female"))
            ivMigrantImage.setImageResource(R.drawable.ic_female);
        ivMigrantImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityTileHome.this, ActivityProfileEdit.class);
                intent.putExtra("userType", 0);
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

    private void getPercentComplete() {
        SQLDatabaseHelper dbHelper = new SQLDatabaseHelper(ActivityTileHome.this);
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
    }

    private boolean checkIfVerifiedAnswers() {
        String verified = new SQLDatabaseHelper(ActivityTileHome.this).getResponse(ApplicationClass.getInstance().getMigrantId(),
                "mg_verified_answers");
        String isFeedbackSaved = new SQLDatabaseHelper(ActivityTileHome.this).getResponse(ApplicationClass.getInstance().getMigrantId(),
                "mg_feedback_saved");
        Log.d("mylog", "VERIFIED ans: " + verified + " SAVED Feedback: " + isFeedbackSaved);
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
        if (migId > 0) {
            ArrayList<HashMap> responses = sqlDatabaseHelper.getAllFeedbackResponses(migId);
            for (int i = 0; i < responses.size(); i++) {
                responses.get(i).put("user_id", ApplicationClass.getInstance().getUserId() + "");
                responses.get(i).put("migrant_id", ApplicationClass.getInstance().getMigrantId() + "");
                saveResponseToServer(responses.get(i), 2);
            }
        } else Log.d("mylog", "MigID: " + migId + " Not saving to server");
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
                        intent.putExtra("userType", 1);
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
                }
                return false;
            }
        });
    }

    public void goToNextSection() {
        if (!finalSection)
            new DialogAnswersVerification().show(getSupportFragmentManager(), "dialog");
        else {
            //Toast.makeText(ActivityTileHome.this, "Completed", Toast.LENGTH_SHORT).show();
            showSnackbar("The steps are Completed");
        }
        getAllResponses();
    }

    private void deleteMigrant() {
        new SQLDatabaseHelper(ActivityTileHome.this).insertMigrantDeletion(ApplicationClass.getInstance().getMigrantId()
                , ApplicationClass.getInstance().getUserId(), System.currentTimeMillis() + "");

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
                new SQLDatabaseHelper(ActivityTileHome.this).insertMigrantDeletion(ApplicationClass.getInstance().getMigrantId(),
                        ApplicationClass.getInstance().getUserId(), "");
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
        //if (ApplicationClass.getInstance().getUserId() == -1)
        super.onBackPressed();
       /* else {
            Intent intent = new Intent(ActivityTileHome.this, ActivityMigrantList.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }*/
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
            //tilesGAS = new ArrayList<>();
        } else if (tileType.equalsIgnoreCase("fep")) {
            Log.d("mylog", "Should Get: " + tileType);
            tiles = new SQLDatabaseHelper(ActivityTileHome.this).getTiles("FEP");
            //tilesGAS = new ArrayList<>();
        } else {
            tiles = new SQLDatabaseHelper(ActivityTileHome.this).getTiles("GAS");
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

    private void getAllResponses() {
        int migId = ApplicationClass.getInstance().getMigrantId();
        if (migId > 0) {
            ArrayList<HashMap> allParams = new SQLDatabaseHelper(ActivityTileHome.this)
                    .getAllResponse(migId);
            for (int i = 0; i < allParams.size(); i++) {
                //Log.d("mylog", "Saving to server: " + i);
                allParams.get(i).put("user_id", ApplicationClass.getInstance().getUserId() + "");
                allParams.get(i).put("migrant_id", ApplicationClass.getInstance().getMigrantId() + "");
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

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", userToken);
                return headers;
            }
        };
        Log.d("mylog", "Calling: " + api);
        queue.add(stringRequest);
        //Log.d("mylog", "Token: " + getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE).getString(SharedPrefKeys.token, ""));
    }

}