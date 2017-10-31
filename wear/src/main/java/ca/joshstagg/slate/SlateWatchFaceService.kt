package ca.joshstagg.slate

import android.content.ComponentName
import android.content.Context
import android.graphics.Canvas
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

/**
 * Slate ca.joshstagg.slate
 * Copyright 2017 Josh Stagg
 */
class SlateWatchFaceService : CanvasWatchFaceService() {

    override fun onCreateEngine(): Engine {
        return Engine(this.applicationContext)
    }

    inner class Engine internal constructor(private val context: Context) : CanvasWatchFaceService.Engine() {
        private val updateTimeHandler: Handler = EngineHandler(this)
        private val paints: SlatePaints = SlatePaints(context)
        private val slateTime: SlateTime = SlateTime(context)

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        private var lowBitAmbient: Boolean = false
        private var burnInProtection: Boolean = false

        private var width: Int = 0
        private var height: Int = 0

        private lateinit var watchEngine: WatchEngine
        private lateinit var complicationEngine: ComplicationEngine
        private lateinit var notificationEngine: NotificationEngine

        /**
         * Initialize the watch face

         * @param holder SurfaceHolder that the watch face will use
         */
        override fun onCreate(holder: SurfaceHolder?) {
            super.onCreate(holder)
            val component = ComponentName(context, SlateWatchFaceService::class.java)
            setWatchFaceStyle(WatchFaceStyle.Builder.forComponentName(component)
                    .setStatusBarGravity(Gravity.CENTER_HORIZONTAL or Gravity.TOP)
                    .setAcceptsTapEvents(true)
                    .build())
            watchEngine = WatchEngine(context, paints)
            complicationEngine = ComplicationEngine(applicationContext, paints)
            notificationEngine = NotificationEngine(paints)
            setActiveComplications(*Constants.COMPLICATION_IDS)
        }

        override fun onPropertiesChanged(properties: Bundle?) {
            super.onPropertiesChanged(properties)
            lowBitAmbient = properties?.getBoolean(WatchFaceService.PROPERTY_LOW_BIT_AMBIENT, false) ?: false
            burnInProtection = properties?.getBoolean(WatchFaceService.PROPERTY_BURN_IN_PROTECTION, false) ?: false
        }

        /*
         * Called when there is updated data for a complication id.
         */
        override fun onComplicationDataUpdate(complicationId: Int, complicationData: ComplicationData?) {
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
            if (lowBitAmbient) {
                paints.setAntiAlias(!inAmbientMode)
            }
            invalidate()
            restartTimer()
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)

            this.width = width
            this.height = height

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

            val isAmbient = isInAmbientMode
            val calendar = slateTime.timeNow

            watchEngine.drawBackground(canvas, isAmbient)
            complicationEngine.drawComplications(canvas, isAmbient, calendar)
            watchEngine.drawTicks(canvas, isAmbient)
            watchEngine.drawHands(canvas, isAmbient, calendar, centerX, centerY)
            notificationEngine.drawUnreadIndicator(canvas, isAmbient)
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
            updateTimeHandler.removeMessages(Constants.MSG_UPDATE_TIME)
            if (shouldTimerBeRunning()) {
                updateTimeHandler.sendEmptyMessage(Constants.MSG_UPDATE_TIME)
            }
        }

        override fun onDestroy() {
            updateTimeHandler.removeMessages(Constants.MSG_UPDATE_TIME)
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
                updateTimeHandler.sendEmptyMessageDelayed(Constants.MSG_UPDATE_TIME, delayMs)
            }
        }
    }

    private class EngineHandler internal constructor(private val engine: Engine) : Handler() {
        override fun handleMessage(message: Message) {
            if (Constants.MSG_UPDATE_TIME == message.what) {
                engine.updateTime()
            }
        }
    }
}
