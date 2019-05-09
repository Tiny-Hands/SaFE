package com.vysh.subairoma.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
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

import pl.droidsonroids.gif.GifImageView;

/**
 * Created by Vishal on 6/12/2017.
 */

public class TileAdapter extends RecyclerView.Adapter<TileAdapter.TileViewHolder> {
    ArrayList<TilesModel> tileList;
    int[] ivTiles;
    SQLDatabaseHelper sqlDatabaseHelper;
    Context context;
    String cid;

    public TileAdapter(ArrayList<TilesModel> list, int[] tiles, Context cxt, String countryId) {
        tileList = list;
        ivTiles = tiles;
        context = cxt;
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
        int tileId = tileList.get(position).getTileId();
        holder.tvTile.setText(tileList.get(position).getTitle());

        if (ActivityTileHome.finalSection) {
            if (tileList.get(position).getType().equalsIgnoreCase("GAS")) {
                holder.viewDisabled.setVisibility(View.GONE);
            } else {
                holder.viewDisabled.setVisibility(View.VISIBLE);
            }
        } else if (ActivityTileHome.showIndia) {
            holder.viewDisabled.setVisibility(View.GONE);
        }

        int errorCount = sqlDatabaseHelper.getTileErrorCount(ApplicationClass.getInstance().getMigrantId(),
                tileId);
        Log.d("mylog", "Tile Errors: " + errorCount + " For tile ID: " + tileId);


        int total = sqlDatabaseHelper.getQuestions(tileId).size();
        int answered = sqlDatabaseHelper.getTileResponse(ApplicationClass.getInstance().getMigrantId(), tileId);
        String completeText = answered + " " + context.getResources().getString(R.string.answered) + " /" + total + " " + context.getResources().getString(R.string.questions);
        if (errorCount > 0) {
            for (int i = 0; i < errorCount; i++) {
                ImageView imgView = new ImageView(holder.llErrorLayout.getContext());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams((int) context.getResources().getDimension(R.dimen.redflag_dimen),
                        (int) context.getResources().getDimension(R.dimen.redflag_dimen));
                imgView.setLayoutParams(lp);
                imgView.setImageResource(R.drawable.ic_redflag);
                holder.llErrorLayout.addView(imgView);
            }
            holder.tvCompletionStatus.setBackgroundResource(R.color.colorError);
        }
        //DecimalFormat decimalFormat = new DecimalFormat("##");
        else if (tileList.get(position).getPercentComplete() > 99.9) {
            completeText = "COMPLETED";
            holder.ivDone.setVisibility(View.VISIBLE);
            Animation animZoomin = AnimationUtils.loadAnimation(context, R.anim.zoomin);
            animZoomin.setInterpolator(new BounceInterpolator());
            holder.ivDone.startAnimation(animZoomin);
            holder.tvCompletionStatus.setBackgroundResource(R.color.darkGreen);
            //holder.tvPercent.setVisibility(View.GONE);
        } else {
            holder.tvCompletionStatus.setBackgroundResource(R.color.grey);
        }

        holder.tvCompletionStatus.setText(completeText);

        //int noRedflagQuestionCount = sqlDatabaseHelper.getNoRedFlagQuestionsCount(tileId);

        setTileIcons(holder.ivTile, tileId);
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
            case 18:
                ivTile.setImageResource(R.drawable.ic_travel_work);
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
        public TextView tvTile, tvCompletionStatus;
        public ImageView ivTile, ivDone;
        public View viewDisabled;
        public LinearLayout llErrorLayout;
        public GifImageView gifImageView;

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
                        Intent intent = new Intent(v.getContext(), ActivityTileQuestions.class);
                        intent.putExtra("tileId", tileList.get(getAdapterPosition()).getTileId());
                        intent.putExtra("tileName", tileList.get(getAdapterPosition()).getTitle());
                        intent.putExtra("iconId", tileList.get(getAdapterPosition()).getTileId());
                        v.getContext().startActivity(intent);
                    }
                }
            });
            //tvPercent = itemView.findViewById(R.id.tvPercent);
            tvCompletionStatus = itemView.findViewById(R.id.tvCompletionStatus);
            tvTile = itemView.findViewById(R.id.tvTitle);
            ivTile = itemView.findViewById(R.id.ivTitle);
            ivDone = itemView.findViewById(R.id.ivDone);
            gifImageView = itemView.findViewById(R.id.gifView);
            viewDisabled = itemView.findViewById(R.id.viewDisabled);
            llErrorLayout = itemView.findViewById(R.id.llRedflags);
        }
    }
}
