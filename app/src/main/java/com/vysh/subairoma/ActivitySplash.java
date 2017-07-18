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
    private final String countiesAPI = "/getallcountries.php";

    SQLDatabaseHelper dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Thread splashTime = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (Exception ex) {
                    Log.d("mylog", "Sleeping exception");
                } finally {
                    Intent intent = new Intent(ActivitySplash.this, ActivityRegister.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });
        // If already has data show splash
        // else
        getAllData();
    }

    private void getAllData() {
        SharedPreferences sp = getSharedPreferences("subairomapreferences", MODE_PRIVATE);
        if (!sp.getBoolean("savedall", false)) {
            dbHelper = new SQLDatabaseHelper(ActivitySplash.this);
            dbHelper.getWritableDatabase();
            if(!sp.getBoolean("savedtiles", false))
            getTiles();
            if(!sp.getBoolean("savedquestions", false))
            getQuestions();
            if(!sp.getBoolean("savedoptions", false))
            getOptions();
            if(!sp.getBoolean("savedcountries", false))
            getCountries();
        } else {
            //Start activity directly or show splash
        }
    }

    private void getTiles() {
        String api = ApplicationClass.getInstance().getAPIROOT() + tilesAPI;
        StringRequest getRequest = new StringRequest(Request.Method.GET, api, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //SAVE RESPONSE IN LOCAL DB
                parseAndSaveTiles(response);
                SharedPreferences sp = getSharedPreferences("subairomapreferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("savedtiles", true);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

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
                parseAndSaveQuestions(response);
                SharedPreferences sp = getSharedPreferences("subairomapreferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("savedquestions", true);
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
                parseAndSaveOptions(response);
                SharedPreferences sp = getSharedPreferences("subairomapreferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("savedoptions", true);
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
                SharedPreferences sp = getSharedPreferences("subairomapreferences", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("savedcountries", true);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        RequestQueue queue = Volley.newRequestQueue(ActivitySplash.this);
        queue.add(getRequest);
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
}
