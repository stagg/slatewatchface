package ca.joshstagg.slate

import android.app.Application
import ca.joshstagg.slate.config.ConfigManager
import timber.log.Timber


class Slate : Application() {

    companion object {
        lateinit var instance: Slate
    }

    val configService: ConfigManager by lazy {
        ConfigManager(applicationContext)
    }

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}
