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

    inner class Engine internal constructor(private val mContext: Context) : CanvasWatchFaceService.Engine() {
        private val mUpdateTimeHandler: Handler
        private val mTicks = arrayOfNulls<FloatArray>(12)
        private val mComplications = mutableMapOf<Int, Rect>()

        private val mPaints: SlatePaints = SlatePaints()
        private var mSlateTime: SlateTime? = null
        private var mBackgroundBitmap: Bitmap? = null
        private var mBackgroundScaledBitmap: Bitmap? = null
        private var mComplicationRenderFactory: ComplicationRenderFactory? = null
        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        private var mLowBitAmbient: Boolean = false
        private var mBurnInProtection: Boolean = false

        private var mWidth: Int = 0
        private var mHeight: Int = 0

        private var mActiveComplicationDataSparseArray: SparseArray<ComplicationData> = SparseArray(Constants.COMPLICATION_IDS.size)

        init {
            mUpdateTimeHandler = EngineHandler(this)
        }

        /**
         * Initialize the watch face

         * @param holder SurfaceHolder that the watch face will use
         */
        override fun onCreate(holder: SurfaceHolder?) {
            super.onCreate(holder)
            val component: ComponentName = ComponentName(mContext, SlateWatchFaceService::class.java)
            setWatchFaceStyle(WatchFaceStyle.Builder.forComponentName(component)
                    .setStatusBarGravity(Gravity.CENTER_HORIZONTAL or Gravity.TOP)
                    .setAcceptsTapEvents(true)
                    .build())

            mSlateTime = SlateTime(mContext)
            mComplicationRenderFactory = ComplicationRenderFactory(mContext)
            mBackgroundBitmap = (ContextCompat.getDrawable(mContext, R.drawable.bg) as BitmapDrawable).bitmap

            createComplications()
        }

        private fun createComplications() {
            setDefaultSystemComplicationProvider(Constants.LEFT_DIAL_COMPLICATION, SystemProviders.WATCH_BATTERY, ComplicationData.TYPE_SHORT_TEXT)
            setDefaultSystemComplicationProvider(Constants.RIGHT_DIAL_COMPLICATION, SystemProviders.DATE, ComplicationData.TYPE_SHORT_TEXT)
            setDefaultSystemComplicationProvider(Constants.TOP_DIAL_COMPLICATION, SystemProviders.UNREAD_NOTIFICATION_COUNT, ComplicationData.TYPE_SHORT_TEXT)
            setDefaultSystemComplicationProvider(Constants.BOTTOM_DIAL_COMPLICATION, SystemProviders.WORLD_CLOCK, ComplicationData.TYPE_SHORT_TEXT)
            setActiveComplications(*Constants.COMPLICATION_IDS)
        }

        override fun onPropertiesChanged(properties: Bundle?) {
            super.onPropertiesChanged(properties)
            mLowBitAmbient = properties?.getBoolean(WatchFaceService.PROPERTY_LOW_BIT_AMBIENT, false) ?: false
            mBurnInProtection = properties?.getBoolean(WatchFaceService.PROPERTY_BURN_IN_PROTECTION, false) ?: false
        }

        /*
         * Called when there is updated data for a complication id.
         */
        override fun onComplicationDataUpdate(complicationId: Int, complicationData: ComplicationData?) {
            // Adds/updates active complication data in the array.
            mActiveComplicationDataSparseArray.put(complicationId, complicationData)
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
            var complicationData: ComplicationData?
            val currentTimeMillis = System.currentTimeMillis()
            val complicationBoundingRect = Rect(0, 0, 0, 0)

            for (id in Constants.COMPLICATION_IDS) {
                complicationData = mActiveComplicationDataSparseArray.get(id)

                if (null != complicationData
                        && complicationData.isActive(currentTimeMillis)
                        && complicationData.type != ComplicationData.TYPE_NOT_CONFIGURED
                        && complicationData.type != ComplicationData.TYPE_EMPTY) {


                    val rect = mComplications.getValue(id)
//                    val top = rect.left - Constants.COMPLICATION_TAP_BUFFER
//                    val left = rect.top - Constants.COMPLICATION_TAP_BUFFER
//                    val right = rect.right + Constants.COMPLICATION_TAP_BUFFER
//                    val bottom = rect.bottom + Constants.COMPLICATION_TAP_BUFFER

                    complicationBoundingRect.set(rect)

                    if (complicationBoundingRect.width() > 0 && complicationBoundingRect.contains(x, y)) {
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
            val complicationData = mActiveComplicationDataSparseArray.get(complicationId)

            if (complicationData != null) {
                if (complicationData.tapAction != null) {
                    try {
                        complicationData.tapAction.send()
                    } catch (e: PendingIntent.CanceledException) {
                        Log.e(TAG, "On complication tap action error " + e)
                    }

                } else if (complicationData.type == ComplicationData.TYPE_NO_PERMISSION) {

                    // Watch face does not have permission to receive complication data, so launch
                    // permission request.
                    val componentName = ComponentName(applicationContext, SlateWatchFaceService::class.java)

                    val permissionRequestIntent = ComplicationHelperActivity.createPermissionRequestHelperIntent(applicationContext, componentName)

                    startActivity(permissionRequestIntent)
                }

            }
        }

        override fun onAmbientModeChanged(inAmbientMode: Boolean) {
            /* the wearable switched between modes */
            super.onAmbientModeChanged(inAmbientMode)
            if (mLowBitAmbient) {
                mPaints.setAntiAlias(!inAmbientMode)
            }
            invalidate()
            restartTimer()
        }

        override fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)

            mWidth = width
            mHeight = height

            initializeBackground(width, height)
            initializeTicks(width, height)
            initializeComplications(width, height)
        }

        // Scale the background to fit.
        private fun initializeBackground(width: Int, height: Int) {
            if (null == mBackgroundScaledBitmap || mBackgroundScaledBitmap?.width != width || mBackgroundScaledBitmap?.height != height) {
                mBackgroundScaledBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap, width, height, true)
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
                mTicks[tickIndex] = floatArrayOf(centerX + innerX, centerY + innerY, centerX + outerX, centerY + outerY)
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
                mComplications.put(id, Rect(left, top, right, bottom))
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
            val calendar = mSlateTime?.timeNow ?: Calendar.getInstance()

            drawBackground(canvas, isAmbient)
            drawComplications(canvas, isAmbient, calendar.timeInMillis)
            drawTicks(canvas, isAmbient)
            drawHands(canvas, isAmbient, calendar, centerX, centerY)
        }

        private fun drawBackground(canvas: Canvas, isAmbient: Boolean) {
            if (isAmbient) {
                canvas.drawColor(Color.BLACK)
            } else if (null != mBackgroundScaledBitmap) {
                canvas.drawBitmap(mBackgroundScaledBitmap, 0f, 0f, null)
            }
        }

        private fun drawComplications(canvas: Canvas, isAmbient: Boolean, currentTimeMillis: Long) {
            var complicationData: ComplicationData?
            for (id in Constants.COMPLICATION_IDS) {
                complicationData = mActiveComplicationDataSparseArray.get(id)
                if (complicationData != null && complicationData.isActive(currentTimeMillis)) {
                    val render = Render(canvas, mComplications.getValue(id), currentTimeMillis, mPaints, complicationData)
                    val renderer = mComplicationRenderFactory?.renderFor(complicationData.type)
                    if (isAmbient) {
                        renderer?.ambientRender(render)
                    } else {
                        renderer?.render(render)
                    }

                }
            }
        }

        private fun drawTicks(canvas: Canvas, isAmbient: Boolean) {
            if (!isAmbient) {
                for (tick in mTicks) {
                    canvas.drawLines(tick, mPaints.tick)
                }
            }
        }

        private fun drawHands(canvas: Canvas, isAmbient: Boolean, calendar: Calendar, centerX: Float, centerY: Float) {
            val config = Slate.instance?.configService?.config

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
            canvas.drawLine(centerX, centerY, centerX + hrX, centerY + hrY, mPaints.hour)

            val minX = Math.sin(minRot.toDouble()).toFloat() * minLength
            val minY = (-Math.cos(minRot.toDouble())).toFloat() * minLength
            canvas.drawCircle(centerX, centerY, 10f, mPaints.minute)
            canvas.drawLine(centerX, centerY, centerX + minX, centerY + minY, mPaints.minute)
            canvas.drawCircle(centerX, centerY, 10f, mPaints.center)

            if (!isAmbient) {
                mPaints.accentHandColor = config?.accentColor ?: Constants.ACCENT_COLOR_DEFAULT

                val rotate = if (config?.isSmoothMovement ?: Constants.SMOOTH_MOVEMENT_DEFAULT) milliRotate else secRotate
                val secStartX = Math.sin(rotate.toDouble()).toFloat() * -40
                val secStartY = (-Math.cos(rotate.toDouble())).toFloat() * -40
                val secX = Math.sin(rotate.toDouble()).toFloat() * secLength
                val secY = (-Math.cos(rotate.toDouble())).toFloat() * secLength

                canvas.drawLine(centerX + secStartX,
                        centerY + secStartY,
                        centerX + secX,
                        centerY + secY, mPaints.second)

                canvas.drawCircle(centerX, centerY, 6f, mPaints.second)
            }
        }

        /**
         * The watch face became visible or invisible

         * @param visible If the watch face is visible or not
         */
        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                Slate.instance?.configService?.connect()
                mSlateTime?.registerReceiver()
                mSlateTime?.reset()
            } else {
                mSlateTime?.unregisterReceiver()
                Slate.instance?.configService?.disconnect()
            }
            restartTimer()
        }

        /**
         * Whether the timer should be running depends on whether we're in ambient mode
         * (as well as whether we're visible), so we may need to start or stop the timer.
         *
         *
         * Starts the [.mUpdateTimeHandler] timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private fun restartTimer() {
            mUpdateTimeHandler.removeMessages(Constants.MSG_UPDATE_TIME)
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(Constants.MSG_UPDATE_TIME)
            }
        }

        override fun onDestroy() {
            mUpdateTimeHandler.removeMessages(Constants.MSG_UPDATE_TIME)
            super.onDestroy()
        }

        /**
         * Returns whether the [.mUpdateTimeHandler] timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        fun shouldTimerBeRunning(): Boolean {
            return isVisible && !isInAmbientMode
        }

        fun updateTime() {
            invalidate()
            if (shouldTimerBeRunning()) {
                val updateRate = Slate.instance?.configService?.config?.updateRate ?: Constants.INTERACTIVE_UPDATE_RATE_MS
                val delayMs = updateRate - System.currentTimeMillis() % updateRate
                mUpdateTimeHandler.sendEmptyMessageDelayed(Constants.MSG_UPDATE_TIME, delayMs)
            }
        }
    }

    private class EngineHandler internal constructor(private val mEngine: Engine) : Handler() {

        override fun handleMessage(message: Message) {
            when (message.what) {
                Constants.MSG_UPDATE_TIME -> {
                    mEngine.updateTime()
                }
            }
        }
    }

    companion object {
        private val TAG = "SlateWatchFaceService"
    }
}
