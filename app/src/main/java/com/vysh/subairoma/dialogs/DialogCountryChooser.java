package com.vysh.subairoma.dialogs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.SharedPrefKeys;
import com.vysh.subairoma.activities.ActivityTileChooser;
import com.vysh.subairoma.activities.ActivityTileHome;
import com.vysh.subairoma.models.CountryModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Created by Vishal on 7/12/2017.
 */

public class DialogCountryChooser extends DialogFragment {
    Spinner spinner;
    String migName;
    int status, blacklist, importantCount = 0;

    public static DialogCountryChooser newInstance() {
        DialogCountryChooser frag = new DialogCountryChooser();
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_countries_spinner, container, false);
        spinner = view.findViewById(R.id.spinnerCountries);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //String[] countries = getResources().getStringArray(R.array.countries_array);
        final ArrayList<CountryModel> countries = new SQLDatabaseHelper(view.getContext()).getCountries();
        ArrayList<String> countryNameList = new ArrayList<>();
        //To show in the beginning, nothing selected index 0 called by default
        countryNameList.add("-------");
        for (CountryModel country : countries) {
            Log.d("mylog", "Country name: " + country.getCountryName());
            countryNameList.add(country.getCountryName().toUpperCase());
            if (country.getOrder() >= 1)
                importantCount++;
        }
        AdapterCountry adapterCountry = new AdapterCountry(getContext(), R.layout.country_spinner_row, countryNameList);
        spinner.setAdapter(adapterCountry);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("mylog", "Country position: " + position);
                if (position != 0) {
                    //Subtracted one because countryNameList has 1 extra default item but countries array doesn't
                    CountryModel country = countries.get(position - 1);
                    String cid = country.getCountryId();
                    String cname = country.getCountryName();
                    blacklist = country.getCountryBlacklist();
                    status = country.getCountrySatus();
                    Log.d("mylog", "Country code: " + cid + " Status: " + status
                            + " Blacklist: " + blacklist);
                    if (blacklist == 1) {
                        showDialog(getContext().getResources().getString(R.string.blacklisted),
                                getContext().getResources().getString(R.string.blacklisted_message), cid, cname, 0);
                    } else if (status == 1) {
                        showDialog(getContext().getResources().getString(R.string.not_open),
                                getContext().getResources().getString(R.string.not_open_message), cid, cname, 0);
                    } else {
                        showDialog(getString(R.string.confirm), getString(R.string.confirm_message) + " " + cname + "?", cid, cname, 1);
                        Log.d("mylog", "Saving country for MID: " + ApplicationClass.getInstance().getMigrantId());
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void showDialog(String title, String message, final String cid, final String cname, final int cType) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getContext());
        mBuilder.setTitle(title);
        mBuilder.setMessage(message);
        String negativeMsg;
        if (cType == 0)
            negativeMsg = getString(R.string.go_regardless);
        else
            negativeMsg = getString(R.string.yes);
        mBuilder.setNegativeButton(negativeMsg,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Calendar cal = Calendar.getInstance();
                        String time = cal.getTimeInMillis() + "";
                        new SQLDatabaseHelper(getContext()).insertResponseTableData(cid, SharedPrefKeys.questionCountryId, -1,
                                ApplicationClass.getInstance().getMigrantId(), "mg_destination", time);

                        Intent intent = new Intent(getContext(), ActivityTileChooser.class);
                        intent.putExtra("countryId", cid);
                        intent.putExtra("migrantName", migName);
                        intent.putExtra("countryName", cname);
                        intent.putExtra("countryStatus", status);
                        intent.putExtra("countryBlacklist", blacklist);
                        dismiss();
                        if (ApplicationClass.getInstance().getUserId() == -1)
                            intent = intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        getContext().startActivity(intent);
                    }
                });
        mBuilder.setPositiveButton(getContext().getResources().getString(R.string.choose_another_country), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mBuilder.show();
    }

    public void setMigrantName(String migrantName) {
        migName = migrantName;
    }

    private class AdapterCountry extends ArrayAdapter<String> {
        ArrayList<String> countryList;

        public AdapterCountry(Context context, int resource, ArrayList objects) {
            super(context, resource, objects);
            countryList = objects;
        }

        @Override
        public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View textView = LayoutInflater.from(getContext()).inflate(R.layout.country_spinner_row, parent, false);
            TextView tv = textView.findViewById(R.id.tvCountry);
            if (position != 0 && position <= importantCount) {
                textView.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                tv.setTextColor(Color.WHITE);
            }
            tv.setText(countryList.get(position));
            return textView;
        }
    }
}