package com.vysh.subairoma;
import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.messaging.FirebaseMessagingService;

public class MyFirebaseMessagingActivity extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.d("mylog", "Refreshed token: " + s);

    }
}
