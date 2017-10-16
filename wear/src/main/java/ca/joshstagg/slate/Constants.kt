package ca.joshstagg.slate

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

import android.graphics.Color
import android.support.wearable.complications.ComplicationData

import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataMap

/**
 * Slate ca.joshstagg.slate
 * Copyright 2017 Josh Stagg
 */
internal object Constants {

    val MSG_UPDATE_TIME = 0
    /**
     * Update rate in milliseconds for interactive mode. We update once a second to advance the
     * second hand.
     */
    val INTERACTIVE_UPDATE_RATE_MS: Long = 1000
    val INTERACTIVE_SMOOTH_UPDATE_RATE_MS: Long = 4

    /**
     * The [DataMap] key for [SlateWatchFaceService] second digits color name.
     * The color name must be a [String] recognized by [Color.parseColor].
     */
    val KEY_SECONDS_COLOR = "SECONDS_COLOR_1_2_1"
    val KEY_SMOOTH_MODE = "SMOOTH_MODE"
    val KEY_NOTIFICATION_DOT = "NOTIFICATION_DOT"

    /**
     * The path for the [DataItem] containing [SlateWatchFaceService] configuration.
     */
    val SCHEME_WEAR = "wear"
    val PATH_WITH_FEATURE = "/watch_face_config/slate"


    val ACCENT_COLOR_STRING_DEFAULT = "#FF5E35B1"

    // Unique IDs for each complication.
    val LEFT_DIAL_COMPLICATION = 0
    val TOP_DIAL_COMPLICATION = 1
    val RIGHT_DIAL_COMPLICATION = 2
    val BOTTOM_DIAL_COMPLICATION = 3

    // Left and right complication IDs as array for Complication API.
    val COMPLICATION_IDS = intArrayOf(LEFT_DIAL_COMPLICATION, TOP_DIAL_COMPLICATION, RIGHT_DIAL_COMPLICATION, BOTTOM_DIAL_COMPLICATION)

    // Left and right dial supported types.
    val COMPLICATION_SUPPORTED_TYPES = arrayOf(intArrayOf(ComplicationData.TYPE_SHORT_TEXT), intArrayOf(ComplicationData.TYPE_SHORT_TEXT))

    val COMPLICATION_TEXT_SIZE = 24f
    val COMPLICATION_MAIN_TEXT_SIZE = 20f
    val COMPLICATION_SUB_TEXT_SIZE = 16f
    val COMPLICATION_TAP_BUFFER = 40
}

