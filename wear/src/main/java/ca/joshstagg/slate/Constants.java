package ca.joshstagg.slate;

/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.graphics.Color;
import android.support.wearable.complications.ComplicationData;

import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;

/**
 * Slate ca.joshstagg.slate
 * Copyright 2017 Josh Stagg
 */
final class Constants {

    private Constants() {
    }

    static final int MSG_UPDATE_TIME = 0;
    /**
     * Update rate in milliseconds for interactive mode. We update once a second to advance the
     * second hand.
     */
    static final long INTERACTIVE_UPDATE_RATE_MS = 1000;
    static final long INTERACTIVE_SMOOTH_UPDATE_RATE_MS = 4;

    /**
     * The {@link DataMap} key for {@link SlateWatchFaceService} second digits color name.
     * The color name must be a {@link String} recognized by {@link Color#parseColor}.
     */
    static final String KEY_SECONDS_COLOR = "SECONDS_COLOR_1_2_1";
    static final String KEY_SMOOTH_MODE = "SMOOTH_MODE";

    /**
     * The path for the {@link DataItem} containing {@link SlateWatchFaceService} configuration.
     */
    static final String SCHEME_WEAR = "wear";
    static final String PATH_WITH_FEATURE = "/watch_face_config/slate";


    static final String ACCENT_COLOR_STRING_DEFAULT = "#FF5E35B1";
    static final int ACCENT_COLOR_DEFAULT = Color.parseColor(ACCENT_COLOR_STRING_DEFAULT);
    static final boolean SMOOTH_MOVEMENT_DEFAULT = false;

    // Unique IDs for each complication.
    static final int LEFT_DIAL_COMPLICATION = 0;
    static final int RIGHT_DIAL_COMPLICATION = 1;

    // Left and right complication IDs as array for Complication API.
    static final int[] COMPLICATION_IDS = {LEFT_DIAL_COMPLICATION, RIGHT_DIAL_COMPLICATION};

    // Left and right dial supported types.
    static final int[][] COMPLICATION_SUPPORTED_TYPES = {
            {ComplicationData.TYPE_SHORT_TEXT},
            {ComplicationData.TYPE_SHORT_TEXT}
    };

    static final float COMPLICATION_TEXT_SIZE = 24f;
    static final int COMPLICATION_TAP_BUFFER = 40;
}

