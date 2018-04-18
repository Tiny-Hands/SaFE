package com.vysh.subairoma;

/**
 * Created by Vishal on 10/14/2017.
 */

public final class SharedPrefKeys {
    public static String sharedPrefName = "subairomasharedprefs";
    public static String dbVersion = "dbVersion";
    public static String userName = "username";
    public static String userAge = "userage";
    public static String userPhone = "userphone";
    public static String userSex = "usersex";
    public static String userId = "id";
    public static String userType = "type";
    public static String savedTableCount = "savedcount";
    public static String savedTiles = "savedquestions";
    public static String savedQuestions = "savedquestions";
    public static String savedOptions = "savedoptions";
    public static String savedCountries = "savedcountries";
    public static String savedContacts = "savedcontacts";
    public static String savedFeedbackQuestions = "savedfeedbackquestions";
    public static String feedbackResponseSaved = "savedfeedbackresponse";
    public static String defMigID = "defmigid";
    public static String otpCode = "otpCode";
    public static String lang = "lang";

    //Not for shared prefrences but for questions not in tiles
    public static int questionGender = -2;
    public static int questionCountryId = -1;
    public static int questionVerifiedAns = -3;
    public static int questionFeedbackSaved = -4;
}
