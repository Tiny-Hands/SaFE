package com.vysh.subairoma.dialogs;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
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
                    if (activityRegister == null)
                        activityRegister = (ActivityRegister) getActivity();
                    activityRegister.userType = uType;
                    activityRegister.showDisclaimerDialog();
                } else if (rbMigrant.isChecked()) {
                    uType = 0;
                    ApplicationClass.getInstance().setUserType(0);
                    showHelperDialog(uType);
                }
            }
        });
        return view;
    }

    private void showHelperDialog(final int uType) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = activityRegister.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_edittext, null);
        TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
        tvTitle.setText("Helper Details");
        TextView tvMsg = dialogView.findViewById(R.id.tvMsg);
        tvMsg.setText("If you are being helped by someone, enter the helpers' name below.");
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
                    activityRegister.showDisclaimerDialog();
                } else {
                    etName.setError("Please enter a valid name");
                }
            }
        });
        dialogBuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
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
