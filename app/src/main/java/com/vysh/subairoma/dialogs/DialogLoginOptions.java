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
import com.facebook.ProfileTracker;
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
    EditText etPhone;

    ActivityRegister activityRegister;
    ProfileTracker mProfileTracker;

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
        btnPhoneLogin = view.findViewById(R.id.btnPhoneLogin);
        loginButton = view.findViewById(R.id.login_button);
        etPhone = view.findViewById(R.id.etInput);
        setUpListeners();
        return view;
    }

    private void setUpListeners() {
        loginButton.setReadPermissions("email");
        // Callback registration
        loginButton.registerCallback(activityRegister.callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                if (Profile.getCurrentProfile() == null) {
                    mProfileTracker = new ProfileTracker() {
                        @Override
                        protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                            mProfileTracker.stopTracking();
                            //get data here
                            Log.d("mylog", "In profile Tracker, User ID: " + Profile.getCurrentProfile().getId());
                            activityRegister.checkIfFBUserExists(Profile.getCurrentProfile().getId());
                        }
                    };
                    // no need to call startTracking() on mProfileTracker
                    // because it is called by its constructor, internally.
                } else {
                    Log.d("mylog", "Successful, User ID: " + Profile.getCurrentProfile().getId());
                    activityRegister.checkIfFBUserExists(Profile.getCurrentProfile().getId());
                }
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
                String number = etPhone.getText().toString();
                if (!number.isEmpty() && number.length() == 10) {
                    activityRegister.entered_phone = number;
                    activityRegister.checkUserRegistration(number);
                } else {
                    etPhone.setError("Please enter a valid number");
                }
            }
        });
    }
}