package com.vysh.subairoma.adapters;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vysh.subairoma.ActivityTileQuestions;
import com.vysh.subairoma.R;

/**
 * Created by Vishal on 6/12/2017.
 */

public class TileAdapter extends RecyclerView.Adapter<TileAdapter.TileViewHolder> {

    String[] tileList;
    int[] ivTiles;

    public TileAdapter(String[] list, int[] tiles) {
        tileList = list;
        ivTiles = tiles;
    }

    @Override
    public TileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_tile_row, parent, false);
        return new TileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TileViewHolder holder, int position) {
        holder.tvTile.setText(tileList[position]);
        holder.ivTile.setBackgroundResource(ivTiles[position]);
    }

    @Override
    public int getItemCount() {
        return tileList.length;
    }

    public class TileViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTile;
        public ImageView ivTile;
        public TileViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ActivityTileQuestions.class);
                    v.getContext().startActivity(intent);
                }
            });
            tvTile = (TextView)itemView.findViewById(R.id.tvTitle);
            ivTile = (ImageView)itemView.findViewById(R.id.ivTitle);
        }
    }
}
