package com.vysh.subairoma.models;

import java.util.ArrayList;

/**
 * Created by Vishal on 12/30/2017.
 */

public class FeedbackQuestionModel {
    String questionTitle, questionVariable;
    ArrayList<String> questionOptions;
    int questionId, questionType, questionGroup;

    public ArrayList<String> getQuestionOptions() {
        return questionOptions;
    }

    public void setQuestionOptions(ArrayList<String> questionOptions) {
        this.questionOptions = questionOptions;
    }

    public String getQuestionTitle() {
        return questionTitle;
    }

    public void setQuestionTitle(String questionTitle) {
        this.questionTitle = questionTitle;
    }

    public String getQuestionVariable() {
        return questionVariable;
    }

    public void setQuestionVariable(String questionVariable) {
        this.questionVariable = questionVariable;
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getQuestionType() {
        return questionType;
    }

    public void setQuestionType(int questionType) {
        this.questionType = questionType;
    }

    public int getQuestionGroup() {
        return questionGroup;
    }

    public void setQuestionGroup(int questionGroup) {
        this.questionGroup = questionGroup;
    }
}
