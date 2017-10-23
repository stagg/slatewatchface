package ca.joshstagg.slate.config

import android.content.Context
import android.graphics.*
import android.icu.util.Calendar
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import ca.joshstagg.slate.SlatePaints
import ca.joshstagg.slate.WatchEngine

/**
 * Slate ca.joshstagg.slate.config
 * Copyright 2017  Josh Stagg
 */
class WatchFacePreviewView @JvmOverloads constructor(context: Context,
                                                     attrs: AttributeSet? = null,
                                                     defStyleAttr: Int = 0)
    : SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback {


    private val pathClip = Path()
    private val rectF = RectF()
    private val engine = WatchEngine(context, SlatePaints(0.75f))
    private val calender: Calendar = Calendar.getInstance()

    init {
        calender.set(0, 0, 0, 13, 50, 0)
        setZOrderOnTop(true)
        holder.addCallback(this)
        holder.setFormat(PixelFormat.TRANSPARENT)
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
    }

    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        holder?.draw()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
    }

    private fun SurfaceHolder.draw() {
        val canvas = lockCanvas()
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        rectF.set(0f, 0f, width.toFloat(), height.toFloat())
        pathClip.reset()
        pathClip.moveTo(rectF.left, rectF.top)
        pathClip.addArc(rectF, 0f, 360f)
        canvas.save()
        canvas.clipPath(pathClip)
        engine.initialize(width, height)
        engine.drawBackground(canvas, false)
        engine.drawTicks(canvas, false)
        engine.drawHands(canvas, false, calender, width / 2f, height / 2f)
        canvas.restore()
        unlockCanvasAndPost(canvas)
    }

}