package com.vysh.subairoma.adapters;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vysh.subairoma.ActivityTileHome;
import com.vysh.subairoma.R;

/**
 * Created by Vishal on 6/16/2017.
 */

public class MigrantListAdapter extends RecyclerView.Adapter<MigrantListAdapter.MigrantHolder> {
    @Override
    public MigrantHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new MigrantHolder(LayoutInflater.from(parent.getContext()).
                inflate(R.layout.recycler_view_migrant_row, parent, false));
    }

    @Override
    public void onBindViewHolder(MigrantHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 8;
    }

    public class MigrantHolder extends RecyclerView.ViewHolder {

        public MigrantHolder(final View itemView) {
            super(itemView);
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
