package com.vysh.subairoma.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;

/**
 * Created by Vishal on 12/30/2017.
 */

public class ActivityFeedback extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        SQLDatabaseHelper helper = new SQLDatabaseHelper(this);
        helper.getFeedbackQuestions();
    }
}
