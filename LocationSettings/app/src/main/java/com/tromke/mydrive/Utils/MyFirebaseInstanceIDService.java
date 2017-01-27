package com.tromke.mydrive.Utils;

import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by foda_ on 2017-01-25.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    DatabaseReference Reference;
    public final String TAG = "NOTIFICATION_TOKEN";
    public static int TOKEN_CHANGED = -1;
    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        Log.d(TAG, FirebaseInstanceId.getInstance().getToken());
        TOKEN_CHANGED = 1;
    }
}
