package com.vysh.subairoma.dialogs;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;

import com.vysh.subairoma.R;
import com.vysh.subairoma.activities.ActivityRegister;
import com.vysh.subairoma.activities.ActivityTileHome;
import com.vysh.subairoma.activities.ActivityTileQuestions;

/**
 * Created by Vishal on 4/15/2018.
 */

public class DialogCountryChangeOptions extends DialogFragment {
    RadioButton rbMistake, rbHelped;
    Button btnChosen;
    ActivityTileQuestions activityQuestions;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_usertype_chooser, container, false);
        rbHelped = view.findViewById(R.id.rbHelper);
        rbMistake = view.findViewById(R.id.rbMigrant);
        rbHelped.setText("Changing Country Based on Direction Provided by the App");
        rbMistake.setText("Had Accidentally Entered a Wrong Country");
        btnChosen = view.findViewById(R.id.btnChosen);
        btnChosen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ActivityTileQuestions)
            activityQuestions = (ActivityTileQuestions) context;
    }
}

