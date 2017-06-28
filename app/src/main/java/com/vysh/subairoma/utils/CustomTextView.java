package com.vysh.subairoma.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.vysh.subairoma.R;

/**
 * Created by Vishal on 6/12/2017.
 */

public class CustomTextView extends TextView {

    public CustomTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyCustomFont(context, attrs);

    }
    private void applyCustomFont(Context context, AttributeSet attrs) {
        TypedArray attributeArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.CustomTextView);

        String fontName = attributeArray.getString(R.styleable.CustomTextView_font);
        Typeface customFont = selectTypeface(context, fontName);
        setTypeface(customFont);
        attributeArray.recycle();
    }

    private Typeface selectTypeface(Context context, String fontName) {
        try {
            return FontCache.getTypeface(fontName, context);
        }
        catch (Exception e)
        {
            //Font specified in XML not found in the resources
            return null;
        }
    }
}
