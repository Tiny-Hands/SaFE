package com.vysh.subairoma;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.WindowManager;

import com.vysh.subairoma.adapters.TileQuestionsAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Vishal on 6/15/2017.
 */

public class ActivityTileQuestions extends AppCompatActivity {

    @BindView(R.id.rvTileQuestions)
    RecyclerView rvQuestions;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tile_details);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ButterKnife.bind(ActivityTileQuestions.this);
        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        rvQuestions.setLayoutManager(new LinearLayoutManager(ActivityTileQuestions.this));
        rvQuestions.setAdapter(new TileQuestionsAdapter());
    }
}
