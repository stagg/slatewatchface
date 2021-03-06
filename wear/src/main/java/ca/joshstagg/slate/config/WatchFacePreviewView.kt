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
import android.util.Size
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

class WatchFacePreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {

    private val pool = Executors.newCachedThreadPool()

    private lateinit var providerInfoRetriever: ProviderInfoRetriever
    private lateinit var handlerThread: HandlerThread
    private lateinit var previewHandler: PreviewHandler

    init {
        setZOrderOnTop(true)
        holder.addCallback(this)
        holder.setFormat(PixelFormat.TRANSPARENT)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        handlerThread = HandlerThread("WatchFacePreviewView")
        handlerThread.start()
        previewHandler = PreviewHandler(context, holder, handlerThread.looper)
        providerInfoRetriever = ProviderInfoRetriever(context, pool)
        providerInfoRetriever.init()
        setupComplication(TOP_DIAL_COMPLICATION)
        setupComplication(RIGHT_DIAL_COMPLICATION)
        setupComplication(LEFT_DIAL_COMPLICATION)
        setupComplication(BOTTOM_DIAL_COMPLICATION)
    }

    private fun setupComplication(complicationId: Int) {
        val msg = Message().apply {
            what = MESSAGE_COMPLICATION_UPDATE
            arg1 = complicationId
            obj = Icon.createWithResource(context, R.drawable.ic_add_24dp)
        }
        previewHandler.sendMessage(msg)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        val watchFace = ComponentName(context, SlateWatchFaceService::class.java)
        providerInfoRetriever.retrieveProviderInfo(object :
            ProviderInfoRetriever.OnProviderInfoReceivedCallback() {
            override fun onProviderInfoReceived(
                complicationId: Int,
                providerInfo: ComplicationProviderInfo?
            ) {
                providerInfo?.providerIcon?.also { icon ->
                    val msg = Message().apply {
                        what = MESSAGE_COMPLICATION_UPDATE
                        arg1 = complicationId
                        obj = icon
                    }
                    previewHandler.sendMessage(msg)
                    previewHandler.sendEmptyMessage(MESSAGE_DRAW)
                }
            }
        }, watchFace, *COMPLICATION_IDS)

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

    override fun surfaceDestroyed(holder: SurfaceHolder?) {}

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        providerInfoRetriever.release()
        handlerThread.quit()
    }

    override fun invalidate() {
        super.invalidate()
        previewHandler.sendEmptyMessage(MESSAGE_DRAW)
    }

    private class PreviewHandler(
        private val context: Context,
        private val surfaceHolder: SurfaceHolder,
        looper: Looper
    ) : Handler(looper) {

        private val pathClip = Path()
        private val rectF = RectF()
        private var size = Size(0, 0)

        private val supportedTypes = COMPLICATION_SUPPORTED_TYPES
        private val watchFace = ComponentName(context, SlateWatchFaceService::class.java)

        private val paints = SlatePaints(context, 0.75f)
        private val watchEngine = WatchEngine(context, paints)
        private val notificationEngine = NotificationEngine(paints)
        private val complicationEngine = ComplicationEngine(context, paints)

        private val calendar: Calendar by lazy {
            Calendar.getInstance().apply {
                set(2000, 0, 0, 10, 10, 0)
            }
        }

        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MESSAGE_DRAW -> surfaceHolder.draw()
                MESSAGE_COMPLICATION_UPDATE -> complicationDataUpdate(msg.arg1, msg.obj as Icon)
                MESSAGE_COMPLICATION_TAP -> complicationEngine.complicationTap(msg.arg1, msg.arg2)
            }
        }

        private fun complicationDataUpdate(complicationId: Int, icon: Icon) {
            val intent = ComplicationHelperActivity.createProviderChooserHelperIntent(
                context,
                watchFace,
                complicationId,
                *supportedTypes
            )
            val action = PendingIntent.getActivity(context, complicationId, intent, 0)
            val data = ComplicationData
                .Builder(ComplicationData.TYPE_ICON)
                .setIcon(icon)
                .setTapAction(action)
                .build()
            complicationEngine.dataUpdate(complicationId, data)
        }

        private fun SurfaceHolder.draw() {
            val canvas: Canvas = lockCanvas() ?: return
            val width = canvas.width
            val height = canvas.height

            if (size.width != width || size.height != height) {
                watchEngine.initialize(width, height)
                complicationEngine.initialize(width, height)
                size = Size(width, height)
            }

            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

            rectF.set(0f, 0f, width.toFloat(), height.toFloat())
            pathClip.reset()
            pathClip.moveTo(rectF.left, rectF.top)
            pathClip.addArc(rectF, 0f, 360f)
            canvas.save()
            canvas.clipPath(pathClip)

            watchEngine.drawBackground(canvas, Ambient.NORMAL)
            complicationEngine.drawComplications(canvas, Ambient.NORMAL, calendar)
            watchEngine.drawTicks(canvas, Ambient.NORMAL)
            watchEngine.drawHands(canvas, Ambient.NORMAL, calendar, width / 2f, height / 2f)

            if (Slate.instance.configService.config.notificationDot) {
                notificationEngine.unreadCountChanged(1)
                notificationEngine.drawUnreadIndicator(canvas)
            }

            canvas.restore()
            unlockCanvasAndPost(canvas)
        }
    }
}