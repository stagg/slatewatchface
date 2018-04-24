package ca.joshstagg.slate.config

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.preference.PreferenceManager
import ca.joshstagg.slate.*
import ca.joshstagg.slate.config.items.ConfigCheckBox
import ca.joshstagg.slate.config.items.ConfigColor
import ca.joshstagg.slate.config.items.ConfigComplication
import ca.joshstagg.slate.config.items.ConfigSwitch
import java.util.concurrent.atomic.AtomicInteger

class ConfigManager internal constructor(
    private val context: Context
) : SharedPreferences.OnSharedPreferenceChangeListener {

    internal var config: Config = Config()
    internal val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    private val connected = AtomicInteger(0)

    val configItems by lazy {
        val config = Config()
        val res = context.resources

        val colorNames = res.getStringArray(R.array.color_array_names)
        val colorValues = res.getStringArray(R.array.color_array)

        val greyNames = res.getStringArray(R.array.ambient_color_array_names)
        val greyValues = res.getStringArray(R.array.ambient_color_array)

        return@lazy listOf(
            ConfigComplication(
                key = KEY_COMPLICATIONS,
                title = "",
                default = COMPLICATION_IDS
            ),
            ConfigColor(
                key = KEY_SECONDS_COLOR,
                title = context.getString(R.string.slate_second_hand_color),
                default = colorValues[2],
                defaultText = colorNames[2],
                colorNames = colorNames,
                colorValues = colorValues
            ),
            ConfigCheckBox(
                key = KEY_SMOOTH_MODE,
                title = context.getString(R.string.slate_smooth_mode),
                default = config.smoothMovement,
                onText = context.getString(R.string.slate_smooth_mode_summary_on),
                onIcon = R.drawable.ic_panorama_fish_eye_white_40dp,
                offText = context.getString(R.string.slate_smooth_mode_summary_off),
                offIcon = R.drawable.ic_schedule_white_40dp
            ),
            ConfigColor(
                key = KEY_AMBIENT_COLOR,
                title = context.getString(R.string.slate_ambient_color),
                default = greyValues[0],
                defaultText = greyNames[0],
                colorNames = greyNames,
                colorValues = greyValues
            ),
            ConfigSwitch(
                key = KEY_NOTIFICATION_DOT,
                title = context.getString(R.string.slate_notification_dot),
                default = config.notificationDot,
                onText = context.getString(R.string.slate_notification_dot_summary_on),
                offText = context.getString(R.string.slate_notification_dot_summary_off)
            ),
            ConfigSwitch(
                key = KEY_BACKGROUND,
                title = context.getString(R.string.slate_background),
                default = config.background,
                onText = context.getString(R.string.slate_background_summary_on),
                offText = context.getString(R.string.slate_background_summary_off)
            )
        )
    }

    override fun onSharedPreferenceChanged(prefs: SharedPreferences, key: String) {
        val configDefault = Config()
        when (key) {
            KEY_SECONDS_COLOR -> {
                val color = prefs.getString(key, ACCENT_COLOR_STRING_DEFAULT)
                Logger.d(TAG, "Update config color: $color")
                config.copy(accentColor = Color.parseColor(color))
            }
            KEY_SMOOTH_MODE -> {
                val smoothMode = prefs.getBoolean(key, configDefault.smoothMovement)
                Logger.d(TAG, "Update config smooth mode: $smoothMode")
                config.copy(smoothMovement = smoothMode)
            }
            KEY_NOTIFICATION_DOT -> {
                val notificationDot = prefs.getBoolean(key, configDefault.notificationDot)
                Logger.d(TAG, "Update config notification dot: $notificationDot")
                config.copy(notificationDot = notificationDot)
            }
            KEY_BACKGROUND -> {
                val background = prefs.getBoolean(key, configDefault.background)
                Logger.d(TAG, "Update config background dot: $background")
                config.copy(background = background)
            }
            KEY_AMBIENT_COLOR -> {
                val grey = prefs.getString(key, AMBIENT_COLOR_STRING_DEFAULT)
                Logger.d(TAG, "Update config grey: $grey")
                config.copy(ambientColor = Color.parseColor(grey))
            }
            else -> return
        }.let {
            config = it
        }
    }

    internal fun connect() {
        val connected = connected.getAndIncrement()
        if (connected == 0) {
            preferences.registerOnSharedPreferenceChangeListener(this)
            preferences.apply {
                val accentColor = getString(KEY_SECONDS_COLOR, ACCENT_COLOR_STRING_DEFAULT)
                val smoothMode = getBoolean(KEY_SMOOTH_MODE, config.smoothMovement)
                val notificationDot = getBoolean(KEY_NOTIFICATION_DOT, config.notificationDot)
                val background = getBoolean(KEY_BACKGROUND, config.background)
                val ambientColor = getString(KEY_AMBIENT_COLOR, AMBIENT_COLOR_STRING_DEFAULT)
                config = config.copy(
                    accentColor = Color.parseColor(accentColor),
                    smoothMovement = smoothMode,
                    notificationDot = notificationDot,
                    background = background,
                    ambientColor = Color.parseColor(ambientColor)
                )
            }
        }
    }

    internal fun disconnect() {
        val connected = connected.decrementAndGet()
        if (connected < 1) {
            preferences.unregisterOnSharedPreferenceChangeListener(this)
        }
    }

    companion object {
        private const val TAG = "ConfigManager"
    }
}
