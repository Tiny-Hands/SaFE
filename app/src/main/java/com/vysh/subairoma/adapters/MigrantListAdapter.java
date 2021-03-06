package com.vysh.subairoma.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.vysh.subairoma.activities.ActivityTileChooser;
import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.dialogs.DialogCountryChooser;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.imageHelpers.ImageEncoder;
import com.vysh.subairoma.models.CountryModel;
import com.vysh.subairoma.models.MigrantModel;
import com.wordpress.priyankvex.smarttextview.SmartTextView;

import java.util.ArrayList;

/**
 * Created by Vishal on 6/16/2017.
 */

public class MigrantListAdapter extends RecyclerView.Adapter<MigrantListAdapter.MigrantHolder> {

    ArrayList<MigrantModel> migrants;
    Context mContext;

    public void setMigrants(ArrayList<MigrantModel> migrants) {
        this.migrants = migrants;
    }

    @Override
    public MigrantHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        return new MigrantHolder(LayoutInflater.from(mContext).
                inflate(R.layout.recycler_view_migrant_row, parent, false));
    }

    @Override
    public void onBindViewHolder(MigrantHolder holder, int position) {
        MigrantModel migrantModel = migrants.get(position);
        holder.textViewName.setText(migrantModel.getMigrantName());
        holder.tvPhone.setText(migrantModel.getMigrantPhone());
        holder.tvPercentComp.setText("Completed: " + migrantModel.getPercentComp() + "%");
        String tsex = migrantModel.getMigrantSex();
        if (tsex.equalsIgnoreCase("male")) {
            tsex = mContext.getResources().getString(R.string.male);
        } else if (tsex.equalsIgnoreCase("female")) {
            tsex = mContext.getResources().getString(R.string.female);
        }
        holder.sex.setText(tsex + ", " + migrantModel.getMigrantAge());
        Log.d("mylog", "Migrant Image: " + migrantModel.getMigImg());
        if (migrantModel.getMigImg() == null || migrantModel.getMigImg().length() < 10) {
            if (migrantModel.getMigrantSex().equalsIgnoreCase("female"))
                holder.ivAvatar.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_female));
            else
                holder.ivAvatar.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_male));
        } else {
            holder.ivAvatar.setImageBitmap(ImageEncoder.decodeFromBase64(migrantModel.getMigImg()));
        }

        //Showing going to country
        String cid = SQLDatabaseHelper.getInstance(mContext).getResponse(migrants.get(position).getMigrantId(), "mg_destination");
        Log.d("mylog", "Going To CID Received: " + cid);

        if (cid == null || cid.isEmpty()) {
            holder.tvGoingCountry.setText(mContext.getResources().getString(R.string.not_started));
            holder.tvGoingCountry.setTextColor(Color.GRAY);
        } else {
            CountryModel goingTo = SQLDatabaseHelper.getInstance(mContext).getCountry(cid);
            Log.d("mylog", "Going To Received: " + goingTo);
            if (goingTo == null)
                holder.tvGoingCountry.setText(cid);
            else {
                holder.tvGoingCountry.setText(goingTo.getCountryName().toUpperCase());

                if (goingTo.getCountryBlacklist() == 1)
                    holder.tvGoingCountry.setTextColor(mContext.getResources().getColor(R.color.colorError));
                else if (goingTo.getCountrySatus() == 1)
                    holder.tvGoingCountry.setTextColor(mContext.getResources().getColor(R.color.colorNeutral));
                else
                    holder.tvGoingCountry.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
            }
        }
        //Showing error count
        int errorCount = SQLDatabaseHelper.getInstance(mContext).getMigrantErrorCount(migrantModel.getMigrantId());

        if (errorCount > 0) {
            holder.llErrorLayout.setVisibility(View.VISIBLE);
            holder.llErrorLayout.removeAllViews();
            for (int i = 0; i < errorCount; i++) {
                ImageView imgView = new ImageView(holder.llErrorLayout.getContext());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) holder.llErrorLayout.getContext().getResources().getDimension(R.dimen.redflag_dimen),
                        (int) holder.llErrorLayout.getContext().getResources().getDimension(R.dimen.redflag_dimen));
                imgView.setLayoutParams(lp);
                imgView.setImageResource(R.drawable.ic_redflag);
                holder.llErrorLayout.addView(imgView);
            }
        } else if (errorCount <= 0)
            holder.llErrorLayout.setVisibility(View.GONE);
        if (migrantModel.getMigrantId() < 0)
            holder.viewForeground.setBackgroundColor(mContext.getResources().getColor(R.color.colorOffline));
        else holder.viewForeground.setBackgroundColor(Color.WHITE);

    }

    @Override
    public int getItemCount() {
        if (migrants == null)
            return 7;
        else
            return migrants.size();
    }

    public void removeItem(int position) {
        migrants.remove(position);
        // notify the item removed by position
        // to perform recycler view delete animations
        // NOTE: don't call notifyDataSetChanged()
        notifyItemRemoved(position);
        //Save Removed in Migrants Table
    }

    public void restoreItem(MigrantModel mig, int position) {
        migrants.add(position, mig);
        // notify item added by position
        notifyItemInserted(position);
    }

    public class MigrantHolder extends RecyclerView.ViewHolder {
        public TextView textViewName, sex, tvPercentComp, tvGoingCountry;
        public SmartTextView tvPhone;
        public ImageView ivAvatar;
        public LinearLayout llErrorLayout;
        public RelativeLayout viewForeground;

        public MigrantHolder(final View itemView) {
            super(itemView);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            tvPercentComp = itemView.findViewById(R.id.tvPercentComplete);
            textViewName = itemView.findViewById(R.id.tvMigrantName);
            tvGoingCountry = itemView.findViewById(R.id.tvCountryGoing);
            ivAvatar = itemView.findViewById(R.id.ivUserLogo);
            llErrorLayout = itemView.findViewById(R.id.llRedflags);
            sex = itemView.findViewById(R.id.tvMigrantAgeSex);
            viewForeground = itemView.findViewById(R.id.viewForeground);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int migId = migrants.get(getAdapterPosition()).getMigrantId();
                    ApplicationClass.getInstance().setMigrantId(migId);
                    String cid = SQLDatabaseHelper.getInstance(itemView.getContext()).getResponse(migId, "mg_destination");
                    Log.d("mylog", "Country ID: " + cid);
                    if (cid != null && !cid.isEmpty()) {
                        Intent intent = new Intent(itemView.getContext(), ActivityTileChooser.class);
                        intent.putExtra("countryId", cid);
                        intent.putExtra("migrantName", migrants.get(getAdapterPosition()).getMigrantName());
                        CountryModel savedCountry = SQLDatabaseHelper.getInstance(itemView.getContext()).getCountry(cid);
                        Log.d("mylog", "Country name: " + savedCountry.getCountryName());
                        intent.putExtra("countryName", savedCountry.getCountryName().toUpperCase());
                        intent.putExtra("countryStatus", savedCountry.getCountrySatus());
                        intent.putExtra("migrantName", migrants.get(getAdapterPosition()).getMigrantName());
                        intent.putExtra("migrantPhone", migrants.get(getAdapterPosition()).getMigrantPhone());
                        intent.putExtra("migrantGender", migrants.get(getAdapterPosition()).getMigrantSex());
                        intent.putExtra("countryBlacklist", savedCountry.getCountryBlacklist());
                        itemView.getContext().startActivity(intent);
                    } else {
                        showCountryChooser(migrants.get(getAdapterPosition()).getMigrantName(),
                                ((AppCompatActivity) itemView.getContext()).getSupportFragmentManager());
                    }
                }
            });
        }
    }

    private void showCountryChooser(String name, FragmentManager fragmentManager) {
        DialogCountryChooser dialog = DialogCountryChooser.newInstance();
        dialog.setMigrantName(name);
        dialog.show(fragmentManager, "tag");
    }
}
