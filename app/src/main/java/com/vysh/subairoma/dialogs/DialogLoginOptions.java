package com.vysh.subairoma.dialogs;

import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.vysh.subairoma.R;
import com.vysh.subairoma.activities.ActivityRegister;

/**
 * Created by Vishal on 11/11/2017.
 */

public class DialogLoginOptions extends DialogFragment {
    Context context;
    Button btnPhoneLogin;
    LoginButton loginButton;
    CallbackManager callbackManager;

    ActivityRegister activityRegister;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        activityRegister = (ActivityRegister) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_login_options, container, false);
        btnPhoneLogin = (Button) view.findViewById(R.id.btnPhoneLogin);
        loginButton = (LoginButton) view.findViewById(R.id.login_button);
        setUpListeners();
        return view;
    }

    private void setUpListeners() {
        loginButton.setReadPermissions("email");
        callbackManager = CallbackManager.Factory.create();
        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("mylog", "Successful, User ID: " + Profile.getCurrentProfile().getId());
                activityRegister.checkIfFBUserExists(Profile.getCurrentProfile().getId());
            }

            @Override
            public void onCancel() {
                Log.d("mylog", "Canceled");
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
                Log.d("mylog", "Error: " + exception.toString());
            }
        });

        btnPhoneLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
                LayoutInflater inflater = activityRegister.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.edittext_dialog, null);
                dialogBuilder.setView(dialogView);

                final EditText etRNumber = (EditText) dialogView.findViewById(R.id.etInput);
                etRNumber.setHint("Phone number");
                dialogBuilder.setTitle("Login");
                dialogBuilder.setMessage("Enter the number that you registered");
                dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        final String number = etRNumber.getText().toString();
                        if (!number.isEmpty() && number.length() == 10) {
                            activityRegister.checkUserRegistration(number);
                        } else {
                            etRNumber.setError("Please enter a valid number");
                        }
                    }
                });
                dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                    }
                });
                AlertDialog b = dialogBuilder.create();
                b.show();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}