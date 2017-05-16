package ca.joshstagg.slate;

import android.app.Application;

public class Slate extends Application {

    private static Slate instance;
    private ConfigManager mConfigService;

    public Slate() {
        super();
        instance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mConfigService = new ConfigManager(this.getApplicationContext());
    }

    public ConfigManager getConfigService() {
        return mConfigService;
    }

    public static Slate getInstance() {
        return instance;
    }
}
