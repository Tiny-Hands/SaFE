package com.vysh.subairoma.SQLHelpers;

import android.provider.BaseColumns;

/**
 * Created by Vishal on 6/24/2017.
 */

public final class DatabaseTables {

    private DatabaseTables() {
    }

    public static class ResponseTable {
        public static final String TABLE_NAME = "tbl_response";
        public static final String response_id = "response_id";
        public static final String question_id = "question_id";
        public static final String response = "response";
        public static final String response_variable = "response_variable";
    }

    public static class TilesTable {
        public static final String TABLE_NAME = "tbl_tiles";
        public static final String tile_id = "tile_id";
        public static final String tile_type = "tile_type";
        public static final String tile_title = "tile_title";
        public static final String tile_description = "tile_description";
        public static final String tile_order = "tile_order";
    }

    public static class QuestionsTable {
        public static final String TABLE_NAME = "tbl_questions";
        public static final String question_id = "question_id";
        public static final String question_step = "question_step";
        public static final String question_description = "question_description";
        public static final String question_condition = "question_condition";
        public static final String response_type = "response_type";
        public static final String tile_id = "tile_id";
        public static final String question_order = "order";
        public static final String question_title = "question_title";
        public static final String question_variable = "question_variable";
    }

    public static class OptionsTable {
        public static final String TABLE_NAME = "tbl_options";
        public static final String optionId = "option_id";
        public static final String optionText = "option_text";
        public static final String questionId = "question_id";
    }

    //OTHER TABLES IF REQUIRED
}
