package com.vysh.subairoma.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.adapters.FeedbackQuestionAdapter;
import com.vysh.subairoma.models.FeedbackQuestionModel;

import java.util.ArrayList;

/**
 * Created by Vishal on 12/30/2017.
 */

public class ActivityFeedback extends AppCompatActivity {

    RecyclerView rvFeedback;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        rvFeedback = findViewById(R.id.rvFeedbackQuestions);
        SQLDatabaseHelper helper = new SQLDatabaseHelper(this);
        ArrayList<FeedbackQuestionModel> questionModels = helper.getFeedbackQuestions();
        rvFeedback.setLayoutManager(new LinearLayoutManager(this));
        rvFeedback.setAdapter(new FeedbackQuestionAdapter(this, questionModels));
    }
}
