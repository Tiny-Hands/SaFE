package com.vysh.subairoma.adapters;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vysh.subairoma.ActivityTileHome;
import com.vysh.subairoma.R;
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
        return migrants.size()-1;
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
                    Intent intent = new Intent(itemView.getContext(), ActivityTileHome.class);
                    itemView.getContext().startActivity(intent);
                }
            });
        }
    }
}
