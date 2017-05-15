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

import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;

import java.util.concurrent.TimeUnit;

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
    static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);
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


    static final String COLOR_STRING_DEFAULT = "#FF5E35B1";
    static final int COLOR_DEFAULT = Color.parseColor(COLOR_STRING_DEFAULT);
    static final boolean SMOOTH_MOVEMENT_DEFAULT = false;
}

