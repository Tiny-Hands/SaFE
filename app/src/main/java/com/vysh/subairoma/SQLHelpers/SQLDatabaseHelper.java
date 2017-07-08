package com.vysh.subairoma.SQLHelpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * Created by Vishal on 6/7/2017.
 */

public class SQLDatabaseHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "SubairomaLocal.db";
    final String SQL_CREATE_ResponseTable =
            "CREATE TABLE " + DatabaseTables.ResponseTable.TABLE_NAME + " (" +
                    DatabaseTables.ResponseTable.response_id + " INTEGER PRIMARY KEY," +
                    DatabaseTables.ResponseTable.question_id + " INTEGER," +
                    DatabaseTables.ResponseTable.response + " TEXT" + ");";
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
                    DatabaseTables.QuestionsTable.response_type + " TEXT" + ");";
    final String SQL_CREATE_OptionsTable =
            "CREATE TABLE " + DatabaseTables.OptionsTable.TABLE_NAME + " (" +
                    DatabaseTables.OptionsTable.optionId + " INTEGER PRIMARY KEY," +
                    DatabaseTables.OptionsTable.questionId + " INTEGER," +
                    DatabaseTables.OptionsTable.optionText + " TEXT" + ");";

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
        Log.d("mylog", "Tables Created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insertResponseTableData(String response, int question_id) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(DatabaseTables.ResponseTable.question_id, question_id);
        values.put(DatabaseTables.ResponseTable.response, response);
        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(DatabaseTables.ResponseTable.TABLE_NAME, null, values);
        Log.d("mylog", "Inserted row ID; " + newRowId);
    }

    public void deleteAll(String tableName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + tableName);
        db.close();
    }
}
