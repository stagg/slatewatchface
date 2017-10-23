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
import ca.joshstagg.slate.R
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.*
import com.google.android.gms.wearable.*
import java.util.concurrent.atomic.AtomicInteger

class ConfigManager internal constructor(private val context: Context) :
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    internal var config: Config = Config()
    internal val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    private val connected = AtomicInteger(0)
    private val googleApiClient: GoogleApiClient = GoogleApiClient.Builder(context)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(Wearable.API)
            .build()

    val configItems by lazy {
        val config = Config()
        listOf(
                ConfigComplication(key = Constants.KEY_COMPLICATIONS,
                        title = "",
                        default = Constants.COMPLICATION_IDS),
                ConfigColor(key = Constants.KEY_SECONDS_COLOR,
                        title = context.getString(R.string.slate_second_hand_color),
                        default = Constants.ACCENT_COLOR_STRING_DEFAULT,
                        defaultText = Constants.ACCENT_COLOR_STRING_NAME_DEFAULT,
                        colorNames = context.resources.getStringArray(R.array.color_array_names),
                        colorValues = context.resources.getStringArray(R.array.color_array)),
                ConfigCheckBox(key = Constants.KEY_SMOOTH_MODE,
                        title = context.getString(R.string.slate_smooth_mode),
                        default = config.smoothMovement,
                        onText = context.getString(R.string.slate_smooth_mode_summary_on),
                        onIcon = R.drawable.ic_panorama_fish_eye_white_40dp,
                        offText = context.getString(R.string.slate_smooth_mode_summary_off),
                        offIcon = R.drawable.ic_schedule_white_40dp),
                ConfigSwitch(key = Constants.KEY_NOTIFICATION_DOT,
                        title = context.getString(R.string.slate_notification_dot),
                        default = config.notificationDot,
                        onText = context.getString(R.string.slate_notification_dot_summary_on),
                        offText = context.getString(R.string.slate_notification_dot_summary_off))
        )
    }

    override fun onConnected(connectionHint: Bundle?) {
        Wearable.DataApi.addListener(googleApiClient, this)
                .setResultCallback { updateConfigOnConnected() }
    }

    override fun onConnectionSuspended(cause: Int) {}

    override fun onConnectionFailed(result: ConnectionResult) {}

    // DataApi.DataListener
    override fun onDataChanged(dataEvents: DataEventBuffer) {
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
        val configDefault = Config()
        when (key) {
            Constants.KEY_SECONDS_COLOR -> {
                val color = sharedPreferences.getString(key, Constants.ACCENT_COLOR_STRING_DEFAULT)
                configKeysToOverwrite.putString(Constants.KEY_SECONDS_COLOR, color)
                Logger.d(TAG, "Update config color: $color")
            }
            Constants.KEY_SMOOTH_MODE -> {
                val smoothMode = sharedPreferences.getBoolean(key, configDefault.smoothMovement)
                configKeysToOverwrite.putBoolean(Constants.KEY_SMOOTH_MODE, smoothMode)
                Logger.d(TAG, "Update config smooth mode: $smoothMode")
            }
            Constants.KEY_NOTIFICATION_DOT -> {
                val notificationDot = sharedPreferences.getBoolean(key, configDefault.notificationDot)
                configKeysToOverwrite.putBoolean(Constants.KEY_NOTIFICATION_DOT, notificationDot)
                Logger.d(TAG, "Update config notification dot: $notificationDot")
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
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    @Synchronized internal fun disconnect() {
        val connected = connected.decrementAndGet()
        if (connected < 1) {
            if (googleApiClient.isConnected) {
                Wearable.DataApi.removeListener(googleApiClient, this)
                googleApiClient.disconnect()
            }
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
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
        addBoolKeyIfMissing(config, Constants.KEY_SMOOTH_MODE, Config().smoothMovement)
        addBoolKeyIfMissing(config, Constants.KEY_NOTIFICATION_DOT, Config().notificationDot)
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
    private fun updateConfigForKey(key: String, dataMap: DataMap) {
        when (key) {
            Constants.KEY_SECONDS_COLOR -> {
                dataMap.getString(key)?.let {
                    val color = Color.parseColor(it)
                    this.config = this.config.copy(accentColor = color)
                    Logger.d(TAG, "Found watch face dataMap key: $key -> ${Integer.toHexString(color)}")
                }
            }
            Constants.KEY_SMOOTH_MODE -> this.config = this.config.copy(smoothMovement = dataMap.getBoolean(key))
            Constants.KEY_NOTIFICATION_DOT -> this.config = this.config.copy(notificationDot = dataMap.getBoolean(key))
            else -> Logger.d(TAG, "Ignoring unknown dataMap key: $key")
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
                        updateConfig(overwrittenConfig)
                    }
                })
    }

    /**
     * Asynchronously fetches the current config [DataMap]
     * for [ca.joshstagg.slate.SlateWatchFaceService] and passes it to the given callback.
     *
     *
     * If the current config [DataItem] doesn't exist, it isn't created and the callback
     * receives an empty DataMap.
     */
    private fun fetchConfigDataMap(): TransformedResult<DataApi.DataItemResult> {
        return Wearable.NodeApi
                .getLocalNode(googleApiClient)
                .then(object : ResultTransform<NodeApi.GetLocalNodeResult, DataApi.DataItemResult>() {
                    override fun onSuccess(getLocalNodeResult: NodeApi.GetLocalNodeResult): PendingResult<DataApi.DataItemResult> {
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
            val dataMap = if (dataItemResult.status.isSuccess && dataItemResult.dataItem != null) {
                val configDataItem = dataItemResult.dataItem
                val dataMapItem = DataMapItem.fromDataItem(configDataItem)
                dataMapItem.dataMap
            } else {
                DataMap()
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