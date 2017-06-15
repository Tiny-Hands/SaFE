package com.vysh.subairoma.utils;

import android.content.Context;
import android.graphics.Typeface;

import java.util.HashMap;

/**
 * Created by Vishal on 6/12/2017.
 */

public class FontCache  {

    private static HashMap<String, Typeface> fontMap = new HashMap<>();

    public static Typeface getTypeface(String fontName, Context context){
        if (fontMap.containsKey(fontName)){
            return fontMap.get(fontName);
        }
        else {
            Typeface tf = Typeface.createFromAsset(context.getAssets(), "fonts/"+fontName);
            fontMap.put(fontName, tf);
            return tf;
        }
    }
}
