package com.vysh.subairoma.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.vysh.subairoma.ApplicationClass;
import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.activities.ActivityImportantContacts;
import com.vysh.subairoma.activities.ActivityTileHome;
import com.vysh.subairoma.activities.ActivityTileQuestions;
import com.vysh.subairoma.R;
import com.vysh.subairoma.models.TilesModel;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Vishal on 6/12/2017.
 */

public class TileAdapter extends RecyclerView.Adapter<TileAdapter.TileViewHolder> {
    ArrayList<TilesModel> tileList;
    int[] ivTiles;
    SQLDatabaseHelper sqlDatabaseHelper;
    Context context;
    int impContactsPlace;
    String cid;
    int offset = 0;

    public TileAdapter(ArrayList list, int impContactsPlace, int[] tiles, Context cxt, String countryId) {
        tileList = list;
        ivTiles = tiles;
        context = cxt;
        this.impContactsPlace = impContactsPlace;
        cid = countryId;
    }

    @Override
    public TileViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_tile_row, parent, false);
        sqlDatabaseHelper = new SQLDatabaseHelper(parent.getContext());
        return new TileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TileViewHolder holder, final int position) {
        //Show Important contacts tile at the bottom
        if ((position == impContactsPlace) || (position == tileList.size() && ActivityTileHome.showIndia)) {
            holder.tvTile.setText(context.getResources().getString(R.string.important_contacts));
            holder.ivTile.setImageResource(R.drawable.ic_phonebook);
            holder.tvPercent.setVisibility(View.GONE);
            offset = 1;
        } else if (position <= tileList.size()) {
            holder.tvTile.setText(tileList.get(position - offset).getTitle());
            if (ActivityTileHome.finalSection) {
                if (tileList.get(position - offset).getType().equalsIgnoreCase("GAS")) {
                    holder.viewDisabled.setVisibility(View.GONE);
                } else {
                    holder.viewDisabled.setVisibility(View.VISIBLE);
                }
            } else {
                if (tileList.get(position - offset).getType().equals("FEP")) {
                    holder.viewDisabled.setVisibility(View.GONE);
                } else
                    holder.viewDisabled.setVisibility(View.VISIBLE);
            }
            int errorCount = sqlDatabaseHelper.getTileErrorCount(ApplicationClass.getInstance().getMigrantId(),
                    tileList.get(position - offset).getTileId());
            Log.d("mylog", "Tile Errors: " + errorCount + " For tile ID: " + tileList.get(position - offset).getTileId());
            if (errorCount > 0) {
                for (int i = 0; i < errorCount; i++) {
                    ImageView imgView = new ImageView(holder.llErrorLayout.getContext());
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) context.getResources().getDimension(R.dimen.redflag_dimen),
                            (int) context.getResources().getDimension(R.dimen.redflag_dimen));
                    imgView.setLayoutParams(lp);
                    imgView.setImageResource(R.drawable.ic_redflag);
                    holder.llErrorLayout.addView(imgView);
                }
            }
            DecimalFormat decimalFormat = new DecimalFormat("##");
            if (tileList.get(position - offset).getPercentComplete() > 99.9) {
                holder.progressPercent.setVisibility(View.GONE);
                holder.ivDone.setVisibility(View.VISIBLE);
                holder.tvPercent.setVisibility(View.GONE);
            } else {
                holder.progressPercent.setProgress((int) tileList.get(position - offset).getPercentComplete());
                holder.tvPercent.setText(decimalFormat.format(tileList.get(position - offset).getPercentComplete()));
            }
            setTileIcons(holder.ivTile, tileList.get(position - offset).getTileId());
            //holder.ivTile.setBackgroundResource(ivTiles[position]);
        }
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
        //Cuz showing extra tile in the bottom of GAS
        /*if (ActivityTileHome.finalSection || ActivityTileHome.showIndia) {
            return tileList.size() + 1;
        } else*/
        return tileList.size() + 1;
    }

    public class TileViewHolder extends RecyclerView.ViewHolder {
        public TextView tvTile, tvPercent;
        public ImageView ivTile, ivDone;
        public View viewDisabled;
        public ProgressBar progressPercent;
        public LinearLayout llErrorLayout;

        public TileViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getAdapterPosition() == impContactsPlace) {
                        Intent impIntent = new Intent(context.getApplicationContext(), ActivityImportantContacts.class);
                        impIntent.putExtra("countryId", cid);
                        context.startActivity(impIntent);
                    } else {
                        if (ActivityTileHome.finalSection) {
                            if (tileList.get(getAdapterPosition()).getType().equalsIgnoreCase("GAS")) {
                                Intent intent = new Intent(v.getContext(), ActivityTileQuestions.class);
                                intent.putExtra("tileId", tileList.get(getAdapterPosition()).getTileId());
                                intent.putExtra("tileName", tileList.get(getAdapterPosition()).getTitle());
                                intent.putExtra("iconId", tileList.get(getAdapterPosition()).getTileId());
                                v.getContext().startActivity(intent);
                            } else {
                                Intent intent = new Intent(v.getContext(), ActivityTileQuestions.class);
                                intent.putExtra("tileId", tileList.get(getAdapterPosition()).getTileId());
                                intent.putExtra("tileName", tileList.get(getAdapterPosition()).getTitle());
                                intent.putExtra("stateDisabled", true);
                                intent.putExtra("iconId", tileList.get(getAdapterPosition()).getTileId());
                                v.getContext().startActivity(intent);
                            }
                        } else {
                            if (getAdapterPosition() >= impContactsPlace)
                                return;
                            Intent intent = new Intent(v.getContext(), ActivityTileQuestions.class);
                            intent.putExtra("tileId", tileList.get(getAdapterPosition()).getTileId());
                            intent.putExtra("tileName", tileList.get(getAdapterPosition()).getTitle());
                            intent.putExtra("iconId", tileList.get(getAdapterPosition()).getTileId());
                            v.getContext().startActivity(intent);
                        }
                    }
                }
            });
            progressPercent = itemView.findViewById(R.id.progressPercent);
            tvPercent = itemView.findViewById(R.id.tvPercent);
            tvTile = itemView.findViewById(R.id.tvTitle);
            ivTile = itemView.findViewById(R.id.ivTitle);
            ivDone = itemView.findViewById(R.id.ivDone);
            viewDisabled = itemView.findViewById(R.id.viewDisabled);
            llErrorLayout = itemView.findViewById(R.id.llRedflags);
        }
    }
}
