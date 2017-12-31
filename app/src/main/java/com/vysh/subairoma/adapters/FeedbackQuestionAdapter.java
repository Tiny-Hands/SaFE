package com.vysh.subairoma.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.vysh.subairoma.R;
import com.vysh.subairoma.models.FeedbackQuestionModel;

import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by Vishal on 12/31/2017.
 */

public class FeedbackQuestionAdapter extends RecyclerView.Adapter<FeedbackQuestionAdapter.FeedbackHolder> {

    Context mContext;
    ArrayList<FeedbackQuestionModel> feedbackQuestionModels;

    public FeedbackQuestionAdapter(Context context, ArrayList<FeedbackQuestionModel> feedbackQuestionModels) {
        this.feedbackQuestionModels = feedbackQuestionModels;
        mContext = context;
    }

    @Override
    public FeedbackHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_feedback_row, parent, false);
        FeedbackHolder holder = new FeedbackHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final FeedbackHolder holder, int position) {
        FeedbackQuestionModel tempModel = feedbackQuestionModels.get(position);
        holder.tvMainText.setText(tempModel.getQuestionTitle());
        if (tempModel.getQuestionType() == 2) {
            holder.main.setVisibility(View.GONE);
            holder.tvMainText.setTextSize(TypedValue.COMPLEX_UNIT_PX, mContext.getResources().getDimension(R.dimen.feedback_list_heading));
            holder.tvMainText.setBackgroundColor(mContext.getResources().getColor(R.color.editTextBackground));
        }
        holder.main.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked)
                    holder.editText.setVisibility(View.VISIBLE);
                else
                    holder.editText.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public int getItemCount() {
        //Size is number of groups
        return feedbackQuestionModels.size();
    }

    public class FeedbackHolder extends RecyclerView.ViewHolder {
        CheckBox main;
        TextView tvMainText;
        EditText editText;

        public FeedbackHolder(View itemView) {
            super(itemView);
            main = itemView.findViewById(R.id.cbQ);
            tvMainText = itemView.findViewById(R.id.tvQtitle);
            editText = itemView.findViewById(R.id.etFeedback);
        }
    }
}
