package com.vysh.subairoma.dialogs;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.activities.ActivityFeedback;
import com.vysh.subairoma.activities.ActivityGasSection;
import com.vysh.subairoma.activities.ActivityRegister;
import com.vysh.subairoma.activities.ActivityTileHome;

import java.util.Calendar;

/**
 * Created by Vishal on 8/1/2017.
 */

public class DialogAnswersVerification extends DialogFragment implements View.OnClickListener {
    Button back, proceed;
    CheckBox checkbox;
    TextView tvCheckTerm;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_final_answers_verification, container, false);
        back = (Button) view.findViewById(R.id.btnBack);
        proceed = (Button) view.findViewById(R.id.btnProceed);
        tvCheckTerm = (TextView) view.findViewById(R.id.tvCheckTerm);
        checkbox = (CheckBox) view.findViewById(R.id.checkboxTermsAccept);
        back.setOnClickListener(this);
        proceed.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnBack:
                dismiss();
                break;
            case R.id.btnProceed:
                if (checkbox.isChecked()) {
                    ActivityTileHome activity = (ActivityTileHome) getActivity();

                    Intent intent = new Intent(getContext(), ActivityFeedback.class);
                    intent.putExtra("countryId", activity.countryId);
                    intent.putExtra("migrantName", activity.migName);
                    intent.putExtra("countryName", activity.countryName);
                    intent.putExtra("countryStatus", activity.status);
                    intent.putExtra("countryBlacklist", activity.blacklist);
                    dismiss();


                    Calendar cal = Calendar.getInstance();
                    String time = cal.getTimeInMillis() + "";
                    new SQLDatabaseHelper(getContext()).insertResponseTableData("true", -3, -1,
                            ApplicationClass.getInstance().getMigrantId(), "mg_verified_answers", time);
                    startActivity(intent);
                } else {
                    tvCheckTerm.setTextColor(getResources().getColor(R.color.colorError));
                }
                break;
        }
    }
}
