package com.vysh.subairoma.models;

import java.util.ArrayList;

/**
 * Created by Vishal on 12/30/2017.
 */

public class FeedbackQuestionModel {
    String questionTitle, questionVariable, questionOptions;
    int questionId;

    public String getQuestionOptions() {
        return questionOptions;
    }

    public void setQuestionOptions(String questionOptions) {
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
}
