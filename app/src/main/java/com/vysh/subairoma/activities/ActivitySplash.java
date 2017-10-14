package com.vysh.subairoma.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.share.Share;
import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.SharedPrefKeys;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    SharedPreferences sp;

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
                    if (sleepTime > 0)
                        Thread.sleep(sleepTime);
                } catch (Exception ex) {
                    Log.d("mylog", "Sleeping exception: " + ex.toString());
                } finally {
                    Intent intent = new Intent(ActivitySplash.this, ActivityRegister.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });
        sp = getSharedPreferences(SharedPrefKeys.sharedPrefName, MODE_PRIVATE);
        getAllData();
    }

    private void getAllData() {
        if (sp.getInt(SharedPrefKeys.savedTableCount, 0) != 4) {
            Log.d("mylog", "Starting save");
            dbHelper = new SQLDatabaseHelper(ActivitySplash.this);
            dbHelper.getWritableDatabase();
            if (!sp.getBoolean(SharedPrefKeys.savedTiles, false)) {
                Log.d("mylog", "Getting tiles");
                getTiles();
            } else incrementCount();
            if (!sp.getBoolean(SharedPrefKeys.savedQuestions, false)) {
                Log.d("mylog", "Getting questions");
                getQuestions();
            } else incrementCount();
            if (!sp.getBoolean(SharedPrefKeys.savedOptions, false)) {
                Log.d("mylog", "Getting options");
                getOptions();
            } else incrementCount();
            if (!sp.getBoolean(SharedPrefKeys.savedCountries, false)) {
                Log.d("mylog", "Getting countries");
                getCountries();
            } else incrementCount();
        } else {
            Log.d("mylog", "Saved already, starting");
            //Start activity directly or show splash
            long currTime = System.currentTimeMillis();
            sleepTime = 2000;
            sleepThread.start();
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
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(SharedPrefKeys.savedTiles, true);
                editor.commit();
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
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(SharedPrefKeys.savedQuestions, true);
                editor.commit();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("mylog", "Error getting questions: " + error.toString());
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
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(SharedPrefKeys.savedOptions, true);
                editor.commit();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("mylog", "Error getting options: " + error.toString());
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
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean(SharedPrefKeys.savedCountries, true);
                editor.commit();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("mylog", "Error getting countries: " + error.toString());
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
                    String condition = tempQuestion.getString("condition");
                    String variable = tempQuestion.getString("variable");
                    String order = tempQuestion.getString("order");
                    String responseType = tempQuestion.getString("response_type");
                    dbHelper.insertQuestion(id, tileId, order, step, title, description, condition, responseType, variable);
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
                    Log.d("mylog", "Country id: " + tempCountry.getString("country_id") + " Country name: " + tempCountry.getString("country_name")
                            + " Status: " + tempCountry.getInt("country_status") + " Blacklist: " + tempCountry.getInt("country_blacklist"));
                    dbHelper.insertCountry(id, name, status, blacklist);
                }
            }
            incrementCount();
        } catch (JSONException e) {
            Log.d("mylog", "Error parsing countries: " + e.toString());
        }
    }

    public synchronized void incrementCount() {
        savedCount++;
        Log.d("mylog", "Incremented Count: " + savedCount);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("savedcount", savedCount);
        editor.commit();
        if (savedCount == 4) {
            checkSleep();
        }
    }
}
