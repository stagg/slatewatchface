package ca.joshstagg.slate

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.icu.util.Calendar

/**
 * Slate ca.joshstagg.slate
 * Copyright 2017  Josh Stagg
 */
class WatchEngine(context: Context, private val paints: SlatePaints) {

    private val ticks = arrayOfNulls<FloatArray>(12)
    private var backgroundBitmap = (context.getDrawable(R.drawable.bg) as BitmapDrawable).bitmap
    private var backgroundScaledBitmap: Bitmap? = null

    fun initialize(width: Int, height: Int) {
        initializeBackground(width, height)
        initializeTicks(width, height)
    }

    // Scale the background to fit.
    private fun initializeBackground(width: Int, height: Int) {
        if (null == backgroundScaledBitmap
            || backgroundScaledBitmap?.width != width
            || backgroundScaledBitmap?.height != height
        ) {
            backgroundScaledBitmap =
                    Bitmap.createScaledBitmap(backgroundBitmap, width, height, true)
        }
    }

    private fun initializeTicks(width: Int, height: Int) {
        val centerX = width / 2f
        val centerY = height / 2f

        val innerTickRadius = centerX - paints.innerTickRadius
        val largeInnerTickRadius = centerX - paints.largeInnerTickRadius
        for (tickIndex in 0..11) {
            val tickRot = (tickIndex.toDouble() * Math.PI * 2.0 / 12).toFloat()
            val innerX: Float
            val innerY: Float
            if (tickIndex == 0 || tickIndex == 3 || tickIndex == 6 || tickIndex == 9) {
                innerX = Math.sin(tickRot.toDouble()).toFloat() * largeInnerTickRadius
                innerY = (-Math.cos(tickRot.toDouble())).toFloat() * largeInnerTickRadius
            } else {
                innerX = Math.sin(tickRot.toDouble()).toFloat() * innerTickRadius
                innerY = (-Math.cos(tickRot.toDouble())).toFloat() * innerTickRadius
            }
            val outerX = Math.sin(tickRot.toDouble()).toFloat() * centerX
            val outerY = (-Math.cos(tickRot.toDouble())).toFloat() * centerX
            ticks[tickIndex] = floatArrayOf(
                centerX + innerX,
                centerY + innerY,
                centerX + outerX,
                centerY + outerY
            )
        }
    }

    fun drawBackground(canvas: Canvas, ambient: Ambient) {
        if (Ambient.NORMAL != ambient) {
            canvas.drawColor(Color.BLACK)
        } else if (null != backgroundScaledBitmap) {
            canvas.drawBitmap(backgroundScaledBitmap, 0f, 0f, null)
        }
    }

    fun drawTicks(canvas: Canvas, ambient: Ambient) {
        if (Ambient.NORMAL == ambient) {
            for (tick in ticks) {
                canvas.drawLines(tick, paints.tick)
            }
        }
    }

    fun drawHands(
        canvas: Canvas,
        ambient: Ambient,
        calendar: Calendar,
        centerX: Float,
        centerY: Float
    ) {
        val config = Slate.instance.configService.config

        val milliRotate = calendar.timeInMillis % 60000 / 1000f / 30f * Math.PI.toFloat()
        val secRotate = calendar.get(Calendar.SECOND) / 30f * Math.PI.toFloat()
        val minutes = calendar.get(Calendar.MINUTE)
        val minRot = minutes / 30f * Math.PI.toFloat()
        val hrRot = (calendar.get(Calendar.HOUR) + minutes / 60f) / 6f * Math.PI.toFloat()

        val secLength = centerX - paints.secLength
        val minLength = centerX - paints.minLength
        val hrLength = centerX - paints.hrLength

        val hrX = Math.sin(hrRot.toDouble()).toFloat() * hrLength
        val hrY = (-Math.cos(hrRot.toDouble())).toFloat() * hrLength
        canvas.drawLine(centerX, centerY, centerX + hrX, centerY + hrY, paints.hour)

        val minX = Math.sin(minRot.toDouble()).toFloat() * minLength
        val minY = (-Math.cos(minRot.toDouble())).toFloat() * minLength
        canvas.drawCircle(centerX, centerY, paints.centerRadius, paints.minute)
        canvas.drawLine(centerX, centerY, centerX + minX, centerY + minY, paints.minute)
        canvas.drawCircle(centerX, centerY, paints.centerRadius, paints.center)

        if (Ambient.NORMAL == ambient) {
            paints.accentHandColor = config.accentColor

            val rotate = if (config.smoothMovement) milliRotate else secRotate
            val secStartX = Math.sin(rotate.toDouble()).toFloat() * paints.secStart
            val secStartY = (-Math.cos(rotate.toDouble())).toFloat() * paints.secStart
            val secX = Math.sin(rotate.toDouble()).toFloat() * secLength
            val secY = (-Math.cos(rotate.toDouble())).toFloat() * secLength

            canvas.drawLine(
                centerX + secStartX,
                centerY + secStartY,
                centerX + secX,
                centerY + secY, paints.second
            )
            canvas.drawCircle(centerX, centerY, paints.centerSecondRadius, paints.second)
        }
    }
}