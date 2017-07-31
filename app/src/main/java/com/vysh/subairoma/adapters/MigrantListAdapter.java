package com.vysh.subairoma.adapters;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vysh.subairoma.ActivityTileHome;
import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.DialogCountryChooser;
import com.vysh.subairoma.R;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.models.CountryModel;
import com.vysh.subairoma.models.MigrantModel;

import java.util.ArrayList;

/**
 * Created by Vishal on 6/16/2017.
 */

public class MigrantListAdapter extends RecyclerView.Adapter<MigrantListAdapter.MigrantHolder> {

    public void setMigrants(ArrayList<MigrantModel> migrants) {
        this.migrants = migrants;
    }

    ArrayList<MigrantModel> migrants;

    @Override
    public MigrantHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MigrantHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.recycler_view_migrant_row, parent, false));
    }

    @Override
    public void onBindViewHolder(MigrantHolder holder, int position) {
        MigrantModel migrantModel = migrants.get(position);
        holder.textViewName.setText(migrantModel.getMigrantName());
        holder.sex.setText(migrantModel.getMigrantSex() + ", " + migrantModel.getMigrantAge());
    }

    @Override
    public int getItemCount() {
        if (migrants == null)
            return 7;
        else
            return migrants.size();
    }

    public class MigrantHolder extends RecyclerView.ViewHolder {
        public TextView textViewName, sex;

        public MigrantHolder(final View itemView) {
            super(itemView);
            textViewName = (TextView) itemView.findViewById(R.id.tvMigrantName);
            sex = (TextView) itemView.findViewById(R.id.tvMigrantAgeSex);
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
                        intent.putExtra("countryBlacklist", savedCountry.getCountryBlacklist());
                        itemView.getContext().startActivity(intent);
                    } else {
                        DialogCountryChooser dialog = DialogCountryChooser.newInstance();
                        dialog.setMigrantName(migrants.get(getAdapterPosition()).getMigrantName());
                        dialog.show(((AppCompatActivity) itemView.getContext()).getSupportFragmentManager(), "tag");
                    }
                }
            });
        }
    }
}
