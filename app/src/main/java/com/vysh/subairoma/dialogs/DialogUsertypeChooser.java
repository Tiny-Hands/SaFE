package com.vysh.subairoma.dialogs;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SharedPrefKeys;
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
                if (rbHelper.isChecked()) {
                    ApplicationClass.getInstance().setUserType(SharedPrefKeys.helperUser);
                    if (activityRegister == null)
                        activityRegister = (ActivityRegister) getActivity();
                    activityRegister.userType = SharedPrefKeys.helperUser;
                    activityRegister.showDisclaimerAndContinue();
                } else if (rbMigrant.isChecked()) {
                    ApplicationClass.getInstance().setUserType(SharedPrefKeys.migrantUser);
                    showHelperDialog(SharedPrefKeys.migrantUser);
                }
            }
        });
        return view;
    }

    private void showHelperDialog(final String uType) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = activityRegister.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_edittext, null);
        TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
        tvTitle.setText("Helper Details");
        TextView tvMsg = dialogView.findViewById(R.id.tvMsg);
        tvMsg.setText(getResources().getString(R.string.enterhelpersdetail));
        dialogBuilder.setView(dialogView);

        final EditText etName = dialogView.findViewById(R.id.etInput);
        etName.setHint("Name");
        etName.setInputType(InputType.TYPE_CLASS_TEXT);
        etName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20)});
        dialogBuilder.setPositiveButton(getResources().getString(R.string.done), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                final String name = etName.getText().toString();
                if (!name.isEmpty() && name.length() > 3) {
                    //activityRegister.checkUserRegistration(number);
                    Toast.makeText(activityRegister, "Helper Added", Toast.LENGTH_SHORT).show();
                    if (activityRegister == null)
                        activityRegister = (ActivityRegister) getActivity();
                    activityRegister.userType = uType;
                    activityRegister.showDisclaimerAndContinue();
                } else {
                    etName.setError("Please enter a valid name");
                }
            }
        });
        dialogBuilder.setNegativeButton(getResources().getString(R.string.not_being_helped), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                if (activityRegister == null)
                    activityRegister = (ActivityRegister) getActivity();
                activityRegister.userType = uType;
                activityRegister.showDisclaimerAndContinue();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ActivityRegister)
            activityRegister = (ActivityRegister) context;
    }
}
