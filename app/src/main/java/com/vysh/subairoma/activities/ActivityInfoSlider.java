package com.vysh.subairoma.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.vysh.subairoma.R;

import java.util.ArrayList;
import java.util.List;

public class ActivityInfoSlider extends AppCompatActivity {
    ViewPager infoPager;
    private ImageView[] dots;
    private int dotsCount;
    private LinearLayout pager_indicator;
    private Button btnGotIt;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        infoPager = (ViewPager) findViewById(R.id.vpPager);
        pager_indicator = (LinearLayout) findViewById(R.id.viewPagerCountDots);
        btnGotIt = (Button) findViewById(R.id.btnGotIt);
        btnGotIt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ActivityInfoSlider.this, ActivityRegister.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        btnGotIt.setVisibility(View.INVISIBLE);
        setUpViewPagerFragments();
        setPagerListener();
    }

    private void setPagerListener() {
        infoPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (position == 2)
                    btnGotIt.setVisibility(View.VISIBLE);
                else
                    btnGotIt.setVisibility(View.INVISIBLE);
                for (int i = 0; i < dotsCount; i++) {
                    dots[i].setImageDrawable(getResources().getDrawable(R.drawable.non_selected_dot));
                }
                dots[position].setImageDrawable(getResources().getDrawable(R.drawable.selected_dot));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setUpViewPagerFragments() {
        MyPagerAdapter myPagerAdapter = new MyPagerAdapter(getSupportFragmentManager());

        Resources resources = getResources();
        FragmentInfo fragmentInfo1 = new FragmentInfo();
        Bitmap first = BitmapFactory.decodeResource(getResources(), R.drawable.ic_country);
        fragmentInfo1.setInfoDetails(first, resources.getString(R.string.country), resources.getString(R.string.select_country_detail));
        fragmentInfo1.setBgColor(R.color.colorInfo1);

        Bitmap second = BitmapFactory.decodeResource(getResources(), R.drawable.ic_prep);
        FragmentInfo fragmentInfo2 = new FragmentInfo();
        fragmentInfo2.setBgColor(R.color.colorInfo2);
        fragmentInfo2.setInfoDetails(second, resources.getString(R.string.preparation), resources.getString(R.string.preparation_details));

        Bitmap third = BitmapFactory.decodeResource(getResources(), R.drawable.ic_traveltile);
        FragmentInfo fragmentInfo3 = new FragmentInfo();
        fragmentInfo3.setBgColor(R.color.colorInfo3);
        fragmentInfo3.setInfoDetails(third, resources.getString(R.string.travel), resources.getString(R.string.travel_details));

        //Setting adapters
        myPagerAdapter.addFragment(fragmentInfo1);
        myPagerAdapter.addFragment(fragmentInfo2);
        myPagerAdapter.addFragment(fragmentInfo3);

        setUpDots();
        infoPager.setAdapter(myPagerAdapter);
    }

    private void setUpDots() {
        dotsCount = 3;
        dots = new ImageView[dotsCount];

        for (int i = 0; i < dotsCount; i++) {
            dots[i] = new ImageView(getApplicationContext());
            dots[i].setImageDrawable(getResources().getDrawable(R.drawable.non_selected_dot));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(4, 0, 4, 0);
            pager_indicator.addView(dots[i], params);
        }

        dots[0].setImageDrawable(getResources().getDrawable(R.drawable.selected_dot));
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragments = new ArrayList<>();

        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        public void addFragment(Fragment fragment) {
            mFragments.add(fragment);
        }
    }
}
