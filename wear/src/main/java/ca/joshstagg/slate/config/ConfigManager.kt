package ca.joshstagg.slate.config

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import ca.joshstagg.slate.Constants
import ca.joshstagg.slate.Constants.PATH_WITH_FEATURE
import ca.joshstagg.slate.Constants.SCHEME_WEAR
import ca.joshstagg.slate.Logger
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.*
import com.google.android.gms.wearable.*
import java.util.concurrent.atomic.AtomicInteger

class ConfigManager internal constructor(private val context: Context) :
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    internal val config: Config = Config()
    private val connected = AtomicInteger(0)
    private val googleApiClient: GoogleApiClient = GoogleApiClient.Builder(context)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(Wearable.API)
            .build()

    override fun onConnected(connectionHint: Bundle?) {
        Wearable.DataApi.addListener(googleApiClient, this)
                .setResultCallback { updateConfigOnConnected() }
    }

    override fun onConnectionSuspended(cause: Int) {}

    override fun onConnectionFailed(result: ConnectionResult) {}

    override // DataApi.DataListener
    fun onDataChanged(dataEvents: DataEventBuffer) {
        try {
            for (dataEvent in dataEvents) {
                if (dataEvent.type != DataEvent.TYPE_CHANGED) {
                    continue
                }

                val dataItem = dataEvent.dataItem
                if (dataItem.uri.path != PATH_WITH_FEATURE) {
                    continue
                }

                val dataMapItem = DataMapItem.fromDataItem(dataItem)
                val config = dataMapItem.dataMap
                Logger.d(TAG, "Config DataItem updated:" + config)
                updateConfig(config)
            }
        } finally {
            dataEvents.release()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val configKeysToOverwrite = DataMap()
        when (key) {
            Constants.KEY_SECONDS_COLOR -> {
                val color= sharedPreferences.getString(key, Constants.ACCENT_COLOR_STRING_DEFAULT)
                configKeysToOverwrite.putString(Constants.KEY_SECONDS_COLOR, color)
                Logger.d(TAG, "Update config color:" + color)
            }
            Constants.KEY_SMOOTH_MODE -> {
                val smoothMode = sharedPreferences.getBoolean(key, Constants.SMOOTH_MOVEMENT_DEFAULT)
                configKeysToOverwrite.putBoolean(Constants.KEY_SMOOTH_MODE, smoothMode)
                Logger.d(TAG, "Update config smooth mode:" + smoothMode)
            }
            else -> return
        }
        overwriteKeysInConfigDataMap(configKeysToOverwrite)
    }

    @Synchronized internal fun connect() {
        connected.incrementAndGet()
        if (!googleApiClient.isConnected && !googleApiClient.isConnecting) {
            googleApiClient.connect()
        }
        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this)
    }

    @Synchronized internal fun disconnect() {
        val connected = connected.decrementAndGet()
        if (connected < 1) {
            if (googleApiClient.isConnected) {
                Wearable.DataApi.removeListener(googleApiClient, this)
                googleApiClient.disconnect()
            }
            PreferenceManager.getDefaultSharedPreferences(context).unregisterOnSharedPreferenceChangeListener(this)
        }
    }

    private fun updateConfigOnConnected() {
        fetchConfigDataMap()
                .andFinally(object : DataItemResultCallback() {
                    override fun onSuccess(dataMap: DataMap) {
                        setDefaultValuesForMissingConfigKeys(dataMap)
                        updateConfig(dataMap)
                        putConfigDataItem(dataMap)
                    }
                })
    }

    /**
     * If the DataItem hasn't been created yet or some keys are missing, use the default values.

     * @param config The current DataMap
     */
    private fun setDefaultValuesForMissingConfigKeys(config: DataMap) {
        addStringKeyIfMissing(config, Constants.KEY_SECONDS_COLOR, Constants.ACCENT_COLOR_STRING_DEFAULT)
        addBoolKeyIfMissing(config, Constants.KEY_SMOOTH_MODE, Constants.SMOOTH_MOVEMENT_DEFAULT)
    }

    private fun updateConfig(config: DataMap) {
        for (configKey in config.keySet()) {
            updateConfigForKey(configKey, config)
        }
    }

    // Data Api

    /**
     * Updates the color of a UI item according to the given `configKey`. Does nothing if
     * `configKey` isn't recognized.
     */
    private fun updateConfigForKey(key: String, config: DataMap) {
        when (key) {
            Constants.KEY_SECONDS_COLOR -> {
                val colorStr = config.getString(key)
                if (null != colorStr) {
                    val color = Color.parseColor(config.getString(key))
                    Logger.d(TAG, "Found watch face config key: " + key + " -> " + Integer.toHexString(color))
                    this.config.accentColor = color
                }
            }
            Constants.KEY_SMOOTH_MODE -> this.config.smoothMovement = config.getBoolean(key)
            else -> Logger.d(TAG, "Ignoring unknown config key: " + key)
        }
    }

    /**
     * Overwrites (or sets, if not present) the keys in the current config [DataItem] with
     * the ones appearing in the given [DataMap]. If the config DataItem doesn't exist,
     * it's created.
     *
     *
     * It is allowed that only some of the keys used in the config DataItem appear in
     * `configKeysToOverwrite`. The rest of the keys remains unmodified in this case.
     */
    private fun overwriteKeysInConfigDataMap(configKeysToOverwrite: DataMap) {
        fetchConfigDataMap()
                .andFinally(object : DataItemResultCallback() {
                    override fun onSuccess(dataMap: DataMap) {
                        val overwrittenConfig = DataMap()
                        overwrittenConfig.putAll(dataMap)
                        overwrittenConfig.putAll(configKeysToOverwrite)
                        putConfigDataItem(overwrittenConfig)
                    }
                })
    }

    /**
     * Asynchronously fetches the current config [DataMap] for [SlateWatchFaceService]
     * and passes it to the given callback.
     *
     *
     * If the current config [DataItem] doesn't exist, it isn't created and the callback
     * receives an empty DataMap.
     */
    private fun fetchConfigDataMap(): TransformedResult<DataApi.DataItemResult> {
        return Wearable.NodeApi
                .getLocalNode(googleApiClient)
                .then<DataApi.DataItemResult>(object : ResultTransform<NodeApi.GetLocalNodeResult, DataApi.DataItemResult>() {
                    override fun onSuccess(getLocalNodeResult: NodeApi.GetLocalNodeResult): PendingResult<DataApi.DataItemResult>? {
                        val localNode = getLocalNodeResult.node.id
                        val uri = Uri.Builder()
                                .scheme(SCHEME_WEAR)
                                .path(PATH_WITH_FEATURE)
                                .authority(localNode)
                                .build()
                        return Wearable.DataApi.getDataItem(googleApiClient, uri)
                    }
                })
    }

    private fun addStringKeyIfMissing(config: DataMap, key: String, value: String) {
        if (!config.containsKey(key)) {
            config.putString(key, value)
        }
    }

    private fun addBoolKeyIfMissing(config: DataMap, key: String, value: Boolean) {
        if (!config.containsKey(key)) {
            config.putBoolean(key, value)
        }
    }

    /**
     * Overwrites the current config [DataItem]'s [DataMap] with `newConfig`.
     * If the config DataItem doesn't exist, it's created.
     */
    private fun putConfigDataItem(newConfig: DataMap) {
        val putDataMapRequest = PutDataMapRequest.create(PATH_WITH_FEATURE)
        val configToPut = putDataMapRequest.dataMap
        configToPut.putAll(newConfig)
        Wearable.DataApi.putDataItem(googleApiClient, putDataMapRequest.asPutDataRequest())
    }

    private abstract inner class DataItemResultCallback : ResultCallbacks<DataApi.DataItemResult>() {
        override fun onSuccess(dataItemResult: DataApi.DataItemResult) {
            var dataMap = DataMap()
            if (dataItemResult.status.isSuccess && dataItemResult.dataItem != null) {
                val configDataItem = dataItemResult.dataItem
                val dataMapItem = DataMapItem.fromDataItem(configDataItem)
                dataMap = dataMapItem.dataMap
            }
            onSuccess(dataMap)
        }

        internal abstract fun onSuccess(dataMap: DataMap)

        override fun onFailure(status: Status) {}
    }

    companion object {
        private val TAG = "ConfigManager"
    }
}
