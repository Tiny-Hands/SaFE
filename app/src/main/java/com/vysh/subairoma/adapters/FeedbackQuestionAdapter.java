package com.vysh.subairoma.adapters;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.activities.ActivityFeedback;
import com.vysh.subairoma.models.FeedbackQuestionModel;
import com.vysh.subairoma.models.TileQuestionsModel;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

/**
 * Created by Vishal on 12/31/2017.
 */

public class FeedbackQuestionAdapter extends RecyclerView.Adapter<FeedbackQuestionAdapter.FeedbackHolder> {
    final int migrantId = ApplicationClass.getInstance().getMigrantId();
    Context mContext;
    ArrayList<FeedbackQuestionModel> feedbackQuestionModels;
    SQLDatabaseHelper sqlDatabaseHelper;
    ActivityFeedback activityFeedback;

    public FeedbackQuestionAdapter(Context context, ArrayList<FeedbackQuestionModel> feedbackQuestionModels) {
        this.feedbackQuestionModels = feedbackQuestionModels;
        if (context instanceof ActivityFeedback) {
            mContext = context;
            activityFeedback = (ActivityFeedback) context;
        }
        sqlDatabaseHelper = SQLDatabaseHelper.getInstance(mContext);
    }

    @Override
    public FeedbackHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_feedback_row, parent, false);
        FeedbackHolder holder = new FeedbackHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final FeedbackHolder holder, int position) {
        holder.tvMainText.setText(feedbackQuestionModels.get(position).getQuestionTitle());
        if (position == feedbackQuestionModels.size() - 1) {
            holder.bottomLine.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        //Size is number of groups
        return feedbackQuestionModels.size();
    }

    public class FeedbackHolder extends RecyclerView.ViewHolder {
        CheckBox main;
        TextView tvMainText;
        //EditText editText;
        ListView listView;
        View bottomLine;

        public FeedbackHolder(final View itemView) {
            super(itemView);
            main = itemView.findViewById(R.id.cbQ);
            tvMainText = itemView.findViewById(R.id.tvQtitle);
            bottomLine = itemView.findViewById(R.id.row_bottom_line);
            //editText = itemView.findViewById(R.id.etFeedback);
            listView = itemView.findViewById(R.id.lvOptions);
            main.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        activityFeedback.count++;
                        String feedbackOption = feedbackQuestionModels.get(getAdapterPosition()).getQuestionOptions();
                        Log.d("mylog", "FB Options: " + feedbackOption);
                        if (feedbackOption.contains("red")) {
                            Log.d("mylog", "Show Redflags");
                            //editText.setVisibility(View.VISIBLE);
                        } else if (feedbackOption.length() > 5) {
                            try {
                                JSONArray optionsArray = new JSONArray(feedbackOption);
                                ArrayList<String> options = new ArrayList<>();
                                for (int i = 0; i < optionsArray.length(); i++) {
                                    options.add(optionsArray.getString(i));
                                }
                                listView.setVisibility(View.VISIBLE);
                                listView.setAdapter(new OptionsListViewAdapter(options, feedbackQuestionModels.get(getAdapterPosition()).getQuestionId()));
                            } catch (JSONException e) {
                                Log.d("mylog", "Error parsing options: " + e.toString());
                            }
                        } else {
                            //editText.setVisibility(View.VISIBLE);
                        }
                    } else {
                        activityFeedback.count--;
                        if (listView.getVisibility() == View.VISIBLE)
                            listView.setVisibility(View.GONE);
                       /* if (editText.getVisibility() == View.VISIBLE)
                            editText.setVisibility(View.GONE);*/
                    }
                }
            });
            listView.setOnTouchListener(new ListView.OnTouchListener() {
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
        }
    }

    private class OptionsListViewAdapter extends ArrayAdapter<String> {
        ArrayList<String> options;

        public OptionsListViewAdapter(List<String> objects, int questionId) {
            super(mContext, R.layout.listview_feedback_options_row, objects);
            options = new ArrayList<>();
            options.addAll(objects);

        }

        final class ViewHolder {
            public TextView text;
            //public EditText editText;
            public CheckBox checkBox;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.listview_feedback_options_row, parent, false);
                viewHolder.text = convertView.findViewById(R.id.tvListViewOptions);
                viewHolder.checkBox = convertView.findViewById(R.id.cbListViewOptions);
                //viewHolder.editText = convertView.findViewById(R.id.etOptFeedback);
                //final EditText et = viewHolder.editText;
                viewHolder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean ischecked) {
                        if (ischecked) {
                            //et.setVisibility(View.VISIBLE);
                        } else {
                            /*if (et.getVisibility() == View.VISIBLE)
                                et.setVisibility(View.GONE);*/
                        }
                    }
                });

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.text.setText(options.get(position));
            return convertView;
        }
    }
}
