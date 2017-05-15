package ca.joshstagg.slate;

import android.app.Application;

public class Slate extends Application {

    private static Slate instance;
    private ConfigService mConfigService;

    public Slate() {
        super();
        instance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mConfigService = new ConfigService(this.getApplicationContext());
    }

    public ConfigService getConfigService() {
        return mConfigService;
    }

    public static Slate getInstance() {
        return instance;
    }
}
