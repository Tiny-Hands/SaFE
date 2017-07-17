package com.vysh.subairoma;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.adapters.TileAdapter;
import com.vysh.subairoma.models.MigrantModel;
import com.vysh.subairoma.models.TilesModel;
import com.vysh.subairoma.volley.VolleyController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Vishal on 6/12/2017.
 */

public class ActivityTileHome extends AppCompatActivity {
    private final String API = "/subairoma/gettiles.php";

    int[] tileIcons;
    String[] tileTitles;

    @BindView(R.id.rvTiles)
    RecyclerView rvTiles;

    @BindView(R.id.rlRoot)
    RelativeLayout rootLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tile_home);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ButterKnife.bind(this);

        //Tile titles and Icons
        tileTitles = getResources().getStringArray(R.array.fep_sections);

        setTileIcons();
        setUpRecyclerView();
        if (!getTiles()) {
            getLiveTiles();
        }
    }

    private boolean getTiles() {
        SQLDatabaseHelper dbHelper = new SQLDatabaseHelper(ActivityTileHome.this);

        //dbHelper.getTiles("FEP");
        //OR other according to country
        //dbHelper.getTiles("OTHER");
        //Get tiles here if not already from Local Database
        return false;
    }

    private void getLiveTiles() {
        //Get tiles from server here and save into database
        String api = ApplicationClass.getInstance().getAPIROOT() + API;
        StringRequest saveRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("mylog", "response : " + response);
                ArrayList<TilesModel> migrantModels = parseResponse(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
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
                params.put("tile_type", "Some type");
                return params;
            }
        };
        VolleyController.getInstance(getApplicationContext()).addToRequestQueue(saveRequest);

    }

    private ArrayList<TilesModel> parseResponse(String response) {
        return null;
    }

    private void setTileIcons() {
        int size = tileTitles.length;
        tileIcons = new int[size];
        for (int i = 0; i < size; i++)
            tileIcons[i] = R.drawable.roller;
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
        rvTiles.setLayoutManager(new GridLayoutManager(this, 2));
        rvTiles.setAdapter(new TileAdapter(tileTitles, tileIcons));
    }
}
