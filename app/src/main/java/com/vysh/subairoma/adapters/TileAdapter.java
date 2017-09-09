package com.vysh.subairoma.adapters;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vysh.subairoma.activities.ActivityTileHome;
import com.vysh.subairoma.activities.ActivityTileQuestions;
import com.vysh.subairoma.R;
import com.vysh.subairoma.models.TilesModel;

import java.util.ArrayList;

/**
 * Created by Vishal on 6/12/2017.
 */

public class TileAdapter extends RecyclerView.Adapter<TileAdapter.TileViewHolder> {

    ArrayList<TilesModel> tileList;
    int[] ivTiles;

    public TileAdapter(ArrayList list, int[] tiles) {
        tileList = list;
        ivTiles = tiles;
    }

    @Override
    public TileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_tile_row, parent, false);
        return new TileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TileViewHolder holder, final int position) {
        holder.tvTile.setText(tileList.get(position).getTitle());
        if (ActivityTileHome.finalSection) {
            if (tileList.get(position).getType().equalsIgnoreCase("GAS")) {
                holder.viewDisabled.setVisibility(View.GONE);
            } else {
                holder.viewDisabled.setVisibility(View.VISIBLE);
            }
        }
        //holder.ivTile.setBackgroundResource(ivTiles[position]);
    }

    @Override
    public int getItemCount() {
        return tileList.size();
    }

    public class TileViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTile;
        public ImageView ivTile;
        public View viewDisabled;

        public TileViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (ActivityTileHome.finalSection) {
                        if (tileList.get(getAdapterPosition()).getType().equalsIgnoreCase("GAS")) {
                            Intent intent = new Intent(v.getContext(), ActivityTileQuestions.class);
                            intent.putExtra("tileId", tileList.get(getAdapterPosition()).getTileId());
                            intent.putExtra("tileName", tileList.get(getAdapterPosition()).getTitle());
                            v.getContext().startActivity(intent);
                        }
                    } else {
                        Intent intent = new Intent(v.getContext(), ActivityTileQuestions.class);
                        intent.putExtra("tileId", tileList.get(getAdapterPosition()).getTileId());
                        intent.putExtra("tileName", tileList.get(getAdapterPosition()).getTitle());
                        v.getContext().startActivity(intent);
                    }
                }
            });
            tvTile = (TextView) itemView.findViewById(R.id.tvTitle);
            ivTile = (ImageView) itemView.findViewById(R.id.ivTitle);
            viewDisabled = itemView.findViewById(R.id.viewDisabled);
        }
    }
}
