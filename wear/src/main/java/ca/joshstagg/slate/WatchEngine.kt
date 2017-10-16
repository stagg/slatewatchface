package ca.joshstagg.slate

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.icu.util.Calendar
import android.support.v4.content.ContextCompat

/**
 * Slate ca.joshstagg.slate
 * Copyright 2017  Josh Stagg
 */
class WatchEngine(context: Context, private val paints: SlatePaints) {

    private val ticks = arrayOfNulls<FloatArray>(12)
    private var backgroundBitmap: Bitmap? = null
    private var backgroundScaledBitmap: Bitmap? = null

    init {
        backgroundBitmap = (ContextCompat.getDrawable(context, R.drawable.bg) as BitmapDrawable).bitmap
    }

    fun initialize(width: Int, height: Int) {
        initializeBackground(width, height)
        initializeTicks(width, height)
    }

    // Scale the background to fit.
    private fun initializeBackground(width: Int, height: Int) {
        if (null == backgroundScaledBitmap || backgroundScaledBitmap?.width != width || backgroundScaledBitmap?.height != height) {
            backgroundScaledBitmap = Bitmap.createScaledBitmap(backgroundBitmap, width, height, true)
        }
    }

    private fun initializeTicks(width: Int, height: Int) {
        val centerX = width / 2f
        val centerY = height / 2f

        val innerTickRadius = centerX - 18
        val largeInnerTickRadius = centerX - 42
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
            ticks[tickIndex] = floatArrayOf(centerX + innerX, centerY + innerY, centerX + outerX, centerY + outerY)
        }
    }

    fun drawBackground(canvas: Canvas, isAmbient: Boolean) {
        if (isAmbient) {
            canvas.drawColor(Color.BLACK)
        } else if (null != backgroundScaledBitmap) {
            canvas.drawBitmap(backgroundScaledBitmap, 0f, 0f, null)
        }
    }

    fun drawTicks(canvas: Canvas, isAmbient: Boolean) {
        if (!isAmbient) {
            for (tick in ticks) {
                canvas.drawLines(tick, paints.tick)
            }
        }
    }

    fun drawHands(canvas: Canvas, isAmbient: Boolean, calendar: Calendar, centerX: Float, centerY: Float) {
        val config = Slate.instance.configService.config

        val milliRotate = calendar.timeInMillis % 60000 / 1000f / 30f * Math.PI.toFloat()
        val secRotate = calendar.get(Calendar.SECOND) / 30f * Math.PI.toFloat()
        val minutes = calendar.get(Calendar.MINUTE)
        val minRot = minutes / 30f * Math.PI.toFloat()
        val hrRot = (calendar.get(Calendar.HOUR) + minutes / 60f) / 6f * Math.PI.toFloat()

        val secLength = centerX - 16
        val minLength = centerX - 26
        val hrLength = centerX - 70

        val hrX = Math.sin(hrRot.toDouble()).toFloat() * hrLength
        val hrY = (-Math.cos(hrRot.toDouble())).toFloat() * hrLength
        canvas.drawLine(centerX, centerY, centerX + hrX, centerY + hrY, paints.hour)

        val minX = Math.sin(minRot.toDouble()).toFloat() * minLength
        val minY = (-Math.cos(minRot.toDouble())).toFloat() * minLength
        canvas.drawCircle(centerX, centerY, 10f, paints.minute)
        canvas.drawLine(centerX, centerY, centerX + minX, centerY + minY, paints.minute)
        canvas.drawCircle(centerX, centerY, 10f, paints.center)

        if (!isAmbient) {
            paints.accentHandColor = config.accentColor

            val rotate = if (config.smoothMovement) milliRotate else secRotate
            val secStartX = Math.sin(rotate.toDouble()).toFloat() * -40
            val secStartY = (-Math.cos(rotate.toDouble())).toFloat() * -40
            val secX = Math.sin(rotate.toDouble()).toFloat() * secLength
            val secY = (-Math.cos(rotate.toDouble())).toFloat() * secLength

            canvas.drawLine(centerX + secStartX,
                    centerY + secStartY,
                    centerX + secX,
                    centerY + secY, paints.second)

            canvas.drawCircle(centerX, centerY, 6f, paints.second)
        }
    }
}