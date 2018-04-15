package com.vysh.subairoma.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.vysh.subairoma.R;

import java.util.ArrayList;

/**
 * Created by Vishal on 4/14/2018.
 */

public class CountryDropdownList extends ArrayAdapter<String> {
    ArrayList<String> countryList;
    int importantCount = 0;

    public CountryDropdownList(Context context, int resource, ArrayList objects, int impContacts) {
        super(context, resource, objects);
        countryList = objects;
        importantCount = impContacts;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View textView = LayoutInflater.from(getContext()).inflate(R.layout.country_spinner_row, parent, false);
        TextView tv = textView.findViewById(R.id.tvCountry);
        if (position != 0 && position <= importantCount) {
            textView.setBackgroundColor(getContext().getResources().getColor(R.color.colorPrimary));
            tv.setTextColor(Color.WHITE);
        }
        tv.setText(countryList.get(position));
        return textView;
    }
}
