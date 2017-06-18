package com.vysh.subairoma;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.WindowManager;

import com.vysh.subairoma.adapters.TileAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Vishal on 6/12/2017.
 */

public class ActivityTileHome extends AppCompatActivity {

    int[] tileIcons;
    String[] tileTitles;

    @BindView(R.id.rvTiles)
    RecyclerView rvTiles;

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
    }

    private void setTileIcons() {
        int size = tileTitles.length;
        tileIcons = new int[size];
        for (int i = 0; i < size; i++)
            tileIcons[i] = R.drawable.roller;
    }

    private void setUpRecyclerView() {
        rvTiles.setLayoutManager(new GridLayoutManager(this, 2));
        rvTiles.setAdapter(new TileAdapter(tileTitles, tileIcons));
    }
}
