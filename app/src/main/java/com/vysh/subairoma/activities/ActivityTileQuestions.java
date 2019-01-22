package com.vysh.subairoma.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
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
    @BindView(R.id.ivImage)
    ImageView ivTile;
    @BindView(R.id.btnBack)
    ImageView btnBack;
    @BindView(R.id.btnDone)
    Button btnDone;

    Boolean stateDisabled;

    public ArrayList<TileQuestionsModel> questionList;
    public TileQuestionsAdapter questionsAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tile_details);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ButterKnife.bind(ActivityTileQuestions.this);

        stateDisabled = getIntent().hasExtra("stateDisabled");
        tvTitle.setText(getIntent().getStringExtra("tileName").toUpperCase());
        getQuestions(getIntent().getIntExtra("tileId", -1));
        int icId = getIntent().getIntExtra("iconId", -1);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        setTileIcons(icId);
        setUpRecyclerView();
    }

    private void setTileIcons(int tileId) {
        switch (tileId) {
            case 0:
                ivTile.setImageResource(R.drawable.ic_travel_work);
                break;
            case 1:
                ivTile.setImageResource(R.drawable.ic_manpower);
                break;
            case 2:
                ivTile.setImageResource(R.drawable.ic_work);
                break;
            case 3:
                ivTile.setImageResource(R.drawable.ic_contract);
                break;
            case 4:
                ivTile.setImageResource(R.drawable.ic_cost);
                break;
            case 5:
                ivTile.setImageResource(R.drawable.ic_government);
                break;
            case 6:
                ivTile.setImageResource(R.drawable.ic_preparation);
                break;
            case 7:
                ivTile.setImageResource(R.drawable.ic_passport_visa);
                break;
            case 8:
                ivTile.setImageResource(R.drawable.ic_packing);
                break;
            case 9:
                ivTile.setImageResource(R.drawable.ic_travel);
                break;
            case 10:
                ivTile.setImageResource(R.drawable.ic_incountry);
                break;
            case 15:
                ivTile.setImageResource(R.drawable.ic_preparation);
                break;
            case 16:
                ivTile.setImageResource(R.drawable.ic_travel);
                break;
            case 18:
                ivTile.setImageResource(R.drawable.ic_travel_work);
                break;
            default:
                ivTile.setImageResource(R.drawable.ic_default);
        }
    }

    private void getQuestions(int tileId) {
        //Get Questions if not already in Local Database
        questionList = new SQLDatabaseHelper(ActivityTileQuestions.this).getQuestions(tileId);
        SQLDatabaseHelper sqlDatabaseHelper = new SQLDatabaseHelper(ActivityTileQuestions.this);
        for (TileQuestionsModel question : questionList) {
            int resType = question.getResponseType();
            if (resType == 2 || resType == 3 || resType == 4) {
                String[] options = sqlDatabaseHelper.getOptions(question.getQuestionId());
                ArrayList<String> temp = new ArrayList<>();
                for (int i = 0; i < options.length; i++) {
                    temp.add(i, options[i]);
                }
                question.setOptions(temp);
            }
        }
    }

    private void setUpRecyclerView() {
        rvQuestions.setLayoutManager(new LinearLayoutManager(ActivityTileQuestions.this));
        questionsAdapter = new TileQuestionsAdapter(questionList, stateDisabled, ActivityTileQuestions.this);
        rvQuestions.setAdapter(questionsAdapter);
    }

    public void showDoneButton(boolean i) {
        if (i)
            btnDone.setVisibility(View.VISIBLE);
        else
            btnDone.setVisibility(View.GONE);
    }
}
