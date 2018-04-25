package com.vysh.subairoma.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.SharedPrefKeys;
import com.vysh.subairoma.activities.ActivityMigrantList;
import com.vysh.subairoma.activities.ActivityTileHome;
import com.vysh.subairoma.dialogs.DialogNeedHelp;
import com.vysh.subairoma.models.CountryModel;
import com.vysh.subairoma.models.TileQuestionsModel;
import com.wordpress.priyankvex.smarttextview.SmartTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by Vishal on 6/15/2017.
 */

public class TileQuestionsAdapter extends RecyclerView.Adapter<TileQuestionsAdapter.QuestionHolder> {
    final int migrantId = ApplicationClass.getInstance().getMigrantId();

    int previousClickedPos = -1;
    int currentClickedPos = -1;
    boolean fromSetView, fromSetViewSpinner, disabled;
    Boolean isExpanded = false;
    ArrayList<TileQuestionsModel> questionsList;
    ArrayList<TileQuestionsModel> questionsListDisplay;
    SQLDatabaseHelper sqlDatabaseHelper;

    ArrayList<String> conditionVariables;
    HashMap<String, String> conditionVariableValues;
    HashMap<String, ArrayList<Integer>> conditionOnQuestions;
    HashMap<Integer, Integer> conditionQuestionIndex;

    String multiResponse = "";
    ArrayList<String> multiOptions;
    Boolean initialStep = false;

    Context context;

    public TileQuestionsAdapter(ArrayList<TileQuestionsModel> questions,
                                boolean disabled, Context context) {
        sqlDatabaseHelper = new SQLDatabaseHelper(context);
        this.context = context;
        this.disabled = disabled;
        questionsList = questions;
        setConditionVariables();
        setConditionVariableValues();

        questionsListDisplay = new ArrayList<>();
        //questionsListDisplay.addAll(questions);

        for (int i = 0; i < questions.size(); i++) {
            TileQuestionsModel questionModel = questions.get(i);
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
            question.setConflictDescription(questionModel.getConflictDescription());
            question.setVideoLinke(questionModel.getVideoLinke());
            question.setNumber(questionModel.getNumber());

            //To Display question in beginning or not
            String condition = questionModel.getCondition();
            if (!condition.equalsIgnoreCase("null") && !condition.isEmpty()) {
                try {
                    Log.d("mylog", "Condition : " + condition);
                    JSONObject jsonObject = new JSONObject(condition);
                    JSONArray conditionsArray = jsonObject.getJSONArray("conditions");
                    for (int j = 0; j < conditionsArray.length(); j++) {
                        JSONObject jsonCondition = conditionsArray.getJSONObject(j);
                        String type = jsonCondition.getString("type");
                        Log.d("mylog", "Condition type: " + type);

                        if (type.equalsIgnoreCase("visibility")) {
                            JSONObject conditionVars = jsonCondition.getJSONObject("condition");
                            Iterator iter = conditionVars.keys();
                            boolean conditionMatch = false;
                            while (iter.hasNext()) {
                                String key = iter.next().toString();
                                String curValue = conditionVariableValues.get(key);
                                if (curValue == null || curValue.isEmpty()) {
                                    //Setting to false as we might need to display a question when a variable is not filled or false
                                    curValue = "false";
                                }
                                //If Current value of the variable matches the condition satisfying value of the variable
                                Log.d("mylog", "Current Value: " + curValue + " Required Value: " + conditionVars.get(key));
                                if (curValue.equalsIgnoreCase(conditionVars.getString(key))) {
                                    conditionMatch = true;
                                } else {
                                    conditionMatch = false;
                                }
                                //This means that there are multiple options as response, even if one is clicked, we show error currently
                                if (curValue.length() > 5)
                                    conditionMatch = true;
                            }
                            if (conditionMatch) {
                                //Add to question list display only if it needs to be shown on conditions match
                                //Might Already exist if a question has multiple conditions and has been added previously
                                if (!questionsListDisplay.contains(question)) {
                                    Log.d("mylog", "Added to display list");
                                    questionsListDisplay.add(question);
                                } else {
                                    Log.d("mylog", "Question already exists in the Display list");
                                }
                            } else {
                                //Remove question from display list if visible
                                if (questionsListDisplay.contains(question)) {
                                    Log.d("mylog", "Removing from display list");
                                    questionsListDisplay.remove(question);
                                } else {
                                    Log.d("mylog", "Question doesn't exist in the Display list");
                                }
                            }
                        } else {
                            if (!questionsListDisplay.contains(question)) {
                                Log.d("mylog", "Added to display list");
                                questionsListDisplay.add(question);
                            } else {
                                Log.d("mylog", "Question already exists in the Display list");
                            }
                        }
                    }
                } catch (JSONException e) {
                    Log.d("mylog", "Error: " + e.toString());
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
        //Log.d("mylog", "Position: " + position + " Previous pos: " + previousClickedPos);
        //holder.hideExpandView();
        //isExpanded = false;
        TileQuestionsModel question = questionsListDisplay.get(position);
        holder.title.setText(question.getTitle());
        holder.question.setText(question.getQuestion());
        holder.details.setText(question.getDescription());
        Log.d("mylog", "Response type: " + question.getResponseType());
        //Check else if for every view as notifyItemChanged giving problems otherwise
        if (question.getResponseType() == 0) {
            //holder.checkbox.setVisibility(View.VISIBLE);
           /*holder.spinnerOptions.setVisibility(View.GONE);
            holder.etResponse.setVisibility(View.GONE);
            holder.listViewOptions.setVisibility(View.GONE);
            holder.rbGroup.setVisibility(View.GONE);*/
        } else if (question.getResponseType() == 1) {
           /* holder.checkbox.setVisibility(GONE);
            holder.spinnerOptions.setVisibility(View.GONE);
            holder.listViewOptions.setVisibility(View.GONE);
            //holder.etResponse.setVisibility(View.VISIBLE);
            holder.rbGroup.setVisibility(View.GONE);*/
        } else if (question.getResponseType() == 2) {
            /*holder.checkbox.setVisibility(GONE);
            holder.etResponse.setVisibility(View.GONE);
            holder.listViewOptions.setVisibility(View.GONE);
            //holder.spinnerOptions.setVisibility(View.VISIBLE);
            holder.rbGroup.setVisibility(View.GONE);*/
            ArrayList<String> options = question.getOptions();
            //Showing ---- if nothing selected
            //Somehow spinner displays index at 0 multiple time when reinitialized
            if (!options.get(0).contains("-----"))
                options.add(0, "---------");
            SpinnerAdapter adapter = new ArrayAdapter<>(context, R.layout.support_simple_spinner_dropdown_item, options);
            holder.spinnerOptions.setAdapter(adapter);
        } else if (question.getResponseType() == 3) {
            /*holder.checkbox.setVisibility(GONE);
            holder.etResponse.setVisibility(View.GONE);
            holder.spinnerOptions.setVisibility(View.GONE);
            holder.rbGroup.setVisibility(View.GONE);*/
            //holder.listViewOptions.setVisibility(View.VISIBLE);
            multiOptions = question.getOptions();
            OptionsListViewAdapter adapter = new OptionsListViewAdapter(context, multiOptions, position);
            holder.listViewOptions.setAdapter(adapter);
        } else if (question.getResponseType() == 4) {
            //holder.rbGroup.setVisibility(View.VISIBLE);
          /*holder.checkbox.setVisibility(GONE);
            holder.etResponse.setVisibility(View.GONE);
            holder.spinnerOptions.setVisibility(View.GONE);
            holder.listViewOptions.setVisibility(View.GONE);*/
            ArrayList<String> rbOptions = question.getOptions();
            Log.d("mylog", "Option Question Title: " + question.getTitle());
            if (rbOptions != null && rbOptions.size() > 1) {
                holder.rb1.setText(rbOptions.get(0));
                holder.rb2.setText(rbOptions.get(1));
            }
        } else if (question.getResponseType() == 5) {
            int importantCount = 0;
            final ArrayList<CountryModel> countries = new SQLDatabaseHelper(context).getCountries();
            ArrayList<String> countryNameList = new ArrayList<>();
            countryNameList.add("-------");
            for (CountryModel country : countries) {
                Log.d("mylog", "Country name: " + country.getCountryName());
                countryNameList.add(country.getCountryName().toUpperCase());
                if (country.getOrder() >= 1)
                    importantCount++;
            }
            if (!countryNameList.get(0).contains("-----"))
                countryNameList.add(0, "---------");
            CountryDropdownList adapter = new CountryDropdownList(context, R.layout.support_simple_spinner_dropdown_item, countryNameList, importantCount);
            holder.spinnerOptions.setAdapter(adapter);
        }
        holder.rbGroup.setVisibility(View.GONE);
        holder.checkbox.setVisibility(GONE);
        holder.etResponse.setVisibility(View.GONE);
        holder.spinnerOptions.setVisibility(View.GONE);
        holder.listViewOptions.setVisibility(View.GONE);

        holder.helpLayout.setVisibility(GONE);
        holder.question.setVisibility(GONE);
        holder.details.setVisibility(GONE);

        setValue(holder.title, holder.checkbox, holder.etResponse, holder.spinnerOptions,
                holder.question, holder.ivError, holder.ivDone, holder.btnShowMore, holder.listViewOptions, holder.rootLayout, holder.rb1, holder.rb2, position);
        if (disabled) {
            holder.disabledView.setVisibility(View.VISIBLE);
            holder.disabledView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
                    holder.toggleExpandView();
                }
            });
        }
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
        calculateAndSavePercentComplete();
        return questionsListDisplay.size();
    }

    private void calculateAndSavePercentComplete() {
        int totalQuestion = questionsListDisplay.size();
        ArrayList<Integer> questionIdsToGetAnswers = new ArrayList<>();
        for (int i = 0; i < questionsListDisplay.size(); i++) {
            if (questionsListDisplay.get(i).getCondition().contains("error"))
                totalQuestion--;
            else
                questionIdsToGetAnswers.add(questionsListDisplay.get(i).getQuestionId());
        }
        //int answersCount = sqlDatabaseHelper.getTileResponse(migrantId, questionsList.get(0).getTileId());
        int answersCount = sqlDatabaseHelper.getQuestionResponse(migrantId, questionIdsToGetAnswers);
        Log.d("mylog", "Calculating Percent for : " + totalQuestion + " Questions & " + answersCount + " Answers");
        float percent = ((float) answersCount / (float) totalQuestion) * 100;
        sqlDatabaseHelper.insertResponseTableData(percent + "", SharedPrefKeys.percentComplete, questionsListDisplay.get(0).getTileId(),
                migrantId, "percent_complete", Calendar.getInstance().getTimeInMillis() + "");
    }

    private void setValue(TextView tvTitle, CheckBox checkBox, EditText etResponse, Spinner spinner, TextView question,
                          ImageView ivError, ImageView ivDone, ImageButton btnShowMore, ListView lvOptions, RelativeLayout rootLayout, RadioButton rb1, RadioButton rb2, int position) {
        //For showing/hiding error on condition variable change
        Log.d("mylog", "Setting value now");
        String isError = sqlDatabaseHelper.getIsError(migrantId, questionsListDisplay.get(position).getVariable());
        Log.d("mylog", "Should show error for: " + questionsListDisplay.get(position).getVariable() + " : " + isError);
        String variable = questionsListDisplay.get(position).getVariable();
        Log.d("mylog", "Getting response for Migrant: " + migrantId + " Variable: " + variable);
        int responseType = questionsListDisplay.get(position).getResponseType();
        String response = sqlDatabaseHelper.getResponse(migrantId, variable);
        Log.d("mylog", "Response is: " + response);
        if (isError.equalsIgnoreCase("true")) {
            ivError.setVisibility(View.VISIBLE);
            rootLayout.setBackgroundColor(context.getResources().getColor(R.color.colorErrorFaded));
            ivDone.setVisibility(GONE);
            btnShowMore.setVisibility(GONE);
        } else {
            //Show green checkmark only if checkbox is checked and no error
            if (response.equalsIgnoreCase("true")) {
                ivDone.setVisibility(View.VISIBLE);
                tvTitle.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                btnShowMore.setVisibility(GONE);
            } else if (!response.isEmpty() && !response.equalsIgnoreCase("false") && !response.contains("---")) {
                ivDone.setVisibility(View.VISIBLE);
                tvTitle.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                btnShowMore.setVisibility(GONE);
            } else {
                ivDone.setVisibility(View.GONE);
                tvTitle.setTextColor(Color.GRAY);
            }
            ivError.setVisibility(View.INVISIBLE);
            rootLayout.setBackgroundColor(Color.TRANSPARENT);
        }

        if (responseType == 0) {
            if (response == null || response.isEmpty()) {
                Log.d("mylog", "0 Response: " + response);
            } else if (response.equalsIgnoreCase("true")) {
                fromSetView = true;
                checkBox.setChecked(true);
            } else if (response.equalsIgnoreCase("false")) {
                fromSetView = true;
                checkBox.setChecked(false);
            }
            //Hiding visibility No Matter what as If checked showing Checkmark
            question.setVisibility(View.GONE);
            checkBox.setVisibility(View.GONE);
        } else if (responseType == 1) {
            if (response == null || response.isEmpty()) {
                Log.d("mylog", "1 Response: " + response);
                question.setVisibility(GONE);
                etResponse.setVisibility(GONE);
            } else {
                fromSetView = true;
                etResponse.setText(response);
            }
        } else if (responseType == 2 || responseType == 5) {
            Log.d("mylog", "2/5 Response: " + response);
            if (response == null || response.isEmpty()) {
                question.setVisibility(GONE);
                spinner.setVisibility(GONE);
                if (responseType == 5)
                    initialStep = true;
            } else {
                if (responseType == 5)
                    response = new SQLDatabaseHelper(context).getCountry(response).getCountryName();
                fromSetView = true;
                for (int i = 0; i < spinner.getCount(); i++) {
                    if (response.equalsIgnoreCase(spinner.getItemAtPosition(i).toString())) {
                        fromSetViewSpinner = true;
                        spinner.setSelection(i);
                        break;
                    }
                }
            }
        } else if (responseType == 3) {
            multiResponse = response;
            OptionsListViewAdapter adapter = new OptionsListViewAdapter(context, multiOptions, position);
            lvOptions.setAdapter(adapter);
        } else if (responseType == 4) {
            if (response == null || response.isEmpty()) {
                Log.d("mylog", "4 Response: " + response);
            } else if (response.equalsIgnoreCase("true")) {
                fromSetView = true;
                rb1.setChecked(true);
            } else if (response.equalsIgnoreCase("false")) {
                fromSetView = true;
                rb2.setChecked(true);
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
            if (!conditionString.equalsIgnoreCase("null") && !conditionString.isEmpty()) {
                conditionQuestionIndex.put(question.getQuestionId(), i);
                Log.d("mylog", "Condition on Question Index: " + i);
                try {
                    condition = new JSONObject(conditionString);
                    JSONArray conditionList = condition.getJSONArray("conditions");
                    for (int k = 0; k < conditionList.length(); k++) {
                        JSONObject tempJson = conditionList.getJSONObject(k);
                        JSONObject tempCondition = tempJson.getJSONObject("condition");
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
                                preArrayTemp.add(question.getQuestionId());
                                conditionOnQuestions.put(key, preArrayTemp);
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void setConditionVariableValues() {
        conditionVariableValues = new HashMap<>();
        for (int i = 0; i < conditionVariables.size(); i++) {
            String response = sqlDatabaseHelper.getResponse(migrantId, conditionVariables.get(i));
            Log.d("mylog", "Condition variable to get value for: " + conditionVariables.get(i));
            if (response == null || response.isEmpty()) {
                //response = "false";
                Log.d("mylog", "Not filled yet: " + conditionVariables.get(i));
            }
            Log.d("mylog", conditionVariables.get(i) + " response is: " + response);
            conditionVariableValues.put(conditionVariables.get(i), response);
        }
    }

    private void parseCondition(String condition, int mainIndex) {
        Log.d("mylog", "Parse condition: " + condition);
        try {
            JSONObject jsonObject = new JSONObject(condition);
            JSONArray conditionsArray = jsonObject.getJSONArray("conditions");
            for (int i = 0; i < conditionsArray.length(); i++) {
                JSONObject conditionJson = conditionsArray.getJSONObject(i);
                String conditionType = conditionJson.getString("type");
                JSONObject varsJson = conditionJson.getJSONObject("condition");
                Iterator iter = varsJson.keys();
                String key = "";
                String reqValue;
                String currValue;
                boolean conditionValid = false;
                while (iter.hasNext()) {
                    key = iter.next().toString();
                    reqValue = varsJson.getString(key);
                    currValue = conditionVariableValues.get(key);

                    if (questionsList.get(mainIndex).getResponseType() == 3) {
                        //Means that it's a multi options question. Show error even if one is selected
                        if (currValue.length() > 5)
                            currValue = "true";
                    }
                    Log.d("mylog", "Current Value: " + currValue + " Required Value: " + reqValue);
                    if (!reqValue.equalsIgnoreCase(currValue)) {
                        Log.d("mylog", "Condition failed, do not perform the action");
                        //If the question is already visible, then removing it
                        int changedId = -1;
                        int id = questionsList.get(mainIndex).getQuestionId();
                        for (int j = 0; j < questionsListDisplay.size(); j++) {
                            int k = questionsListDisplay.get(j).getQuestionId();
                            Log.d("mylog", "index In Main List: " + mainIndex + " index In Display List: " + j);
                            if (id == k) {
                                //Question is visible
                                Log.d("mylog", "Take action for question with display index: " + j);
                                changedId = j;
                                break;
                            }
                        }
                        if (conditionType.equalsIgnoreCase("visibility")) {
                            //If even one condition is not satisfied, break out of while loop
                            //ChangedId is -1 if the question to hide is not in the questions display list
                            if (changedId != -1) {
                                questionsListDisplay.remove(changedId);
                                conditionValid = false;
                                Log.d("mylog", "Removing question: " + changedId);
                                notifyItemRemoved(changedId);
                            }
                            break;
                        } else if (conditionType.equalsIgnoreCase("error")) {
                            //There was already an error in previous condition so don't hide error
                            //if (!errorAlready)
                            //showError = false;
                            //showError = false;
                            conditionValid = false;
                            //ChangeId == -1 Means that question is not visible, no no need to show error
                            if (changedId != -1) {
                                //Insert no error for the question(changedId) not the key(variable)
                                Log.d("mylog", "Inserting no error for: " + migrantId + " and " +
                                        " Question: " + questionsListDisplay.get(changedId).getVariable());
                                sqlDatabaseHelper.insertIsError(migrantId, questionsListDisplay.get(changedId).getVariable(), "false");
                                notifyItemChanged(changedId);
                            }
                            break;
                        }
                    } else {
                        Log.d("mylog", "Current condition variable value match");
                        conditionValid = true;
                    }
                }
                if (conditionType.equalsIgnoreCase("visibility")) {
                    if (conditionValid) {
                        Log.d("mylog", "All variables match, showing view");
                        int mainListIdCompare = questionsList.get(mainIndex).getQuestionId();
                        boolean alreadyVisible = false;
                        for (int j = 0; j < questionsListDisplay.size(); j++) {
                            int currIdToCompare = questionsListDisplay.get(j).getQuestionId();
                            Log.d("mylog", "Main List Index: " + mainIndex + " Display List Index: " + j);
                            if (mainListIdCompare == currIdToCompare) {
                                //Question is visible
                                Log.d("mylog", "Take action for question with display index: " + j);
                                alreadyVisible = true;
                                break;
                            }
                        }
                        if (alreadyVisible)
                            Log.d("mylog", "Question already exists with display index: " + mainIndex);
                        else {
                            Log.d("mylog", "Question doesn't already exist, adding in display index: " + mainIndex);
                            questionsListDisplay.add(mainIndex, questionsList.get(mainIndex));
                            notifyItemChanged(mainIndex);
                        }
                        //If any further conditions just check but do now remove error if no error
                        //errorAlready = true;
                    }
                } else if (conditionType.equalsIgnoreCase("error")) {
                    int displayListIndex = -1;
                    if (conditionValid) {
                        Log.d("mylog", "All variables match, showing error");

                        sqlDatabaseHelper.insertIsError(migrantId, key, "true");
                        //Check if question is visible
                        showErrorDialog(mainIndex, "");
                        Log.d("mylog", "All variables match, showing view");
                        int mainListIdCompare = questionsList.get(mainIndex).getQuestionId();
                        boolean alreadyVisible = false;
                        for (int j = 0; j < questionsListDisplay.size(); j++) {
                            int currIdToCompare = questionsListDisplay.get(j).getQuestionId();
                            Log.d("mylog", "Main List Index: " + mainIndex + " Display List Index: " + j);
                            if (mainListIdCompare == currIdToCompare) {
                                //Question is visible
                                Log.d("mylog", "Take action for question with display index: " + j);
                                displayListIndex = j;
                                alreadyVisible = true;
                                break;
                            }
                        }

                        // }

                        //Checking if question is visible, then only notifying item changed
                        if (alreadyVisible) {
                            Log.d("mylog", "Question is visible, showing error");
                            if (displayListIndex != -1)
                                notifyItemChanged(displayListIndex);
                            else
                                Log.d("mylog", "Display list index is -1");
                        }
                        //If any further conditions just check but do now remove error if no error
                    }
                }
            }
        } catch (JSONException e) {
            Log.d("mylog", "Parsing condition error: " + e.toString());
        }
    }

    private void showErrorDialog(int mainIndex, String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogError);
        View errView = LayoutInflater.from(context).inflate(R.layout.dialog_error, null);
        TextView errDesc = errView.findViewById(R.id.tvErrorDescription);
        Button btnClose = errView.findViewById(R.id.btnUnderstood);

        builder.setView(errView);
        builder.setCancelable(false);
        if (mainIndex != -1)
            errDesc.setText(questionsList.get(mainIndex).getDescription());
        else
            errDesc.setText(type);
        final AlertDialog dialog = builder.show();
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }

    private void showConflictDialog(int index) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.DialogError);
        builder.setNegativeButton(context.getResources().getString(R.string.okay), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        //builder.setMessage("Conflicting Option");
        String conf = questionsListDisplay.get(index).getConflictDescription();
        String confT = questionsListDisplay.get(index).getTitle();
        Log.d("mylog", "Conflict to display: " + conf + " Title: " + confT);
        builder.setMessage(conf);
        builder.setCancelable(false);
        AlertDialog dialog = builder.show();

        final Button positiveButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        LinearLayout.LayoutParams positiveButtonLL = (LinearLayout.LayoutParams) positiveButton.getLayoutParams();
        positiveButtonLL.width = ViewGroup.LayoutParams.MATCH_PARENT;
        positiveButton.setLayoutParams(positiveButtonLL);
    }

    public class QuestionHolder extends RecyclerView.ViewHolder {
        TextView title, question;
        SmartTextView details;
        CheckBox checkbox;
        EditText etResponse;
        ImageView ivError, ivDone;
        Spinner spinnerOptions;
        ListView listViewOptions;
        Button btnHelp;
        ImageButton btnShowMore;
        CardView cardView;

        RadioGroup rbGroup;
        RadioButton rb1;
        RadioButton rb2;

        RelativeLayout rlResponse;
        LinearLayout helpLayout;
        RelativeLayout rootLayout;
        View disabledView;

        public QuestionHolder(final View itemView) {
            super(itemView);

            rlResponse = itemView.findViewById(R.id.rlResponse);
            disabledView = itemView.findViewById(R.id.viewDisabled);
            rootLayout = itemView.findViewById(R.id.rlRoot);
            ivError = itemView.findViewById(R.id.questionMarker);
            ivDone = itemView.findViewById(R.id.questionDone);
            cardView = itemView.findViewById(R.id.cv);
            btnShowMore = itemView.findViewById(R.id.btnShowMore);
            details = itemView.findViewById(R.id.tvDetail);
            //Setting color for phone numbers and weblinks
            details.setUrlColorCode("#2992dc");
            details.setPhoneNumberColorCode("#3d63f2");

            rbGroup = itemView.findViewById(R.id.rbGroup);
            rb1 = itemView.findViewById(R.id.rb1);
            rb2 = itemView.findViewById(R.id.rb2);

            //  details.setVisibility(GONE);
            question = itemView.findViewById(R.id.tvQuestion);
            checkbox = itemView.findViewById(R.id.cbResponse);
            helpLayout = itemView.findViewById(R.id.llHelp);
            btnHelp = itemView.findViewById(R.id.btnNeedHelp);
            btnHelp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int qid = questionsListDisplay.get(getAdapterPosition()).getQuestionId();
                    Log.d("mylog", "Getting for QID: " + qid);
                    int tileId = questionsListDisplay.get(getAdapterPosition()).getTileId();
                    String number = questionsListDisplay.get(getAdapterPosition()).getNumber();
                    String link = questionsListDisplay.get(getAdapterPosition()).getVideoLinke();
                    Log.d("mylog", "Got Number Local: " + number);
                    Log.d("mylog", "Got Link Local for qid: " + link + qid);
                    DialogNeedHelp dialogNeedHelp = new DialogNeedHelp();
                    dialogNeedHelp.setArgs(qid, tileId, context, migrantId, number, link);
                    dialogNeedHelp.show(((Activity) context).getFragmentManager(), "DialogLoginFrag");
                }
            });
            title = itemView.findViewById(R.id.tvStep);
            title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleExpandView();
                }
            });
            listViewOptions = itemView.findViewById(R.id.listViewMultipleOptions);
            listViewOptions.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
            listViewOptions.setOnTouchListener(new ListView.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    int action = event.getAction();
                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            // Disallow RecyclerView to intercept touch events.
                            v.getParent().requestDisallowInterceptTouchEvent(true);
                            break;

                        case MotionEvent.ACTION_UP:
                            // Allow RecyclerView to intercept touch events.
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }

                    // Handle ListView touch events.
                    v.onTouchEvent(event);
                    return true;
                }
            });
            etResponse = itemView.findViewById(R.id.etResponse);
            etResponse.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        String response = etResponse.getText().toString();
                        String variable = questionsList.get(getAdapterPosition()).getVariable();
                        if (!response.isEmpty()) {
                            Log.d("mylog", "Inserting text response for question: " +
                                    questionsListDisplay.get(getAdapterPosition()).getQuestionId() + " Tile ID: " +
                                    questionsListDisplay.get(getAdapterPosition()).getTileId()
                            );

                            Calendar cal = Calendar.getInstance();
                            String time = cal.getTimeInMillis() + "";
                            sqlDatabaseHelper.insertResponseTableData(response,
                                    questionsListDisplay.get(getAdapterPosition()).getQuestionId(),
                                    questionsListDisplay.get(getAdapterPosition()).getTileId(),
                                    migrantId, variable, time);
                        }
                        InputMethodManager inputMethodManager = (InputMethodManager) context.
                                getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputMethodManager.hideSoftInputFromWindow(itemView.getWindowToken(), 0);
                        etResponse.setCursorVisible(false);
                        return true;
                    }
                    etResponse.setCursorVisible(false);
                    return false;
                }
            });
            etResponse.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    etResponse.setCursorVisible(true);
                    return false;
                }
            });
            spinnerOptions = itemView.findViewById(R.id.spinnerOptions);
            spinnerOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String variable = questionsListDisplay.get(getAdapterPosition()).getVariable();
                    String response = spinnerOptions.getSelectedItem().toString();
                    Log.d("mylog", "Inserting spinner response for question: " +
                            questionsListDisplay.get(getAdapterPosition()).getQuestionId() + " Tile ID: " +
                            questionsListDisplay.get(getAdapterPosition()).getTileId()
                    );
                    if (conditionVariables.contains(variable)) {
                        Log.d("mylog", "Current spinner variable: " + variable + " Is in condition for some question");
                        conditionVariableValues.put(variable, response);
                    }
                    if (!fromSetViewSpinner) {
                        if (questionsListDisplay.get(getAdapterPosition()).getResponseType() == 5) {
                            Log.d("mylog", "Country position: " + position);
                            if (position != 0) {
                                final ArrayList<CountryModel> countries = new SQLDatabaseHelper(context).getCountries();
                                //ArrayList<String> countryNameList = new ArrayList<>();
                                //Subtracted one because countryNameList has 1 extra default item but countries array doesn't
                                CountryModel country = countries.get(position - 1);
                                String cid = country.getCountryId();
                                String cname = country.getCountryName();
                                int blacklist = country.getCountryBlacklist();
                                int status = country.getCountrySatus();
                                Log.d("mylog", "Country code: " + cid + " Status: " + status
                                        + " Blacklist: " + blacklist);
                                if (blacklist == 1) {
                                    showDialog(context.getResources().getString(R.string.blacklisted),
                                            context.getResources().getString(R.string.blacklisted_message), questionsListDisplay.get(getAdapterPosition()).getTileId(),
                                            questionsListDisplay.get(getAdapterPosition()).getQuestionId(), cid, cname, 0);
                                } else if (status == 1) {
                                    showDialog(context.getResources().getString(R.string.not_open),
                                            context.getResources().getString(R.string.not_open_message), questionsListDisplay.get(getAdapterPosition()).getTileId(),
                                            questionsListDisplay.get(getAdapterPosition()).getQuestionId(), cid, cname, 0);
                                } else {
                                    showDialog(context.getResources().getString(R.string.confirm),
                                            context.getResources().getString(R.string.confirm_message) + " " + cname + "?", questionsListDisplay.get(getAdapterPosition()).getTileId(),
                                            questionsListDisplay.get(getAdapterPosition()).getQuestionId(), cid, cname, 1);
                                    Log.d("mylog", "Saving country for MID: " + ApplicationClass.getInstance().getMigrantId());
                                }
                                if (!initialStep)
                                    showCountryChangeDialog();
                            }
                        } else {
                            Calendar cal = Calendar.getInstance();
                            String time = cal.getTimeInMillis() + "";

                            sqlDatabaseHelper.insertResponseTableData(response,
                                    questionsListDisplay.get(getAdapterPosition()).getQuestionId(),
                                    questionsListDisplay.get(getAdapterPosition()).getTileId(),
                                    migrantId, variable, time);
                        }
                        if (conditionVariables.contains(variable)) {
                            Log.d("mylog", "From Set View spinner: " + fromSetViewSpinner);
                            if (!fromSetViewSpinner) {
                                ArrayList<Integer> questionIds = conditionOnQuestions.get(variable);
                                notifyConditionVariableChange(questionIds);
                            }
                        }
                    }
                    fromSetViewSpinner = false;
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            rb1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (!fromSetView) {
                        if (b) {
                            Calendar cal = Calendar.getInstance();
                            String time = cal.getTimeInMillis() + "";
                            sqlDatabaseHelper.insertResponseTableData("true",
                                    questionsListDisplay.get(getAdapterPosition()).getQuestionId(),
                                    questionsListDisplay.get(getAdapterPosition()).getTileId(),
                                    migrantId, questionsListDisplay.get(getAdapterPosition()).getVariable(), time);
                            sqlDatabaseHelper.insertIsError(migrantId, questionsListDisplay.get(getAdapterPosition()).getVariable(), "false");
                            notifyItemChanged(getAdapterPosition());
                            //isExpanded = false;
                        } else {
                        }
                    }
                }
            });
            rb2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (!fromSetView) {
                        if (b) {
                            String message = questionsListDisplay.get(getAdapterPosition()).getConflictDescription();
                            showErrorDialog(-1, message);
                            Calendar cal = Calendar.getInstance();
                            String time = cal.getTimeInMillis() + "";
                            sqlDatabaseHelper.insertResponseTableData("false",
                                    questionsListDisplay.get(getAdapterPosition()).getQuestionId(),
                                    questionsListDisplay.get(getAdapterPosition()).getTileId(),
                                    migrantId, questionsListDisplay.get(getAdapterPosition()).getVariable(), time);
                            sqlDatabaseHelper.insertIsError(migrantId, questionsListDisplay.get(getAdapterPosition()).getVariable(), "true");
                            notifyItemChanged(getAdapterPosition());
                        } else {
                        }
                    }
                }
            });
            checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String variable = questionsListDisplay.get(getAdapterPosition()).getVariable();
                    if (isChecked) {
                        if (!fromSetView) {
                            //This condition is just to check If conflicting
                            boolean shouldAllowMarking = true;
                            String condition = questionsList.get(getAdapterPosition()).getCondition();
                            if (condition.contains("conflict")) {
                                shouldAllowMarking = getConflictingVariable(condition);
                            }
                            //Show conflict dialog
                            if (!shouldAllowMarking) {
                                showConflictDialog(getAdapterPosition());
                                checkbox.setChecked(false);
                                return;
                            } else {
                                Log.d("mylog", "Inserting response for question variable: " + variable +
                                        " Inserting response for question ID: " + questionsListDisplay.get(getAdapterPosition()).getQuestionId() +
                                        " Tile ID: " + questionsListDisplay.get(getAdapterPosition()).getTileId()
                                );

                                Calendar cal = Calendar.getInstance();
                                String time = cal.getTimeInMillis() + "";
                                sqlDatabaseHelper.insertResponseTableData("true",
                                        questionsListDisplay.get(getAdapterPosition()).getQuestionId(),
                                        questionsListDisplay.get(getAdapterPosition()).getTileId(),
                                        migrantId, variable, time);
                            }
                        }
                        if (conditionVariables.contains(variable)) {
                            Log.d("mylog", "Current variable: " + variable + " Is in condition for some question");
                            conditionVariableValues.put(variable, "true");
                        }
                    } else {
                        if (!fromSetView) {

                            Calendar cal = Calendar.getInstance();
                            String time = cal.getTimeInMillis() + "";
                            sqlDatabaseHelper.insertResponseTableData("false",
                                    questionsListDisplay.get(getAdapterPosition()).getQuestionId(),
                                    questionsListDisplay.get(getAdapterPosition()).getTileId(),
                                    migrantId, variable, time);
                        }

                        if (conditionVariables.contains(variable)) {
                            Log.d("mylog", "Current variable: " + variable + " Is in condition for some question");
                            conditionVariableValues.put(variable, "false");
                        }
                    }
                    if (conditionVariables.contains(variable)) {
                        ArrayList<Integer> questionIds = conditionOnQuestions.get(variable);
                        if (!fromSetView) {
                            notifyConditionVariableChange(questionIds);
                        }
                    }
                    if (!fromSetView)
                        notifyItemChanged(getAdapterPosition());

                }
            });
        }

        private void toggleExpandView() {
            previousClickedPos = currentClickedPos;
            currentClickedPos = getAdapterPosition();
            if (previousClickedPos != currentClickedPos && previousClickedPos != -1) {
                isExpanded = false;
                notifyItemChanged(previousClickedPos);
            }
            Log.d("mylog", "Is Expanded: " + isExpanded);
            if (!isExpanded) {
                showExpandView();
            } else {
                hideExpandView();
            }
        }

        private void hideExpandView() {
            if (Build.VERSION.SDK_INT >= 19) {
                TransitionManager.beginDelayedTransition((ViewGroup) cardView.getParent());
            }
            details.setVisibility(GONE);
            helpLayout.setVisibility(View.GONE);
            helpLayout.setVisibility(View.GONE);
            question.setVisibility(View.GONE);
            if (listViewOptions.getVisibility() == View.VISIBLE)
                listViewOptions.setVisibility(GONE);
            if (question.getVisibility() == View.VISIBLE)
                question.setVisibility(GONE);
            if (checkbox.getVisibility() == View.VISIBLE)
                checkbox.setVisibility(GONE);
            if (rbGroup.getVisibility() == View.VISIBLE)
                rbGroup.setVisibility(GONE);
            if (spinnerOptions.getVisibility() == VISIBLE)
                spinnerOptions.setVisibility(GONE);
            if (etResponse.getVisibility() == VISIBLE)
                etResponse.setVisibility(GONE);
            isExpanded = false;
        }

        private void showExpandView() {
            // Start Expanding
            if (Build.VERSION.SDK_INT >= 19) {
                TransitionManager.beginDelayedTransition((ViewGroup) details.getParent());
            }
            details.setVisibility(View.VISIBLE);
            helpLayout.setVisibility(View.VISIBLE);
            question.setVisibility(View.VISIBLE);
            int responseType = questionsListDisplay.get(getAdapterPosition()).getResponseType();
            if (responseType == 0) {
                checkbox.setVisibility(View.VISIBLE);
            } else if (responseType == 1) {
                etResponse.setVisibility(View.VISIBLE);
            } else if (responseType == 2 || responseType == 5) {
                spinnerOptions.setVisibility(View.VISIBLE);
            } else if (responseType == 3) {
                listViewOptions.setVisibility(View.VISIBLE);
            } else if (responseType == 4) {
                rbGroup.setVisibility(View.VISIBLE);
            }
            isExpanded = true;
        }
    }

    private boolean getConflictingVariable(String condition) {
        try {
            JSONObject conditionsObject = new JSONObject(condition);
            JSONArray conditionsArray = conditionsObject.getJSONArray("conditions");
            for (int i = 0; i < conditionsArray.length(); i++) {
                JSONObject conditionJson = conditionsArray.getJSONObject(i);
                String type = conditionJson.getString("type");
                if (type.equalsIgnoreCase("conflict")) {
                    JSONObject varsJson = conditionJson.getJSONObject("condition");
                    Iterator iter = varsJson.keys();
                    String variableValueToCheck = "";
                    if (iter.hasNext())
                        variableValueToCheck = iter.next().toString();
                    String conditionValue = conditionVariableValues.get(variableValueToCheck);
                    if (conditionValue.contains("true"))
                        return false;
                    else
                        return true;
                }
            }
        } catch (JSONException e) {
            Log.d("mylog", "Error getting conflicting variable value: " + e.toString());
        }
        return true;
    }

    private void showDialog(String title, String message, final int tileId, final int questionID, final String cid, final String cname, final int cType) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        mBuilder.setTitle(title);
        mBuilder.setMessage(message);
        String negativeMsg;
        if (cType == 0)
            negativeMsg = context.getResources().getString(R.string.go_regardless);
        else
            negativeMsg = context.getResources().getString(R.string.yes);
        mBuilder.setNegativeButton(negativeMsg,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Calendar cal = Calendar.getInstance();
                        String time = cal.getTimeInMillis() + "";
                        new SQLDatabaseHelper(context).insertCountryResponse(cid,
                                ApplicationClass.getInstance().getMigrantId(), "mg_destination", time);

                        /*Intent intent = new Intent(context, ActivityTileHome.class);
                        intent.putExtra("countryId", cid);
                        intent.putExtra("migrantName", migName);
                        intent.putExtra("countryName", cname);
                        intent.putExtra("countryStatus", status);
                        intent.putExtra("countryBlacklist", blacklist);
                        dismiss();
                        if (ApplicationClass.getInstance().getUserId() == -1)
                            intent = intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        getContext().startActivity(intent);*/
                    }
                });
        mBuilder.setPositiveButton(context.getResources().getString(R.string.choose_another_country), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mBuilder.show();
    }

    private void showCountryChangeDialog() {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        mBuilder.setView(R.layout.dialog_usertype_chooser);
        final AlertDialog alertDialog = mBuilder.show();
        RadioButton rbHelped = alertDialog.findViewById(R.id.rbHelper);
        RadioButton rbMistake = alertDialog.findViewById(R.id.rbMigrant);
        rbHelped.setText("Changing Country Based on Direction Provided by the App");
        rbMistake.setText("Had Accidentally Entered a Wrong Country");
        Button btnChosen = alertDialog.findViewById(R.id.btnChosen);
        btnChosen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });
    }

    private class OptionsListViewAdapter extends ArrayAdapter<String> {

        int questionId;
        ArrayList<String> options;
        Context mContext;
        int mainPos;
        boolean shownError = false;

        public OptionsListViewAdapter(Context context, List<String> objects, int position) {
            super(context, R.layout.listview_options_row, objects);
            options = new ArrayList<>();
            options.addAll(objects);
            mContext = context;
            mainPos = position;
        }

        final class ViewHolder {
            public TextView text;
            public CheckBox checkBox;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View rowView;
            //if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            rowView = inflater.inflate(R.layout.listview_options_row, parent, false);
            //Configure viewHolder
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = (TextView) rowView.findViewById(R.id.tvListViewOptions);
            viewHolder.checkBox = (CheckBox) rowView.findViewById(R.id.cbListViewOptions);
            rowView.setTag(viewHolder);
            //}// fill data
            ViewHolder holder = (ViewHolder) rowView.getTag();
            String s = options.get(position);
            holder.text.setText(s);
            if (multiResponse.contains(s)) {
                holder.checkBox.setChecked(true);
            }
            final String selectedOption = holder.text.getText().toString();
            holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    //If a checkbox has already been clicked before initilization, that means error dialog has also been show some-time before.
                    if (multiResponse.length() > 5)
                        shownError = true;

                    if (isChecked) {
                        //If already exists do nothing
                        if (multiResponse.contains(selectedOption)) ;
                        else {
                            multiResponse = multiResponse.concat("," + selectedOption);
                        }
                    } else {
                        if (multiResponse.contains(selectedOption))
                            multiResponse = multiResponse.replace("," + selectedOption, "");
                    }
                    //This needs to be inserted before error data insertion
                    TileQuestionsModel tempQuestion = questionsListDisplay.get(mainPos);
                    Calendar cal = Calendar.getInstance();
                    String time = cal.getTimeInMillis() + "";
                    sqlDatabaseHelper.insertResponseTableData(multiResponse,
                            tempQuestion.getQuestionId(),
                            tempQuestion.getTileId(),
                            migrantId, tempQuestion.getVariable(), time);

                    Log.d("mylog", "Multi Response Length: " + multiResponse.length());
                    if (conditionVariables.contains(tempQuestion.getVariable())) {
                        if (multiResponse.length() > 5) {
                            conditionVariableValues.put(tempQuestion.getVariable(), "true");
                            sqlDatabaseHelper.insertIsError(migrantId, tempQuestion.getVariable(), "true");
                            if (!shownError & !fromSetView) {
                                ArrayList<Integer> questionIds = conditionOnQuestions.get(questionsListDisplay.get(mainPos).getVariable());
                                notifyConditionVariableChange(questionIds);
                                //(mainPos);
                            }
                            shownError = true;
                        } else {
                            conditionVariableValues.put(tempQuestion.getVariable(), "false");
                            sqlDatabaseHelper.insertIsError(migrantId, tempQuestion.getVariable(), "false");
                            if (!fromSetView) {
                                ArrayList<Integer> questionIds = conditionOnQuestions.get(questionsListDisplay.get(mainPos).getVariable());
                                notifyConditionVariableChange(questionIds);
                            }
                            shownError = false;
                        }
                    }
                }
            });
            return rowView;
        }
    }
}
