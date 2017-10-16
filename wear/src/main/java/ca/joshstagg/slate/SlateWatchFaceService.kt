package ca.joshstagg.slate

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v4.content.ContextCompat
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.ComplicationHelperActivity
import android.support.wearable.complications.SystemProviders
import android.support.wearable.watchface.CanvasWatchFaceService
import android.support.wearable.watchface.WatchFaceService
import android.support.wearable.watchface.WatchFaceStyle
import android.util.Log
import android.util.SparseArray
import android.view.Gravity
import android.view.SurfaceHolder
import ca.joshstagg.slate.complication.ComplicationRenderFactory
import ca.joshstagg.slate.complication.Render

/**
 * Slate ca.joshstagg.slate
 * Copyright 2017 Josh Stagg
 */
class SlateWatchFaceService : CanvasWatchFaceService() {

    override fun onCreateEngine(): Engine {
        return Engine(this.applicationContext)
    }

    inner class Engine internal constructor(private val context: Context) : CanvasWatchFaceService.Engine() {
        private val updateTimeHandler: Handler
        private val ticks = arrayOfNulls<FloatArray>(12)
        private val complications = mutableMapOf<Int, Rect>()

        private val paints: SlatePaints = SlatePaints()
        private val slateTime: SlateTime by lazy {
            SlateTime(context)
        }
        private var backgroundBitmap: Bitmap? = null
        private var backgroundScaledBitmap: Bitmap? = null
        private val complicationRenderFactory by lazy {
            ComplicationRenderFactory(context)
        }
        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        private var lowBitAmbient: Boolean = false
        private var burnInProtection: Boolean = false

        private var width: Int = 0
        private var height: Int = 0

        private var activeComplicationDataSparseArray: SparseArray<ComplicationData?> = SparseArray(Constants.COMPLICATION_IDS.size)

        private var unreadNotificationCount = 0

        init {
            updateTimeHandler = EngineHandler(this)
        }

        /**
         * Initialize the watch face

         * @param holder SurfaceHolder that the watch face will use
         */
        override fun onCreate(holder: SurfaceHolder?) {
            super.onCreate(holder)
            val component: ComponentName = ComponentName(context, SlateWatchFaceService::class.java)
            setWatchFaceStyle(WatchFaceStyle.Builder.forComponentName(component)
                    .setStatusBarGravity(Gravity.CENTER_HORIZONTAL or Gravity.TOP)
                    .setAcceptsTapEvents(true)
                    .build())
            backgroundBitmap = (ContextCompat.getDrawable(context, R.drawable.bg) as BitmapDrawable).bitmap
            createComplications()
        }

        private val PROVIDER_TEST_SUITE = "com.example.android.wearable.wear.wearcomplicationproviderstestsuite"

        private fun createComplications() {
            val cnL = ComponentName(PROVIDER_TEST_SUITE, "$PROVIDER_TEST_SUITE.SmallImageProviderService")
            val cnR = ComponentName(PROVIDER_TEST_SUITE, "$PROVIDER_TEST_SUITE.ShortTextProviderService")
            val cnT = ComponentName(PROVIDER_TEST_SUITE, "$PROVIDER_TEST_SUITE.IconProviderService")
            val cnB = ComponentName(PROVIDER_TEST_SUITE, "$PROVIDER_TEST_SUITE.RangedValueProviderService")
            setDefaultComplicationProvider(Constants.LEFT_DIAL_COMPLICATION, cnL, ComplicationData.TYPE_SMALL_IMAGE)
            setDefaultComplicationProvider(Constants.RIGHT_DIAL_COMPLICATION, cnR, ComplicationData.TYPE_SHORT_TEXT)
            setDefaultComplicationProvider(Constants.TOP_DIAL_COMPLICATION, cnT, ComplicationData.TYPE_ICON)
            setDefaultComplicationProvider(Constants.BOTTOM_DIAL_COMPLICATION, cnB, ComplicationData.TYPE_RANGED_VALUE)
            setDefaultSystemComplicationProvider(Constants.LEFT_DIAL_COMPLICATION, SystemProviders.WATCH_BATTERY, ComplicationData.TYPE_ICON)
            setDefaultSystemComplicationProvider(Constants.RIGHT_DIAL_COMPLICATION, SystemProviders.DATE, ComplicationData.TYPE_SHORT_TEXT)
            setDefaultSystemComplicationProvider(Constants.TOP_DIAL_COMPLICATION, SystemProviders.UNREAD_NOTIFICATION_COUNT, ComplicationData.TYPE_SHORT_TEXT)
            setDefaultSystemComplicationProvider(Constants.BOTTOM_DIAL_COMPLICATION, SystemProviders.WORLD_CLOCK, ComplicationData.TYPE_SHORT_TEXT)
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
            activeComplicationDataSparseArray.put(complicationId, complicationData)
            invalidate()
        }

        override fun onTapCommand(tapType: Int, x: Int, y: Int, eventTime: Long) {
            when (tapType) {
                WatchFaceService.TAP_TYPE_TAP -> {
                    val tappedComplicationId = getTappedComplicationId(x, y)
                    if (tappedComplicationId != -1) {
                        onComplicationTap(tappedComplicationId)
                    }
                }
            }
        }

        private fun getTappedComplicationId(x: Int, y: Int): Int {
            val currentTimeMillis = System.currentTimeMillis()
            for (id in Constants.COMPLICATION_IDS) {
                activeComplicationDataSparseArray.get(id)
                        ?.takeUnless { it.type == ComplicationData.TYPE_NOT_CONFIGURED }
                        ?.takeUnless { it.type == ComplicationData.TYPE_EMPTY }
                        ?.takeIf { it.isActive(currentTimeMillis) }
                        ?.let {
                            val rect = complications.getValue(id)
                            if (rect.width() > 0 && rect.contains(x, y)) {
                                return id
                            }
                        }
            }
            return -1
        }

        /*
         * Fires PendingIntent associated with complication (if it has one).
         */
        private fun onComplicationTap(complicationId: Int) {
            val complicationData = activeComplicationDataSparseArray.get(complicationId)

            if (complicationData != null) {
                if (complicationData.tapAction != null) {
                    try {
                        complicationData.tapAction.send()
                    } catch (e: PendingIntent.CanceledException) {
                        Log.e(TAG, "On complication tap action error " + e)
                    }
                } else if (complicationData.type == ComplicationData.TYPE_NO_PERMISSION) {
                    // Watch face does not have permission to receive complication data, so launch permission request.
                    val componentName = ComponentName(applicationContext, SlateWatchFaceService::class.java)
                    val permissionRequestIntent = ComplicationHelperActivity.createPermissionRequestHelperIntent(applicationContext, componentName)
                    startActivity(permissionRequestIntent)
                }
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

            initializeBackground(width, height)
            initializeTicks(width, height)
            initializeComplications(width, height)
        }

        override fun onUnreadCountChanged(count: Int) {
            val config = Slate.instance.configService.config
            if (config.notificationDot && unreadNotificationCount != count) {
                unreadNotificationCount = count
                invalidate()
            }
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

        private fun initializeComplications(width: Int, height: Int) {
            val radius: Int = width / 8
            for (id in Constants.COMPLICATION_IDS) {
                val cx: Number
                val cy: Number
                when (id) {
                    Constants.TOP_DIAL_COMPLICATION -> {
                        cx = width / 2
                        cy = height / 4
                    }
                    Constants.BOTTOM_DIAL_COMPLICATION -> {
                        cx = width / 2
                        cy = height * .75
                    }
                    Constants.LEFT_DIAL_COMPLICATION -> {
                        cx = width / 4
                        cy = height / 2
                    }
                    else -> { //Constants.RIGHT_DIAL_COMPLICATION
                        cx = width * .75
                        cy = height / 2
                    }
                }
                val left = cx.toInt() - radius
                val top = cy.toInt() - radius
                val right = cx.toInt() + radius
                val bottom = cy.toInt() + radius
                complications.put(id, Rect(left, top, right, bottom))
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

            drawBackground(canvas, isAmbient)
            drawComplications(canvas, isAmbient, calendar.timeInMillis)
            drawTicks(canvas, isAmbient)
            drawHands(canvas, isAmbient, calendar, centerX, centerY)
            drawUnreadIndicator(canvas, isAmbient)
        }

        private fun drawBackground(canvas: Canvas, isAmbient: Boolean) {
            if (isAmbient) {
                canvas.drawColor(Color.BLACK)
            } else if (null != backgroundScaledBitmap) {
                canvas.drawBitmap(backgroundScaledBitmap, 0f, 0f, null)
            }
        }

        private fun drawComplications(canvas: Canvas, isAmbient: Boolean, currentTimeMillis: Long) {
            for (id in Constants.COMPLICATION_IDS) {
                activeComplicationDataSparseArray.get(id)
                        ?.takeIf { complicationData -> complicationData.isActive(currentTimeMillis) }
                        ?.let { complicationData ->
                            canvas.save()
                            val rect = complications.getValue(id)
                            val render = Render(canvas, rect, currentTimeMillis, paints, complicationData)
                            val renderer = complicationRenderFactory.rendererFor(complicationData.type)
                            if (isAmbient) {
                                renderer.ambientRender(render)
                            } else {
                                renderer.render(render)
                            }
                            canvas.restore()
                        }
            }
        }

        private fun drawTicks(canvas: Canvas, isAmbient: Boolean) {
            if (!isAmbient) {
                for (tick in ticks) {
                    canvas.drawLines(tick, paints.tick)
                }
            }
        }

        private fun drawHands(canvas: Canvas, isAmbient: Boolean, calendar: Calendar, centerX: Float, centerY: Float) {
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

        //todo Review
        private fun drawUnreadIndicator(canvas: Canvas, isAmbient: Boolean) {
            val config = Slate.instance.configService.config
            if (config.notificationDot && unreadNotificationCount > -1) {
                val width = canvas.width
                val height = canvas.height
                canvas.drawCircle((width / 2).toFloat(), (height - 40).toFloat(), 10f, paints.center)
                if (!isAmbient) {
                    canvas.drawCircle((width / 2).toFloat(), (height - 40).toFloat(), 4f, paints.second)
                }
            }
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
            when (message.what) {
                Constants.MSG_UPDATE_TIME -> {
                    engine.updateTime()
                }
            }
        }
    }

    companion object {
        private val TAG = "SlateWatchFaceService"
    }
}
