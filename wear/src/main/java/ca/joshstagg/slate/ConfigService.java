package ca.joshstagg.slate;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.ResultTransform;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.TransformedResult;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.concurrent.atomic.AtomicInteger;

import static ca.joshstagg.slate.Constants.PATH_WITH_FEATURE;
import static ca.joshstagg.slate.Constants.SCHEME_WEAR;

public class ConfigService implements DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "ConfigService";

    private final Context mContext;
    private final Config mConfig;
    private final GoogleApiClient mGoogleApiClient;
    private final AtomicInteger mConnected = new AtomicInteger(0);

    ConfigService(Context context) {
        mContext = context;
        mConfig = new Config();
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Wearable.DataApi.addListener(mGoogleApiClient, this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(@NonNull Status status) {
                        updateConfigOnConnected();
                    }
                });

    }

    @Override
    public void onConnectionSuspended(int cause) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
    }

    @Override // DataApi.DataListener
    public void onDataChanged(DataEventBuffer dataEvents) {
        try {
            for (DataEvent dataEvent : dataEvents) {
                if (dataEvent.getType() != DataEvent.TYPE_CHANGED) {
                    continue;
                }

                DataItem dataItem = dataEvent.getDataItem();
                if (!dataItem.getUri().getPath().equals(PATH_WITH_FEATURE)) {
                    continue;
                }

                DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
                DataMap config = dataMapItem.getDataMap();
                Logger.d(TAG, "Config DataItem updated:" + config);
                updateConfig(config);
            }
        } finally {
            dataEvents.release();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        DataMap configKeysToOverwrite = new DataMap();
        switch (key) {
            case Constants.KEY_SECONDS_COLOR:
                String color = sharedPreferences.getString(key, Constants.COLOR_STRING_DEFAULT);
                configKeysToOverwrite.putString(Constants.KEY_SECONDS_COLOR, color);
                Logger.d(TAG, "Update config color:" + color);
                break;
            case Constants.KEY_SMOOTH_MODE:
                boolean smoothMode = sharedPreferences.getBoolean(key, Constants.SMOOTH_MOVEMENT_DEFAULT);
                configKeysToOverwrite.putBoolean(Constants.KEY_SMOOTH_MODE, smoothMode);
                Logger.d(TAG, "Update config smooth mode:" + smoothMode);
                break;
            default:
                return;
        }
        overwriteKeysInConfigDataMap(configKeysToOverwrite);
    }

    Config getConfig() {
        return mConfig;
    }

    synchronized void connect() {
        mConnected.incrementAndGet();
        if (null != mGoogleApiClient && !mGoogleApiClient.isConnected() && !mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
        PreferenceManager.getDefaultSharedPreferences(mContext).registerOnSharedPreferenceChangeListener(this);
    }

    synchronized void disconnect() {
        int connected = mConnected.decrementAndGet();
        if (connected < 1) {
            if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                Wearable.DataApi.removeListener(mGoogleApiClient, this);
                mGoogleApiClient.disconnect();
            }
            PreferenceManager.getDefaultSharedPreferences(mContext).unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    private void updateConfigOnConnected() {
        fetchConfigDataMap()
                .andFinally(new DataItemResultCallback() {
                    @Override
                    void onSuccess(@NonNull DataMap startupConfig) {
                        setDefaultValuesForMissingConfigKeys(startupConfig);
                        putConfigDataItem(startupConfig);
                        updateConfig(startupConfig);
                    }
                });
    }

    /**
     * If the DataItem hasn't been created yet or some keys are missing, use the default values.
     *
     * @param config The current DataMap
     */
    private void setDefaultValuesForMissingConfigKeys(DataMap config) {
        addStringKeyIfMissing(config, Constants.KEY_SECONDS_COLOR, Constants.COLOR_STRING_DEFAULT);
        addBoolKeyIfMissing(config, Constants.KEY_SMOOTH_MODE, Constants.SMOOTH_MOVEMENT_DEFAULT);
    }

    private void updateConfig(final DataMap config) {
        for (String configKey : config.keySet()) {
            if (!config.containsKey(configKey)) {
                continue;
            }
            updateConfigForKey(configKey, config);
        }
    }

    // Data Api

    /**
     * Updates the color of a UI item according to the given {@code configKey}. Does nothing if
     * {@code configKey} isn't recognized.
     */
    private void updateConfigForKey(String key, DataMap config) {
        switch (key) {
            case Constants.KEY_SECONDS_COLOR:
                String colorStr = config.getString(key);
                if (null != colorStr) {
                    int color = Color.parseColor(config.getString(key));
                    Logger.d(TAG, "Found watch face config key: " + key + " -> " + Integer.toHexString(color));
                    mConfig.setAccentColor(color);
                }
                break;
            case Constants.KEY_SMOOTH_MODE:
                mConfig.setSmoothMode(config.getBoolean(key));
                break;
            default:
                Logger.w(TAG, "Ignoring unknown config key: " + key);
        }
    }

    /**
     * Overwrites (or sets, if not present) the keys in the current config {@link DataItem} with
     * the ones appearing in the given {@link DataMap}. If the config DataItem doesn't exist,
     * it's created.
     * <p>
     * It is allowed that only some of the keys used in the config DataItem appear in
     * {@code configKeysToOverwrite}. The rest of the keys remains unmodified in this case.
     */
    private void overwriteKeysInConfigDataMap(final DataMap configKeysToOverwrite) {
        fetchConfigDataMap()
                .andFinally(new DataItemResultCallback() {
                    @Override
                    void onSuccess(@NonNull DataMap currentConfig) {
                        DataMap overwrittenConfig = new DataMap();
                        overwrittenConfig.putAll(currentConfig);
                        overwrittenConfig.putAll(configKeysToOverwrite);
                        putConfigDataItem(overwrittenConfig);
                    }
                });
    }

    /**
     * Asynchronously fetches the current config {@link DataMap} for {@link SlateWatchFaceService}
     * and passes it to the given callback.
     * <p>
     * If the current config {@link DataItem} doesn't exist, it isn't created and the callback
     * receives an empty DataMap.
     */
    private TransformedResult<DataApi.DataItemResult> fetchConfigDataMap() {
        return Wearable.NodeApi
                .getLocalNode(mGoogleApiClient)
                .then(new ResultTransform<NodeApi.GetLocalNodeResult, DataApi.DataItemResult>() {
                    @Nullable
                    @Override
                    public PendingResult<DataApi.DataItemResult> onSuccess(@NonNull NodeApi.GetLocalNodeResult getLocalNodeResult) {
                        String localNode = getLocalNodeResult.getNode().getId();
                        Uri uri = new Uri.Builder()
                                .scheme(SCHEME_WEAR)
                                .path(PATH_WITH_FEATURE)
                                .authority(localNode)
                                .build();
                        return Wearable.DataApi.getDataItem(mGoogleApiClient, uri);
                    }
                });
    }

    private void addStringKeyIfMissing(DataMap config, String key, String value) {
        if (!config.containsKey(key)) {
            config.putString(key, value);
        }
    }

    private void addBoolKeyIfMissing(DataMap config, String key, boolean value) {
        if (!config.containsKey(key)) {
            config.putBoolean(key, value);
        }
    }

    /**
     * Overwrites the current config {@link DataItem}'s {@link DataMap} with {@code newConfig}.
     * If the config DataItem doesn't exist, it's created.
     */
    private void putConfigDataItem(DataMap newConfig) {
        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(PATH_WITH_FEATURE);
        DataMap configToPut = putDataMapRequest.getDataMap();
        configToPut.putAll(newConfig);
        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataMapRequest.asPutDataRequest());
    }

    private abstract class DataItemResultCallback extends ResultCallbacks<DataApi.DataItemResult> {
        @Override
        public void onSuccess(@NonNull DataApi.DataItemResult dataItemResult) {
            DataMap dataMap = new DataMap();
            if (dataItemResult.getStatus().isSuccess() && dataItemResult.getDataItem() != null) {
                DataItem configDataItem = dataItemResult.getDataItem();
                DataMapItem dataMapItem = DataMapItem.fromDataItem(configDataItem);
                dataMap = dataMapItem.getDataMap();
            }
            onSuccess(dataMap);
        }

        abstract void onSuccess(@NonNull DataMap dataMap);

        @Override
        public void onFailure(@NonNull Status status) {
            // no-op
        }
    }
}
