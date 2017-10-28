package ca.joshstagg.slate.config

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Icon
import android.icu.util.Calendar
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.ComplicationHelperActivity
import android.support.wearable.complications.ComplicationProviderInfo
import android.support.wearable.complications.ProviderInfoRetriever
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import ca.joshstagg.slate.*
import ca.joshstagg.slate.complication.ComplicationEngine
import java.util.concurrent.Executors

/**
 * Slate ca.joshstagg.slate.config
 * Copyright 2017  Josh Stagg
 */

const val MESSAGE_DRAW = 0
const val MESSAGE_COMPLICATION_UPDATE = 1
const val MESSAGE_COMPLICATION_TAP = 2

class WatchFacePreviewView @JvmOverloads constructor(context: Context,
                                                     attrs: AttributeSet? = null,
                                                     defStyleAttr: Int = 0)
    : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {

    private val supportedTypes = Constants.COMPLICATION_SUPPORTED_TYPES
    private val watchFace = ComponentName(context, SlateWatchFaceService::class.java)

    private val providerInfoRetriever = ProviderInfoRetriever(context, Executors.newCachedThreadPool())

    private val handlerThread = HandlerThread("WatchFacePreviewThread")
    private val previewHandler: PreviewHandler

    init {
        handlerThread.start()
        previewHandler = PreviewHandler(context, handlerThread.looper, holder)

        setZOrderOnTop(true)
        holder.addCallback(this)
        holder.setFormat(PixelFormat.TRANSPARENT)

        setupComplication(Constants.TOP_DIAL_COMPLICATION)
        setupComplication(Constants.RIGHT_DIAL_COMPLICATION)
        setupComplication(Constants.LEFT_DIAL_COMPLICATION)
        setupComplication(Constants.BOTTOM_DIAL_COMPLICATION)

        providerInfoRetriever.init()
        providerInfoRetriever.retrieveProviderInfo(object : ProviderInfoRetriever.OnProviderInfoReceivedCallback() {
            override fun onProviderInfoReceived(complicationId: Int, providerInfo: ComplicationProviderInfo?) {
                providerInfo?.providerIcon?.let {
                    val intent = ComplicationHelperActivity
                            .createProviderChooserHelperIntent(context, watchFace, complicationId, *supportedTypes)
                    val data = ComplicationData
                            .Builder(ComplicationData.TYPE_ICON)
                            .setIcon(it)
                            .setTapAction(PendingIntent.getActivity(context, complicationId, intent, 0))
                            .build()
                    val msg = Message()
                    msg.what = MESSAGE_COMPLICATION_UPDATE
                    msg.arg1 = complicationId
                    msg.obj = data
                    previewHandler.sendMessage(msg)
                    previewHandler.sendEmptyMessage(MESSAGE_DRAW)
                }
            }
        }, watchFace, *Constants.COMPLICATION_IDS)
    }

    fun destroy() {
        providerInfoRetriever.release()
        handlerThread.quit()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val msg = Message()
                msg.what = MESSAGE_COMPLICATION_TAP
                msg.arg1 = event.x.toInt()
                msg.arg2 = event.y.toInt()
                previewHandler.sendMessage(msg)
                true
            } else {
                false
            }
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        previewHandler.sendEmptyMessage(MESSAGE_DRAW)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
    }

    private fun setupComplication(complicationId: Int) {
        val intent = ComplicationHelperActivity
                .createProviderChooserHelperIntent(context, watchFace, complicationId, *supportedTypes)

        val data = ComplicationData
                .Builder(ComplicationData.TYPE_ICON)
                .setIcon(Icon.createWithResource(context, R.drawable.ic_add_24dp))
                .setTapAction(PendingIntent.getActivity(context, complicationId, intent, 0))
                .build()

        val msg = Message()
        msg.what = MESSAGE_COMPLICATION_UPDATE
        msg.arg1 = complicationId
        msg.obj = data
        previewHandler.sendMessage(msg)
    }

    private class PreviewHandler(context: Context, looper: Looper, val surfaceHolder: SurfaceHolder) : Handler(looper) {

        private val calender: Calendar = Calendar.getInstance()

        private val paints = SlatePaints(context, 0.75f)
        private val pathClip = Path()
        private val rectF = RectF()

        private val watchEngine = WatchEngine(context, paints)
        private val notificationEngine = NotificationEngine(paints)
        private val complicationEngine = ComplicationEngine(context, paints)


        init {
            calender.set(2000, 0, 0, 10, 10, 0)
        }

        override fun handleMessage(msg: Message) {
            when(msg.what) {
                MESSAGE_DRAW -> surfaceHolder.draw()
                MESSAGE_COMPLICATION_UPDATE -> complicationEngine.dataUpdate(msg.arg1, msg.obj as? ComplicationData)
                MESSAGE_COMPLICATION_TAP -> complicationEngine.complicationTap(msg.arg1, msg.arg2)
            }
        }

        private fun SurfaceHolder.draw() {
            val canvas = lockCanvas()
            val width = canvas.width
            val height = canvas.height

            watchEngine.initialize(width, height)
            complicationEngine.initialize(width, height)

            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

            rectF.set(0f, 0f, width.toFloat(), height.toFloat())
            pathClip.reset()
            pathClip.moveTo(rectF.left, rectF.top)
            pathClip.addArc(rectF, 0f, 360f)
            canvas.save()
            canvas.clipPath(pathClip)

            watchEngine.drawBackground(canvas, false)
            complicationEngine.drawComplications(canvas, false, calender)
            watchEngine.drawTicks(canvas, false)
            watchEngine.drawHands(canvas, false, calender, width / 2f, height / 2f)

            if (Slate.instance.configService.config.notificationDot) {
                notificationEngine.unreadCountChanged(1)
                notificationEngine.drawUnreadIndicator(canvas, false)
            }

            canvas.restore()
            unlockCanvasAndPost(canvas)
        }
    }
}