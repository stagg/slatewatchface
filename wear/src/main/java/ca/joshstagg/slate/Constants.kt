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
const val MSG_UPDATE_TIME = 0
/**
 * Update rate in milliseconds for interactive mode. We update once a second to advance the
 * second hand.
 */
const val INTERACTIVE_UPDATE_RATE_MS: Long = 1000
const val INTERACTIVE_SMOOTH_UPDATE_RATE_MS: Long = 1000 / 30

/**
 * The [DataMap] key for [SlateWatchFaceService] second digits color name.
 * The color name must be a [String] recognized by [Color.parseColor].
 */
const val KEY_COMPLICATIONS = "COMPLICATIONS"
const val KEY_SECONDS_COLOR = "SECONDS_COLOR_1_2_1"
const val KEY_SMOOTH_MODE = "SMOOTH_MODE"
const val KEY_NOTIFICATION_DOT = "NOTIFICATION_DOT"
const val KEY_AMBIENT_COLOR = "AMBIENT_COLOR"
const val KEY_BACKGROUND = "BACKGROUND"

/**
 * The path for the [DataItem] containing [SlateWatchFaceService] configuration.
 */
const val SCHEME_WEAR = "wear"
const val PATH_WITH_FEATURE = "/watch_face_config/slate"

const val ACCENT_COLOR_STRING_DEFAULT = "#FF5E35B1"
const val AMBIENT_COLOR_STRING_DEFAULT = "#FFFFFFFF"

// Unique IDs for each complication.
const val LEFT_DIAL_COMPLICATION = 0
const val TOP_DIAL_COMPLICATION = 1
const val RIGHT_DIAL_COMPLICATION = 2
const val BOTTOM_DIAL_COMPLICATION = 3

// Left and right complication IDs as array for Complication API.
val COMPLICATION_IDS = intArrayOf(
    LEFT_DIAL_COMPLICATION,
    TOP_DIAL_COMPLICATION,
    RIGHT_DIAL_COMPLICATION,
    BOTTOM_DIAL_COMPLICATION
)

// Left and right dial supported types.
val COMPLICATION_SUPPORTED_TYPES = intArrayOf(
    ComplicationData.TYPE_RANGED_VALUE,
    ComplicationData.TYPE_ICON,
    ComplicationData.TYPE_SHORT_TEXT,
    ComplicationData.TYPE_SMALL_IMAGE
)
