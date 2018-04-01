package ca.joshstagg.slate

import android.content.ComponentName
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.wearable.complications.ComplicationData
import android.support.wearable.watchface.CanvasWatchFaceService
import android.support.wearable.watchface.WatchFaceService
import android.support.wearable.watchface.WatchFaceStyle
import android.view.Gravity
import android.view.SurfaceHolder
import ca.joshstagg.slate.complication.ComplicationEngine
import java.util.*

/**
 * Slate ca.joshstagg.slate
 * Copyright 2017 Josh Stagg
 */
class SlateWatchFaceService : CanvasWatchFaceService() {

    override fun onCreateEngine(): Engine {
        return Engine(applicationContext)
    }

    inner class Engine internal constructor(private val context: Context) :
        CanvasWatchFaceService.Engine() {
        private val updateTimeHandler: Handler = EngineHandler(this)
        private val paints: SlatePaints = SlatePaints(context)
        private val slateTime: SlateTime = SlateTime(context)
        private val random = Random()

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        private var lowBitAmbient: Boolean = false
        private var burnInProtection: Boolean = false
        private var ambientMode: Ambient = Ambient.NORMAL

        private lateinit var watchEngine: WatchEngine
        private lateinit var complicationEngine: ComplicationEngine
        private lateinit var notificationEngine: NotificationEngine

        /**
         * Initialize the watch face

         * @param holder SurfaceHolder that the watch face will use
         */
        override fun onCreate(holder: SurfaceHolder?) {
            super.onCreate(holder)
            setWatchFaceStyle(
                WatchFaceStyle.Builder
                    .forComponentName(ComponentName(context, SlateWatchFaceService::class.java))
                    .setStatusBarGravity(Gravity.CENTER_HORIZONTAL or Gravity.TOP)
                    .setAcceptsTapEvents(true)
                    .setAccentColor(paints.accentHandColor)
                    .setHideNotificationIndicator(true)
                    .setShowUnreadCountIndicator(false)
                    .build()
            )
            watchEngine = WatchEngine(context, paints)
            complicationEngine = ComplicationEngine(applicationContext, paints)
            notificationEngine = NotificationEngine(paints)
            setActiveComplications(*COMPLICATION_IDS)
        }

        override fun onPropertiesChanged(properties: Bundle) {
            super.onPropertiesChanged(properties)
            properties.apply {
                lowBitAmbient = getBoolean(WatchFaceService.PROPERTY_LOW_BIT_AMBIENT, false)
                burnInProtection = getBoolean(WatchFaceService.PROPERTY_BURN_IN_PROTECTION, false)
            }
        }

        /*
         * Called when there is updated data for a complication id.
         */
        override fun onComplicationDataUpdate(
            complicationId: Int,
            complicationData: ComplicationData?
        ) {
            // Adds/updates active complication data in the array.
            complicationEngine.dataUpdate(complicationId, complicationData)
            invalidate()
        }

        override fun onTapCommand(tapType: Int, x: Int, y: Int, eventTime: Long) {
            when (tapType) {
                WatchFaceService.TAP_TYPE_TAP -> complicationEngine.complicationTap(x, y)
            }
        }

        override fun onAmbientModeChanged(inAmbientMode: Boolean) {
            /* the wearable switched between modes */
            super.onAmbientModeChanged(inAmbientMode)
            val config = Slate.instance.configService.config

            ambientMode = when {
                inAmbientMode && lowBitAmbient && burnInProtection -> Ambient.AMBIENT_LOW_BIT_BURN_IN
                inAmbientMode && burnInProtection -> Ambient.AMBIENT_BURN_IN
                inAmbientMode && lowBitAmbient -> Ambient.AMBIENT_LOW_BIT
                inAmbientMode -> Ambient.AMBIENT
                else -> Ambient.NORMAL
            }
            if (lowBitAmbient) {
                paints.setAntiAlias(!inAmbientMode)
            }

            if (inAmbientMode) {
                paints.handColor = config.ambientColor
                paints.second.color = Color.BLACK
            } else {
                paints.handColor = paints.primaryHandColor
                paints.second.color = config.accentColor
            }

            invalidate()
            restartTimer()
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            watchEngine.initialize(width, height)
            complicationEngine.initialize(width, height)
        }

        override fun onUnreadCountChanged(count: Int) {
            if (notificationEngine.unreadCountChanged(count)) {
                invalidate()
            }
        }

        override fun onTimeTick() {
            super.onTimeTick()
            invalidate()
        }

        override fun onDraw(canvas: Canvas, bounds: Rect) {
            val width = bounds.width()
            val height = bounds.height()

            val centerX = width / 2f
            val centerY = height / 2f

            val calendar = slateTime.timeNow

            if (Ambient.AMBIENT_BURN_IN == ambientMode ||
                Ambient.AMBIENT_LOW_BIT_BURN_IN == ambientMode
            ) {
                val shift = paints.burnInShift
                val shift2 = paints.burnInShift / 2
                val offsetX = (random.nextFloat() * shift) - shift2
                val offsetY = (random.nextFloat() * shift) - shift2
                Logger.d("SlateWatchFaceService", "Translate: $offsetX, $offsetY")
                canvas.translate(offsetX, offsetY)
            }

            watchEngine.drawBackground(canvas, ambientMode)
            complicationEngine.drawComplications(canvas, ambientMode, calendar)
            watchEngine.drawTicks(canvas, ambientMode)
            watchEngine.drawHands(canvas, ambientMode, calendar, centerX, centerY)
            notificationEngine.drawUnreadIndicator(canvas)
        }

        /**
         * The watch face became visible or invisible

         * @param visible If the watch face is visible or not
         */
        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                Slate.instance.configService.connect()
                slateTime.registerReceiver()
                slateTime.reset()
            } else {
                slateTime.unregisterReceiver()
                Slate.instance.configService.disconnect()
            }
            restartTimer()
        }

        /**
         * Whether the timer should be running depends on whether we're in ambient mode
         * (as well as whether we're visible), so we may need to start or stop the timer.
         *
         *
         * Starts the [.updateTimeHandler] timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private fun restartTimer() {
            updateTimeHandler.removeMessages(MSG_UPDATE_TIME)
            if (shouldTimerBeRunning()) {
                updateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME)
            }
        }

        override fun onDestroy() {
            updateTimeHandler.removeMessages(MSG_UPDATE_TIME)
            super.onDestroy()
        }

        /**
         * Returns whether the [.updateTimeHandler] timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private fun shouldTimerBeRunning(): Boolean {
            return isVisible && !isInAmbientMode
        }

        fun updateTime() {
            invalidate()
            if (shouldTimerBeRunning()) {
                val config = Slate.instance.configService.config
                val updateRate = config.updateRate
                val delayMs = updateRate - System.currentTimeMillis() % updateRate
                updateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs)
            }
        }
    }

    private class EngineHandler internal constructor(private val engine: Engine) : Handler() {
        override fun handleMessage(message: Message) {
            if (MSG_UPDATE_TIME == message.what) {
                engine.updateTime()
            }
        }
    }
}
