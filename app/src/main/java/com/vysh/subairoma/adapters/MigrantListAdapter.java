package com.vysh.subairoma.adapters;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vysh.subairoma.activities.ActivityMigrantList;
import com.vysh.subairoma.activities.ActivityTileHome;
import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.dialogs.DialogCountryChooser;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
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
        String tsex = migrantModel.getMigrantSex();
        if (tsex.equalsIgnoreCase("male")) {
            tsex = mContext.getResources().getString(R.string.male);
        } else if (tsex.equalsIgnoreCase("female")) {
            tsex = mContext.getResources().getString(R.string.female);
        }
        holder.sex.setText(tsex + ", " + migrantModel.getMigrantAge());
        if (migrantModel.getMigrantSex().equalsIgnoreCase("female"))
            holder.ivAvatar.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_female));

        //Showing going to country
        String cid = new SQLDatabaseHelper(mContext).getResponse(migrants.get(position).getMigrantId(), "mg_destination");
        if (cid == null || cid.isEmpty()) {
            holder.tvGoingCountry.setText(mContext.getResources().getString(R.string.not_started));
        } else {
            CountryModel goingTo = new SQLDatabaseHelper(mContext).getCountry(cid);
            holder.tvGoingCountry.setText(goingTo.getCountryName().toUpperCase());
            if (goingTo.getCountryBlacklist() == 1)
                holder.tvGoingCountry.setTextColor(mContext.getResources().getColor(R.color.colorError));
            else if (goingTo.getCountrySatus() == 1)
                holder.tvGoingCountry.setTextColor(mContext.getResources().getColor(R.color.colorNeutral));
            else
                holder.tvGoingCountry.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));

            //Showing error count
            int errorCount = new SQLDatabaseHelper(mContext).getMigrantErrorCount(migrantModel.getMigrantId());

            if (errorCount > 0 && (holder.llErrorLayout.getTag()) == null) {
                for (int i = 0; i < errorCount; i++) {
                    ImageView imgView = new ImageView(holder.llErrorLayout.getContext());
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) holder.llErrorLayout.getContext().getResources().getDimension(R.dimen.redflag_dimen),
                            (int) holder.llErrorLayout.getContext().getResources().getDimension(R.dimen.redflag_dimen));
                    imgView.setLayoutParams(lp);
                    imgView.setImageResource(R.drawable.ic_redflag);
                    holder.llErrorLayout.addView(imgView);
                }
                holder.llErrorLayout.setTag("shownerror");
            }
        }
        if (migrants.size() == 1) {
            showCountryChooser(migrants.get(0).getMigrantName(), ((AppCompatActivity) mContext).getSupportFragmentManager());
        }

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

    public class MigrantHolder extends RecyclerView.ViewHolder {
        public TextView textViewName, sex, tvGoingCountry;
        public SmartTextView tvPhone;
        public ImageView ivAvatar;
        public LinearLayout llErrorLayout;
        public RelativeLayout viewForeground;

        public MigrantHolder(final View itemView) {
            super(itemView);
            tvPhone = itemView.findViewById(R.id.tvPhone);
            textViewName = (TextView) itemView.findViewById(R.id.tvMigrantName);
            tvGoingCountry = (TextView) itemView.findViewById(R.id.tvCountryGoing);
            ivAvatar = (ImageView) itemView.findViewById(R.id.ivUserLogo);
            llErrorLayout = (LinearLayout) itemView.findViewById(R.id.llRedflags);
            sex = (TextView) itemView.findViewById(R.id.tvMigrantAgeSex);
            viewForeground = itemView.findViewById(R.id.viewForeground);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int migId = migrants.get(getAdapterPosition()).getMigrantId();
                    ApplicationClass.getInstance().setMigrantId(migId);
                    String cid = new SQLDatabaseHelper(itemView.getContext()).getResponse(migId, "mg_destination");
                    Log.d("mylog", "Country ID: " + cid);
                    if (cid != null && !cid.isEmpty()) {
                        Intent intent = new Intent(itemView.getContext(), ActivityTileHome.class);
                        intent.putExtra("countryId", cid);
                        intent.putExtra("migrantName", migrants.get(getAdapterPosition()).getMigrantName());
                        CountryModel savedCountry = new SQLDatabaseHelper(itemView.getContext()).getCountry(cid);
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
                                ((AppCompatActivity) itemView.getContext()).getSupportFragmentManager())
                        ;
                    }
                }
            });
        }
    }

    private void showCountryChooser(String name, android.support.v4.app.FragmentManager fragmentManager) {
        DialogCountryChooser dialog = DialogCountryChooser.newInstance();
        dialog.setMigrantName(name);
        dialog.show(fragmentManager, "tag");
    }
}
