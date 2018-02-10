package com.vysh.subairoma.dialogs;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;

/**
 * Created by Vishal on 2/10/2018.
 */

public class DialogNeedHelp extends DialogFragment implements View.OnClickListener {
    Button btnHelp, btnVideo, btnCall;
    int qid, tileId, migrantId;
    Context context;

    public void setArgs(int qid, int tileId, Context context, int migrantId) {
        this.qid = qid;
        this.tileId = tileId;
        this.migrantId = migrantId;
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_help, container, false);
        btnHelp = view.findViewById(R.id.btnHelp);
        btnVideo = view.findViewById(R.id.btnVideo);
        btnCall = view.findViewById(R.id.btnCall);
        btnHelp.setOnClickListener(this);
        btnCall.setOnClickListener(this);
        btnVideo.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnCall:
                break;
            case R.id.btnHelp:
                showSupportDialog(qid, tileId);
                break;
            case R.id.btnVideo:
                break;
        }
    }

    private void showSupportDialog(final int qid, final int tileId) {
        //EditText queryEt;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        View supportView = LayoutInflater.from(context).inflate(R.layout.dialog_questionfeedback, null);
        Button btnSaveQuery = supportView.findViewById(R.id.btnQuery);
        builder.setView(supportView);
        final AlertDialog dialog = builder.show();

        //Query editText
        final EditText queryEt = dialog.findViewById(R.id.etInput);
        btnSaveQuery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String queryText = queryEt.getText().toString();
                if (queryText.isEmpty() || queryText.length() < 10) {
                    queryEt.setError("Please explain your query");
                } else {
                    //Save and Send Query
                    Log.d("mylog", "Saving: " + queryText);
                    SQLDatabaseHelper sqlDatabaseHelper = new SQLDatabaseHelper(context);
                    sqlDatabaseHelper.insertQuestionQuery(qid, tileId, migrantId, queryText);
                    dialog.dismiss();
                }
            }
        });
    }
}
