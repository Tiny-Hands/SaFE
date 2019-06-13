package com.vysh.subairoma.SQLHelpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.SharedPrefKeys;
import com.vysh.subairoma.activities.ActivitySplash;
import com.vysh.subairoma.models.CountryModel;
import com.vysh.subairoma.models.FeedbackQuestionModel;
import com.vysh.subairoma.models.ImportantContactsModel;
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
    public static final int DATABASE_VERSION = 17;
    public static final String DATABASE_NAME = "SubairomaLocal.db";
    final String SQL_CREATE_ResponseTable =
            "CREATE TABLE IF NOT EXISTS " + DatabaseTables.ResponseTable.TABLE_NAME + " (" +
                    DatabaseTables.ResponseTable.migrant_id + " INTEGER," +
                    DatabaseTables.ResponseTable.question_id + " INTEGER," +
                    DatabaseTables.ResponseTable.tile_id + " INTEGER," +
                    DatabaseTables.ResponseTable.is_error + " TEXT," +
                    DatabaseTables.ResponseTable.response_variable + " TEXT," +
                    DatabaseTables.ResponseTable.response + " TEXT," +
                    DatabaseTables.ResponseTable.question_query + " TEXT," +
                    DatabaseTables.ResponseTable.response_time + " TEXT," +
                    " UNIQUE (" + DatabaseTables.ResponseTable.question_id + ", " +
                    DatabaseTables.ResponseTable.tile_id + ", " +
                    DatabaseTables.ResponseTable.migrant_id + "));";
    final String SQL_CREATE_ResponseTempTable =
            "CREATE TABLE IF NOT EXISTS " + DatabaseTables.TempResponseTable.TABLE_NAME + " (" +
                    DatabaseTables.TempResponseTable.migrant_id + " INTEGER," +
                    DatabaseTables.TempResponseTable.question_id + " INTEGER," +
                    DatabaseTables.TempResponseTable.tile_id + " INTEGER," +
                    DatabaseTables.TempResponseTable.is_error + " TEXT," +
                    DatabaseTables.TempResponseTable.response_variable + " TEXT," +
                    DatabaseTables.TempResponseTable.response + " TEXT," +
                    DatabaseTables.TempResponseTable.question_query + " TEXT," +
                    DatabaseTables.TempResponseTable.response_time + " TEXT," +
                    " UNIQUE (" + DatabaseTables.TempResponseTable.question_id + ", " +
                    DatabaseTables.TempResponseTable.tile_id + ", " +
                    DatabaseTables.TempResponseTable.migrant_id + "));";
    final String SQL_CREATE_TilesTable =
            "CREATE TABLE IF NOT EXISTS " + DatabaseTables.TilesTable.TABLE_NAME + " (" +
                    DatabaseTables.TilesTable.tile_id + " INTEGER PRIMARY KEY," +
                    DatabaseTables.TilesTable.tile_order + " INTEGER," +
                    DatabaseTables.TilesTable.tile_description + " TEXT," +
                    DatabaseTables.TilesTable.tile_type + " TEXT," +
                    DatabaseTables.TilesTable.tile_title + " TEXT" + ");";
    final String SQL_CREATE_ManpowerTable =
            "CREATE TABLE IF NOT EXISTS " + DatabaseTables.ManpowersTable.TABLE_NAME + " (" +
                    DatabaseTables.ManpowersTable.manpower_id + " INTEGER PRIMARY KEY," +
                    DatabaseTables.ManpowersTable.manpower_name + " TEXT" + ");";
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
                    DatabaseTables.QuestionsTable.response_type + " TEXT," +
                    DatabaseTables.QuestionsTable.question_call + " TEXT," +
                    DatabaseTables.QuestionsTable.question_video + " TEXT" +
                    ");";
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
                    DatabaseTables.ImportantContacts.contact_id + " INT PRIMARY KEY," +
                    DatabaseTables.ImportantContacts.country_id + " TEXT," +
                    DatabaseTables.ImportantContacts.title + " TEXT," +
                    DatabaseTables.ImportantContacts.description + " TEXT," +
                    DatabaseTables.ImportantContacts.address + " TEXT," +
                    DatabaseTables.ImportantContacts.phone + " TEXT," +
                    DatabaseTables.ImportantContacts.email + " TEXT," +
                    DatabaseTables.ImportantContacts.website + " TEXT" + ");";
    final String SQL_CREATE_ContactsTableDefault =
            "CREATE TABLE IF NOT EXISTS " + DatabaseTables.ImportantContactsDefault.TABLE_NAME + " (" +
                    DatabaseTables.ImportantContactsDefault.contact_id + " INT PRIMARY KEY," +
                    DatabaseTables.ImportantContactsDefault.title + " TEXT," +
                    DatabaseTables.ImportantContactsDefault.description + " TEXT," +
                    DatabaseTables.ImportantContactsDefault.address + " TEXT," +
                    DatabaseTables.ImportantContactsDefault.phone + " TEXT," +
                    DatabaseTables.ImportantContactsDefault.email + " TEXT," +
                    DatabaseTables.ImportantContactsDefault.website + " TEXT" + ");";
    final String SQL_CREATE_MigrantsTable =
            "CREATE TABLE IF NOT EXISTS " + DatabaseTables.MigrantsTable.TABLE_NAME + " (" +
                    DatabaseTables.MigrantsTable.migrant_id + " INTEGER PRIMARY KEY," +
                    DatabaseTables.MigrantsTable.name + " TEXT," +
                    DatabaseTables.MigrantsTable.age + " INTEGER," +
                    DatabaseTables.MigrantsTable.user_id + " INTEGER," +
                    DatabaseTables.MigrantsTable.percent_comp + " INTEGER," +
                    DatabaseTables.MigrantsTable.inactivate_date + " TEXT," +
                    DatabaseTables.MigrantsTable.sex + " TEXT," +
                    DatabaseTables.MigrantsTable.migrant_img + " TEXT," +
                    DatabaseTables.MigrantsTable.phone_number + " TEXT," +
                    DatabaseTables.MigrantsTable.helper_name + " TEXT," +
                    " UNIQUE (" + DatabaseTables.MigrantsTable.migrant_id +
                    ", " + DatabaseTables.MigrantsTable.user_id + "));";
    final String SQL_CREATE_MigrantsTempTable =
            "CREATE TABLE IF NOT EXISTS " + DatabaseTables.MigrantsTempTable.TABLE_NAME + " (" +
                    DatabaseTables.MigrantsTempTable.migrant_id + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    DatabaseTables.MigrantsTempTable.name + " TEXT," +
                    DatabaseTables.MigrantsTempTable.age + " INTEGER," +
                    DatabaseTables.MigrantsTempTable.user_id + " INTEGER," +
                    DatabaseTables.MigrantsTempTable.sex + " TEXT," +
                    DatabaseTables.MigrantsTempTable.migrant_img + " TEXT," +
                    DatabaseTables.MigrantsTempTable.phone_number + " TEXT," +
                    " UNIQUE (" + DatabaseTables.MigrantsTempTable.migrant_id +
                    ", " + DatabaseTables.MigrantsTempTable.user_id + "));";
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

    private static SQLDatabaseHelper sDbHelperInstance;

    public static synchronized SQLDatabaseHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sDbHelperInstance == null) {
            sDbHelperInstance = new SQLDatabaseHelper(context.getApplicationContext());
        }
        return sDbHelperInstance;
    }

    private SQLDatabaseHelper(Context context) {
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
        db.execSQL(SQL_CREATE_ContactsTableDefault);
        db.execSQL(SQL_CREATE_FeedbackQuestionTable);
        db.execSQL(SQL_CREATE_FeedbackQuestionResponseTable);
        db.execSQL(SQL_CREATE_MigrantsTempTable);
        db.execSQL(SQL_CREATE_ResponseTempTable);
        db.execSQL(SQL_CREATE_ManpowerTable);
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

    public void insertResponseTableData(String response, int question_id, int tileId, int migrant_id, String variable, String timestamp) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseTables.ResponseTable.response, response);
        values.put(DatabaseTables.ResponseTable.response_variable, variable);
        values.put(DatabaseTables.ResponseTable.tile_id, tileId);
        values.put(DatabaseTables.ResponseTable.response_time, timestamp);

        //If already exist the Update
        String whereClause = DatabaseTables.ResponseTable.tile_id + " = " + tileId + " AND " +
                DatabaseTables.ResponseTable.migrant_id + " = " + migrant_id + " AND " +
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
        db.close();
    }

    public void insertPercentComp(int migId, int percentComp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseTables.MigrantsTable.percent_comp, percentComp);
        String whereClause = DatabaseTables.MigrantsTable.migrant_id + " = " + migId;
        long updateCount = db.update(DatabaseTables.MigrantsTable.TABLE_NAME, values, whereClause, null);
        Log.d("mylog", "Percent Complete for MIG: " + migId + " Percent: " + percentComp + " Updated: " + updateCount);
        db.close();
    }

    public int getPercentComp(int migId) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseTables.MigrantsTable.TABLE_NAME + " WHERE "
                + DatabaseTables.MigrantsTable.migrant_id + "=" + "'" + migId + "'", null);
        int percent = 0;
        cursor.moveToFirst();
        if (cursor.getCount() > 0)
            percent = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTable.percent_comp));
        else
            Log.d("mylog", "Cursor size: " + 0);
        cursor.close();
        db.close();
        return percent;
    }

    public void insertCountryResponse(String response, int qid, int migrant_id, String variable, String timestamp) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseTables.ResponseTable.response, response);
        values.put(DatabaseTables.ResponseTable.response_variable, variable);
        values.put(DatabaseTables.ResponseTable.response_time, timestamp);

        //If already exist the Update
        String whereClause = DatabaseTables.ResponseTable.migrant_id + " = " + migrant_id + " AND " +
                DatabaseTables.ResponseTable.response_variable + " = '" + variable + "'";
        long updateCount = db.update(DatabaseTables.ResponseTable.TABLE_NAME, values, whereClause, null);
        Log.d("mylog", "Updated row count: " + updateCount);
        //If not update then insert
        if (updateCount < 1) {
            // Insert the new row, returning the primary key value of the new row
            values.put(DatabaseTables.ResponseTable.migrant_id, migrant_id);
            values.put(DatabaseTables.ResponseTable.question_id, qid);
            long newRowId = db.insert(DatabaseTables.ResponseTable.TABLE_NAME, null, values);
            Log.d("mylog", "Inserted row ID: " + newRowId);
        }
        db.close();
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
        db.close();
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
        db.close();
    }

    public void insertIsError(int migId, String variable, String isError) {
        SQLiteDatabase db = this.getWritableDatabase();
        Log.d("mylog", "Inserting isError: " + isError);
        ContentValues newValue = new ContentValues();
        if (isError.equalsIgnoreCase("false"))
            newValue.put(DatabaseTables.ResponseTable.is_error, "false");
        else if (isError.equalsIgnoreCase("true"))
            newValue.put(DatabaseTables.ResponseTable.is_error, "true");
        else
            newValue.put(DatabaseTables.ResponseTable.is_error, "-");
        String selection = DatabaseTables.ResponseTable.migrant_id + " = " + "'" + migId + "'" + " AND " +
                DatabaseTables.ResponseTable.response_variable + " = " + "'" + variable + "'";
        int updateCount = db.update(DatabaseTables.ResponseTable.TABLE_NAME, newValue, selection, null);
        Log.d("mylog", "Updated iserror rows: " + updateCount);
        db.close();
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
        int userId = ApplicationClass.getInstance().getSafeUserId();
        while (cursor.moveToNext()) {
            String qvar = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.response_variable));
            response = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.response));
            String time = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.response_time));
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
            if (time == null)
                time = "";
            params.put("time", time);
            if (error == null)
                error = "-";
            params.put("is_error", error);
            if (responseQuery == null)
                responseQuery = "";
            params.put("question_query", responseQuery);
            allResponses.add(params);
        }
        cursor.close();
        db.close();
        return allResponses;
    }

    public int getAllResponseCount(int migId) {
        int count = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        String query;
        query = "SELECT * FROM " + DatabaseTables.ResponseTable.TABLE_NAME + " WHERE " +
                DatabaseTables.ResponseTable.migrant_id + "=" + "'" + migId + "'";
        //Log.d("mylog", "Query: " + query);
        Cursor cursor = db.rawQuery(query, null);
        Log.d("mylog", "Total Res count: " + cursor.getCount());
        while (cursor.moveToNext()) {
            String error = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.is_error));
            Log.d("mylog", "Currest Res Error: " + error);
            if (error != null)
                if (error.length() < 3)
                    count++;
        }
        cursor.close();
        db.close();
        return count;
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
        cursor.close();
        db.close();
        return response;
    }

    public int getTileResponse(int migId, int tileId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + DatabaseTables.ResponseTable.TABLE_NAME +
                " WHERE " + DatabaseTables.ResponseTable.migrant_id + " = " + "'" + migId + "'"
                + " AND " + DatabaseTables.ResponseTable.tile_id + " = " + "'" + tileId + "'";

        int count = 0;
        Cursor cursor = db.rawQuery(query, null);
        while (cursor.moveToNext()) {
            String error = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.is_error));
            String response = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.response));
            String responseVar = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.response_variable));
            Log.d("mylog", "Response Variable: " + responseVar + " Response: " + response);
            if (response != null && !responseVar.equalsIgnoreCase("percent_complete") && !response.contains("false"))
                count++;
        }
        cursor.close();
        db.close();
        return count;
        //Log.d("mylog", "Query: " + query);

    }

    public String getIsError(int migId, String variable) {
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
        String isError = "-";
        while (cursor.moveToNext()) {
            isError = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.is_error));
            //Log.d("mylog", "Mid: " + mid + " Qid: " + qid + " rvar: " + response);
        }
        if (isError == null)
            isError = "-";
        cursor.close();
        db.close();
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
        db.close();
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
        cursor.close();
        db.close();
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
            String c_id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.CountriesTable.country_id));
            CountryModel countryModel = new CountryModel();
            countryModel.setCountryName(name);
            countryModel.setCountryBlacklist(blacklist);
            countryModel.setCountrySatus(status);
            countryModel.setCountryId(c_id);
            return countryModel;
        }
        cursor.close();
        db.close();
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

        db.close();
    }

    public void insertImportantContacts(int contactId, String cid, String title, String description, String address, String phone, String email, String website) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseTables.ImportantContacts.contact_id, contactId);
        values.put(DatabaseTables.ImportantContacts.country_id, cid);
        values.put(DatabaseTables.ImportantContacts.title, title);
        values.put(DatabaseTables.ImportantContacts.description, description);
        values.put(DatabaseTables.ImportantContacts.address, address);
        values.put(DatabaseTables.ImportantContacts.phone, phone);
        values.put(DatabaseTables.ImportantContacts.email, email);
        values.put(DatabaseTables.ImportantContacts.website, website);

        long newRowId = db.insert(DatabaseTables.ImportantContacts.TABLE_NAME, null, values);
        Log.d("mylog", "Inserted row ID; " + newRowId);
        db.close();
    }

    public void insertImportantContactsDefault(int contactId, String title, String description, String address, String phone, String email, String website) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseTables.ImportantContactsDefault.contact_id, contactId);
        values.put(DatabaseTables.ImportantContactsDefault.title, title);
        values.put(DatabaseTables.ImportantContactsDefault.description, description);
        values.put(DatabaseTables.ImportantContactsDefault.address, address);
        values.put(DatabaseTables.ImportantContactsDefault.phone, phone);
        values.put(DatabaseTables.ImportantContactsDefault.email, email);
        values.put(DatabaseTables.ImportantContactsDefault.website, website);

        long newRowId = db.insert(DatabaseTables.ImportantContactsDefault.TABLE_NAME, null, values);
        Log.d("mylog", "Inserted Default row ID; " + newRowId);
        db.close();
    }

    public ArrayList<TilesModel> getTiles(String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<TilesModel> tileList = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseTables.TilesTable.TABLE_NAME + " WHERE "
                + DatabaseTables.TilesTable.tile_type + "=" + "'" + type + "'" + " ORDER BY " + DatabaseTables.TilesTable.tile_order, null);
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
        cursor.close();
        db.close();
        return tileList;
    }

    public void insertQuestion(int qid, int tid, String order, String step, String title
            , String description, String condition, String responseType, String variable, String conflictDesc, String number, String link) {
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
        values.put(DatabaseTables.QuestionsTable.question_call, number);
        values.put(DatabaseTables.QuestionsTable.question_video, link);
        Log.d("mylog", "Inserting Link : " + link);
        Log.d("mylog", "Inserting Number: " + number);
        values.put(DatabaseTables.QuestionsTable.conflict_description, conflictDesc);

        long newRowId = db.insert(DatabaseTables.QuestionsTable.TABLE_NAME, null, values);
        Log.d("mylog", "Inserted row ID; " + newRowId);
        db.close();
    }

    public void insertFeedbackQuestions(int qid, String qTitle, String qOption) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseTables.FeedbackQuestionsTable.question_id, qid);
        values.put(DatabaseTables.FeedbackQuestionsTable.question_title, qTitle);
        values.put(DatabaseTables.FeedbackQuestionsTable.question_option, qOption);

        long newRowId = db.insert(DatabaseTables.FeedbackQuestionsTable.TABLE_NAME, null, values);
        Log.d("mylog", "Inserted Feedback row ID; " + newRowId);
        db.close();
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
        db.close();

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
        cursor.close();
        db.close();
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
        cursor.close();
        db.close();
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
            String link = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.QuestionsTable.question_video));
            String number = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.QuestionsTable.question_call));
            Log.d("mylog", "Got number from DB: " + number);
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
            questionModel.setNumber(number);
            questionModel.setVideoLinke(link);
            Log.d("mylog", "Got link from DB for QID: " + questionModel.getVideoLinke() + id);

            questionList.add(questionModel);
        }
        cursor.close();
        db.close();
        return questionList;
    }

    public int getNoRedFlagQuestionsCount(int tileId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String statement = "SELECT * FROM " + DatabaseTables.QuestionsTable.TABLE_NAME +
                " WHERE " + DatabaseTables.QuestionsTable.tile_id + "=" + "'" + tileId + "'";
        //Log.d("mylog", "Query: " + statement);
        int count = 0;
        Cursor cursor = db.rawQuery(statement, null);
        while (cursor.moveToNext()) {
            String condition = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.QuestionsTable.question_condition));
            if (!condition.contains("error"))
                count++;
        }
        return count;
    }

    public void insertOption(int qid, int oid, String option) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseTables.OptionsTable.option_id, oid);
        values.put(DatabaseTables.OptionsTable.question_id, qid);
        values.put(DatabaseTables.OptionsTable.option_text, option);

        long newRowId = db.insert(DatabaseTables.OptionsTable.TABLE_NAME, null, values);
        Log.d("mylog", "Inserted row ID; " + newRowId);
        db.close();
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
        cursor.close();
        db.close();
        return options;
    }

    public void deleteAll(String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + tableName);
        db.close();
    }

    public void insertMigrantDeletion(int migrantId, int userId, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseTables.MigrantsTable.inactivate_date, time);
        //If already exist the Update
        String whereClause = DatabaseTables.MigrantsTable.migrant_id + " = " + migrantId + " AND " +
                DatabaseTables.MigrantsTable.user_id + " = " + userId;
        Log.d("mylog", "Setting inactive date: " + time + " For: " + migrantId);
        long updateCount = db.update(DatabaseTables.MigrantsTable.TABLE_NAME, values, whereClause, null);
        Log.d("mylog", "Migrant Deleted row count: " + updateCount);
        db.close();
    }

    public void insertMigrants(int id, String name, int age, String phone, String sex, int userId, String migImg, int percentComp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseTables.MigrantsTable.name, name);
        values.put(DatabaseTables.MigrantsTable.age, age);
        values.put(DatabaseTables.MigrantsTable.sex, sex);
        values.put(DatabaseTables.MigrantsTable.name, name);
        values.put(DatabaseTables.MigrantsTable.phone_number, phone);
        values.put(DatabaseTables.MigrantsTable.migrant_img, migImg);
        //Checking this as not sending current percent from update mig, rather just sending -1
        if (percentComp != -1)
            values.put(DatabaseTables.MigrantsTable.percent_comp, percentComp);
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
        db.close();
    }

    public int insertTempMigrants(String name, int age, String phone, String sex, int userId, String migImg) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseTables.MigrantsTempTable.name, name);
        values.put(DatabaseTables.MigrantsTempTable.age, age);
        values.put(DatabaseTables.MigrantsTempTable.sex, sex);
        values.put(DatabaseTables.MigrantsTempTable.migrant_img, migImg);
        values.put(DatabaseTables.MigrantsTempTable.name, name);
        values.put(DatabaseTables.MigrantsTempTable.phone_number, phone);
        values.put(DatabaseTables.MigrantsTempTable.user_id, userId);
        long newRowId = db.insert(DatabaseTables.MigrantsTempTable.TABLE_NAME, null, values);
        Log.d("mylog", "Inserted row ID: " + newRowId);
        db.close();
        return (int) newRowId;
    }

    public ArrayList<MigrantModel> getAllTempMigrants() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<MigrantModel> migrantModels = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseTables.MigrantsTempTable.TABLE_NAME, null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTempTable.migrant_id));
            int uid = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTempTable.user_id));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTempTable.name));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTempTable.phone_number));
            String sex = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTempTable.sex));
            int age = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTempTable.age));
            MigrantModel migrantModel = new MigrantModel();
            migrantModel.setMigrantName(name);
            migrantModel.setMigrantAge(age);
            migrantModel.setMigrantPhone(phone);
            migrantModel.setMigrantId(id);
            migrantModel.setMigrantSex(sex);
            migrantModels.add(migrantModel);
        }
        cursor.close();
        db.close();
        return migrantModels;
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
        cursor.close();
        db.close();
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
        int count = cursor.getCount();
        cursor.close();
        db.close();
        return count;
    }

    public ArrayList<MigrantModel> getMigrants() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<MigrantModel> migrantModels = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseTables.MigrantsTable.TABLE_NAME + " WHERE "
                + DatabaseTables.MigrantsTable.user_id + "=" + "'" + ApplicationClass.getInstance().getSafeUserId() + "'", null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTable.migrant_id));
            int uid = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTable.user_id));
            String status = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTable.inactivate_date));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTable.name));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTable.phone_number));
            String sex = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTable.sex));
            String img = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTable.migrant_img));
            int age = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTable.age));
            int percentComp = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTable.percent_comp));
            MigrantModel migrantModel = new MigrantModel();
            migrantModel.setMigrantName(name);
            migrantModel.setUserId(uid);
            migrantModel.setMigrantAge(age);
            migrantModel.setMigrantPhone(phone);
            migrantModel.setMigrantId(id);
            migrantModel.setMigrantSex(sex);
            migrantModel.setMigImg(img);
            migrantModel.setInactiveDate(status);
            migrantModel.setPercentComp(percentComp);

            Log.d("mylog", "Migrant Status: " + status);
            migrantModels.add(migrantModel);
        }
        cursor.close();
        db.close();
        return migrantModels;
    }

    public String getMigrantImg(int migId) {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<MigrantModel> migrantModels = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT " + DatabaseTables.MigrantsTable.migrant_img + " FROM " +
                DatabaseTables.MigrantsTable.TABLE_NAME + " WHERE " +
                DatabaseTables.MigrantsTable.migrant_id + "=" + "'" + migId + "'", null);
        if (cursor.getCount() < 1)
            return "";
        cursor.moveToFirst();
        String img = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTable.migrant_img));
        cursor.close();
        db.close();
        return img;
    }

    public ArrayList<ImportantContactsModel> getImportantContacts(String countryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseTables.ImportantContacts.TABLE_NAME
                + " WHERE " + DatabaseTables.ImportantContacts.country_id + "=" + "'" + countryId + "'", null);
        Log.d("mylog", "Raw Data: " + cursor.toString());
        ArrayList<ImportantContactsModel> contactsModels = new ArrayList<>();
        while (cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ImportantContacts.title));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ImportantContacts.description));
            String address = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ImportantContacts.address));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ImportantContacts.phone));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ImportantContacts.email));
            String website = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ImportantContacts.website));

            ImportantContactsModel tempModel = new ImportantContactsModel();
            tempModel.setAddress(address);
            tempModel.setPhone(phone);
            tempModel.setTitle(title);
            tempModel.setDescription(description);
            tempModel.setEmail(email);
            tempModel.setWebsite(website);
            contactsModels.add(tempModel);
        }
        cursor.close();
        db.close();
        return contactsModels;
    }

    public ArrayList<ImportantContactsModel> getDefaultImportantContacts() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseTables.ImportantContactsDefault.TABLE_NAME, null);
        ArrayList<ImportantContactsModel> contactsModels = new ArrayList<>();
        //cursor.moveToFirst();
        while (cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ImportantContactsDefault.title));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ImportantContactsDefault.description));
            String address = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ImportantContactsDefault.address));
            String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ImportantContactsDefault.phone));
            String email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ImportantContactsDefault.email));
            String website = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ImportantContactsDefault.website));

            ImportantContactsModel tempModel = new ImportantContactsModel();
            tempModel.setAddress(address);
            tempModel.setPhone(phone);
            tempModel.setTitle(title);
            tempModel.setDescription(description);
            tempModel.setEmail(email);
            tempModel.setWebsite(website);
            contactsModels.add(tempModel);
            Log.d("mylog", "Default Contact title: " + title);
        }
        cursor.close();
        db.close();
        return contactsModels;
    }

    public MigrantModel getMigrantDetails() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<MigrantModel> migrantModels = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseTables.MigrantsTable.TABLE_NAME + " WHERE "
                + DatabaseTables.MigrantsTable.user_id + "=" + "'" + ApplicationClass.getInstance().getSafeUserId() + "'" + " AND " +
                DatabaseTables.MigrantsTable.migrant_id + "=" + "'" + ApplicationClass.getInstance().getMigrantId() + "'", null);
        cursor.moveToNext();
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTable.migrant_id));
        int uid = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTable.user_id));
        String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTable.name));
        String img = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTable.migrant_img));
        String phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTable.phone_number));
        String sex = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTable.sex));
        int age = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.MigrantsTable.age));
        MigrantModel migrantModel = new MigrantModel();
        migrantModel.setMigrantName(name);
        migrantModel.setUserId(uid);
        migrantModel.setMigrantAge(age);
        migrantModel.setMigrantPhone(phone);
        migrantModel.setMigrantId(id);
        migrantModel.setMigImg(img);
        migrantModel.setMigrantSex(sex);
        migrantModels.add(migrantModel);
        cursor.close();
        db.close();
        return migrantModel;
    }

    public float getPercentComplete(int migrantId, int tileId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query;
        query = "SELECT * FROM " + DatabaseTables.ResponseTable.TABLE_NAME +
                " WHERE " + DatabaseTables.ResponseTable.migrant_id + " = " + "'" + migrantId + "'" +
                " AND " + DatabaseTables.ResponseTable.tile_id + " = " + "'" + tileId + "'" +
                " AND " + DatabaseTables.ResponseTable.response_variable + " = " + "'percent_complete'";

        //Log.d("mylog", "Query: " + query);
        Cursor cursor = db.rawQuery(query, null);
        float response = 0f;
        while (cursor.moveToNext()) {
            response = cursor.getFloat(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.response));
            Log.d("mylog", "Got percent complete for MID: " + migrantId + " Tile ID: " + tileId + " : " + response);
            //Log.d("mylog", "Mid: " + mid + " Qid: " + qid + " rvar: " + response);
        }
        cursor.close();
        db.close();
        return response;
    }

    public int getQuestionResponse(int migrantId, ArrayList<Integer> questionIdsToGetAnswers) {
        int totalCount = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        for (int i = 0; i < questionIdsToGetAnswers.size(); i++) {
            Log.d("mylog", "Curr question ID: " + questionIdsToGetAnswers.get(i));
            String query = "SELECT * FROM " + DatabaseTables.ResponseTable.TABLE_NAME +
                    " WHERE " + DatabaseTables.ResponseTable.migrant_id + " = " + "'" + migrantId + "'"
                    + " AND " + DatabaseTables.ResponseTable.question_id + " = " + "'" + questionIdsToGetAnswers.get(i) + "'";

            Cursor cursor = db.rawQuery(query, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                String response = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.response));
                String responseVar = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.response_variable));
                Log.d("mylog", "Response Variable: " + responseVar + " Response: " + response);
                if (!response.equalsIgnoreCase("false"))
                    totalCount++;
                //}
                cursor.close();
            }
        }
        db.close();
        return totalCount;
    }

    public void insertTempResponseTableData(String response, int question_id, int tileId, int migrant_id, String variable, String timestamp) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseTables.TempResponseTable.response, response);
        values.put(DatabaseTables.TempResponseTable.response_variable, variable);
        values.put(DatabaseTables.TempResponseTable.tile_id, tileId);
        values.put(DatabaseTables.TempResponseTable.response_time, timestamp);

        //If already exist the Update
        String whereClause = DatabaseTables.TempResponseTable.tile_id + " = " + tileId + " AND " +
                DatabaseTables.TempResponseTable.migrant_id + " = " + migrant_id + " AND " +
                DatabaseTables.TempResponseTable.question_id + " = " + question_id;
        long updateCount = db.update(DatabaseTables.TempResponseTable.TABLE_NAME, values, whereClause, null);
        Log.d("mylog", "Updated row count: " + updateCount);
        //If not update then insert
        if (updateCount < 1) {
            // Insert the new row, returning the primary key value of the new row
            values.put(DatabaseTables.TempResponseTable.migrant_id, migrant_id);
            values.put(DatabaseTables.TempResponseTable.question_id, question_id);
            values.put(DatabaseTables.TempResponseTable.tile_id, tileId);
            long newRowId = db.insert(DatabaseTables.TempResponseTable.TABLE_NAME, null, values);
            Log.d("mylog", "Inserted row ID: " + newRowId);
        }
        db.close();
    }

    public void makeMigIdChanges(int migrantIdOld, int migrantIdNew) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseTables.MigrantsTable.migrant_id, migrantIdNew);

        //Changing in MigrantTable first
        String whereClause = DatabaseTables.MigrantsTable.migrant_id + " = " + migrantIdOld;
        long updateCount = db.update(DatabaseTables.MigrantsTable.TABLE_NAME, values, whereClause, null);
        Log.d("mylog", "Updated row count: " + updateCount);
        if (updateCount < 1) {
            Log.d("mylog", "Not Updated in Mig Table");
        }

        //Changing in MigrantTable first
        String whereClause2 = DatabaseTables.ResponseTable.migrant_id + " = " + migrantIdOld;
        long updateCount2 = db.update(DatabaseTables.ResponseTable.TABLE_NAME, values, whereClause2, null);
        Log.d("mylog", "Updated row count: " + updateCount);
        if (updateCount2 < 1) {
            Log.d("mylog", "Not Updated in Responses");
        }

        //Delete Mig From Temp Table
        db.execSQL("DELETE FROM " + DatabaseTables.MigrantsTempTable.TABLE_NAME + " WHERE " +
                DatabaseTables.MigrantsTempTable.migrant_id + "=" + migrantIdOld);
        db.close();
    }

    public void makeUserIdChanges(int userIdOld, int userIdNew) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseTables.MigrantsTable.user_id, userIdNew);

        //Changing in MigrantTable first
        String whereClause = DatabaseTables.MigrantsTable.user_id + " = " + userIdOld;
        long updateCount = db.update(DatabaseTables.MigrantsTable.TABLE_NAME, values, whereClause, null);
        Log.d("mylog", "Updated User ID row count: " + updateCount);
        if (updateCount < 1) {
            Log.d("mylog", "Not Updated User ID");
        }
        db.close();
    }

    public void insertManpower(int id, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseTables.ManpowersTable.manpower_id, id);
        values.put(DatabaseTables.ManpowersTable.manpower_name, name);

        long newRowId = db.insert(DatabaseTables.ManpowersTable.TABLE_NAME, null, values);
        db.close();
        Log.d("mylog", "Inserted manpower row ID; " + newRowId);
    }

    public ArrayList<String> getManpowers() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<String> names = new ArrayList<>();
        String statement = "SELECT * FROM " + DatabaseTables.ManpowersTable.TABLE_NAME;
        //Log.d("mylog", "Query: " + statement);
        Cursor cursor = db.rawQuery(statement, null);
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ManpowersTable.manpower_name));
            Log.d("mylog", "Got Manpower from DB:" + name);
            names.add(name);
        }
        cursor.close();
        db.close();
        return names;
    }

    public int getRedflagsQuestionCount(int migrantId, ArrayList<Integer> questionIdsWithPossibleRedflags) {
        int totalCount = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        for (int i = 0; i < questionIdsWithPossibleRedflags.size(); i++) {
            Log.d("mylog", "Curr question ID: " + questionIdsWithPossibleRedflags.get(i));
            String query = "SELECT * FROM " + DatabaseTables.ResponseTable.TABLE_NAME +
                    " WHERE " + DatabaseTables.ResponseTable.migrant_id + " = " + "'" + migrantId + "'"
                    + " AND " + DatabaseTables.ResponseTable.question_id + " = " + "'" + questionIdsWithPossibleRedflags.get(i) + "'";

            Cursor cursor = db.rawQuery(query, null);
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                String isError = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.is_error));
                Log.d("mylog", "Percent is error: " + isError);
                if (isError != null)
                    if (isError.equalsIgnoreCase("true"))
                        totalCount++;
                cursor.close();
            }
        }

        db.close();
        return totalCount;
    }
}
