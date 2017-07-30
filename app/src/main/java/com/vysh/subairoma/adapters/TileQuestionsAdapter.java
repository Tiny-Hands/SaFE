package com.vysh.subairoma.adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
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

import static android.view.View.GONE;

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
                                Context context) {
        sqlDatabaseHelper = new SQLDatabaseHelper(context);
        this.context = context;
        questionsList = questions;
        setConditionVariables();
        setConditionVariableValues();

        questionsListDisplay = new ArrayList<>();
        for (TileQuestionsModel questionModel : questions) {
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
        View view = LayoutInflater.from(context).inflate(R.layout.recycler_view_tile_question, parent, false);
        return new QuestionHolder(view);
    }


    @Override
    public void onBindViewHolder(final QuestionHolder holder, final int position) {
        //Values
        TileQuestionsModel question = questionsListDisplay.get(position);
        holder.title.setText(question.getTitle());
        holder.question.setText(question.getQuestion());
        holder.details.setText(question.getDescription());
        if (question.getResponseType() == 1) {
            holder.checkbox.setVisibility(GONE);
            holder.question.setVisibility(GONE);
            holder.etResponse.setVisibility(View.VISIBLE);
        } else if (question.getResponseType() == 2) {
            holder.checkbox.setVisibility(GONE);
            holder.question.setVisibility(GONE);
            holder.etResponse.setVisibility(View.GONE);
            holder.spinnerOptions.setVisibility(View.VISIBLE);
            String[] options = question.getOptions();
            //Showing ---- if nothing selected
            options[0] = "---------";
            for (int i = 1; i < options.length; i++) {
                Log.d("mylog", "Options: " + options[i]);
            }
            SpinnerAdapter adapter = new ArrayAdapter<>(context, R.layout.support_simple_spinner_dropdown_item, options);
            holder.spinnerOptions.setAdapter(adapter);
        }
        setValue(holder.checkbox, holder.etResponse, holder.spinnerOptions, position);
    }

    private boolean notifyConditionVariableChange(ArrayList<Integer> questionIds) {
        boolean error = false;
        for (int i = 0; i < questionIds.size(); i++) {
            Log.d("mylog", "Question ID in consideration: " + questionIds.get(i));
            int indexOnMainList = conditionQuestionIndex.get(questionIds.get(i));
            Log.d("mylog", "Index in consideration: " + indexOnMainList);
            Log.d("mylog", "Condition in consideration: " + questionsList.get(indexOnMainList).getCondition());
            error = parseCondition(questionsList.get(indexOnMainList).getCondition(), indexOnMainList);
        }
        return error;
    }

    @Override
    public int getItemCount() {
        return questionsListDisplay.size();
    }

    private void setValue(CheckBox checkBox, EditText etResponse, Spinner spinner, int position) {
        int migrantId = ApplicationClass.getInstance().getMigrantId();
        String variable = questionsListDisplay.get(position).getVariable();
        String response = sqlDatabaseHelper.getResponse(migrantId, variable);
        int responseType = questionsListDisplay.get(position).getResponseType();
        if (responseType == 0) {
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
        } else if (responseType == 1) {
            fromSetView = true;
            etResponse.setText(response);
        } else if (responseType == 2) {
            fromSetView = true;
            for (int i = 0; i < spinner.getCount(); i++) {
                if (response.equalsIgnoreCase(spinner.getItemAtPosition(i).toString())) {
                    spinner.setSelection(i);
                    break;
                }
            }
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


    private boolean parseCondition(String condition, int mainIndex) {
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
                    } else if (conditionType.equalsIgnoreCase("error")) {
                        break;
                    }
                } else {
                    Log.d("mylog", "Condition match, Perform the action");
                    if (conditionType.equalsIgnoreCase("visibility")) {
                        questionsListDisplay.add(mainIndex, questionsList.get(mainIndex));
                        notifyDataSetChanged();
                    } else if (conditionType.equalsIgnoreCase("error")) {
                        return true;
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public class QuestionHolder extends RecyclerView.ViewHolder {
        TextView title, details, question;
        CheckBox checkbox;
        EditText etResponse;
        Boolean isExpanded = false;
        ImageView ivWarning;
        Spinner spinnerOptions;

        public QuestionHolder(final View itemView) {
            super(itemView);

            ivWarning = (ImageView) itemView.findViewById(R.id.questionMarker);
            details = (TextView) itemView.findViewById(R.id.tvDetail);
            details.setVisibility(GONE);
            question = (TextView) itemView.findViewById(R.id.tvQuestion);
            checkbox = (CheckBox) itemView.findViewById(R.id.cbResponse);
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
                        details.setVisibility(GONE);
                        isExpanded = false;
                    }
                }
            });
            etResponse = (EditText) itemView.findViewById(R.id.etResponse);
            etResponse.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        String response = etResponse.getText().toString();
                        String variable = questionsList.get(getAdapterPosition()).getVariable();
                        if (!response.isEmpty()) {
                            sqlDatabaseHelper.insertResponseTableData(response,
                                    questionsListDisplay.get(getAdapterPosition()).getQuestionId(),
                                    ApplicationClass.getInstance().getMigrantId(), variable);
                        }
                        InputMethodManager inputMethodManager = (InputMethodManager) context.
                                getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(itemView.getWindowToken(), 0);
                        return true;
                    }
                    return false;
                }
            });
            spinnerOptions = (Spinner) itemView.findViewById(R.id.spinnerOptions);
            spinnerOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String variable = questionsList.get(getAdapterPosition()).getVariable();
                    String response = spinnerOptions.getSelectedItem().toString();
                    sqlDatabaseHelper.insertResponseTableData(response,
                            questionsListDisplay.get(getAdapterPosition()).getQuestionId(),
                            ApplicationClass.getInstance().getMigrantId(), variable);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String variable = questionsList.get(getAdapterPosition()).getVariable();
                    if (isChecked) {
                        sqlDatabaseHelper.insertResponseTableData("true", questionsListDisplay.get(getAdapterPosition()).getQuestionId(),
                                ApplicationClass.getInstance().getMigrantId(), variable);
                        if (conditionVariables.contains(variable)) {
                            Log.d("mylog", "Current variable: " + variable + " Is is condition for some question");
                            conditionVariableValues.put(variable, "true");
                        }
                    } else {
                        sqlDatabaseHelper.insertResponseTableData("false", questionsListDisplay.get(getAdapterPosition()).getQuestionId(),
                                ApplicationClass.getInstance().getMigrantId(), variable);
                        if (conditionVariables.contains(variable)) {
                            Log.d("mylog", "Current variable: " + variable + " Is is condition for some question");
                            conditionVariableValues.put(variable, "false");
                        }
                    }
                    if (conditionVariables.contains(variable) && !fromSetView) {
                        ArrayList<Integer> questionIds = conditionOnQuestions.get(variable);
                        boolean error = notifyConditionVariableChange(questionIds);
                        if (error) ivWarning.setVisibility(View.VISIBLE);
                        else ivWarning.setVisibility(View.INVISIBLE);
                    }
                }
            });
        }
    }
}
