package ca.joshstagg.slate

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.graphics.*
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
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import android.view.Gravity
import android.view.SurfaceHolder

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

        private val mPaints: SlatePaints = SlatePaints()
        private var mSlateTime: SlateTime? = null
        private var mBackgroundBitmap: Bitmap? = null
        private var mBackgroundScaledBitmap: Bitmap? = null
        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        private var mLowBitAmbient: Boolean = false
        private var mBurnInProtection: Boolean = false

        private var mWidth: Int = 0
        private var mHeight: Int = 0
        private var mComplicationsY: Int = 0

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
                    .build())

            mSlateTime = SlateTime(mContext)
            mBackgroundBitmap = (ContextCompat.getDrawable(mContext, R.drawable.bg) as BitmapDrawable).bitmap

            initializeComplication()
        }

        private fun initializeComplication() {
            setDefaultSystemComplicationProvider(Constants.LEFT_DIAL_COMPLICATION, SystemProviders.WATCH_BATTERY, ComplicationData.TYPE_SHORT_TEXT)
            setDefaultSystemComplicationProvider(Constants.RIGHT_DIAL_COMPLICATION, SystemProviders.DATE, ComplicationData.TYPE_SHORT_TEXT)
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

            for (id in Constants.COMPLICATION_IDS) {
                complicationData = mActiveComplicationDataSparseArray.get(id)

                if (null != complicationData
                        && complicationData.isActive(currentTimeMillis)
                        && complicationData.type != ComplicationData.TYPE_NOT_CONFIGURED
                        && complicationData.type != ComplicationData.TYPE_EMPTY) {

                    val complicationBoundingRect = Rect(0, 0, 0, 0)

                    when (id) {
                        Constants.LEFT_DIAL_COMPLICATION -> complicationBoundingRect.set(
                                0, // left
                                mComplicationsY - Constants.COMPLICATION_TAP_BUFFER, // top
                                mWidth / 2, // right
                                Constants.COMPLICATION_TEXT_SIZE.toInt()               // bottom

                                        + mComplicationsY
                                        + Constants.COMPLICATION_TAP_BUFFER)

                        Constants.RIGHT_DIAL_COMPLICATION -> complicationBoundingRect.set(
                                mWidth / 2, // left
                                mComplicationsY - Constants.COMPLICATION_TAP_BUFFER, // top
                                mWidth, // right
                                Constants.COMPLICATION_TEXT_SIZE.toInt()               // bottom

                                        + mComplicationsY
                                        + Constants.COMPLICATION_TAP_BUFFER)
                    }

                    if (complicationBoundingRect.width() > 0) {
                        if (complicationBoundingRect.contains(x, y)) {
                            return id
                        }
                    } else {
                        Log.e(TAG, "Not a recognized complication id.")
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

            /*
             * Since the height of the complications text does not change, we only have to
             * recalculate when the surface changes.
             */
            mComplicationsY = (mHeight / 2 + mPaints.complication.textSize / 3).toInt()

            initializeBackground(width, height)
            initializeTicks(width, height)
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

                    // Both Short Text and No Permission Types can be rendered with the same code.
                    // No Permission will display "--" with an Intent to launch a permission prompt.
                    // If you want to support more types, just add a "else if" below with your
                    // rendering code inside.
                    // Render factory here -> factory picks a complication renderer, gives it dimensions, data, and a section of canvas to draw on.
                    // Keep the main location and size/positional rendering here
                    if (complicationData.type == ComplicationData.TYPE_SHORT_TEXT || complicationData.type == ComplicationData.TYPE_NO_PERMISSION) {

                        val mainText = complicationData.shortText
                        val subText = complicationData.shortTitle

                        var complicationMessage = mainText.getText(applicationContext, currentTimeMillis)

                        /* In most cases you would want the subText (Title) under the
                         * mainText (Text), but to keep it simple for the code lab, we are
                         * concatenating them all on one line.
                         */
                        if (subText != null) {
                            complicationMessage = TextUtils.concat(complicationMessage, " ",
                                    subText.getText(applicationContext, currentTimeMillis))
                        }

                        //Log.d(TAG, "Com id: " + COMPLICATION_IDS[i] + "\t" + complicationMessage);
                        val textWidth = mPaints.complication.measureText(
                                complicationMessage,
                                0,
                                complicationMessage.length).toDouble()

                        val complicationsX: Int
                        when (id) {
                            Constants.LEFT_DIAL_COMPLICATION -> {
                                complicationsX = (mWidth / 2 - textWidth).toInt() / 2
                            }
                            else -> {
                                val offset = (mWidth / 2 - textWidth).toInt() / 2
                                complicationsX = mWidth / 2 + offset
                            }
                        }

                        val cx = complicationsX + (textWidth / 2).toFloat()
                        val cy = (mHeight / 2).toFloat()
                        val radius = (mHeight / 8).toFloat()
                        if (!isAmbient) {
                            val temp2 = Paint()
                            temp2.isAntiAlias = true
                            temp2.setARGB(80, 80, 80, 80)
                            canvas.drawCircle(cx, cy, radius, temp2)

                            val temp = Paint()
                            temp.isAntiAlias = true
                            temp.setARGB(60, 0, 0, 0)
                            canvas.drawCircle(cx, cy, radius - 4f, temp)
                        }
                        canvas.drawText(
                                complicationMessage,
                                0,
                                complicationMessage.length,
                                complicationsX.toFloat(),
                                mComplicationsY.toFloat(),
                                mPaints.complication)
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
