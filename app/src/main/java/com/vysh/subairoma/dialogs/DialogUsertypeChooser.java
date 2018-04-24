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

import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.R;
import com.vysh.subairoma.activities.ActivityRegister;

/**
 * Created by Vishal on 3/10/2018.
 */

public class DialogUsertypeChooser extends DialogFragment {
    RadioButton rbMigrant, rbHelper;
    Button btnChosen;
    ActivityRegister activityRegister;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_usertype_chooser, container, false);
        rbHelper = view.findViewById(R.id.rbHelper);
        rbMigrant = view.findViewById(R.id.rbMigrant);
        btnChosen = view.findViewById(R.id.btnChosen);
        btnChosen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int uType = -1;
                if (rbHelper.isChecked()) {
                    uType = 1;
                    ApplicationClass.getInstance().setUserType(1);
                } else if (rbMigrant.isChecked()) {
                    uType = 0;
                    ApplicationClass.getInstance().setUserType(0);
                }
                Log.d("mylog", "Current usertype: " + uType);
                if (uType != -1) {
                    dismiss();
                    if (activityRegister == null)
                        activityRegister = (ActivityRegister) getActivity();
                    activityRegister.userType = uType;
                    activityRegister.showDisclaimerDialog();
                }
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ActivityRegister)
            activityRegister = (ActivityRegister) context;
    }
}
