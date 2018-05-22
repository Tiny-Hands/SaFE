package com.vysh.subairoma.imageHelpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Created by Vishal on 3/26/2017.
 */

public class ImageEncoder {

    public static String encodeToBase64(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        byte[] byte_arr = bytes.toByteArray();
        return Base64.encodeToString(byte_arr, 0);
    }

    public static Bitmap decodeFromBase64(String string) {
        byte[] decodedString = Base64.decode(string, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }
}
