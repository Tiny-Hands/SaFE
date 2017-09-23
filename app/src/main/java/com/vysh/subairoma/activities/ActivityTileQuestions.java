package com.vysh.subairoma.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.WindowManager;
import android.widget.TextView;

import com.vysh.subairoma.R;
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
    public RecyclerView rvQuestions;

    @BindView(R.id.tvTitle)
    public TextView tvTitle;

    public ArrayList<TileQuestionsModel> questionList;

    public TileQuestionsAdapter questionsAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tile_details);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ButterKnife.bind(ActivityTileQuestions.this);

        tvTitle.setText(getIntent().getStringExtra("tileName").toUpperCase());
        getQuestions(getIntent().getIntExtra("tileId", -1));
        setUpRecyclerView();
    }

    private void getQuestions(int tileId) {
        //Get Questions if not already in Local Database
        questionList = new SQLDatabaseHelper(ActivityTileQuestions.this).getQuestions(tileId);
        SQLDatabaseHelper sqlDatabaseHelper = new SQLDatabaseHelper(ActivityTileQuestions.this);
        for (TileQuestionsModel question : questionList) {
            if (question.getResponseType() == 2) {
                String[] options = sqlDatabaseHelper.getOptions(question.getQuestionId());
                ArrayList<String> temp = new ArrayList<>();
                for(int i = 0; i<options.length; i++){
                    temp.add(i, options[i]);
                }
                question.setOptions(temp);
            }
        }
    }

    private void setUpRecyclerView() {
        rvQuestions.setLayoutManager(new LinearLayoutManager(ActivityTileQuestions.this));
        questionsAdapter = new TileQuestionsAdapter(questionList, ActivityTileQuestions.this);
        rvQuestions.setAdapter(questionsAdapter);
    }
}
