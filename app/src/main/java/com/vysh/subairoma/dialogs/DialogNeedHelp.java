package com.vysh.subairoma.dialogs;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.flurry.android.FlurryAgent;
import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.SharedPrefKeys;
import com.vysh.subairoma.activities.ActivityRegister;
import com.vysh.subairoma.activities.ActivityTileHome;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Vishal on 2/10/2018.
 */

public class DialogNeedHelp extends DialogFragment implements View.OnClickListener {
    final String queryAPI = "/savequestionquery.php";
    Button btnHelp, btnVideo, btnCall;
    TextView tvVideoLabel;
    int qid, tileId, migrantId;
    String number, link;
    Context context;

    public void setArgs(int qid, int tileId, Context context, int migrantId, String number, String link) {
        this.qid = qid;
        this.tileId = tileId;
        this.migrantId = migrantId;
        this.context = context;
        if (number == null || number.equalsIgnoreCase("null") || number.isEmpty())
            this.number = "+9779840337809";
        else
            this.number = number;
        this.link = link;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_help, container, false);
        btnHelp = view.findViewById(R.id.btnHelp);
        btnCall = view.findViewById(R.id.btnCall);
        btnVideo = view.findViewById(R.id.btnVideo);
        tvVideoLabel = view.findViewById(R.id.tvLabelVideo);
        btnHelp.setOnClickListener(this);
        btnCall.setOnClickListener(this);
        btnVideo.setOnClickListener(this);
        if (link == null || link.equalsIgnoreCase("null") || link.isEmpty()) {
            btnVideo.setVisibility(View.GONE);
            tvVideoLabel.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnCall:
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + number));
                FlurryAgent.logEvent("help_call_action_chosen");
                startActivity(intent);
                break;
            case R.id.btnHelp:
                FlurryAgent.logEvent("help_support_action_chosen");
                dismiss();
                showSupportDialog(qid, tileId);
                break;
            case R.id.btnVideo:
                FlurryAgent.logEvent("help_video_action_chosen");
                if (!link.startsWith("http://") && !link.startsWith("http://"))
                    link = "http://" + link;
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                startActivity(browserIntent);
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
                FlurryAgent.logEvent("help_query_saved");
                String queryText = queryEt.getText().toString();
                if (queryText.isEmpty() || queryText.length() < 10) {
                    queryEt.setError("Please explain your query");
                } else {
                    //Save and Send Query
                    Log.d("mylog", "Saving: " + queryText);
                    SQLDatabaseHelper sqlDatabaseHelper = new SQLDatabaseHelper(context);
                    sqlDatabaseHelper.insertQuestionQuery(qid, tileId, migrantId, queryText);
                    saveQuery(queryText);
                    dialog.dismiss();
                }
            }
        });
    }

    private void saveQuery(final String query) {
        String api = ApplicationClass.getInstance().getAPIROOT() + queryAPI;
        final String fapi = api;

        final ProgressDialog progressDialog = new ProgressDialog(context);
        //progressDialog.setTitle("Please wait");
        progressDialog.setMessage(getResources().getString(R.string.saving_query));
        progressDialog.show();
        progressDialog.setCancelable(false);

        StringRequest stringRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                Log.d("mylog", "Response: " + response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                String err = error.toString();
                if (!err.isEmpty() && err.contains("NoConnection")) {
                    //showSnackbar("Response cannot be saved at the moment, please check your Intenet connection.");
                    Log.d("mylog", "Response cannot be saved at the moment, please check your Intenet connection.");
                } else
                    Log.d("mylog", "Error saving response: " + err + " \n For: " + fapi);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("user_id", ApplicationClass.getInstance().getUserId() + "");
                params.put("migrant_id", ApplicationClass.getInstance().getMigrantId() + "");
                params.put("query", query);
                params.put("tile_id", tileId + "");
                params.put("question_id", qid + "");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", context.getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE).getString(SharedPrefKeys.token, ""));
                return headers;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(stringRequest);
    }
}
