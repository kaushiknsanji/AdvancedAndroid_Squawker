/*
* Copyright (C) 2017 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*  	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package android.example.com.squawker.following;

import android.content.SharedPreferences;
import android.example.com.squawker.R;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;


/**
 * Shows the list of instructors you can follow
 */
// COMPLETED (1) Implement onSharedPreferenceChangeListener
public class FollowingPreferenceFragment extends PreferenceFragmentCompat
    implements SharedPreferences.OnSharedPreferenceChangeListener{

    private final static String LOG_TAG = FollowingPreferenceFragment.class.getSimpleName();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Add visualizer preferences, defined in the XML file in res->xml->preferences_squawker
        addPreferencesFromResource(R.xml.following_squawker);
    }

    // COMPLETED (2) When a SharedPreference changes, check which preference it is and subscribe or
    // un-subscribe to the correct topics.

    // Ex. FirebaseMessaging.getInstance().subscribeToTopic("key_lyla");
    // subscribes to Lyla's squawks.

    // HINT: Checkout res->xml->following_squawker.xml. Note how the keys for each of the
    // preferences matches the topic to subscribe to for each instructor.

    /**
     * Called when a shared preference is changed, added, or removed. This
     * may be called even if a preference is set to its existing value.
     * <p>
     * <p>This callback will be run on your main thread.
     *
     * @param sharedPreferences The {@link SharedPreferences} that received
     *                          the change.
     * @param key               The key of the preference that was changed, added, or
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, final String key) {
        //Find the Preference for the Key
        Preference preference = findPreference(key);
        if(preference != null && preference instanceof SwitchPreferenceCompat){
            //When the Preference is a SwitchPreferenceCompat

            //Get the state of SwitchPreferenceCompat
            boolean subscribe = sharedPreferences.getBoolean(key, false);

            if(subscribe){
                //Subscribe when True
                FirebaseMessaging.getInstance().subscribeToTopic(key)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(!task.isSuccessful()){
                                    Log.e(LOG_TAG, "onComplete: Subscribe to Topic failed for " + key, task.getException());
                                }
                                Log.d(LOG_TAG, "onComplete: Subscribed to Topic " + key + " successfully");
                            }
                        });
            } else {
                //UnSubscribe when False
                FirebaseMessaging.getInstance().unsubscribeFromTopic(key)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(!task.isSuccessful()){
                                    Log.e(LOG_TAG, "onComplete: UnSubscribe to Topic failed for " + key, task.getException());
                                }
                                Log.d(LOG_TAG, "onComplete: UnSubscribed to Topic " + key + " successfully");
                            }
                        });
            }
        }

    }


    // COMPLETED (3) Make sure to register and unregister this as a Shared Preference Change listener, in
    // onCreate and onDestroy.


    /**
     * Called when the fragment is visible to the user and actively running.
     * This is generally
     * tied to {@link android.support.v4.app.FragmentActivity#onResume() Activity.onResume} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onResume() {
        super.onResume();

        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Called when the Fragment is no longer resumed.  This is generally
     * tied to {@link android.support.v4.app.FragmentActivity#onPause() Activity.onPause} of the containing
     * Activity's lifecycle.
     */
    @Override
    public void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}
