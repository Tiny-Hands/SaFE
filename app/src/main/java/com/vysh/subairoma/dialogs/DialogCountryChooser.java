package com.vysh.subairoma.dialogs;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.activities.ActivityTileHome;
import com.vysh.subairoma.models.CountryModel;

import java.util.ArrayList;

/**
 * Created by Vishal on 7/12/2017.
 */

public class DialogCountryChooser extends DialogFragment {
    Spinner spinner;
    String migName;
    int status, blacklist;

    public static DialogCountryChooser newInstance() {
        DialogCountryChooser frag = new DialogCountryChooser();
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_countries_spinner, container, false);
        spinner = (Spinner) view.findViewById(R.id.spinnerCountries);
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
        }
        spinner.setAdapter(new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_dropdown_item, countryNameList));
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
                                getContext().getResources().getString(R.string.blacklisted_message), cid, cname);
                    } else if (status == 1) {
                        showDialog(getContext().getResources().getString(R.string.not_open),
                                getContext().getResources().getString(R.string.not_open_message), cid, cname);
                    } else {
                        //For mg_destination -1 is question id as it's not identified as question currently.
                        Log.d("mylog", "Saving country for MID: " + ApplicationClass.getInstance().getMigrantId());
                        new SQLDatabaseHelper(getContext()).insertResponseTableData(cid, -1, -1,
                                ApplicationClass.getInstance().getMigrantId(), "mg_destination");
                        Intent intent = new Intent(getContext(), ActivityTileHome.class);
                        intent.putExtra("countryId", cid);
                        intent.putExtra("migrantName", migName);
                        intent.putExtra("countryName", cname);
                        intent.putExtra("countryStatus", status);
                        intent.putExtra("countryBlacklist", blacklist);
                        dismiss();
                        if (ApplicationClass.getInstance().getUserId() == -1)
                            intent = intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        getContext().startActivity(intent);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void showDialog(String title, String message, final String cid, final String cname) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getContext());
        mBuilder.setTitle(title);
        mBuilder.setMessage(message);
        mBuilder.setNegativeButton(cname.toUpperCase() + " " +
                        getContext().getResources().getString(R.string.go_regardless),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new SQLDatabaseHelper(getContext()).insertResponseTableData(cid, -1, -1,
                        ApplicationClass.getInstance().getMigrantId(), "mg_destination");

                Intent intent = new Intent(getContext(), ActivityTileHome.class);
                intent.putExtra("countryId", cid);
                intent.putExtra("migrantName", migName);
                intent.putExtra("countryName", cname);
                intent.putExtra("countryStatus", status);
                intent.putExtra("countryBlacklist", blacklist);
                dismiss();
                if (ApplicationClass.getInstance().getUserId() == -1)
                    intent = intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
}
