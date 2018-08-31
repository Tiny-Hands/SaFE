package com.vysh.subairoma.utils;

import android.content.Context;

import com.vysh.subairoma.SQLHelpers.SQLDatabaseHelper;
import com.vysh.subairoma.activities.ActivityTileHome;
import com.vysh.subairoma.models.TilesModel;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Vishal on 8/26/2018.
 */

public class PercentHelper {
    public static String getPercentCompleteBySection(Context context, int migId, String section) {
        SQLDatabaseHelper dbHelper = new SQLDatabaseHelper(context);
        float totalPercent = 0f;
        int tilesCount;

        ArrayList<TilesModel> tiles = new SQLDatabaseHelper(context).getTiles(section.toUpperCase());
        tilesCount = tiles.size();
        for (int i = 0; i < tilesCount; i++) {
            float perComplete = dbHelper.getPercentComplete(migId, tiles.get(i).getTileId());
            totalPercent += perComplete;
        }
        float percent = totalPercent / tilesCount;
        DecimalFormat decimalFormat = new DecimalFormat("##");
        return decimalFormat.format(percent);
    }

    public String getPercentCompleteByTile(int migId, int tileId, String section) {
        return "";
        //dbHelper.insertPercentComp(migId, (int) percent);
    }
}
