package ca.joshstagg.slate.config

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Icon
import android.icu.util.Calendar
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
class WatchFacePreviewView @JvmOverloads constructor(context: Context,
                                                     attrs: AttributeSet? = null,
                                                     defStyleAttr: Int = 0)
    : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {


    private val paints = SlatePaints(0.75f)
    private val pathClip = Path()
    private val rectF = RectF()
    private val watchEngine = WatchEngine(context, paints)
    private val complicationEngine = ComplicationEngine(context, paints)
    private val notificationEngine = NotificationEngine(paints)
    private val calender: Calendar = Calendar.getInstance()

    private val supportedTypes = Constants.COMPLICATION_SUPPORTED_TYPES
    private val watchFace = ComponentName(context, SlateWatchFaceService::class.java)

    private val providerInfoRetriever = ProviderInfoRetriever(context, Executors.newCachedThreadPool());

    private val topDialIntent = ComplicationHelperActivity
            .createProviderChooserHelperIntent(context, watchFace, Constants.TOP_DIAL_COMPLICATION, *supportedTypes)
    private val rightDialIntent = ComplicationHelperActivity
            .createProviderChooserHelperIntent(context, watchFace, Constants.RIGHT_DIAL_COMPLICATION, *supportedTypes)
    private val bottomDialIntent = ComplicationHelperActivity
            .createProviderChooserHelperIntent(context, watchFace, Constants.BOTTOM_DIAL_COMPLICATION, *supportedTypes)
    private val leftDialIntent = ComplicationHelperActivity
            .createProviderChooserHelperIntent(context, watchFace, Constants.LEFT_DIAL_COMPLICATION, *supportedTypes)

    private val topData = ComplicationData
            .Builder(ComplicationData.TYPE_ICON)
            .setIcon(Icon.createWithResource(context, R.drawable.ic_add_24dp))
            .setTapAction(PendingIntent.getActivity(context, Constants.TOP_DIAL_COMPLICATION, topDialIntent, 0))
            .build()

    private val rightData = ComplicationData
            .Builder(ComplicationData.TYPE_ICON)
            .setIcon(Icon.createWithResource(context, R.drawable.ic_add_24dp))
            .setTapAction(PendingIntent.getActivity(context, Constants.RIGHT_DIAL_COMPLICATION, rightDialIntent, 0))
            .build()

    private val bottomData = ComplicationData
            .Builder(ComplicationData.TYPE_ICON)
            .setIcon(Icon.createWithResource(context, R.drawable.ic_add_24dp))
            .setTapAction(PendingIntent.getActivity(context, Constants.BOTTOM_DIAL_COMPLICATION, bottomDialIntent, 0))
            .build()

    private val leftData = ComplicationData
            .Builder(ComplicationData.TYPE_ICON)
            .setIcon(Icon.createWithResource(context, R.drawable.ic_add_24dp))
            .setTapAction(PendingIntent.getActivity(context, Constants.LEFT_DIAL_COMPLICATION, leftDialIntent, 0))
            .build()

    init {
        calender.set(2000, 0, 0, 13, 50, 0)
        setZOrderOnTop(true)
        holder.addCallback(this)
        holder.setFormat(PixelFormat.TRANSPARENT)
        providerInfoRetriever.init()
        complicationEngine.dataUpdate(Constants.TOP_DIAL_COMPLICATION, topData)
        complicationEngine.dataUpdate(Constants.RIGHT_DIAL_COMPLICATION, rightData)
        complicationEngine.dataUpdate(Constants.LEFT_DIAL_COMPLICATION, leftData)
        complicationEngine.dataUpdate(Constants.BOTTOM_DIAL_COMPLICATION, bottomData)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
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
                    complicationEngine.dataUpdate(complicationId, data)
                    holder?.draw()
                }
            }
        }, watchFace, *Constants.COMPLICATION_IDS)

        setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                complicationEngine.complicationTap(event.x.toInt(), event.y.toInt())
                true
            } else {
                false
            }
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        holder?.draw()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
    }

    private fun SurfaceHolder.draw() {
        watchEngine.initialize(width, height)
        complicationEngine.initialize(width, height)

        val canvas = lockCanvas()
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