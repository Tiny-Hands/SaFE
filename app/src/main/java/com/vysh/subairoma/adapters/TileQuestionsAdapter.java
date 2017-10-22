package com.vysh.subairoma.adapters;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.models.TileQuestionsModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static android.view.View.GONE;

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
                            //ALWAYS KEEP THE ERROR CONDITION AT THE START IN THE JSON CONDITION FORMAT
                            questionsListDisplay.add(question);
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
        holder.hideExpandView();
        TileQuestionsModel question = questionsListDisplay.get(position);
        holder.title.setText(question.getTitle());
        holder.question.setText(question.getQuestion());
        holder.details.setText(question.getDescription());
        Log.d("mylog", "Response type: " + question.getResponseType());
        //Check else if for every view as notifyItemChanged giving problems otherwise
        if (question.getResponseType() == 0) {
            holder.checkbox.setVisibility(View.VISIBLE);
            holder.question.setVisibility(View.VISIBLE);
            holder.spinnerOptions.setVisibility(View.GONE);
            holder.etResponse.setVisibility(View.GONE);
            holder.listViewOptions.setVisibility(View.GONE);
        } else if (question.getResponseType() == 1) {
            holder.checkbox.setVisibility(GONE);
            holder.question.setVisibility(View.GONE);
            holder.spinnerOptions.setVisibility(View.GONE);
            holder.listViewOptions.setVisibility(View.GONE);
            holder.etResponse.setVisibility(View.VISIBLE);
        } else if (question.getResponseType() == 2) {
            holder.checkbox.setVisibility(GONE);
            holder.question.setVisibility(View.GONE);
            holder.etResponse.setVisibility(View.GONE);
            holder.listViewOptions.setVisibility(View.GONE);
            holder.spinnerOptions.setVisibility(View.VISIBLE);
            ArrayList<String> options = question.getOptions();
            //Showing ---- if nothing selected
            //Somehow spinner displays index at 0 multiple time when reinitialized
            if (!options.get(0).contains("-----"))
                options.add(0, "---------");
            SpinnerAdapter adapter = new ArrayAdapter<>(context, R.layout.support_simple_spinner_dropdown_item, options);
            holder.spinnerOptions.setAdapter(adapter);
        } else if (question.getResponseType() == 3) {
            holder.checkbox.setVisibility(GONE);
            holder.question.setVisibility(View.GONE);
            holder.etResponse.setVisibility(View.GONE);
            holder.spinnerOptions.setVisibility(View.GONE);
            holder.listViewOptions.setVisibility(View.VISIBLE);
            multiOptions = question.getOptions();
            OptionsListViewAdapter adapter = new OptionsListViewAdapter(context, multiOptions, position);
            holder.listViewOptions.setAdapter(adapter);
        }

        setValue(holder.checkbox, holder.etResponse, holder.spinnerOptions,
                holder.question, holder.ivError, holder.listViewOptions, holder.rootLayout, position);
        if (disabled) {
            holder.disabledView.setVisibility(View.VISIBLE);
            holder.disabledView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
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
        return questionsListDisplay.size();
    }

    private void setValue(CheckBox checkBox, EditText etResponse, Spinner spinner, TextView question,
                          ImageView ivError, ListView lvOptions, RelativeLayout rootLayout, int position) {
        //For showing/hiding error on condition variable change
        Log.d("mylog", "Setting value now");
        boolean isError = sqlDatabaseHelper.getIsError(migrantId, questionsListDisplay.get(position).getVariable());
        Log.d("mylog", "Should show error for: " + questionsListDisplay.get(position).getVariable() + " : " + isError);
        if (isError) {
            ivError.setVisibility(View.VISIBLE);
            rootLayout.setBackgroundColor(context.getResources().getColor(R.color.colorErrorFaded));
        } else {
            ivError.setVisibility(View.INVISIBLE);
            rootLayout.setBackgroundColor(Color.TRANSPARENT);
        }

        String variable = questionsListDisplay.get(position).getVariable();
        Log.d("mylog", "Getting response for Migrant: " + migrantId + " Variable: " + variable);
        String response = sqlDatabaseHelper.getResponse(migrantId, variable);
        Log.d("mylog", "Response is: " + response);
        int responseType = questionsListDisplay.get(position).getResponseType();
        if (responseType == 0) {
            if (response == null || response.isEmpty()) {
                Log.d("mylog", "0 Response: " + response);
                question.setVisibility(View.GONE);
                checkBox.setVisibility(View.GONE);
            }
            if (response.equalsIgnoreCase("true")) {
                fromSetView = true;
                checkBox.setChecked(true);
            } else if (response.equalsIgnoreCase("false")) {
                fromSetView = true;
                checkBox.setChecked(false);
            }
        } else if (responseType == 1) {
            if (response == null || response.isEmpty()) {
                Log.d("mylog", "1 Response: " + response);
                question.setVisibility(GONE);
                etResponse.setVisibility(GONE);
            } else {
                fromSetView = true;
                etResponse.setText(response);
            }
        } else if (responseType == 2) {
            if (response == null || response.isEmpty()) {
                Log.d("mylog", "2 Response: " + response);
                question.setVisibility(GONE);
                spinner.setVisibility(GONE);
            } else {
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

    public class QuestionHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title, details, question;
        CheckBox checkbox;
        EditText etResponse;
        ImageView ivError;
        Spinner spinnerOptions;
        ListView listViewOptions;
        Button btnCall, btnHelp, btnVideo;

        LinearLayout helpLayout;
        RelativeLayout rootLayout;
        View disabledView;

        public QuestionHolder(final View itemView) {
            super(itemView);

            disabledView = itemView.findViewById(R.id.viewDisabled);
            rootLayout = (RelativeLayout) itemView.findViewById(R.id.rlRoot);
            ivError = (ImageView) itemView.findViewById(R.id.questionMarker);
            details = (TextView) itemView.findViewById(R.id.tvDetail);
            details.setVisibility(GONE);
            question = (TextView) itemView.findViewById(R.id.tvQuestion);
            checkbox = (CheckBox) itemView.findViewById(R.id.cbResponse);
            helpLayout = (LinearLayout) itemView.findViewById(R.id.llHelp);
            btnCall = (Button) itemView.findViewById(R.id.btnCall);
            btnCall.setOnClickListener(this);
            btnHelp = (Button) itemView.findViewById(R.id.btnHelp);
            btnHelp.setOnClickListener(this);
            btnVideo = (Button) itemView.findViewById(R.id.btnVideo);
            btnVideo.setOnClickListener(this);
            title = (TextView) itemView.findViewById(R.id.tvStep);
            title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleExpandView();
                }
            });
            listViewOptions = (ListView) itemView.findViewById(R.id.listViewMultipleOptions);
            etResponse = (EditText) itemView.findViewById(R.id.etResponse);
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
                            sqlDatabaseHelper.insertResponseTableData(response,
                                    questionsListDisplay.get(getAdapterPosition()).getQuestionId(),
                                    questionsListDisplay.get(getAdapterPosition()).getTileId(),
                                    migrantId, variable);
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
                    if (!fromSetView) {
                        sqlDatabaseHelper.insertResponseTableData(response,
                                questionsListDisplay.get(getAdapterPosition()).getQuestionId(),
                                questionsListDisplay.get(getAdapterPosition()).getTileId(),
                                migrantId, variable);
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
            checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String variable = questionsListDisplay.get(getAdapterPosition()).getVariable();
                    if (isChecked) {
                        if (!fromSetView) {
                            Log.d("mylog", "Inserting response for question variable: " + variable +
                                    " Inserting response for question ID: " + questionsListDisplay.get(getAdapterPosition()).getQuestionId() +
                                    " Tile ID: " + questionsListDisplay.get(getAdapterPosition()).getTileId()
                            );
                            sqlDatabaseHelper.insertResponseTableData("true",
                                    questionsListDisplay.get(getAdapterPosition()).getQuestionId(),
                                    questionsListDisplay.get(getAdapterPosition()).getTileId(),
                                    migrantId, variable);
                        }
                        if (conditionVariables.contains(variable)) {
                            Log.d("mylog", "Current variable: " + variable + " Is in condition for some question");
                            conditionVariableValues.put(variable, "true");
                        }
                    } else {
                        if (!fromSetView) {
                            sqlDatabaseHelper.insertResponseTableData("false",
                                    questionsListDisplay.get(getAdapterPosition()).getQuestionId(),
                                    questionsListDisplay.get(getAdapterPosition()).getTileId(),
                                    migrantId, variable);
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
                TransitionManager.beginDelayedTransition((ViewGroup) details.getParent());
            }
            details.setVisibility(GONE);
            helpLayout.setVisibility(View.GONE);

            isExpanded = false;
        }

        private void showExpandView() {
            // Start Expanding
            if (Build.VERSION.SDK_INT >= 19) {
                TransitionManager.beginDelayedTransition((ViewGroup) details.getParent());
            }
            details.setVisibility(View.VISIBLE);
            helpLayout.setVisibility(View.VISIBLE);
            int responseType = questionsListDisplay.get(getAdapterPosition()).getResponseType();
            if (responseType == 0) {
                question.setVisibility(View.VISIBLE);
                checkbox.setVisibility(View.VISIBLE);
            } else if (responseType == 1) {
                question.setVisibility(View.VISIBLE);
                etResponse.setVisibility(View.VISIBLE);
            } else if (responseType == 2) {
                question.setVisibility(View.VISIBLE);
                spinnerOptions.setVisibility(View.VISIBLE);
            } else if (responseType == 3) {
                question.setVisibility(View.VISIBLE);
                listViewOptions.setVisibility(View.VISIBLE);
            }
            isExpanded = true;
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnCall:
                    Toast.makeText(context, "Call helpline", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.btnHelp:
                    Toast.makeText(context, "Help Link or Help Text", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.btnVideo:
                    Toast.makeText(context, "Open Video Link", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    private class OptionsListViewAdapter extends ArrayAdapter<String> {

        ArrayList<String> options;
        Context mContext;
        int mainPos;

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
            View rowView = convertView;
            if (rowView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                rowView = inflater.inflate(R.layout.listview_options_row, parent, false);
                //Configure viewHolder
                ViewHolder viewHolder = new ViewHolder();
                viewHolder.text = (TextView) rowView.findViewById(R.id.tvListViewOptions);
                viewHolder.checkBox = (CheckBox) rowView.findViewById(R.id.cbListViewOptions);
                rowView.setTag(viewHolder);
            }// fill data
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
                    if (isChecked) {
                        //If already exists do nothing
                        if (multiResponse.contains(selectedOption)) ;
                        else {
                            multiResponse = multiResponse.concat(", " + selectedOption);
                        }
                    } else {
                        if (multiResponse.contains(selectedOption))
                            multiResponse = multiResponse.replace(selectedOption, "");
                    }
                    sqlDatabaseHelper.insertResponseTableData(multiResponse,
                            questionsListDisplay.get(mainPos).getQuestionId(),
                            questionsListDisplay.get(mainPos).getTileId(),
                            migrantId, questionsListDisplay.get(mainPos).getVariable());
                }
            });
            return rowView;
        }
    }
}
