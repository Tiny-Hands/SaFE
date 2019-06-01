package com.vysh.subairoma.activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

import android.widget.DatePicker;

import java.util.Calendar;

/**
 * Created by Vishal on 6/24/2018.
 */

public class FragmentDatePicker extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    long minDate;

    public void setMinDate(long minDate) {
        this.minDate = minDate;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        //Create a new instance of DatePickerDialog and return it
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
        datePickerDialog.getDatePicker().setMinDate(minDate);
        return datePickerDialog;
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        //i = year, i1 = month, i2 = date
        Calendar cal = Calendar.getInstance();
        cal.set(i, i1, i2);
    }
}
