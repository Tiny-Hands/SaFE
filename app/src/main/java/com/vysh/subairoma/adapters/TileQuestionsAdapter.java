package com.vysh.subairoma.adapters;

import android.content.Context;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.models.TileQuestionsModel;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static android.view.View.GONE;

/**
 * Created by Vishal on 6/15/2017.
 */

public class TileQuestionsAdapter extends RecyclerView.Adapter<TileQuestionsAdapter.QuestionHolder> {

    final int migrantId = ApplicationClass.getInstance().getMigrantId();

    int previousClickedPos = -1;
    int currentClickedPos = -1;
    boolean fromSetView;
    Boolean isExpanded = false;
    Boolean showError = false, onCheckClick = false;
    ArrayList<TileQuestionsModel> questionsList;
    ArrayList<TileQuestionsModel> questionsListDisplay;
    SQLDatabaseHelper sqlDatabaseHelper;

    ArrayList<String> conditionVariables;
    HashMap<String, String> conditionVariableValues;
    HashMap<String, ArrayList<Integer>> conditionOnQuestions;
    HashMap<Integer, Integer> conditionQuestionIndex;
    ArrayList<Integer> showErrorList;

    Context context;

    public TileQuestionsAdapter(ArrayList<TileQuestionsModel> questions,
                                Context context) {
        sqlDatabaseHelper = new SQLDatabaseHelper(context);
        this.context = context;
        questionsList = questions;
        setConditionVariables();
        setConditionVariableValues();

        questionsListDisplay = new ArrayList<>();
        showErrorList = new ArrayList<>();
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

            //To Display in beginning or not
            String condition = questionModel.getCondition();
            if (condition != null && condition.contains("type")) {
                try {
                    Log.d("mylog", "Condition : " + condition);
                    JSONObject jsonCondition = new JSONObject(condition);
                    String type = jsonCondition.getString("type");
                    Log.d("mylog", "Condition type: " + type);
                    JSONObject conditionVars = jsonCondition.getJSONObject("condition");
                    Iterator iter = conditionVars.keys();
                    boolean conditionMatch = false;
                    while (iter.hasNext()) {
                        String key = iter.next().toString();
                        boolean curValue = Boolean.parseBoolean(conditionVariableValues.get(key));
                        if (!curValue) {
                            conditionMatch = false;
                            break;
                        } else {
                            conditionMatch = true;
                        }
                    }
                    if (type.equalsIgnoreCase("error")) {
                        questionsListDisplay.add(question);
                        if (conditionMatch) {
                            Log.d("mylog", "Added to error list: " + i);
                            showErrorList.add(i);
                        }
                    } else if (type.equalsIgnoreCase("visibility")) {
                        if (conditionMatch) questionsListDisplay.add(question);
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
        Log.d("mylog", "Position: " + position + " Previous pos: " + previousClickedPos);
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
        } else if (question.getResponseType() == 1) {
            holder.checkbox.setVisibility(GONE);
            holder.question.setVisibility(View.GONE);
            holder.spinnerOptions.setVisibility(View.GONE);
            holder.etResponse.setVisibility(View.VISIBLE);
        } else if (question.getResponseType() == 2) {
            holder.checkbox.setVisibility(GONE);
            holder.question.setVisibility(View.GONE);
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

        if (showErrorList.contains(position) && !onCheckClick) {
            Log.d("mylog", "Holder position: " + position);
            Log.d("mylog", "Error index: " + showErrorList.get(0));
            holder.ivError.setVisibility(View.VISIBLE);
        }

        //For showing/hiding error on condition variable change
        if (showError && onCheckClick) {
            holder.ivError.setVisibility(View.VISIBLE);
            showError = false;
        } else if (onCheckClick) {
            holder.ivError.setVisibility(View.INVISIBLE);
        }
        setValue(holder.checkbox, holder.etResponse, holder.spinnerOptions, holder.question, position);
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

    private void setValue(CheckBox checkBox, EditText etResponse, Spinner spinner, TextView question, int position) {
        String variable = questionsListDisplay.get(position).getVariable();
        String response = sqlDatabaseHelper.getResponse(migrantId, variable);
        int responseType = questionsListDisplay.get(position).getResponseType();
        if (responseType == 0) {
            if (response == null || response.isEmpty()) {
                Log.d("mylog", "0 Response: " + response);
                question.setVisibility(View.GONE);
                checkBox.setVisibility(View.GONE);
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
                        spinner.setSelection(i);
                        break;
                    }
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
            String key = "";
            boolean reqValue;
            boolean currValue;
            boolean conditionValid = false;
            while (iter.hasNext()) {
                key = iter.next().toString();
                reqValue = varsJson.getBoolean(key);
                currValue = Boolean.parseBoolean(conditionVariableValues.get(key));
                if (reqValue != currValue) {
                    Log.d("mylog", "Condition failed, do not perform the action");
                    int changedId = -1;
                    for (int i = 0; i < questionsListDisplay.size(); i++) {
                        int id = questionsList.get(mainIndex).getQuestionId();
                        int j = questionsListDisplay.get(i).getQuestionId();
                        Log.d("mylog", "Main qid: " + id + " Got qid: " + j);
                        if (id == j) {
                            Log.d("mylog", "Taking action for Item: " + i);
                            changedId = i;
                        }
                    }
                    if (conditionType.equalsIgnoreCase("visibility")) {
                        //If even one condition is not satisfied, break out of while loop
                        questionsListDisplay.remove(changedId);
                        conditionValid = false;
                        notifyItemRemoved(changedId);
                        break;
                    } else if (conditionType.equalsIgnoreCase("error")) {
                        showError = false;
                        conditionValid = false;
                        notifyItemChanged(changedId);
                        //Save no error for the question in database for migrantId and variable
                        Log.d("mylog", "Inserting no error for: " + migrantId + " AND " + key);

                        //Set error false for all the related keys of the condition
                        Iterator setToFalseKeys = varsJson.keys();
                        while (setToFalseKeys.hasNext()) {
                            String var = setToFalseKeys.next().toString();
                            sqlDatabaseHelper.insertIsError(migrantId, var, "false");
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
                    questionsListDisplay.add(mainIndex, questionsList.get(mainIndex));
                    notifyDataSetChanged();
                }
            } else if (conditionType.equalsIgnoreCase("error")) {
                if (conditionValid) {
                    Log.d("mylog", "All variables match, showing error");
                    showError = true;
                    notifyItemChanged(mainIndex);
                    //Save error for the question in database for migrantId and variable
                    if (key != null) {
                        Log.d("mylog", "Inserting error for: " + migrantId + " AND " + key);
                        sqlDatabaseHelper.insertIsError(migrantId, key, "true");
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
        Button btnCall, btnHelp, btnVideo;

        LinearLayout helpLayout;

        public QuestionHolder(final View itemView) {
            super(itemView);

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
                    String variable = questionsList.get(getAdapterPosition()).getVariable();
                    String response = spinnerOptions.getSelectedItem().toString();
                    if (!fromSetView)
                        sqlDatabaseHelper.insertResponseTableData(response,
                                questionsListDisplay.get(getAdapterPosition()).getQuestionId(),
                                migrantId, variable);
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
                        if (!fromSetView)
                            sqlDatabaseHelper.insertResponseTableData("true", questionsListDisplay.get(getAdapterPosition()).getQuestionId(),
                                    migrantId, variable);
                        if (conditionVariables.contains(variable)) {
                            Log.d("mylog", "Current variable: " + variable + " Is is condition for some question");
                            conditionVariableValues.put(variable, "true");
                        }
                    } else {
                        if (!fromSetView)
                            sqlDatabaseHelper.insertResponseTableData("false", questionsListDisplay.get(getAdapterPosition()).getQuestionId(),
                                    migrantId, variable);
                        if (conditionVariables.contains(variable)) {
                            Log.d("mylog", "Current variable: " + variable + " Is is condition for some question");
                            conditionVariableValues.put(variable, "false");
                        }
                    }
                    if (conditionVariables.contains(variable) && !fromSetView) {
                        onCheckClick = true;
                        ArrayList<Integer> questionIds = conditionOnQuestions.get(variable);
                        notifyConditionVariableChange(questionIds);
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
            if(responseType == 0){
                question.setVisibility(View.VISIBLE);
                checkbox.setVisibility(View.VISIBLE);
            }
            else if(responseType == 1){
                question.setVisibility(View.VISIBLE);
                etResponse.setVisibility(View.VISIBLE);
            }
            else if(responseType == 2){
                question.setVisibility(View.VISIBLE);
                spinnerOptions.setVisibility(View.VISIBLE);
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
}
