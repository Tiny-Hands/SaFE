package com.vysh.subairoma;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.models.MigrantModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Vishal on 6/12/2017.
 */

public class ActivitySplash extends AppCompatActivity {
    private final String tilesAPI = "/getalltiles.php";
    private final String questionAPI = "/getallquestions.php";
    private final String optionsAPI = "/getalloptions.php";
    private final String countiesAPI = "/getcountries.php";
    private int savedCount = 0;
    private long startTime;
    private long sleepTime;

    SQLDatabaseHelper dbHelper;
    Thread sleepThread;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        startTime = System.currentTimeMillis();
        sleepThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (sleepTime != 0)
                        Thread.sleep(sleepTime);
                } catch (Exception ex) {
                    Log.d("mylog", "Sleeping exception");
                } finally {
                    Intent intent = new Intent(ActivitySplash.this, ActivityRegister.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });
        getAllData();
    }

    private void getAllData() {
        SharedPreferences sp = getSharedPreferences("subairomapreferences", MODE_PRIVATE);
        if (sp.getInt("savedcount", 0) != 4) {
            Log.d("mylog", "Starting save");
            dbHelper = new SQLDatabaseHelper(ActivitySplash.this);
            dbHelper.getWritableDatabase();
            if (!sp.getBoolean("savedtiles", false)) {
                Log.d("mylog", "Getting tiles");
                getTiles();
            } else incrementCount();
            if (!sp.getBoolean("savedquestions", false)) {
                Log.d("mylog", "Getting questions");
                getQuestions();
            } else incrementCount();
            if (!sp.getBoolean("savedoptions", false)) {
                Log.d("mylog", "Getting options");
                getOptions();
            } else incrementCount();
            if (!sp.getBoolean("savedcountries", false)) {
                Log.d("mylog", "Getting countries");
                getCountries();
            } else incrementCount();
        } else {
            Log.d("mylog", "Saved already, starting");
            //Start activity directly or show splash
            long currTime = System.currentTimeMillis();
            sleepTime = currTime - startTime;
            if (sleepTime > 2000) {
                sleepTime = 0;
                sleepThread.start();
            } else {
                sleepTime = 2000 - startTime;
                sleepThread.start();
            }
        }
    }

    private void getTiles() {
        String api = ApplicationClass.getInstance().getAPIROOT() + tilesAPI;
        StringRequest getRequest = new StringRequest(Request.Method.GET, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //SAVE RESPONSE IN LOCAL DB
                Log.d("mylog", "Got Tiles: " + response);
                parseAndSaveTiles(response);
                SharedPreferences sp = getSharedPreferences("subairomapreferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("savedtiles", true);
                editor.commit();
                incrementCount();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("mylog", "Got Tiles error: " + error.toString());
            }
        });
        RequestQueue queue = Volley.newRequestQueue(ActivitySplash.this);
        queue.add(getRequest);
    }

    private void getQuestions() {
        String api = ApplicationClass.getInstance().getAPIROOT() + questionAPI;
        StringRequest getRequest = new StringRequest(Request.Method.GET, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //SAVE RESPONSE IN LOCAL DB
                Log.d("mylog", "Got questions: " + response);
                parseAndSaveQuestions(response);
                SharedPreferences sp = getSharedPreferences("subairomapreferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("savedquestions", true);
                editor.commit();
                incrementCount();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        RequestQueue queue = Volley.newRequestQueue(ActivitySplash.this);
        queue.add(getRequest);
    }

    private void getOptions() {
        String api = ApplicationClass.getInstance().getAPIROOT() + optionsAPI;
        StringRequest getRequest = new StringRequest(Request.Method.GET, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //SAVE RESPONSE IN LOCAL DB
                Log.d("mylog", "Got options: " + response);
                parseAndSaveOptions(response);
                SharedPreferences sp = getSharedPreferences("subairomapreferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("savedoptions", true);
                editor.commit();
                incrementCount();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        RequestQueue queue = Volley.newRequestQueue(ActivitySplash.this);
        queue.add(getRequest);
    }

    private void getCountries() {
        String api = ApplicationClass.getInstance().getAPIROOT() + countiesAPI;
        StringRequest getRequest = new StringRequest(Request.Method.GET, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //SAVE RESPONSE IN LOCAL DB
                parseAndSaveCountries(response);
                Log.d("mylog", "Got countries: " + response);
                SharedPreferences sp = getSharedPreferences("subairomapreferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("savedcountries", true);
                editor.commit();
                incrementCount();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        RequestQueue queue = Volley.newRequestQueue(ActivitySplash.this);
        queue.add(getRequest);
    }

    private synchronized void checkSleep() {
        if (savedCount == 4) {
            long currTime = System.currentTimeMillis();
            sleepTime = currTime - startTime;
            Log.d("mylog", "Count is 4, Sleep Time: " + sleepTime);
            if (sleepTime > 2000) {
                sleepTime = 0;
                sleepThread.start();
            } else {
                sleepTime = 2000 - sleepTime;
                sleepThread.start();
            }
        }
    }

    private void parseAndSaveTiles(String response) {
        try {
            JSONObject jsonTiles = new JSONObject(response);
            boolean error = jsonTiles.getBoolean("error");
            if (error) {

            } else {
                JSONArray jsonTileArray = jsonTiles.getJSONArray("tiles");
                for (int i = 0; i < jsonTileArray.length(); i++) {
                    JSONObject tempTile = jsonTileArray.getJSONObject(i);
                    int id = tempTile.getInt("id");
                    String tileName = tempTile.getString("title");
                    String tileDescription = tempTile.getString("description");
                    String tileType = tempTile.getString("type");
                    int tileOrder = tempTile.getInt("order");
                    dbHelper.insertTile(id, tileName, tileDescription, tileType, tileOrder);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseAndSaveQuestions(String response) {
        try {
            JSONObject jsonQuestions = new JSONObject(response);
            boolean error = jsonQuestions.getBoolean("error");
            if (error) {

            } else {
                JSONArray jsonTileArray = jsonQuestions.getJSONArray("questions");
                for (int i = 0; i < jsonTileArray.length(); i++) {
                    JSONObject tempQuestion = jsonTileArray.getJSONObject(i);
                    int id = tempQuestion.getInt("id");
                    int tileId = tempQuestion.getInt("tile_id");
                    String step = tempQuestion.getString("title");
                    String title = tempQuestion.getString("description");
                    String description = tempQuestion.getString("type");
                    String condition = tempQuestion.getString("condition");
                    String variable = tempQuestion.getString("variable");
                    int order = tempQuestion.getInt("order");
                    int responseType = tempQuestion.getInt("response_type");
                    dbHelper.insertQuestion(id, tileId, order, step, title, description, condition, responseType, variable);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseAndSaveOptions(String response) {
        try {
            JSONObject jsonOptions = new JSONObject(response);
            boolean error = jsonOptions.getBoolean("error");
            if (error) {

            } else {
                JSONArray jsonOptionsArray = jsonOptions.getJSONArray("options");
                for (int i = 0; i < jsonOptionsArray.length(); i++) {
                    JSONObject tempOption = jsonOptionsArray.getJSONObject(i);
                    int qid = tempOption.getInt("question_id");
                    int oid = tempOption.getInt("id");
                    String option = tempOption.getString("option");
                    dbHelper.insertOption(qid, oid, option);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void parseAndSaveCountries(String response) {
        try {
            JSONObject jsonCountries = new JSONObject(response);
            boolean error = jsonCountries.getBoolean("error");
            if (error) {

            } else {
                JSONArray jsonTileArray = jsonCountries.getJSONArray("countries");
                for (int i = 0; i < jsonTileArray.length(); i++) {
                    JSONObject tempCountry = jsonTileArray.getJSONObject(i);
                    String id = tempCountry.getString("id");
                    String name = tempCountry.getString("name");
                    int status = tempCountry.getInt("status");
                    int blacklist = tempCountry.getInt("blacklist");
                    dbHelper.insertCountry(id, name, status, blacklist);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public synchronized void incrementCount() {
        savedCount++;
        Log.d("mylog", "Incremented Count: " + savedCount);
        SharedPreferences sp = getSharedPreferences("subairomapreferences", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("savedcount", savedCount);
        editor.commit();
        if (savedCount == 4) {
            checkSleep();
        }
    }
}
