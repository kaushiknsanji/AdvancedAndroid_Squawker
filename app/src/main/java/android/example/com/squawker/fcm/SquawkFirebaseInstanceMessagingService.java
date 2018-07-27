package android.example.com.squawker.fcm;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.example.com.squawker.MainActivity;
import android.example.com.squawker.R;
import android.example.com.squawker.provider.SquawkContract;
import android.example.com.squawker.provider.SquawkProvider;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * @author Kaushik N Sanji
 */
public class SquawkFirebaseInstanceMessagingService extends FirebaseMessagingService {
    private static final int SQUAWK_NOTIFICATION_ID = 100;
    private static final int SQUAWK_PENDING_INTENT_ID = SQUAWK_NOTIFICATION_ID;

    // COMPLETED (1) Make a new Service in the fcm package that extends from FirebaseMessagingService.

    // COMPLETED (2) As part of the new Service - Override onMessageReceived. This method will
    // be triggered whenever a squawk is received. You can get the data from the squawk
    // message using getData(). When you send a test message, this data will include the
    // following key/value pairs:
    // test: true
    // author: Ex. "TestAccount"
    // authorKey: Ex. "key_test"
    // message: Ex. "Hello world"
    // date: Ex. 1484358455343

    // COMPLETED (3) As part of the new Service - If there is message data, get the data using
    // the keys and do two things with it :
    // 1. Display a notification with the first 30 character of the message
    // 2. Use the content provider to insert a new message into the local database
    // Hint: You shouldn't be doing content provider operations on the main thread.
    // If you don't know how to make notifications or interact with a content provider
    // look at the notes in the classroom for help.

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //Retrieving the content from the RemoteMessage received
        Map<String, String> data = remoteMessage.getData();
        String author = data.get(SquawkContract.COLUMN_AUTHOR);
        String authorKey = data.get(SquawkContract.COLUMN_AUTHOR_KEY);
        String message = data.get(SquawkContract.COLUMN_MESSAGE);
        long date = Long.parseLong(data.get(SquawkContract.COLUMN_DATE));

        //Insert a record into the database for the RemoteMessage received
        addSquawkEntry(author, authorKey, message, date);

        //Display a Notification for the RemoteMessage received
        showNotification(author, message);
    }

    private void addSquawkEntry(final String author, final String authorKey, final String message, final long date) {

        //Executing insert in AsyncTask thread
        AsyncTask<Void, Void, Void> insertSquawkAsyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                //Preparing the ContentValues for inserting the data
                ContentValues contentValues = new ContentValues();
                contentValues.put(SquawkContract.COLUMN_AUTHOR, author);
                contentValues.put(SquawkContract.COLUMN_AUTHOR_KEY, authorKey);
                contentValues.put(SquawkContract.COLUMN_MESSAGE, message);
                contentValues.put(SquawkContract.COLUMN_DATE, date);
                getContentResolver().insert(SquawkProvider.SquawkMessages.CONTENT_URI, contentValues);
                return null;
            }
        };

        insertSquawkAsyncTask.execute();
    }

    private void showNotification(String author, String message) {
        //Retrieving the instance of NotificationManager to notify the user of the events
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //Building a Notification Channel for Android API level 26+
            NotificationChannel notificationChannel
                    = new NotificationChannel(getString(R.string.squawk_notification_channel_id),
                    getString(R.string.squawk_notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            //Registering the channel with the system
            notificationManager.createNotificationChannel(notificationChannel);
        }

        //Constructing the Notification Content with the NotificationCompat.Builder
        NotificationCompat.Builder notificationBuilder
                = new NotificationCompat.Builder(this, getString(R.string.squawk_notification_channel_id))
                .setSmallIcon(R.drawable.ic_duck)
                .setContentTitle(getString(R.string.message_token_format, author))
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setContentIntent(obtainPendingIntent())
                .setAutoCancel(true);

        //Setting a Priority that works for Android from API level 16+ to 26
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O){
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        }

        //Posting a Notification with the content built
        notificationManager.notify(
                SQUAWK_NOTIFICATION_ID,
                notificationBuilder.build()
        );
    }

    private PendingIntent obtainPendingIntent() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(
            this,
                SQUAWK_PENDING_INTENT_ID,
                notificationIntent,
                PendingIntent.FLAG_ONE_SHOT
        );
    }
}
