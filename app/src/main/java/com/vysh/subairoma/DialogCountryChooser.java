package com.vysh.subairoma;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.models.CountryModel;

import java.util.ArrayList;

/**
 * Created by Vishal on 7/12/2017.
 */

public class DialogCountryChooser extends DialogFragment {

    Spinner spinner;
    int selected = 0;

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
        ArrayList<CountryModel> countries = new SQLDatabaseHelper(view.getContext()).getCountries();
        ArrayList<String> countryNameList = new ArrayList<>();
        for (CountryModel country : countries) {
            countryNameList.add(country.getCountryName());
        }
        spinner.setAdapter(new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_dropdown_item, countryNameList));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (selected != 0) {
                    Intent intent = new Intent(view.getContext(), ActivityTileHome.class);
                    view.getContext().startActivity(intent);
                }
                selected++;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}
