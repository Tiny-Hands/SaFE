package com.vysh.subairoma.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.models.TileQuestionsModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Vishal on 6/15/2017.
 */

public class TileQuestionsAdapter extends RecyclerView.Adapter<TileQuestionsAdapter.QuestionHolder> {

    boolean fromSetView;

    ArrayList<TileQuestionsModel> questionsList;
    ArrayList<TileQuestionsModel> questionsListDisplay;
    SQLDatabaseHelper sqlDatabaseHelper;

    ArrayList<String> conditionVariables;
    HashMap<String, String> conditionVariableValues;
    HashMap<String, ArrayList<Integer>> conditionOnQuestions;
    HashMap<Integer, Integer> conditionQuestionIndex;

    Context context;

    public TileQuestionsAdapter(ArrayList<TileQuestionsModel> questions,
                                ArrayList<TileQuestionsModel> displayList,
                                Context context) {
        sqlDatabaseHelper = new SQLDatabaseHelper(context);
        this.context = context;
        questionsList = questions;
        setConditionVariables();
        setConditionVariableValues();

        questionsListDisplay = new ArrayList<>();
        for (TileQuestionsModel questionModel : displayList) {
            TileQuestionsModel question = new TileQuestionsModel();
            question.setTitle(questionModel.getTitle());
            question.setCondition(questionModel.getCondition());
            question.setVariable(questionModel.getVariable());
            question.setQuestion(questionModel.getQuestion());
            question.setQuestionId(questionModel.getQuestionId());
            question.setDescription(questionModel.getDescription());
            question.setOptions(questionModel.getOptions());
            question.setQuestionNo(questionModel.getQuestionNo());
            question.setTileId(questionModel.getTileId());
            question.setResponseType(questionModel.getResponseType());

            //To Display in beginning or not
            String condition = questionModel.getCondition();
            if (condition != null && condition.contains("type")) {
                try {
                    Log.d("mylog", "Condition : " + condition);
                    JSONObject jsonCondition = new JSONObject(condition);
                    String type = jsonCondition.getString("type");
                    Log.d("mylog", "Condition type: " + type);
                    if (type.equalsIgnoreCase("error")) {
                        questionsListDisplay.add(question);
                    } else {
                        JSONObject conditionVars = jsonCondition.getJSONObject("condition");
                        Iterator iter = conditionVars.keys();
                        while (iter.hasNext()) {
                            String key = iter.next().toString();
                            boolean curValue = Boolean.parseBoolean(conditionVariableValues.get(key));
                            if (!curValue) break;
                            if (!iter.hasNext()) {
                                questionsListDisplay.add(question);
                            }
                        }
                    }
                } catch (JSONException e) {
                    Log.d("mylog", "Error: " + e.toString());
                    e.printStackTrace();
                }
            } else questionsListDisplay.add(question);
        }
    }

    @Override
    public QuestionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_tile_question, parent, false);
        return new QuestionHolder(view);
    }


    @Override
    public void onBindViewHolder(final QuestionHolder holder, final int position) {
        //Values
        holder.title.setText(questionsListDisplay.get(position).getTitle());
        holder.question.setText(questionsListDisplay.get(position).getQuestion());
        holder.details.setText(questionsListDisplay.get(position).getDescription());
        setValue(holder.checkbox, position);
    }

    private void notifyConditionVariableChange(ArrayList<Integer> questionIds) {
        for (int i = 0; i < questionIds.size(); i++) {
            Log.d("mylog", "Question ID in consideration: " + questionIds.get(i));
            int indexOnMainList = conditionQuestionIndex.get(questionIds.get(i));
            Log.d("mylog", "Index in consideration: " + indexOnMainList);
            Log.d("mylog", "Condition in consideration: " + questionsList.get(indexOnMainList).getCondition());
            parseCondition(questionsList.get(indexOnMainList).getCondition(), indexOnMainList);
        }
    }

    @Override
    public int getItemCount() {
        return questionsListDisplay.size();
    }

    private void setValue(CheckBox checkBox, int position) {
        int migrantId = ApplicationClass.getInstance().getMigrantId();
        String variable = questionsList.get(position).getVariable();
        String response = sqlDatabaseHelper.getResponse(migrantId, variable);
        if (response == null || response.isEmpty()) {
            response = "false";
        }
        if (response.equalsIgnoreCase("true")) {
            fromSetView = true;
            checkBox.setChecked(true);
        } else if (response.equalsIgnoreCase("false")) {
            fromSetView = true;
            checkBox.setChecked(false);
        }
        fromSetView = false;
    }

    private void setConditionVariables() {
        String conditionString;
        JSONObject condition;
        conditionVariables = new ArrayList<>();
        conditionOnQuestions = new HashMap<>();
        conditionQuestionIndex = new HashMap<>();
        for (int i = 0; i < questionsList.size(); i++) {
            TileQuestionsModel question = questionsList.get(i);
            conditionString = question.getCondition();
            if (conditionString != null && !conditionString.isEmpty()) {
                conditionQuestionIndex.put(question.getQuestionId(), i);
                Log.d("mylog", "Condition on Question Index: " + i);
                try {
                    condition = new JSONObject(conditionString);
                    JSONObject tempCondition = condition.getJSONObject("condition");
                    Iterator iter = tempCondition.keys();
                    String key;
                    while (iter.hasNext()) {
                        key = iter.next().toString();
                        if (!conditionVariables.contains(key)) {
                            conditionVariables.add(key);
                            ArrayList<Integer> tempQuestionList = new ArrayList<>();
                            tempQuestionList.add(question.getQuestionId());
                            conditionOnQuestions.put(key, tempQuestionList);
                        } else {
                            Log.d("mylog", "Variable: " + key + " Already added");
                            //Get Previous Question Id and create a new arraylist and put.
                            ArrayList<Integer> preArrayTemp = conditionOnQuestions.get(key);
                            for (int j = 0; j < preArrayTemp.size(); j++) {
                                Log.d("mylog", "Variable already defined for question: " + preArrayTemp.get(j));
                            }
                            Log.d("mylog", "New Question to add: " + question.getQuestionId());
                            ArrayList<Integer> tempQuestionList = preArrayTemp;
                            tempQuestionList.add(question.getQuestionId());
                            conditionOnQuestions.put(key, tempQuestionList);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setConditionVariableValues() {
        int migrantId = ApplicationClass.getInstance().getMigrantId();
        conditionVariableValues = new HashMap<>();
        for (int i = 0; i < conditionVariables.size(); i++) {
            String response = sqlDatabaseHelper.getResponse(migrantId, conditionVariables.get(i));
            if (response == null || response.isEmpty()) {
                response = "false";
            }
            //Log.d("mylog", conditionVariables.get(i) + " response is: " + response);
            conditionVariableValues.put(conditionVariables.get(i), response);
        }
    }


    private void parseCondition(String condition, int mainIndex) {
        Log.d("mylog", "Parse condition: " + condition);
        try {
            JSONObject conditionJson = new JSONObject(condition);
            String conditionType = conditionJson.getString("type");
            JSONObject varsJson = conditionJson.getJSONObject("condition");
            Iterator iter = varsJson.keys();
            String key;
            boolean reqValue;
            boolean currValue;
            while (iter.hasNext()) {
                key = iter.next().toString();
                reqValue = varsJson.getBoolean(key);
                currValue = Boolean.parseBoolean(conditionVariableValues.get(key));
                if (reqValue != currValue) {
                    Log.d("mylog", "Condition failed, do not perform the action");
                    if (conditionType.equalsIgnoreCase("visibility")) {
                        int id = questionsList.get(mainIndex).getQuestionId();
                        for (int i = 0; i < questionsListDisplay.size(); i++) {
                            int j = questionsListDisplay.get(i).getQuestionId();
                            Log.d("mylog", "Main qid: " + id + " Got qid: " + j);
                            if (id == j) {
                                Log.d("mylog", "Removing from display list: " + i);
                                questionsListDisplay.remove(i);
                                notifyItemRemoved(i);
                                notifyDataSetChanged();
                                break;
                            }
                        }
                    }
                } else {
                    Log.d("mylog", "Condition match, Perform the action");
                    if (conditionType.equalsIgnoreCase("visibility")) {
                        questionsListDisplay.add(mainIndex, questionsList.get(mainIndex));
                        notifyDataSetChanged();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public class QuestionHolder extends RecyclerView.ViewHolder {
        TextView title, details, question;
        CheckBox checkbox;
        Boolean isExpanded = false;

        public QuestionHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.tvStep);
            title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isExpanded) {
                        // Start recording changes to the view hierarchy
                        if (Build.VERSION.SDK_INT >= 19) {
                            TransitionManager.beginDelayedTransition((ViewGroup) details.getParent());
                        }
                        details.setVisibility(View.VISIBLE);
                        isExpanded = true;
                    } else {
                        if (Build.VERSION.SDK_INT >= 19) {
                            TransitionManager.beginDelayedTransition((ViewGroup) details.getParent());
                        }
                        details.setVisibility(View.GONE);
                        isExpanded = false;
                    }
                }
            });
            details = (TextView) itemView.findViewById(R.id.tvDetail);
            details.setVisibility(View.GONE);
            question = (TextView) itemView.findViewById(R.id.tvQuestion);
            checkbox = (CheckBox) itemView.findViewById(R.id.cbResponse);
            checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String variable = questionsList.get(getAdapterPosition()).getVariable();
                    if (isChecked) {
                        Log.d("mylog", "Inserting: true for " + ApplicationClass.getInstance().getMigrantId() + " in " + variable);
                        sqlDatabaseHelper.insertResponseTableData("true", questionsListDisplay.get(getAdapterPosition()).getQuestionId(),
                                ApplicationClass.getInstance().getMigrantId(), variable);
                        if (conditionVariables.contains(variable)) {
                            Log.d("mylog", "Current variable: " + variable + " Is is condition for some question");
                            conditionVariableValues.put(variable, "true");
                        }
                    } else {
                        Log.d("mylog", "Inserting: false for " + ApplicationClass.getInstance().getMigrantId() + " in " + variable);
                        sqlDatabaseHelper.insertResponseTableData("false", questionsListDisplay.get(getAdapterPosition()).getQuestionId(),
                                ApplicationClass.getInstance().getMigrantId(), variable);
                        if (conditionVariables.contains(variable)) {
                            Log.d("mylog", "Current variable: " + variable + " Is is condition for some question");
                            conditionVariableValues.put(variable, "false");
                        }
                    }
                    if (conditionVariables.contains(variable) && !fromSetView) {
                        ArrayList<Integer> questionIds = conditionOnQuestions.get(variable);
                        notifyConditionVariableChange(questionIds);
                    }
                }
            });
            Log.d("mylog", "Position: " + getAdapterPosition());
        }
    }
}
