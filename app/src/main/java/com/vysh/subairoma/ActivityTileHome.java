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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Vishal on 6/12/2017.
 */

public class ActivityTileHome extends AppCompatActivity {
    //private final String API = "/subairoma/gettiles.php";

    ArrayList<TilesModel> tiles;

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

        String cid = getIntent().getStringExtra("countryId");
        if (cid.equalsIgnoreCase("in")) {
            //GET GIS TILES
            tiles = new SQLDatabaseHelper(ActivityTileHome.this).getTiles("'GIS'");
        } else {
            //GET FEP TILES
            tiles = new SQLDatabaseHelper(ActivityTileHome.this).getTiles("'FEP'");
        }
        //Tile titles and Icons
        tileTitles = new String[tiles.size()];
        for (int i = 0; i < tiles.size(); i++) {
            tileTitles[i] = tiles.get(i).getTitle();
        }

        setTileIcons();
        setUpRecyclerView();
    }

    private void getLiveTiles() {

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
