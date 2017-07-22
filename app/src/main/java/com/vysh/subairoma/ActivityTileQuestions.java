package com.vysh.subairoma;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.WindowManager;

import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.adapters.TileQuestionsAdapter;
import com.vysh.subairoma.models.TileQuestionsModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Vishal on 6/15/2017.
 */

public class ActivityTileQuestions extends AppCompatActivity {

    @BindView(R.id.rvTileQuestions)
    RecyclerView rvQuestions;

    ArrayList<TileQuestionsModel> questionList;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tile_details);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ButterKnife.bind(ActivityTileQuestions.this);

        getQuestions(getIntent().getIntExtra("tileId", -1));
        setUpRecyclerView();
    }

    private void getQuestions(int tileId) {
        //Get Questions if not already in Local Database
        questionList = new SQLDatabaseHelper(ActivityTileQuestions.this).getQuestions(tileId);
    }

    private void setUpRecyclerView() {
        rvQuestions.setLayoutManager(new LinearLayoutManager(ActivityTileQuestions.this));
        TileQuestionsAdapter questionsAdapter = new TileQuestionsAdapter(questionList);
        rvQuestions.setAdapter(questionsAdapter);
    }
}
