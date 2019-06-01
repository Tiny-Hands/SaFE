package com.vysh.subairoma.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.vysh.subairoma.R;
import com.wordpress.priyankvex.smarttextview.SmartTextView;

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
    @BindView(R.id.tvfaq4)
    TextView faq4;
    @BindView(R.id.tvfaq5)
    TextView faq5;
    @BindView(R.id.tvfaq6)
    TextView faq6;
    @BindView(R.id.tvfaq7)
    TextView faq7;
    @BindView(R.id.tvfaq8)
    TextView faq8;
    @BindView(R.id.tvDetail3)
    SmartTextView tvDetail3;
    @BindView(R.id.tvDetail2)
    SmartTextView tvDetail2;
    @BindView(R.id.tvDetail1)
    SmartTextView tvDetail1;
    @BindView(R.id.tvDetail4)
    SmartTextView tvDetail4;
    @BindView(R.id.tvDetail5)
    SmartTextView tvDetail5;
    @BindView(R.id.tvDetail6)
    SmartTextView tvDetail6;
    @BindView(R.id.tvDetail7)
    SmartTextView tvDetail7;
    @BindView(R.id.tvDetail8)
    SmartTextView tvDetail8;
    @BindView(R.id.viewseperator2)
    View viewSeperator;
    @BindView(R.id.tvTitle)
    TextView tvTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aboutus);
        ButterKnife.bind(this);
        hideAll();
        if (getIntent().hasExtra("faq")) {
            tvTitle.setText(R.string.faqs);
            faq1.setVisibility(View.VISIBLE);
            faq1.setText(R.string.fq1);
            faq2.setVisibility(View.VISIBLE);
            faq2.setText(R.string.fq2);
            faq3.setVisibility(View.VISIBLE);
            faq3.setText(R.string.fq3);
            faq4.setVisibility(View.VISIBLE);
            faq4.setText(R.string.fq4);
            faq5.setVisibility(View.VISIBLE);
            faq5.setText(R.string.fq5);
            faq6.setVisibility(View.VISIBLE);
            faq6.setText(R.string.fq6);
            faq7.setVisibility(View.VISIBLE);
            faq7.setText(R.string.fq7);
            faq8.setVisibility(View.VISIBLE);
            faq8.setText(R.string.fq8);

            tvDetail1.setVisibility(View.VISIBLE);
            tvDetail1.setText(R.string.fqa1);
            tvDetail2.setVisibility(View.VISIBLE);
            tvDetail2.setText(R.string.fqa2);
            tvDetail3.setVisibility(View.VISIBLE);
            tvDetail3.setText(R.string.fqa3);
            tvDetail4.setVisibility(View.VISIBLE);
            tvDetail4.setText(R.string.fqa4);
            tvDetail5.setVisibility(View.VISIBLE);
            tvDetail5.setText(R.string.fqa5);
            tvDetail6.setVisibility(View.VISIBLE);
            tvDetail6.setText(R.string.fqa6);
            tvDetail7.setVisibility(View.VISIBLE);
            tvDetail7.setText(R.string.fqa7);
            tvDetail8.setVisibility(View.VISIBLE);
            tvDetail8.setText(R.string.fqa8);
        } else if (getIntent().hasExtra("contact")) {
            tvTitle.setText(R.string.contact_us);
            tvDetail1.setVisibility(View.VISIBLE);
            faq1.setVisibility(View.VISIBLE);
            faq1.setText(R.string.email);
            tvDetail1.setText("safe@tinyhands.org");

            tvDetail2.setVisibility(View.VISIBLE);
            faq2.setVisibility(View.VISIBLE);
            faq2.setText(R.string.phone_number);
            tvDetail2.setText("9840337809");
        } else {
            tvTitle.setText(R.string.about_us);
            tvDetail1.setVisibility(View.VISIBLE);
            tvDetail1.setText(R.string.about_us_content);
        }
    }

    private void hideAll() {
        faq1.setVisibility(View.GONE);
        tvDetail1.setVisibility(View.GONE);
        faq2.setVisibility(View.GONE);
        tvDetail2.setVisibility(View.GONE);
        faq3.setVisibility(View.GONE);
        tvDetail3.setVisibility(View.GONE);
        faq4.setVisibility(View.GONE);
        tvDetail4.setVisibility(View.GONE);
        faq5.setVisibility(View.GONE);
        tvDetail5.setVisibility(View.GONE);
        faq6.setVisibility(View.GONE);
        tvDetail6.setVisibility(View.GONE);
        faq7.setVisibility(View.GONE);
        tvDetail7.setVisibility(View.GONE);
        faq8.setVisibility(View.GONE);
        tvDetail8.setVisibility(View.GONE);
    }
}
