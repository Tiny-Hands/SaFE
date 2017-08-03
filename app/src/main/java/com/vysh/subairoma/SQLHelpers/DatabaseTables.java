package com.vysh.subairoma.SQLHelpers;


/**
 * Created by Vishal on 6/24/2017.
 */

public final class DatabaseTables {

    private DatabaseTables() {
    }

    public static class ResponseTable {
        public static final String TABLE_NAME = "tbl_response";
        public static final String migrant_id = "migrant_id";
        public static final String question_id = "question_id";
        public static final String response = "response";
        public static final String response_variable = "response_variable";
        public static final String is_error = "is_error";
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
        public static final String question_order = "question_order";
        public static final String question_title = "question_title";
        public static final String question_variable = "question_variable";
    }

    public static class CountriesTable {
        public static final String TABLE_NAME = "tbl_countries";
        public static final String country_id = "country_id";
        public static final String country_name = "country_name";
        public static final String country_status = "country_status";
        public static final String country_blacklist = "country_blacklist";
    }

    public static class OptionsTable {
        public static final String TABLE_NAME = "tbl_options";
        public static final String option_id = "option_id";
        public static final String option_text = "option_text";
        public static final String question_id = "question_id";
    }

    public static class MigrantsTable {
        public static final String TABLE_NAME = "tbl_migrants";
        public static final String migrant_id = "migrant_id";
        public static final String name = "migrant_name";
        public static final String user_id = "user_id";
        public static final String age = "migrant_age";
        public static final String sex = "migrant_sex";
        public static final String phone_number = "migrant_phone";
    }
}
