package com.vysh.subairoma.imageHelpers;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by Vishal on 7/19/2018.
 */

public class ImageRotator {
    public static Bitmap getBitmapRotatedByDegree(Bitmap bitmap, int rotationDegree) {
        Matrix matrix = new Matrix();
        matrix.preRotate(rotationDegree);

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
