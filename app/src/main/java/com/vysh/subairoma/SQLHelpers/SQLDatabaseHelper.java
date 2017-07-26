package com.vysh.subairoma.SQLHelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.ContactsContract;
import android.util.Log;

import com.vysh.subairoma.models.CountryModel;
import com.vysh.subairoma.models.TileQuestionsModel;
import com.vysh.subairoma.models.TilesModel;

import java.util.ArrayList;


/**
 * Created by Vishal on 6/7/2017.
 */

public class SQLDatabaseHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "SubairomaLocal.db";
    final String SQL_CREATE_ResponseTable =
            "CREATE TABLE " + DatabaseTables.ResponseTable.TABLE_NAME + " (" +
                    DatabaseTables.ResponseTable.migrant_id + " INTEGER," +
                    DatabaseTables.ResponseTable.question_id + " INTEGER," +
                    DatabaseTables.ResponseTable.response_variable + " TEXT," +
                    DatabaseTables.ResponseTable.response + " TEXT," +
                    " UNIQUE (" + DatabaseTables.ResponseTable.question_id +
                    ", " + DatabaseTables.ResponseTable.migrant_id + ") ON CONFLICT REPLACE);";
    final String SQL_CREATE_TilesTable =
            "CREATE TABLE " + DatabaseTables.TilesTable.TABLE_NAME + " (" +
                    DatabaseTables.TilesTable.tile_id + " INTEGER PRIMARY KEY," +
                    DatabaseTables.TilesTable.tile_order + " INTEGER," +
                    DatabaseTables.TilesTable.tile_description + " TEXT," +
                    DatabaseTables.TilesTable.tile_type + " TEXT," +
                    DatabaseTables.TilesTable.tile_title + " TEXT" + ");";
    final String SQL_CREATE_QuestionsTable =
            "CREATE TABLE " + DatabaseTables.QuestionsTable.TABLE_NAME + " (" +
                    DatabaseTables.QuestionsTable.question_id + " INTEGER PRIMARY KEY," +
                    DatabaseTables.QuestionsTable.tile_id + " INTEGER," +
                    DatabaseTables.QuestionsTable.question_step + " TEXT," +
                    DatabaseTables.QuestionsTable.question_description + " TEXT," +
                    DatabaseTables.QuestionsTable.question_title + " TEXT," +
                    DatabaseTables.QuestionsTable.question_condition + " TEXT," +
                    DatabaseTables.QuestionsTable.question_variable + " TEXT," +
                    DatabaseTables.QuestionsTable.question_order + " TEXT," +
                    DatabaseTables.QuestionsTable.response_type + " TEXT" + ");";
    final String SQL_CREATE_OptionsTable =
            "CREATE TABLE " + DatabaseTables.OptionsTable.TABLE_NAME + " (" +
                    DatabaseTables.OptionsTable.option_id + " INTEGER PRIMARY KEY," +
                    DatabaseTables.OptionsTable.question_id + " INTEGER," +
                    DatabaseTables.OptionsTable.option_text + " TEXT" + ");";
    final String SQL_CREATE_CountriesTable =
            "CREATE TABLE " + DatabaseTables.CountriesTable.TABLE_NAME + " (" +
                    DatabaseTables.CountriesTable.country_id + " TEXT PRIMARY KEY," +
                    DatabaseTables.CountriesTable.country_blacklist + " INTEGER," +
                    DatabaseTables.CountriesTable.country_status + " INTEGER," +
                    DatabaseTables.CountriesTable.country_name + " TEXT" + ");";

    public SQLDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("mylog", "Database created");
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ResponseTable);
        db.execSQL(SQL_CREATE_TilesTable);
        db.execSQL(SQL_CREATE_QuestionsTable);
        db.execSQL(SQL_CREATE_OptionsTable);
        Log.d("mylog", "Countries tbl: " + SQL_CREATE_CountriesTable);
        db.execSQL(SQL_CREATE_CountriesTable);
        Log.d("mylog", "Tables Created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertResponseTableData(String response, int question_id, int migrant_id, String variable) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseTables.ResponseTable.migrant_id, migrant_id);
        values.put(DatabaseTables.ResponseTable.question_id, question_id);
        values.put(DatabaseTables.ResponseTable.response, response);
        values.put(DatabaseTables.ResponseTable.response_variable, variable);
        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(DatabaseTables.ResponseTable.TABLE_NAME, null, values);
        Log.d("mylog", "Inserted row ID; " + newRowId);
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
        Log.d("mylog", "Query: " + query);
        Cursor cursor = db.rawQuery(query, null);
        String response = "";
        while (cursor.moveToNext()) {
            String qvar = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.response_variable));
            response = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.response));
            int mid = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.migrant_id));
            int qid = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.ResponseTable.question_id));
            Log.d("mylog", "Mid: " + mid + " Qid: " + qid + " rvar: " + response);
        }
        return response;
    }

    public void insertCountry(String id, String name, int status, int blacklist) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseTables.CountriesTable.country_id, id);
        values.put(DatabaseTables.CountriesTable.country_name, name);
        values.put(DatabaseTables.CountriesTable.country_status, status);
        values.put(DatabaseTables.CountriesTable.country_blacklist, blacklist);

        long newRowId = db.insert(DatabaseTables.CountriesTable.TABLE_NAME, null, values);
        Log.d("mylog", "Inserted row ID; " + newRowId);
    }

    public ArrayList<CountryModel> getCountries() {
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<CountryModel> countryList = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DatabaseTables.CountriesTable.TABLE_NAME, null);
        while (cursor.moveToNext()) {
            String id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.CountriesTable.country_id));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.CountriesTable.country_name));
            int blacklist = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.CountriesTable.country_blacklist));
            int status = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.CountriesTable.country_status));
            CountryModel countryModel = new CountryModel();
            countryModel.setCountryId(id);
            countryModel.setCountryName(name);
            countryModel.setCountrySatus(status);
            countryModel.setCountryBlacklist(blacklist);
            countryList.add(countryModel);
        }
        return countryList;
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
            tileList.add(tilesModel);
        }
        return tileList;
    }

    public void insertQuestion(int qid, int tid, String order, String step, String title
            , String description, String condition, String responseType, String variable) {
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

        long newRowId = db.insert(DatabaseTables.QuestionsTable.TABLE_NAME, null, values);
        Log.d("mylog", "Inserted row ID; " + newRowId);
    }

    public ArrayList<TileQuestionsModel> getQuestions(int tileId) {
        Log.d("mylog", "Geting questions for tileId: " + tileId);
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<TileQuestionsModel> questionList = new ArrayList<>();
        String statement = "SELECT * FROM " + DatabaseTables.QuestionsTable.TABLE_NAME + " WHERE "
                + DatabaseTables.QuestionsTable.tile_id + "=" + "'" + tileId + "'";
        Log.d("mylog", "Query: " + statement);
        Cursor cursor = db.rawQuery(statement, null);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.QuestionsTable.question_id));
            String question = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.QuestionsTable.question_title));
            String desc = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.QuestionsTable.question_description));
            String step = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.QuestionsTable.question_step));
            String condition = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.QuestionsTable.question_condition));
            int responseType = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseTables.QuestionsTable.response_type));
            String variable = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseTables.QuestionsTable.question_variable));
            TileQuestionsModel questionModel = new TileQuestionsModel();

            //IF RESPONSE TYPE OTHER THEN 1, GET OPTIONS FROM SERVER AND POPULATE OPTIONS IN QUESTION MODEL
            questionModel.setDescription(desc);
            questionModel.setTitle(step);
            questionModel.setQuestionId(id);
            questionModel.setResponseType(responseType);
            questionModel.setCondition(condition);
            questionModel.setQuestion(question);
            questionModel.setVariable(variable);

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

    public void deleteAll(String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + tableName);
        db.close();
    }
}
