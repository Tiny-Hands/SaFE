package com.vysh.subairoma.adapters;

import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.vysh.subairoma.R;
import com.vysh.subairoma.models.TileQuestionsModel;

import java.util.ArrayList;

/**
 * Created by Vishal on 6/15/2017.
 */

public class TileQuestionsAdapter extends RecyclerView.Adapter<TileQuestionsAdapter.QuestionHolder> {

    ArrayList<TileQuestionsModel> questionsList;
    public TileQuestionsAdapter(ArrayList<TileQuestionsModel> questions) {
        questionsList = questions;
    }

    @Override
    public QuestionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_tile_question, parent, false);
        return new QuestionHolder(view);
    }

    @Override
    public void onBindViewHolder(final QuestionHolder holder, int position) {
        //Values
        holder.title.setText(questionsList.get(position).getTitle());
        holder.question.setText(questionsList.get(position).getQuestion());
        holder.details.setText(questionsList.get(position).getDescription());

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
}
