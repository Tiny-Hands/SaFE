package com.vysh.subairoma.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.models.FeedbackQuestionModel;

import java.util.ArrayList;
import java.util.zip.Inflater;

/**
 * Created by Vishal on 12/31/2017.
 */

public class FeedbackQuestionAdapter extends RecyclerView.Adapter<FeedbackQuestionAdapter.FeedbackHolder> {
    final int migrantId = ApplicationClass.getInstance().getMigrantId();
    Context mContext;
    ArrayList<FeedbackQuestionModel> feedbackQuestionModels;
    SQLDatabaseHelper sqlDatabaseHelper;

    public FeedbackQuestionAdapter(Context context, ArrayList<FeedbackQuestionModel> feedbackQuestionModels) {
        this.feedbackQuestionModels = feedbackQuestionModels;
        mContext = context;
        sqlDatabaseHelper = new SQLDatabaseHelper(mContext);
    }

    @Override
    public FeedbackHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_feedback_row, parent, false);
        FeedbackHolder holder = new FeedbackHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final FeedbackHolder holder, int position) {

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
