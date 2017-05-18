package ca.joshstagg.slate

import android.app.Application

class Slate : Application() {
    var configService: ConfigManager? = null
        private set

    init {
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        configService = ConfigManager(this.applicationContext)
    }

    companion object {
        var instance: Slate? = null
    }
}
