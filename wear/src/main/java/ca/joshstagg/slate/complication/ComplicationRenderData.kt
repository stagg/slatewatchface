package ca.joshstagg.slate.complication

import android.app.PendingIntent
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.Handler
import android.os.Looper
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.ComplicationText
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Slate ca.joshstagg.slate.complication
 * Copyright 2018  Josh Stagg
 */
class ComplicationRenderData(
    context: Context,
    private val data: ComplicationData
) {

    private val mainHandler = Handler(Looper.myLooper())

    val icon: Drawable? by DrawableDelegate(context, data.icon, mainHandler)
    val ambientIcon: Drawable? by DrawableDelegate(context, data.burnInProtectionIcon, mainHandler)
    val smallImage: Drawable? by DrawableDelegate(context, data.smallImage, mainHandler)

    val type: Int by lazy { data.type }
    val tapAction: PendingIntent? by lazy { data.tapAction }
    val imageStyle: Int by lazy { data.imageStyle }

    val mainText: ComplicationText? by lazy { data.shortText }
    val subText: ComplicationText? by lazy { data.shortTitle }
    val range: Triple<Float, Float, Float> by lazy {
        Triple(data.value, data.minValue, data.maxValue)
    }

    fun isActive(currentTimeMillis: Long): Boolean {
        return data.isActive(currentTimeMillis)
    }

    private class DrawableDelegate<in R>(
        context: Context,
        icon: Icon?,
        handler: Handler
    ) : ReadOnlyProperty<R, Drawable?> {

        init {
            icon?.loadDrawableAsync(context, { drawable = it }, handler)
        }

        private var drawable: Drawable? = null

        override fun getValue(thisRef: R, property: KProperty<*>): Drawable? {
            return drawable
        }
    }
}