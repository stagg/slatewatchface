package ca.joshstagg.slate;

import android.os.Bundle;

import preference.WearPreferenceActivity;

/**
 * Slate ca.joshstagg.slate
 * Copyright 2017  Josh Stagg
 */
public class SlateWatchFaceWearablePrefActivity extends WearPreferenceActivity {

    private ConfigService mSlateConfigService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        mSlateConfigService = Slate.getInstance().getConfigService();
        mSlateConfigService.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSlateConfigService.disconnect();
    }
}
