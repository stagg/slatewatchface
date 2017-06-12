package ca.joshstagg.slate

import android.app.Application
import ca.joshstagg.slate.config.ConfigManager

class Slate : Application() {

    val configService: ConfigManager by lazy {
        ConfigManager(this.applicationContext)
    }

    init {
        instance = this
    }

    companion object {
        var instance: Slate? = null
    }
}
