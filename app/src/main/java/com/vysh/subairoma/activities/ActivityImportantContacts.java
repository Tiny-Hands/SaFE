package com.vysh.subairoma.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Vishal on 11/6/2017.
 */

public class ActivityImportantContacts extends AppCompatActivity {

    @BindView(R.id.llNepalEmbassy)
    LinearLayout llNepalEmbassy;
    @BindView(R.id.llContact1)
    LinearLayout llContact1;
    @BindView(R.id.llContact2)
    LinearLayout llContact2;
    @BindView(R.id.llContact3)
    LinearLayout llContact3;
    @BindView(R.id.llContact4)
    LinearLayout llContact4;
    @BindView(R.id.llContact5)
    LinearLayout llContact5;

    @BindView(R.id.tvNepalEmbassy)
    TextView tvNepalEmbassy;
    @BindView(R.id.tvContact1)
    TextView tvContact1;
    @BindView(R.id.tvContact2)
    TextView tvContact2;
    @BindView(R.id.tvContact3)
    TextView tvContact3;
    @BindView(R.id.tvContact4)
    TextView tvContact4;
    @BindView(R.id.tvContact5)
    TextView tvContact5;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_important_contacts);
        ButterKnife.bind(this);
        String countryId = getIntent().getStringExtra("countryId");
        setUpInfo(countryId);
    }

    public void setUpInfo(String cid) {
        Log.d("mylog", "Received Country Id: " + cid);
        String[] contacts = new SQLDatabaseHelper(this).getImportantContacts(cid);
        if (contacts != null)
            for (int i = 0; i < 4; i++) {
                String currentInfo = contacts[i];
                Log.d("mylog", "Curr Info: " + currentInfo);
                switch (i) {
                    case 0:
                        if (currentInfo != null && !currentInfo.isEmpty()) {
                            llNepalEmbassy.setVisibility(View.VISIBLE);
                            tvNepalEmbassy.setText(currentInfo);
                        }
                        break;
                    case 1:
                        if (currentInfo != null && !currentInfo.isEmpty()) {
                            llContact1.setVisibility(View.VISIBLE);
                            tvContact1.setText(currentInfo);
                        }
                        break;
                    case 2:
                        if (currentInfo != null && !currentInfo.isEmpty()) {
                            llContact2.setVisibility(View.VISIBLE);
                            tvContact2.setText(getResources().getString(R.string.address)+ ": " + currentInfo);
                        }
                        break;
                    case 3:
                        if (currentInfo != null && !currentInfo.isEmpty()) {
                            llContact3.setVisibility(View.VISIBLE);
                            tvContact3.setText(getResources().getString(R.string.phone_number)+ ": " + currentInfo);
                        }
                        break;
                    case 4:
                        if (currentInfo != null && !currentInfo.isEmpty()) {
                            llContact4.setVisibility(View.VISIBLE);
                            tvContact4.setText(getResources().getString(R.string.email)+ ": " +currentInfo);
                        }
                        break;
                    case 5:
                        if (currentInfo != null && !currentInfo.isEmpty()) {
                            llContact5.setVisibility(View.VISIBLE);
                            tvContact5.setText(getResources().getString(R.string.website)+ ": " +currentInfo);
                        }
                        break;
                }
            }
    }
}
