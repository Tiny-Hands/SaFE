package com.vysh.subairoma.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.crashlytics.android.Crashlytics;
import com.flurry.android.FlurryAgent;
import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.SharedPrefKeys;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.fabric.sdk.android.Fabric;

/**
 * Created by Vishal on 6/12/2017.
 */

public class ActivitySplash extends AppCompatActivity {
    private final String tilesAPI = "/getalltiles.php";
    private final String questionAPI = "/getallquestions.php";
    private final String optionsAPI = "/getalloptions.php";
    private final String countiesAPI = "/getcountries.php";
    private final String manpowersAPI = "/getmanpowers.php";
    private final String importantContactsAPI = "/getimportantcontacts.php";
    private final String importantContactsDefaultAPI = "/getimportantcontactsdefault.php";
    private final String feedbackQuestions = "/getfeedbackquestions.php";
    private int savedCount = 0, apiErrorCount = 0;
    private int apiCount = 8;
    private long startTime, sleepTime;
    private String lang;
    private HashMap<String, String> fParams;

    SharedPreferences sp;

    SQLDatabaseHelper dbHelper;
    Thread sleepThread;

    @BindView(R.id.progressCircle)
    LinearLayout progressBar;
    @BindView(R.id.tvLoadingNep)
    TextView tvLoadingNepali;
    @BindView(R.id.tvLoadingEng)
    TextView tvLoadingEng;
    @BindView(R.id.llBottom)
    LinearLayout bottomLayoutMessage;
    @BindView(R.id.btnTryAgain)
    Button btnTryAgain;
    @BindView(R.id.llLang)
    LinearLayout llLang;
    @BindView(R.id.ibEn)
    ImageButton ibEn;
    @BindView(R.id.ibNp)
    ImageButton ibNp;

    RequestQueue queue;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sp = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE);
        int currVersion = sp.getInt(SharedPrefKeys.dbVersion, -1);
        dbHelper = SQLDatabaseHelper.getInstance(ActivitySplash.this);
        queue = Volley.newRequestQueue(ActivitySplash.this);
        if (dbHelper.getVersion() > currVersion && currVersion != -1) {
            Log.d("mylog", "Greater Version dropping");
            dbHelper.dropDB();
            //Remove User data
            SharedPreferences.Editor editor = sp.edit();
            editor.putInt(SharedPrefKeys.userId, -10);
            editor.putString(SharedPrefKeys.userType, "");
            editor.commit();
            finish();
            Intent intent = new Intent(ActivitySplash.this, ActivitySplash.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return;
        }
        if (sp.getString(SharedPrefKeys.lang, "").equalsIgnoreCase("en")) {
            setLocale("en");
            FlurryAgent.logEvent("locale_english_selection");
        } else {
            setLocale("np");
            FlurryAgent.logEvent("locale_nepal_selection");
        }
        if (sp.getInt(SharedPrefKeys.savedTableCount, 0) == apiCount) {
            startRegisterActivity();
            return;
        }
        setContentView(R.layout.activity_splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Fabric.with(this, new Crashlytics());
        ButterKnife.bind(this);

        btnTryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAllData();
                //progressBar.setVisibility(View.VISIBLE);
                bottomLayoutMessage.setVisibility(View.GONE);
            }
        });

        ibEn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLocale("en");
                lang = "en";
                fParams = new HashMap<>();
                fParams.put("lang", lang);
                hideLangOptions();
                getAllData();

                SharedPreferences.Editor editor = sp.edit();
                editor.putString(SharedPrefKeys.lang, "en");
                editor.commit();
            }
        });

        ibNp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLocale("np");
                lang = "np";
                fParams = new HashMap<>();
                fParams.put("lang", lang);
                hideLangOptions();
                getAllData();

                SharedPreferences.Editor editor = sp.edit();
                editor.putString(SharedPrefKeys.lang, "np");
                editor.commit();
            }
        });

        startTime = System.currentTimeMillis();
        sleepThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (sleepTime > 0)
                        Thread.sleep(sleepTime);
                } catch (Exception ex) {
                    Log.d("mylog", "Sleeping exception: " + ex.toString());
                } finally {
                    startRegisterActivity();
                }
            }
        });
    }

    private void startRegisterActivity() {
        Intent intent = new Intent(ActivitySplash.this, ActivityRegister.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void setLocale(String lang) {
        Locale myLocale = new Locale(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
    }

    private void getAllData() {
        //Showing Progress
        if (sp.getInt(SharedPrefKeys.savedTableCount, 0) != apiCount) {
            showLoading();
            Log.d("mylog", "Starting save");
            dbHelper = SQLDatabaseHelper.getInstance(ActivitySplash.this);
            dbHelper.getWritableDatabase();
            savedCount = 0;
            /*getTiles();
            getQuestions();
            getOptions();
            getCountries();
            getContacts(1);
            getContacts(2);
            getFeedbackQuestions();
            getManpowers();*/

            if (!sp.getBoolean(SharedPrefKeys.savedTiles, false)) {
                Log.d("mylog", "Getting tiles");
                getTiles();
            }
            if (!sp.getBoolean(SharedPrefKeys.savedQuestions, false)) {
                Log.d("mylog", "Getting questions");
                getQuestions();
            }
            if (!sp.getBoolean(SharedPrefKeys.savedOptions, false)) {
                Log.d("mylog", "Getting options");
                getOptions();
            }
            if (!sp.getBoolean(SharedPrefKeys.savedCountries, false)) {
                Log.d("mylog", "Getting countries");
                getCountries();
            }
            if (!sp.getBoolean(SharedPrefKeys.savedContacts, false)) {
                Log.d("mylog", "Getting contacts");
                getContacts(1);
                getContacts(2);
            }
            if (!sp.getBoolean(SharedPrefKeys.savedFeedbackQuestions, false)) {
                Log.d("mylog", "Getting feedback questions");
                getFeedbackQuestions();
            }
            if (!sp.getBoolean(SharedPrefKeys.savedManpowers, false)) {
                Log.d("mylog", "Getting feedback questions");
                getManpowers();
            }

        } else {
            Log.d("mylog", "Saved already, starting");
            //Start activity directly or show splash
            //sleepTime = 2000;
            //sleepThread.start();
            startRegisterActivity();
        }
    }

    private void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
        if (lang.equalsIgnoreCase("np"))
            tvLoadingNepali.setVisibility(View.VISIBLE);
        else if (lang.equalsIgnoreCase("en"))
            tvLoadingEng.setVisibility(View.VISIBLE);
    }

    private void getContacts(int type) {
        String api;
        if (type == 1)
            api = ApplicationClass.getInstance().getAPIROOT() + importantContactsAPI;
        else
            api = ApplicationClass.getInstance().getAPIROOT() + importantContactsDefaultAPI;

        StringRequest getRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //SAVE RESPONSE IN LOCAL DB
                Log.d("mylog", "Got contacts: " + response);
                parseAndSaveContacts(response);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(SharedPrefKeys.savedContacts, true);
                editor.commit();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("mylog", "Error getting questions: " + error.toString());
                if (apiErrorCount > 8)
                    showErrorResponse();
                else {
                    getContacts(1);
                    getContacts(2);
                }
                apiErrorCount++;
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return fParams;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", ApplicationClass.getInstance().getAppToken());
                return headers;
            }
        };

        queue.add(getRequest);
    }

    private void getTiles() {
        String api = ApplicationClass.getInstance().getAPIROOT() + tilesAPI;
        StringRequest getRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //SAVE RESPONSE IN LOCAL DB
                Log.d("mylog", "Got Tiles: " + response);
                parseAndSaveTiles(response);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(SharedPrefKeys.savedTiles, true);
                editor.commit();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("mylog", "Got Tiles error: " + error.toString());
                if (apiErrorCount > 8)
                    showErrorResponse();
                else
                    getTiles();
                apiErrorCount++;
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return fParams;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", ApplicationClass.getInstance().getAppToken());
                return headers;
            }
        };

        queue.add(getRequest);
    }

    private void showErrorResponse() {
        if (Build.VERSION.SDK_INT >= 19) {
            TransitionManager.beginDelayedTransition((ViewGroup) bottomLayoutMessage.getParent());
        }
        if (bottomLayoutMessage.getVisibility() != View.VISIBLE) {
            bottomLayoutMessage.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }
    }

    private void getQuestions() {
        String api = ApplicationClass.getInstance().getAPIROOT() + questionAPI;
        StringRequest getRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //SAVE RESPONSE IN LOCAL DB
                Log.d("mylog", "Got questions: " + response);
                parseAndSaveQuestions(response);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(SharedPrefKeys.savedQuestions, true);
                editor.commit();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("mylog", "Error getting questions: " + error.toString());
                if (apiErrorCount > 8)
                    showErrorResponse();
                else
                    getQuestions();
                apiErrorCount = 0;
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return fParams;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", ApplicationClass.getInstance().getAppToken());
                return headers;
            }
        };
        ;
        queue.add(getRequest);
    }

    private void getOptions() {
        String api = ApplicationClass.getInstance().getAPIROOT() + optionsAPI;
        StringRequest getRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //SAVE RESPONSE IN LOCAL DB
                Log.d("mylog", "Got options: " + response);
                parseAndSaveOptions(response);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(SharedPrefKeys.savedOptions, true);
                editor.commit();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("mylog", "Error getting options: " + error.toString());
                if (apiErrorCount > 8)
                    showErrorResponse();
                else
                    getOptions();
                apiErrorCount = 0;
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return fParams;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", ApplicationClass.getInstance().getAppToken());
                return headers;
            }
        };

        queue.add(getRequest);
    }

    private void getCountries() {
        String api = ApplicationClass.getInstance().getAPIROOT() + countiesAPI;
        StringRequest getRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //SAVE RESPONSE IN LOCAL DB
                parseAndSaveCountries(response);
                Log.d("mylog", "Got countries: " + response);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(SharedPrefKeys.savedCountries, true);
                editor.commit();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("mylog", "Error getting countries: " + error.toString());
                if (apiErrorCount > 8)
                    showErrorResponse();
                else
                    getCountries();
                apiErrorCount++;
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return fParams;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", ApplicationClass.getInstance().getAppToken());
                return headers;
            }
        };

        queue.add(getRequest);
    }

    private void getManpowers() {
        String api = ApplicationClass.getInstance().getAPIROOT() + manpowersAPI;
        StringRequest getRequest = new StringRequest(Request.Method.GET, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //SAVE RESPONSE IN LOCAL DB
                parseAndSaveManpowers(response);
                Log.d("mylog", "Got Manpowers: " + response);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(SharedPrefKeys.savedManpowers, true);
                editor.commit();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("mylog", "Error getting manpowers: " + error.toString());
                if (apiErrorCount > 8)
                    showErrorResponse();
                else
                    getManpowers();
                apiErrorCount++;
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", ApplicationClass.getInstance().getAppToken());
                return headers;
            }
        };

        queue.add(getRequest);
    }

    public void getFeedbackQuestions() {
        String api = ApplicationClass.getInstance().getAPIROOT() + feedbackQuestions;
        StringRequest getRequest = new StringRequest(Request.Method.POST, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //SAVE RESPONSE IN LOCAL DB
                parseAndSaveFeedback(response);
                Log.d("mylog", "Got feedback: " + response);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(SharedPrefKeys.savedFeedbackQuestions, true);
                editor.commit();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("mylog", "Error getting countries: " + error.toString());
                if (apiErrorCount > 8)
                    showErrorResponse();
                else getFeedbackQuestions();
                apiErrorCount++;
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return fParams;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", ApplicationClass.getInstance().getAppToken());
                return headers;
            }
        };

        queue.add(getRequest);
    }

    private void parseAndSaveFeedback(String response) {
        try {
            JSONObject jsonQuestions = new JSONObject(response);
            boolean error = jsonQuestions.getBoolean("error");
            if (error) {
                Log.d("mylog", "Error getting contacts: " + response);
            } else {
                JSONArray questionArray = jsonQuestions.getJSONArray("feedback_questions");
                for (int i = 0; i < questionArray.length(); i++) {
                    JSONObject questionObject = questionArray.getJSONObject(i);

                    String qidString = questionObject.getString("question_id");
                    int qid = -1;
                    if (!qidString.equalsIgnoreCase("null")) {
                        qid = Integer.parseInt(qidString);
                    }
                    String qTitle = questionObject.getString("question_title");
                    String qOption = questionObject.getString("question_option");
                    String qType = questionObject.getString("feedback_type");

                    Log.d("mylog", "Feedback qid: " + qid);
                    dbHelper.insertFeedbackQuestions(qid, qTitle, qOption, qType);
                }
                incrementCount();
            }
        } catch (JSONException e) {
            Log.d("mylog", "Error parsing feedback questions: " + e.toString());
        }
    }

    private void parseAndSaveContacts(String response) {
        try {
            JSONObject jsonContacts = new JSONObject(response);
            boolean error = jsonContacts.getBoolean("error");
            if (error) {
                Log.d("mylog", "Error getting contacts: " + response);
            } else {
                JSONArray contactsArray = jsonContacts.getJSONArray("contacts");
                for (int i = 0; i < contactsArray.length(); i++) {
                    JSONObject contactsObject = contactsArray.getJSONObject(i);
                    String cid;
                    if (contactsObject.has("country_id"))
                        cid = contactsObject.getString("country_id");
                    else
                        cid = "default";

                    int contactId;
                    if (contactsObject.has("contact_id"))
                        contactId = contactsObject.getInt("contact_id");
                    else
                        contactId = contactsObject.getInt("id");

                    String title = contactsObject.getString("title");
                    String description = contactsObject.getString("description");
                    String address = contactsObject.getString("address");
                    String phone = contactsObject.getString("phone");
                    String email = contactsObject.getString("email");
                    String website = contactsObject.getString("website");
                    Log.d("mylog", cid);
                    if (cid.equalsIgnoreCase("default")) {
                        Log.d("myimplog", "Saving default:" + contactId);
                        dbHelper.insertImportantContactsDefault(contactId, title, description, address, phone, email, website);
                        Log.d("mylog", "Inserting Default: " + title);
                    } else {
                        Log.d("myimplog", "Saving specific:" + cid);
                        dbHelper.insertImportantContacts(contactId, cid, title, description, address, phone, email, website);
                    }
                }
                incrementCount();
            }
        } catch (JSONException e) {
            Log.d("mylog", "Error parsing contacts: " + e.toString());
        }
    }

    private void parseAndSaveTiles(String response) {
        try {
            JSONObject jsonTiles = new JSONObject(response);
            boolean error = jsonTiles.getBoolean("error");
            if (error) {
                Log.d("mylog", "Error getting tiles: " + response);
            } else {
                JSONArray jsonTileArray = jsonTiles.getJSONArray("tiles");
                for (int i = 0; i < jsonTileArray.length(); i++) {
                    JSONObject tempTile = jsonTileArray.getJSONObject(i);
                    int id = tempTile.getInt("tile_id");
                    String tileName = tempTile.getString("tile_title");
                    String tileDescription = tempTile.getString("tile_description");
                    String tileType = tempTile.getString("tile_type");
                    int tileOrder = tempTile.getInt("tile_order");
                    Log.d("mylog", tileName);
                    dbHelper.insertTile(id, tileName, tileDescription, tileType, tileOrder);
                }
                incrementCount();
            }
        } catch (JSONException e) {
            Log.d("mylog", "Error parsing tiles: " + e.toString());
        }
    }

    private void parseAndSaveQuestions(String response) {
        try {
            JSONObject jsonQuestions = new JSONObject(response);
            boolean error = jsonQuestions.getBoolean("error");
            if (error) {
                Log.d("mylog", "Error getting questions: " + response);
            } else {
                JSONArray jsonTileArray = jsonQuestions.getJSONArray("questions");
                for (int i = 0; i < jsonTileArray.length(); i++) {
                    JSONObject tempQuestion = jsonTileArray.getJSONObject(i);
                    int id = tempQuestion.getInt("question_id");
                    int tileId = tempQuestion.getInt("tile_id");
                    String step = tempQuestion.getString("question_step");
                    String title = tempQuestion.getString("question_title");
                    String description = tempQuestion.getString("question_description");
                    String confDesc = tempQuestion.getString("conflict_description");
                    Log.d("mylog", "Conflict Desc form server: " + confDesc);
                    String condition = tempQuestion.getString("condition");
                    String variable = tempQuestion.getString("variable");
                    String order = tempQuestion.getString("order");
                    String responseType = tempQuestion.getString("response_type");
                    String questionCall = tempQuestion.getString("question_call");
                    Log.d("mylog", "Got Number: " + questionCall);
                    String questionVideo = tempQuestion.getString("question_video");
                    Log.d("mylog", "Got Link: " + questionVideo);
                    dbHelper.insertQuestion(id, tileId, order, step, title, description, condition, responseType, variable, confDesc, questionCall, questionVideo);
                }
                incrementCount();
            }
        } catch (JSONException e) {
            Log.d("mylog", "Error parsing questions: " + e.toString());
        }
    }

    private void parseAndSaveOptions(String response) {
        try {
            JSONObject jsonOptions = new JSONObject(response);
            boolean error = jsonOptions.getBoolean("error");
            if (error) {
                Log.d("mylog", "Error getting options: " + response);
            } else {
                JSONArray jsonOptionsArray = jsonOptions.getJSONArray("options");
                for (int i = 0; i < jsonOptionsArray.length(); i++) {
                    JSONObject tempOption = jsonOptionsArray.getJSONObject(i);
                    int qid = tempOption.getInt("question_id");
                    int oid = tempOption.getInt("option_id");
                    String option = tempOption.getString("option_text");
                    Log.d("mylog", "Saving options for qid: " + qid + " option: " + option);
                    dbHelper.insertOption(qid, oid, option);
                }
                incrementCount();
            }
        } catch (JSONException e) {
            Log.d("mylog", "Error parsing options: " + e.toString());
        }
    }

    private void parseAndSaveCountries(String response) {
        try {
            JSONObject jsonCountries = new JSONObject(response);
            boolean error = jsonCountries.getBoolean("error");
            if (error) {
                Log.d("mylog", "Error getting countries: " + response);
            } else {
                JSONArray jsonTileArray = jsonCountries.getJSONArray("countries");
                for (int i = 0; i < jsonTileArray.length(); i++) {
                    JSONObject tempCountry = jsonTileArray.getJSONObject(i);
                    String id = tempCountry.getString("country_id");
                    String name = tempCountry.getString("country_name");
                    int status = tempCountry.getInt("country_status");
                    int blacklist = tempCountry.getInt("country_blacklist");
                    String order = null;
                    if (!(tempCountry.get("country_order") == null))
                        order = tempCountry.getString("country_order");
                    Log.d("mylog", "Country id: " + tempCountry.getString("country_id") + " Country name: " + tempCountry.getString("country_name")
                            + " Status: " + tempCountry.getInt("country_status") + " Blacklist: " + tempCountry.getInt("country_blacklist"));
                    dbHelper.insertCountry(id, name, status, blacklist, order);
                }
                incrementCount();
            }
        } catch (JSONException e) {
            Log.d("mylog", "Error parsing countries: " + e.toString());
        }
    }

    private void parseAndSaveManpowers(String response) {
        try {
            JSONObject jsonMan = new JSONObject(response);
            boolean error = jsonMan.getBoolean("error");
            if (error) {
                Log.d("mylog", "Error getting manpowers: " + response);
            } else {
                JSONArray jsonManpowerArray = jsonMan.getJSONArray("manpowers");
                for (int i = 0; i < jsonManpowerArray.length(); i++) {
                    JSONObject tempManpower = jsonManpowerArray.getJSONObject(i);
                    int id = tempManpower.getInt("id");
                    String name = tempManpower.getString("manpower");
                    dbHelper.insertManpower(id, name);
                }
            }
        } catch (JSONException ex) {
            Log.d("mylog", "Error parsing manpower: " + response);
        }
        incrementCount();
    }

    public synchronized void incrementCount() {
        ++savedCount;
        Log.d("mylog", "Incremented Count: " + savedCount);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("savedcount", savedCount);
        editor.commit();
        if (savedCount == apiCount) {
            //checkSleep();
            startInfoActivity();
        }
    }

    private void startInfoActivity() {
        Intent intent = new Intent(ActivitySplash.this, ActivityInfoSlider.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void hideLangOptions() {
        showLoading();
        llLang.setVisibility(View.GONE);
    }
}
