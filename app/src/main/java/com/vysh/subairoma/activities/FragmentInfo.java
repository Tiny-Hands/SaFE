package com.vysh.subairoma.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.vysh.subairoma.R;

public class FragmentInfo extends Fragment {

    private TextView header;
    private TextView subHeader;
    private TextView details;
    private ImageView infoImage;
    private String hText, hDetails;
    private Bitmap bitmap;
    private LinearLayout llBg;
    private int colorResource = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_info, container, false);
        header = (TextView) mainView.findViewById(R.id.tvInfoHeader);
        header.setText(hText);
        llBg = mainView.findViewById(R.id.llbg);
        details = (TextView) mainView.findViewById(R.id.tvInfoDetails);
        details.setText(hDetails);
        infoImage = (ImageView) mainView.findViewById(R.id.ivInfo);
        infoImage.setImageBitmap(bitmap);
        if (colorResource != 0)
            llBg.setBackgroundResource(colorResource);
        return mainView;
    }

    public void setInfoDetails(Bitmap image, String h, String detail) {
        bitmap = image;
        hText = h;
        hDetails = detail;
    }

    public void setBgColor(int colorResource) {
        this.colorResource = colorResource;
    }
}
