package com.vysh.subairoma.SQLHelpers;

import android.provider.BaseColumns;

/**
 * Created by Vishal on 6/24/2017.
 */

public final class DatabaseTables {

    private DatabaseTables() {
    }

    public static class ResponseTable implements BaseColumns {
        public static final String TABLE_NAME = "tbl_response";
        public static final String question_id = "question_id";
        public static final String response = "response";
    }

    //OTHER TABLES IF REQUIRED
}
