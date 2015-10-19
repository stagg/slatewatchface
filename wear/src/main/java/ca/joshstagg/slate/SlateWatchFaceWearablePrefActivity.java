package ca.joshstagg.slate;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.Wearable;

import preference.WearPreferenceActivity;

/**
 * Slate ca.joshstagg.slate
 * Copyright 2015  Josh Stagg
 */
public class SlateWatchFaceWearablePrefActivity extends WearPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "SlateWatchFaceConfig";

    private GoogleApiClient mGoogleApiClient;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        Logger.d(TAG, "onConnected: " + connectionHint);
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                        Logger.d(TAG, "onConnectionSuspended: " + cause);
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Logger.d(TAG, "onConnectionFailed: " + result);
                    }
                })
                .addApi(Wearable.API)
        .build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        DataMap configKeysToOverwrite = new DataMap();
        switch(key) {
            case SlateWatchFaceUtil.KEY_SECONDS_COLOR:
                String color = sharedPreferences.getString(key, SlateWatchFaceUtil.COLOR_VALUE_STRING_DEFAULT);
                configKeysToOverwrite.putString(SlateWatchFaceUtil.KEY_SECONDS_COLOR, color);
                Logger.d(TAG, "Update config color:" + color);
                break;
            case SlateWatchFaceUtil.KEY_SHOW_DATE:
                boolean showDate = sharedPreferences.getBoolean(key, SlateWatchFaceUtil.SHOW_DATE_VALUE_DEFAULT);
                configKeysToOverwrite.putBoolean(SlateWatchFaceUtil.KEY_SHOW_DATE, showDate);
                Logger.d(TAG, "Update config show date:" + showDate);
                break;
            case SlateWatchFaceUtil.KEY_SMOOTH_MODE:
                boolean smoothMode = sharedPreferences.getBoolean(key, SlateWatchFaceUtil.SMOOTH_MODE_VALUE_DEFAULT);
                configKeysToOverwrite.putBoolean(SlateWatchFaceUtil.KEY_SMOOTH_MODE, smoothMode);
                Logger.d(TAG, "Update config smooth mode:" + smoothMode);
                break;
        }
        SlateWatchFaceUtil.overwriteKeysInConfigDataMap(mGoogleApiClient, configKeysToOverwrite);
    }
}
