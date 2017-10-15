package ca.joshstagg.slate

import android.app.Application
import ca.joshstagg.slate.config.ConfigManager

class Slate : Application() {

    companion object {
        lateinit var instance: Slate
    }

    init {
        instance = this
    }

    val configService: ConfigManager by lazy {
        ConfigManager(this.applicationContext)
    }
}
