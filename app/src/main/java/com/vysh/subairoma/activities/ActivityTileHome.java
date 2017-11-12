package com.vysh.subairoma.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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
import com.vysh.subairoma.dialogs.DialogAnswersVerification;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.adapters.TileAdapter;
import com.vysh.subairoma.models.TilesModel;

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
    ArrayList<TilesModel> tiles, tilesGAS;
    public String migName, countryName, countryId;
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
    @BindView(R.id.btnImportantContacts)
    CardView btnImportantContacts;
    @BindView(R.id.ivAvatar)
    ImageView ivAvatar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tile_home);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ButterKnife.bind(this);

        countryId = getIntent().getStringExtra("countryId");
        migName = getIntent().getStringExtra("migrantName");
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        finalSection = false;
        setUpListeners();
        setUpRecyclerView();
        checkIfVerifiedAnswers();
        getAllResponses();
    }

    private void checkIfVerifiedAnswers() {
        String verified = new SQLDatabaseHelper(ActivityTileHome.this).getResponse(ApplicationClass.getInstance().getMigrantId(),
                "mg_verified_answers");
        if (verified.equalsIgnoreCase("true"))
            setUpGasSections();
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

        ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityTileHome.this, ActivityProfileEdit.class);
                intent.putExtra("userType", 1);
                startActivity(intent);
            }
        });

        btnImportantContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent impIntent = new Intent(ActivityTileHome.this, ActivityImportantContacts.class);
                impIntent.putExtra("countryId", countryId);
                startActivity(impIntent);
            }
        });
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
        rvTiles.setLayoutManager(new GridLayoutManager(this, 2));
        tileAdapter = new TileAdapter(tiles, tileIcons, ActivityTileHome.this);
        rvTiles.setAdapter(tileAdapter);

    }

    public void setUpGasSections() {
        for (int i = 0; i < tilesGAS.size(); i++) {
            tiles.add(i, tilesGAS.get(i));
        }
        finalSection = true;
        tileAdapter.notifyDataSetChanged();
        rvTiles.scrollToPosition(tiles.size() - 1);
    }

    private void getAllResponses() {
        ArrayList<HashMap> allParams = new SQLDatabaseHelper(ActivityTileHome.this)
                .getAllResponse(ApplicationClass.getInstance().getMigrantId());
        RequestQueue queue = Volley.newRequestQueue(ActivityTileHome.this);
        for (int i = 0; i < allParams.size(); i++) {
            saveResponseToServer(allParams.get(i), queue);
        }
    }

    private void saveResponseToServer(final HashMap<String, String> fParams, RequestQueue queue) {
        String api = ApplicationClass.getInstance().getAPIROOT() + saveAPI;
        StringRequest stringRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("mylog", "Response: " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String err = error.toString();
                if (!err.isEmpty() && err.contains("NoConnection")){
                    //showSnackbar("Response cannot be saved at the moment, please check your Intenet connection.");
                    Log.d("mylog","Response cannot be saved at the moment, please check your Intenet connection.");
                }
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

        queue.add(stringRequest);
    }

}