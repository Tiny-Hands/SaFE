package com.vysh.subairoma.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.vysh.subairoma.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Vishal on 6/16/2018.
 */

public class ActivityAboutUs extends AppCompatActivity {

    @BindView(R.id.tvfaq1)
    TextView faq1;
    @BindView(R.id.tvfaq2)
    TextView faq2;
    @BindView(R.id.tvfaq3)
    TextView faq3;
    @BindView(R.id.tvDetail3)
    TextView tvDetail3;
    @BindView(R.id.tvDetail2)
    TextView tvDetail2;
    @BindView(R.id.tvDetail1)
    TextView tvDetail1;
    @BindView(R.id.viewseperator2)
    View viewSeperator;
    @BindView(R.id.tvTitle)
    TextView tvTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aboutus);
        ButterKnife.bind(this);
        if (getIntent().hasExtra("faq")) {
            faq1.setText("Question 1");
            faq2.setText("Question 2");
            tvTitle.setText("FAQs");
        } else if (getIntent().hasExtra("contact")) {
            faq1.setText("Email");
            tvDetail1.setText("subairoma@gmail.com");
            faq2.setText("Phone");
            tvDetail2.setText("9800111111");
            viewSeperator.setVisibility(View.VISIBLE);
            faq3.setText("Address");
            faq3.setVisibility(View.VISIBLE);
            tvDetail3.setText("Lorem ipsum dolor sit amet, consectetur adipiscing elit.");
            tvDetail3.setVisibility(View.VISIBLE);
            tvTitle.setText("Contact US");
        }
    }
}
