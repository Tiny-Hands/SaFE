package com.vysh.subairoma.dialogs;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.vysh.subairoma.R;
import com.vysh.subairoma.activities.ActivityMigrantList;
import com.vysh.subairoma.activities.ActivityRegister;
import com.vysh.subairoma.activities.ActivityTileChooser;

/**
 * Created by Vishal on 3/10/2018.
 */

public class DialogUsertypeChooser extends DialogFragment {
    RadioButton rbMigrant, rbHelper;
    Button btnChosen;
    ActivityMigrantList activityMigList = null;
    ActivityTileChooser activityTileChooser = null;
    String userType;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_usertype_chooser, container, false);
        rbHelper = view.findViewById(R.id.rbHelper);
        rbHelper.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    userType = "helper";
            }
        });
        rbMigrant = view.findViewById(R.id.rbMigrant);
        rbMigrant.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    userType = "migrant";
            }
        });
        btnChosen = view.findViewById(R.id.btnChosen);
        btnChosen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (activityMigList != null)
                    activityMigList.updateUserType(userType);
                else if (activityTileChooser != null)
                    activityTileChooser.updateUserType(userType);
                dismiss();
            }
        });
        return view;
    }

    private void showHelperDialog(final String uType) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = activityMigList.getLayoutInflater();
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
                    //activityMigList.checkUserRegistration(number);
                  /*  Toast.makeText(activityMigList, "Helper Added", Toast.LENGTH_SHORT).show();
                    if (activityMigList == null)
                        activityMigList = (ActivityRegister) getActivity();*/
                } else {
                    etName.setError("Please enter a valid name");
                }
            }
        });
        dialogBuilder.setNegativeButton(getResources().getString(R.string.not_being_helped), new DialogInterface.OnClickListener() {
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
        if (context instanceof ActivityMigrantList)
            activityMigList = (ActivityMigrantList) context;
        else if (context instanceof ActivityTileChooser)
            activityTileChooser = (ActivityTileChooser) context;
    }
}
