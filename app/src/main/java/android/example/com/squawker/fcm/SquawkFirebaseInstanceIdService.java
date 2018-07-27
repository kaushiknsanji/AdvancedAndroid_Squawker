package android.example.com.squawker.fcm;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;

/**
 * @author Kaushik N Sanji
 */
public class SquawkFirebaseInstanceIdService extends FirebaseMessagingService {
    // COMPLETED (1) Make a new package for your FCM service classes called "fcm"
    // COMPLETED (2) Create a new Service class that extends FirebaseInstanceIdService.
    // You'll need to implement the onTokenRefresh method. Simply have it print out
    // the new token.

    private static final String LOG_TAG = SquawkFirebaseInstanceIdService.class.getSimpleName();

    @Override
    public void onNewToken(String token) {
        Log.d(LOG_TAG, "onNewToken: Refreshed Token is " + token);
        super.onNewToken(token);
    }
}
