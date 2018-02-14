package com.vysh.subairoma.SQLHelpers;

import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SharedPrefKeys;
import com.vysh.subairoma.activities.ActivitySplash;
import com.vysh.subairoma.models.CountryModel;
import com.vysh.subairoma.models.FeedbackQuestionModel;
import com.vysh.subairoma.models.MigrantModel;
import com.vysh.subairoma.models.TileQuestionsModel;
import com.vysh.subairoma.models.TilesModel;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by Vishal on 6/7/2017.
 */

public class SQLDatabaseHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    Context mContext;
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "SubairomaLocal.db";
    final String SQL_CREATE_ResponseTable =
            "CREATE TABLE IF NOT EXISTS " + DatabaseTables.ResponseTable.TABLE_NAME + " (" +
                    DatabaseTables.ResponseTable.migrant_id + " INTEGER," +
                    DatabaseTables.ResponseTable.question_id + " INTEGER," +
                    DatabaseTables.ResponseTable.is_error + " TEXT," +
                    DatabaseTables.ResponseTable.response_variable + " TEXT," +
                    DatabaseTables.ResponseTable.response + " TEXT," +
                    DatabaseTables.ResponseTable.question_query + " TEXT," +
                    DatabaseTables.ResponseTable.tile_id + " INTEGER," +
                    " UNIQUE (" + DatabaseTables.ResponseTable.question_id +
                    ", " + DatabaseTables.ResponseTable.migrant_id + "));";
    final String SQL_CREATE_TilesTable =
            "CREATE TABLE IF NOT EXISTS " + DatabaseTables.TilesTable.TABLE_NAME + " (" +
                    DatabaseTables.TilesTable.tile_id + " INTEGER PRIMARY KEY," +
                    DatabaseTables.TilesTable.tile_order + " INTEGER," +
                    DatabaseTables.TilesTable.tile_description + " TEXT," +
                    DatabaseTables.TilesTable.tile_type + " TEXT," +
                    DatabaseTables.TilesTable.tile_title + " TEXT" + ");";
    final String SQL_CREATE_QuestionsTable =
            "CREATE TABLE IF NOT EXISTS " + DatabaseTables.QuestionsTable.TABLE_NAME + " (" +
                    DatabaseTables.QuestionsTable.question_id + " INTEGER PRIMARY KEY," +
                    DatabaseTables.QuestionsTable.tile_id + " INTEGER," +
                    DatabaseTables.QuestionsTable.question_step + " TEXT," +
                    DatabaseTables.QuestionsTable.question_description + " TEXT," +
                    DatabaseTables.QuestionsTable.conflict_description + " TEXT," +
                    DatabaseTables.QuestionsTable.question_title + " TEXT," +
                    DatabaseTables.QuestionsTable.question_condition + " TEXT," +
                    DatabaseTables.QuestionsTable.question_variable + " TEXT," +
                    DatabaseTables.QuestionsTable.question_order + " TEXT," +
                    DatabaseTables.QuestionsTable.response_type + " TEXT" + ");";
    final String SQL_CREATE_OptionsTable =
            "CREATE TABLE IF NOT EXISTS " + DatabaseTables.OptionsTable.TABLE_NAME + " (" +
                    DatabaseTables.OptionsTable.option_id + " INTEGER PRIMARY KEY," +
                    DatabaseTables.OptionsTable.question_id + " INTEGER," +
                    DatabaseTables.OptionsTable.option_text + " TEXT" + ");";
    final String SQL_CREATE_CountriesTable =
            "CREATE TABLE IF NOT EXISTS " + DatabaseTables.CountriesTable.TABLE_NAME + " (" +
                    DatabaseTables.CountriesTable.country_id + " TEXT PRIMARY KEY," +
                    DatabaseTables.CountriesTable.country_blacklist + " INTEGER," +
                    DatabaseTables.CountriesTable.country_status + " INTEGER," +
                    DatabaseTables.CountriesTable.country_order + " INTEGER," +
                    DatabaseTables.CountriesTable.country_name + " TEXT" + ");";
    final String SQL_CREATE_ContactsTable =
            "CREATE TABLE IF NOT EXISTS " + DatabaseTables.ImportantContacts.TABLE_NAME + " (" +
                    DatabaseTables.ImportantContacts.country_id + " TEXT PRIMARY KEY," +
                    DatabaseTables.ImportantContacts.title + " TEXT," +
                    DatabaseTables.ImportantContacts.description + " TEXT," +
                    DatabaseTables.ImportantContacts.address + " TEXT," +
                    DatabaseTables.ImportantContacts.phone + " TEXT," +
                    DatabaseTables.ImportantContacts.email + " TEXT," +
                    DatabaseTables.ImportantContacts.website + " TEXT" + ");";
    final String SQL_CREATE_MigrantsTable =
            "CREATE TABLE IF NOT EXISTS " + DatabaseTables.MigrantsTable.TABLE_NAME + " (" +
                    DatabaseTables.MigrantsTable.migrant_id + " INTEGER PRIMARY KEY," +
                    DatabaseTables.MigrantsTable.name + " TEXT," +
                    DatabaseTables.MigrantsTable.age + " INTEGER," +
                    DatabaseTables.MigrantsTable.user_id + " INTEGER," +
                    DatabaseTables.MigrantsTable.sex + " TEXT," +
                    DatabaseTables.MigrantsTable.phone_number + " TEXT," +
                    " UNIQUE (" + DatabaseTables.MigrantsTable.migrant_id +
                    ", " + DatabaseTables.MigrantsTable.user_id + "));";
    final String SQL_CREATE_FeedbackQuestionTable =
            "CREATE TABLE IF NOT EXISTS " + DatabaseTables.FeedbackQuestionsTable.TABLE_NAME + " (" +
                    DatabaseTables.FeedbackQuestionsTable.question_id + " INTEGER PRIMARY KEY," +
                    DatabaseTables.FeedbackQuestionsTable.question_title + " TEXT," +
                    DatabaseTables.FeedbackQuestionsTable.question_option + " TEXT" + ");";
    final String SQL_CREATE_FeedbackQuestionResponseTable =
            "CREATE TABLE IF NOT EXISTS " + DatabaseTables.FeedbackQuestionsResponseTable.TABLE_NAME + " (" +
                    DatabaseTables.FeedbackQuestionsResponseTable.question_id + " INTEGER," +
                    DatabaseTables.FeedbackQuestionsResponseTable.migrant_id + " INTEGER," +
                    DatabaseTables.FeedbackQuestionsResponseTable.response + " TEXT," +
                    DatabaseTables.FeedbackQuestionsResponseTable.option_response + " TEXT," +
                    DatabaseTables.FeedbackQuestionsResponseTable.response_feedback + " TEXT," +
                    " UNIQUE (" + DatabaseTables.FeedbackQuestionsResponseTable.question_id + ", " +
                    DatabaseTables.FeedbackQuestionsResponseTable.migrant_id + "));";

    public SQLDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        SharedPreferences sp = context.getSharedPreferences(SharedPrefKeys.sharedPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(SharedPrefKeys.dbVersion, DATABASE_VERSION);
        editor.commit();
        mContext = context;
        Log.d("mylog", "Database created");
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ResponseTable);
        db.execSQL(SQL_CREATE_TilesTable);
        db.execSQL(SQL_CREATE_QuestionsTable);
        db.execSQL(SQL_CREATE_OptionsTable);
        db.execSQL(SQL_CREATE_CountriesTable);
        db.execSQL(SQL_CREATE_MigrantsTable);
        db.execSQL(SQL_CREATE_ContactsTable);
        db.execSQL(SQL_CREATE_FeedbackQuestionTable);
        db.execSQL(SQL_CREATE_FeedbackQuestionResponseTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("mylog", "Updrading");
    }

    public int getVersion() {
        return DATABASE_VERSION;
    }

    public void dropDB() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.FeedbackQuestionsResponseTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.FeedbackQuestionsTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.ResponseTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.QuestionsTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.OptionsTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.CountriesTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.MigrantsTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.TilesTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DatabaseTables.ImportantContacts.TABLE_NAME);
        mContext.deleteDatabase(DATABASE_NAME);
        allowReloadData();
        Toast.makeText(mContext, "Reloading Data, Please Wait", Toast.LENGTH_LONG).show();
        //restartApp();
    }

    private void restartApp() {
        Intent intent = new Intent(mContext, ActivitySplash.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //mContext.startActivity(intent);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                ApplicationClass.getInstance().getBaseContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);

        Log.d("mylog", "Reloading in 1 second");
        //Restart your app after 1 seconds
        AlarmManager mgr = (AlarmManager) ApplicationClass.getInstance().getBaseContext()
                .getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100,
                pendingIntent);
    }

    public void allowReloadData() {
        SharedPreferences sp = mContext.getSharedPreferences(SharedPrefKeys.sharedPrefName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("savedcount", 0);
        editor.commit();
    }

    public void insertResponseTableData(String response, int question_id, int tileId, int migrant_id, String variable) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseTables.ResponseTable.response, response);
        values.put(DatabaseTables.ResponseTable.response_variable, variable);
        values.put(DatabaseTables.ResponseTable.tile_id, tileId);

        //If already exist the Update
        String whereClause = DatabaseTables.ResponseTable.migrant_id + " = " + migrant_id + " AND " +
                DatabaseTables.ResponseTable.question_id + " = " + question_id;
        long updateCount = db.update(DatabaseTables.ResponseTable.TABLE_NAME, values, whereClause, null);
        Log.d("mylog", "Updated row count: " + updateCount);
        //If not update then insert
        if (updateCount < 1) {
            // Insert the new row, returning the primary key value of the new row
            values.put(DatabaseTables.ResponseTable.migrant_id, migrant_id);
            values.put(DatabaseTables.ResponseTable.question_id, question_id);
            values.put(DatabaseTables.ResponseTable.tile_id, tileId);
            long newRowId = db.insert(DatabaseTables.ResponseTable.TABLE_NAME, null, values);
            Log.d("mylog", "Inserted row ID: " + newRowId);
        }
    }

    public void insertQuestionQuery(int qid, int tileId, int migrantId, String query) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseTables.ResponseTable.question_query, query);

        //If already exist the Update
        String whereClause = DatabaseTables.ResponseTable.migrant_id + " = " + migrantId + " AND " +
                DatabaseTables.ResponseTable.question_id + " = " + qid;
        long updateCount = db.update(DatabaseTables.ResponseTable.TABLE_NAME, values, whereClause, null);
        Log.d("mylog", "Updated query row count: " + updateCount);
        //If not update then insert
        if (updateCount < 1) {
            values.put(DatabaseTables.ResponseTable.question_id, qid);
            values.put(DatabaseTables.ResponseTable.migrant_id, migrantId);
            values.put(DatabaseTables.ResponseTable.tile_id, tileId);
            // Insert the new row, returning the primary key value of the new row
            long newRowId = db.insert(DatabaseTables.ResponseTable.TABLE_NAME, null, values);
            Log.d("mylog", "Inserted query row ID: " + newRowId);
        }
    }

    //This is used when we have isError, when response is fetched from server
    public void insertAllResponses(String response, int question_id, int migrant_id, String variable, String isError, int tileId) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseTables.ResponseTable.response, response);
        values.put(DatabaseTables.ResponseTable.migrant_id, migrant_id);
        values.put(DatabaseTables.ResponseTable.response_variable, variable);
        values.put(DatabaseTables.ResponseTable.question_id, question_id);
        values.put(DatabaseTables.ResponseTable.is_error, isError);
        values.put(DatabaseTables.ResponseTable.tile_id, tileId);
        long newRowId = db.insert(DatabaseTables.ResponseTable.TABLE_NAME, null, values);
        Log.d("mylog", "Inserted row ID: " + newRowId);
    }

    public void insertIsError(int migId, String variable, String isError) {
        SQLiteDatabase db = this.getWritableDatabase();
        Log.d("mylog", "Inserting isError: " + isError);
        ContentValues newValue = new ContentValues();
        if (isError.equalsIgnoreCase("false"))
            newValue.put(DatabaseTables.ResponseTable.is_error, "false");
        else
            newValue.put(DatabaseTables.ResponseTable.is_error, "true");
        String selection = DatabaseTables.ResponseTable.migrant_id + " = " + "'" + migId + "'" + " AND " +
                DatabaseTables.ResponseTable.response_variable + " = " + "'" + variable + "'";
        int updateCount = db.update(DatabaseTables.ResponseTable.TABLE_NAME, newValue, selection, null);
        Log.d("mylog", "Updated iserror rows: " + updateCount);
    }

    public ArrayList<HashMap> getAllResponse(int migId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query;
        ArrayList<HashMap> allResponses = new ArrayList<>();
        query = "SELECT * FROM " + DatabaseTables.ResponseTable.TABLE_NAME + " WHERE " +
                DatabaseTables.ResponseTable.migrant_id + "=" + "'" + migId + "'";
        //Log.d("mylog", "Query: " + query);
        Cursor cursor = db.rawQuery(query, null);
        String response = "", responseQuery = "";
        Log.d("mylog", "Response for Migrant ID: " + migId);
        int userId = ApplicationClass.getInstance().getUserId();
        while (cursor.moveToNext()) {
            String qvar = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.response_variable));
            response = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.response));
            responseQuery = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.question_query));
            int mid = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.migrant_id));
            int qid = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.question_id));
            int tileid = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.tile_id));
            String error = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.is_error));
            Log.d("mylog", "Uid: " + userId + " Mid: " + mid + " Qid: " + qid + " Response: " + response + " Variable: " + qvar + " isError: " + error);
            HashMap<String, String> params = new HashMap<>();
            params.put("user_id", "" + userId);
            params.put("migrant_id", "" + mid);
            params.put("question_id", "" + qid);
            params.put("response", response);
            params.put("response_variable", qvar);
            params.put("tile_id", tileid + "");
            if (error == null)
                error = "false";
            params.put("is_error", error);
            if (responseQuery == null)
                responseQuery = "";
            params.put("question_query", responseQuery);
            allResponses.add(params);
        }
        return allResponses;
    }

    public String getResponse(int migId, String variable) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query;
        if (variable == null) {
            query = "SELECT * FROM " + DatabaseTables.ResponseTable.TABLE_NAME
                    + " WHERE " + DatabaseTables.ResponseTable.migrant_id + " = " + "'" + migId + "'";
        } else {
            query = "SELECT * FROM " + DatabaseTables.ResponseTable.TABLE_NAME +
                    " WHERE " + DatabaseTables.ResponseTable.migrant_id + " = " + "'" + migId + "'"
                    + " AND " + DatabaseTables.ResponseTable.response_variable + " = " + "'" + variable + "'";
        }
        //Log.d("mylog", "Query: " + query);
        Cursor cursor = db.rawQuery(query, null);
        String response = "";
        while (cursor.moveToNext()) {
            String qvar = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.response_variable));
            response = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.response));
            int mid = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.migrant_id));
            int qid = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.question_id));
            //Log.d("mylog", "Mid: " + mid + " Qid: " + qid + " rvar: " + response);
        }
        return response;
    }

    public boolean getIsError(int migId, String variable) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query;
        if (variable == null) {
            query = "SELECT * FROM " + DatabaseTables.ResponseTable.TABLE_NAME
                    + " WHERE " + DatabaseTables.ResponseTable.migrant_id + " = " + "'" + migId + "'";
        } else {
            query = "SELECT * FROM " + DatabaseTables.ResponseTable.TABLE_NAME +
                    " WHERE " + DatabaseTables.ResponseTable.migrant_id + " = " + "'" + migId + "'"
                    + " AND " + DatabaseTables.ResponseTable.response_variable + " = " + "'" + variable + "'";
        }
        //Log.d("mylog", "Query: " + query);
        Cursor cursor = db.rawQuery(query, null);
        boolean isError = false;
        while (cursor.moveToNext()) {
            isError = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.is_error)));
            //Log.d("mylog", "Mid: " + mid + " Qid: " + qid + " rvar: " + response);
        }
        return isError;
    }

    public void insertCountry(String id, String name, int status, int blacklist, String order) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseTables.CountriesTable.country_id, id);
        values.put(DatabaseTables.CountriesTable.country_name, name);
        values.put(DatabaseTables.CountriesTable.country_status, status);
        values.put(DatabaseTables.CountriesTable.country_blacklist, blacklist);
        values.put(DatabaseTables.CountriesTable.country_order, order);

        long newRowId = db.insert(DatabaseTables.CountriesTable.TABLE_NAME, null, values);
        Log.d("mylog", "Inserted row ID; " + newRowId);
    }

    public ArrayList<CountryModel> getCountries() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<CountryModel> countryList = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseTables.CountriesTable.TABLE_NAME + " ORDER BY " + DatabaseTables.CountriesTable.country_order, null);
        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.CountriesTable.country_id));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.CountriesTable.country_name));
            int blacklist = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.CountriesTable.country_blacklist));
            int status = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.CountriesTable.country_status));
            int order = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.CountriesTable.country_order));
            CountryModel countryModel = new CountryModel();
            countryModel.setCountryId(id);
            countryModel.setCountryName(name);
            countryModel.setCountrySatus(status);
            countryModel.setCountryBlacklist(blacklist);
            countryModel.setOrder(order);
            countryList.add(countryModel);
        }
        return countryList;
    }

    public CountryModel getCountry(String cid) {
        String statement = "SELECT * FROM " + DatabaseTables.CountriesTable.TABLE_NAME + " WHERE "
                + DatabaseTables.CountriesTable.country_id + "=" + "'" + cid + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(statement, null);
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.CountriesTable.country_name));
            int status = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.CountriesTable.country_status));
            int blacklist = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.CountriesTable.country_blacklist));
            CountryModel countryModel = new CountryModel();
            countryModel.setCountryName(name);
            countryModel.setCountryBlacklist(blacklist);
            countryModel.setCountrySatus(status);
            return countryModel;
        }
        return null;
    }

    public void insertTile(int id, String title, String description, String type, int order) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseTables.TilesTable.tile_id, id);
        values.put(DatabaseTables.TilesTable.tile_order, order);
        values.put(DatabaseTables.TilesTable.tile_title, title);
        values.put(DatabaseTables.TilesTable.tile_description, description);
        values.put(DatabaseTables.TilesTable.tile_type, type);

        long newRowId = db.insert(DatabaseTables.TilesTable.TABLE_NAME, null, values);
        Log.d("mylog", "Inserted row ID; " + newRowId);
    }

    public void insertImportantContacts(String cid, String title, String description, String address, String phone, String email, String website) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseTables.ImportantContacts.country_id, cid);
        values.put(DatabaseTables.ImportantContacts.title, title);
        values.put(DatabaseTables.ImportantContacts.description, description);
        values.put(DatabaseTables.ImportantContacts.address, address);
        values.put(DatabaseTables.ImportantContacts.phone, phone);
        values.put(DatabaseTables.ImportantContacts.email, email);
        values.put(DatabaseTables.ImportantContacts.website, website);

        long newRowId = db.insert(DatabaseTables.ImportantContacts.TABLE_NAME, null, values);
        Log.d("mylog", "Inserted row ID; " + newRowId);
    }

    public ArrayList<TilesModel> getTiles(String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<TilesModel> tileList = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseTables.TilesTable.TABLE_NAME + " WHERE "
                + DatabaseTables.TilesTable.tile_type + "=" + "'" + type + "'", null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.TilesTable.tile_id));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.TilesTable.tile_title));
            String desc = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.TilesTable.tile_description));
            int order = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.TilesTable.tile_order));
            TilesModel tilesModel = new TilesModel();
            tilesModel.setTitle(title);
            tilesModel.setDescription(desc);
            tilesModel.setTileId(id);
            tilesModel.setTileOrder(order);
            tilesModel.setType(type
            );
            tileList.add(tilesModel);
        }
        return tileList;
    }

    public void insertQuestion(int qid, int tid, String order, String step, String title
            , String description, String condition, String responseType, String variable, String conflictDesc) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseTables.QuestionsTable.question_id, qid);
        values.put(DatabaseTables.QuestionsTable.tile_id, tid);
        values.put(DatabaseTables.QuestionsTable.question_order, order);
        values.put(DatabaseTables.QuestionsTable.question_step, step);
        values.put(DatabaseTables.QuestionsTable.question_description, description);
        values.put(DatabaseTables.QuestionsTable.question_title, title);
        values.put(DatabaseTables.QuestionsTable.response_type, responseType);
        values.put(DatabaseTables.QuestionsTable.question_condition, condition);
        values.put(DatabaseTables.QuestionsTable.question_variable, variable);
        Log.d("mylog", "Inserting conflicting desc: " + conflictDesc);
        values.put(DatabaseTables.QuestionsTable.conflict_description, conflictDesc);

        long newRowId = db.insert(DatabaseTables.QuestionsTable.TABLE_NAME, null, values);
        Log.d("mylog", "Inserted row ID; " + newRowId);
    }

    public void insertFeedbackQuestions(int qid, String qTitle, String qOption) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseTables.FeedbackQuestionsTable.question_id, qid);
        values.put(DatabaseTables.FeedbackQuestionsTable.question_title, qTitle);
        values.put(DatabaseTables.FeedbackQuestionsTable.question_option, qOption);

        long newRowId = db.insert(DatabaseTables.FeedbackQuestionsTable.TABLE_NAME, null, values);
        Log.d("mylog", "Inserted Feedback row ID; " + newRowId);
    }

    public void insertFeedbackResponse(int migrantId, int questionId, String response, String optResponse, String responseFeedback) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseTables.FeedbackQuestionsResponseTable.response, response);
        values.put(DatabaseTables.FeedbackQuestionsResponseTable.response_feedback, responseFeedback);
        values.put(DatabaseTables.FeedbackQuestionsResponseTable.option_response, optResponse);
        //If already exist the Update
        String whereClause = DatabaseTables.FeedbackQuestionsResponseTable.migrant_id + " = " + migrantId + " AND " +
                DatabaseTables.FeedbackQuestionsResponseTable.question_id + " = " + questionId;
        long updateCount = db.update(DatabaseTables.FeedbackQuestionsResponseTable.TABLE_NAME, values, whereClause, null);
        Log.d("mylog", "Updated row count: " + updateCount);
        //If not update then insert
        if (updateCount < 1) {
            // Insert the new row, returning the primary key value of the new row
            values.put(DatabaseTables.FeedbackQuestionsResponseTable.migrant_id, migrantId);
            values.put(DatabaseTables.FeedbackQuestionsResponseTable.question_id, questionId);
            long newRowId = db.insert(DatabaseTables.FeedbackQuestionsResponseTable.TABLE_NAME, null, values);
            Log.d("mylog", "Inserted feedback row ID: " + newRowId);
        }

    }

    public ArrayList<HashMap> getAllFeedbackResponses(int migId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query;
        ArrayList<HashMap> allResponses = new ArrayList<>();
        query = "SELECT * FROM " + DatabaseTables.FeedbackQuestionsResponseTable.TABLE_NAME + " WHERE " +
                DatabaseTables.FeedbackQuestionsResponseTable.migrant_id + "=" + "'" + migId + "'";
        //Log.d("mylog", "Query: " + query);
        Cursor cursor = db.rawQuery(query, null);
        String response = "", resFeedback = "", optResponse;
        Log.d("mylog", "Response for Migrant ID: " + migId);
        while (cursor.moveToNext()) {
            response = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.FeedbackQuestionsResponseTable.response));
            optResponse = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.FeedbackQuestionsResponseTable.option_response));
            resFeedback = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.FeedbackQuestionsResponseTable.response_feedback));
            int mid = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.FeedbackQuestionsResponseTable.migrant_id));
            int qid = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.FeedbackQuestionsResponseTable.question_id));

            HashMap<String, String> params = new HashMap<>();
            params.put("migrant_id", "" + mid);
            params.put("question_id", "" + qid);
            params.put("response", response);
            params.put("opt_response", optResponse);
            params.put("feedback", resFeedback);
            allResponses.add(params);
        }
        return allResponses;
    }

    public ArrayList<FeedbackQuestionModel> getFeedbackQuestions() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<FeedbackQuestionModel> feedbackQuestions = new ArrayList<>();
        //SELECT * FROM `feedback_questions_table` ORDER BY question_group, question_type DESC
        String statement = "SELECT * FROM " + DatabaseTables.FeedbackQuestionsTable.TABLE_NAME;
        Cursor cursor = db.rawQuery(statement, null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(DatabaseTables.FeedbackQuestionsTable.question_id));
            String qTitle = cursor.getString(cursor.getColumnIndex(DatabaseTables.FeedbackQuestionsTable.question_title));
            String qOptions = cursor.getString(cursor.getColumnIndex(DatabaseTables.FeedbackQuestionsTable.question_option));
            FeedbackQuestionModel tempModel = new FeedbackQuestionModel();
            tempModel.setQuestionId(id);
            tempModel.setQuestionTitle(qTitle);
            tempModel.setQuestionOptions(qOptions);
            feedbackQuestions.add(tempModel);
        }
        return feedbackQuestions;
    }

    public ArrayList<TileQuestionsModel> getQuestions(int tileId) {
        //Log.d("mylog", "Geting questions for tileId: " + tileId);
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<TileQuestionsModel> questionList = new ArrayList<>();
        String statement = "SELECT * FROM " + DatabaseTables.QuestionsTable.TABLE_NAME + " WHERE "
                + DatabaseTables.QuestionsTable.tile_id + "=" + "'" + tileId + "'" + " ORDER BY " + DatabaseTables.QuestionsTable.question_order;
        //Log.d("mylog", "Query: " + statement);
        Cursor cursor = db.rawQuery(statement, null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.QuestionsTable.question_id));
            String question = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.QuestionsTable.question_title));
            String desc = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.QuestionsTable.question_description));
            String confDesc = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.QuestionsTable.conflict_description));
            String step = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.QuestionsTable.question_step));
            String condition = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.QuestionsTable.question_condition));
            int responseType = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.QuestionsTable.response_type));
            String variable = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.QuestionsTable.question_variable));
            int gotTileId = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.QuestionsTable.tile_id));

            TileQuestionsModel questionModel = new TileQuestionsModel();

            //IF RESPONSE TYPE OTHER THEN 1, GET OPTIONS FROM SERVER AND POPULATE OPTIONS IN QUESTION MODEL
            questionModel.setDescription(desc);
            questionModel.setTitle(step);
            questionModel.setQuestionId(id);
            questionModel.setResponseType(responseType);
            questionModel.setCondition(condition);
            questionModel.setQuestion(question);
            questionModel.setVariable(variable);
            questionModel.setTileId(gotTileId);
            questionModel.setConflictDescription(confDesc);

            questionList.add(questionModel);
        }
        return questionList;
    }

    public void insertOption(int qid, int oid, String option) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseTables.OptionsTable.option_id, oid);
        values.put(DatabaseTables.OptionsTable.question_id, qid);
        values.put(DatabaseTables.OptionsTable.option_text, option);

        long newRowId = db.insert(DatabaseTables.OptionsTable.TABLE_NAME, null, values);
        Log.d("mylog", "Inserted row ID; " + newRowId);
    }

    public String[] getOptions(int qid) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseTables.OptionsTable.TABLE_NAME + " WHERE "
                + DatabaseTables.OptionsTable.question_id + "=" + "'" + qid + "'", null);
        int i = 0;
        String[] options = new String[cursor.getCount()];
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.OptionsTable.option_id));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.OptionsTable.option_text));
            options[i] = title.toUpperCase();
            i++;
        }
        return options;
    }

    public void deleteAll(String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + tableName);
        db.close();
    }

    public void insertMigrants(int id, String name, int age, String phone, String sex, int userId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseTables.MigrantsTable.name, name);
        values.put(DatabaseTables.MigrantsTable.age, age);
        values.put(DatabaseTables.MigrantsTable.sex, sex);
        values.put(DatabaseTables.MigrantsTable.name, name);
        values.put(DatabaseTables.MigrantsTable.phone_number, phone);
        //If already exist the Update
        String whereClause = DatabaseTables.MigrantsTable.migrant_id + " = " + id + " AND " +
                DatabaseTables.MigrantsTable.user_id + " = " + userId;
        long updateCount = db.update(DatabaseTables.MigrantsTable.TABLE_NAME, values, whereClause, null);
        Log.d("mylog", "Updated row count: " + updateCount);
        //If not update then insert
        if (updateCount < 1) {
            // Insert the new row, returning the primary key value of the new row
            values.put(DatabaseTables.MigrantsTable.migrant_id, id);
            values.put(DatabaseTables.MigrantsTable.user_id, userId);
            long newRowId = db.insert(DatabaseTables.MigrantsTable.TABLE_NAME, null, values);
            Log.d("mylog", "Inserted row ID: " + newRowId);
        }
    }

    public int getMigrantErrorCount(int migId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseTables.ResponseTable.TABLE_NAME + " WHERE "
                + DatabaseTables.ResponseTable.migrant_id + "=" + "'" + migId + "'" +
                " AND " + DatabaseTables.ResponseTable.is_error + "='true'";
        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()) {
            String isError = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.is_error));
            String variable = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.response_variable));
            Log.d("mylog", "Got error: " + isError + " FOR: " + variable);
        }
        return cursor.getCount();
    }

    public int getTileErrorCount(int migId, int tileId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseTables.ResponseTable.TABLE_NAME + " WHERE "
                + DatabaseTables.ResponseTable.migrant_id + "=" + "'" + migId + "'" +
                " AND " + DatabaseTables.ResponseTable.tile_id + "=" + "'" + tileId + "'" +
                " AND " + DatabaseTables.ResponseTable.is_error + "='true'";
        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()) {
            String isError = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.is_error));
            String variable = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.response_variable));
            Log.d("mylog", "Got error: " + isError + " FOR: " + variable);
        }
        return cursor.getCount();
    }

    public ArrayList<MigrantModel> getMigrants() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<MigrantModel> migrantModels = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseTables.MigrantsTable.TABLE_NAME + " WHERE "
                + DatabaseTables.MigrantsTable.user_id + "=" + "'" + ApplicationClass.getInstance().getUserId() + "'", null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTable.migrant_id));
            int uid = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTable.user_id));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTable.name));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTable.phone_number));
            String sex = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTable.sex));
            int age = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTable.age));
            MigrantModel migrantModel = new MigrantModel();
            migrantModel.setMigrantName(name);
            migrantModel.setUserId(uid);
            migrantModel.setMigrantAge(age);
            migrantModel.setMigrantPhone(phone);
            migrantModel.setMigrantId(id);
            migrantModel.setMigrantSex(sex);
            migrantModels.add(migrantModel);
        }
        return migrantModels;
    }

    public String[] getImportantContacts(String countryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] contacts = new String[6];
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseTables.ImportantContacts.TABLE_NAME
                + " WHERE " + DatabaseTables.ImportantContacts.country_id + "=" + "'" + countryId + "'", null);
        Log.d("mylog", "Raw Data: " + cursor.toString());
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            contacts[0] = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ImportantContacts.title));
            contacts[1] = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ImportantContacts.description));
            contacts[2] = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ImportantContacts.address));
            contacts[3] = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ImportantContacts.phone));
            contacts[4] = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ImportantContacts.email));
            contacts[5] = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ImportantContacts.website));
            return contacts;
        } else
            return null;
    }

    public MigrantModel getMigrantDetails() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<MigrantModel> migrantModels = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseTables.MigrantsTable.TABLE_NAME + " WHERE "
                + DatabaseTables.MigrantsTable.user_id + "=" + "'" + ApplicationClass.getInstance().getUserId() + "'" + " AND " +
                DatabaseTables.MigrantsTable.migrant_id + "=" + "'" + ApplicationClass.getInstance().getMigrantId() + "'", null);
        cursor.moveToNext();
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTable.migrant_id));
        int uid = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTable.user_id));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTable.name));
        String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTable.phone_number));
        String sex = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTable.sex));
        int age = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTable.age));
        MigrantModel migrantModel = new MigrantModel();
        migrantModel.setMigrantName(name);
        migrantModel.setUserId(uid);
        migrantModel.setMigrantAge(age);
        migrantModel.setMigrantPhone(phone);
        migrantModel.setMigrantId(id);
        migrantModel.setMigrantSex(sex);
        migrantModels.add(migrantModel);
        return migrantModel;
    }
}
