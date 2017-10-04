package com.vysh.subairoma.adapters;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
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
    SQLDatabaseHelper sqlDatabaseHelper;

    public TileAdapter(ArrayList list, int[] tiles) {
        tileList = list;
        ivTiles = tiles;
    }

    @Override
    public TileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_tile_row, parent, false);
        sqlDatabaseHelper = new SQLDatabaseHelper(parent.getContext());
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
        int errorCount = sqlDatabaseHelper.getTileErrorCount(ApplicationClass.getInstance().getMigrantId(),
                tileList.get(position).getTileId());
        Log.d("mylog", "Tile Errors: " + errorCount + " For tile ID: " + tileList.get(position).getTileId());
        if (errorCount == 1) {
            holder.tvErrorCount.setText(errorCount + " Error");
        } else if (errorCount > 1)
            holder.tvErrorCount.setText(errorCount + " Errors");
        setTileIcons(holder.ivTile, tileList.get(position).getTileId());
        //holder.ivTile.setBackgroundResource(ivTiles[position]);
    }

    private void setTileIcons(ImageView ivTile, int tileId) {
        switch (tileId) {
            case 0:
                ivTile.setImageResource(R.drawable.ic_travel_work);
                break;
            case 1:
                ivTile.setImageResource(R.drawable.ic_manpower);
                break;
            case 2:
                ivTile.setImageResource(R.drawable.ic_work);
                break;
            case 3:
                ivTile.setImageResource(R.drawable.ic_contract);
                break;
            case 4:
                ivTile.setImageResource(R.drawable.ic_cost);
                break;
            case 5:
                ivTile.setImageResource(R.drawable.ic_government);
                break;
            case 6:
                ivTile.setImageResource(R.drawable.ic_preparation);
                break;
            case 7:
                ivTile.setImageResource(R.drawable.ic_passport_visa);
                break;
            case 8:
                ivTile.setImageResource(R.drawable.ic_packing);
                break;
            case 9:
                ivTile.setImageResource(R.drawable.ic_travel);
                break;
            case 10:
                ivTile.setImageResource(R.drawable.ic_incountry);
                break;
            case 15:
                ivTile.setImageResource(R.drawable.ic_preparation);
                break;
            case 16:
                ivTile.setImageResource(R.drawable.ic_travel);
                break;
            default:
                ivTile.setImageResource(R.drawable.ic_default);
        }
    }

    @Override
    public int getItemCount() {
        return tileList.size();
    }

    public class TileViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTile;
        public ImageView ivTile;
        public View viewDisabled;
        public TextView tvErrorCount;

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
            tvErrorCount = (TextView) itemView.findViewById(R.id.tvErrorCount);
            viewDisabled = itemView.findViewById(R.id.viewDisabled);
        }
    }
}
