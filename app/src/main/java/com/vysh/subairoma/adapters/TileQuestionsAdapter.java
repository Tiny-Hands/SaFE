package com.vysh.subairoma.adapters;

import android.content.Context;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Vishal on 6/15/2017.
 */

public class TileQuestionsAdapter extends RecyclerView.Adapter<TileQuestionsAdapter.QuestionHolder> {

    ArrayList<TileQuestionsModel> questionsList;
    SQLDatabaseHelper sqlDatabaseHelper;

    ArrayList<String> conditionVariables;
    HashMap<String, String> conditionVariableValues;

    public TileQuestionsAdapter(ArrayList<TileQuestionsModel> questions, Context context) {
        sqlDatabaseHelper = new SQLDatabaseHelper(context);
        questionsList = questions;
        setConditionVariables();
        setConditionVariableValues();
    }

    @Override
    public QuestionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_tile_question, parent, false);
        return new QuestionHolder(view);
    }

    @Override
    public void onBindViewHolder(final QuestionHolder holder, final int position) {
        //Values
        holder.title.setText(questionsList.get(position).getTitle());
        holder.question.setText(questionsList.get(position).getQuestion());
        holder.details.setText(questionsList.get(position).getDescription());
        setValue(holder.checkbox, position);
        //On Click Transition
        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!holder.isExpanded) {
                    // Start recording changes to the view hierarchy
                    if (Build.VERSION.SDK_INT >= 19) {
                        TransitionManager.beginDelayedTransition((ViewGroup) holder.details.getParent());
                    }
                    holder.details.setVisibility(View.VISIBLE);
                    holder.isExpanded = true;
                } else {
                    if (Build.VERSION.SDK_INT >= 19) {
                        TransitionManager.beginDelayedTransition((ViewGroup) holder.details.getParent());
                    }
                    holder.details.setVisibility(View.GONE);
                    holder.isExpanded = false;
                }
            }
        });
        holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String variable = questionsList.get(position).getVariable();
                if (conditionVariables.contains(variable)) ;
                if (isChecked) {
                    Log.d("mylog", "Inserting: true for " + ApplicationClass.getInstance().getMigrantId() + " in " + variable);
                    sqlDatabaseHelper.insertResponseTableData("true", questionsList.get(position).getQuestionId(),
                            ApplicationClass.getInstance().getMigrantId(), variable);
                } else {
                    Log.d("mylog", "Inserting: false for " + ApplicationClass.getInstance().getMigrantId() + " in " + variable);
                    sqlDatabaseHelper.insertResponseTableData("false", questionsList.get(position).getQuestionId(),
                            ApplicationClass.getInstance().getMigrantId(), variable);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return questionsList.size();
    }

    public class QuestionHolder extends RecyclerView.ViewHolder {
        TextView title, details, question;
        CheckBox checkbox;
        Boolean isExpanded = false;

        public QuestionHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.tvStep);
            details = (TextView) itemView.findViewById(R.id.tvDetail);
            details.setVisibility(View.GONE);
            question = (TextView) itemView.findViewById(R.id.tvQuestion);
            checkbox = (CheckBox) itemView.findViewById(R.id.cbResponse);
        }
    }

    private void setValue(CheckBox checkBox, int position) {
        int migrantId = ApplicationClass.getInstance().getMigrantId();
        String variable = questionsList.get(position).getVariable();
        String response = sqlDatabaseHelper.getResponse(migrantId, variable);
        if (response == null || response.isEmpty()) {
            response = "false";
        }
        if (response.equalsIgnoreCase("true")) {
            checkBox.setChecked(true);
        } else if (response.equalsIgnoreCase("false")) {
            checkBox.setChecked(false);
        }
    }

    private void setConditionVariables() {
        String conditionString;
        JSONObject condition;
        conditionVariables = new ArrayList<>();
        for (TileQuestionsModel question : questionsList) {
            conditionString = question.getCondition();
            if (conditionString != null) {
                try {
                    condition = new JSONObject(conditionString);
                    JSONObject tempCondition = condition.getJSONObject("condition");
                    Iterator iter = tempCondition.keys();
                    String key;
                    while (iter.hasNext()) {
                        key = iter.next().toString();
                        conditionVariables.add(key);
                        Log.d("mylog", "Adding Key: " + key);
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
            Log.d("mylog", conditionVariables.get(i) + " response is: " + response);
            conditionVariableValues.put(conditionVariables.get(i), response);
        }
    }

}
