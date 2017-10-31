package ca.joshstagg.slate.complication

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.icu.util.Calendar
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.ComplicationHelperActivity
import android.util.Log
import android.util.SparseArray
import ca.joshstagg.slate.Constants
import ca.joshstagg.slate.SlatePaints
import ca.joshstagg.slate.SlateWatchFaceService

/**
 * Slate ca.joshstagg.slate
 * Copyright 2017  Josh Stagg
 */
class ComplicationEngine(val context: Context, private val paints: SlatePaints) {

    private val complications = mutableMapOf<Int, Rect>()

    private val complicationRenderFactory by lazy {
        ComplicationRenderFactory(context)
    }

    private var activeComplicationDataSparseArray: SparseArray<ComplicationData?> = SparseArray(Constants.COMPLICATION_IDS.size)

    fun initialize(width: Int, height: Int) {
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

    // Draw
    fun dataUpdate(complicationId: Int, complicationData: ComplicationData?) {
        activeComplicationDataSparseArray.put(complicationId, complicationData)
    }

    fun drawComplications(canvas: Canvas, isAmbient: Boolean, calendar: Calendar) {
        val currentTimeMillis = calendar.timeInMillis
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

    // Tap
    fun complicationTap(x: Int, y: Int) {
        val tappedComplicationId = getTappedComplicationId(x, y)
        if (tappedComplicationId != -1) {
            onComplicationTap(tappedComplicationId)
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
                    Log.e("ComplicationRenderer", "On complication tap action error " + e)
                }
            } else if (complicationData.type == ComplicationData.TYPE_NO_PERMISSION) {
                // Watch face does not have permission to receive complication data, so launch permission request.
                val componentName = ComponentName(context, SlateWatchFaceService::class.java)
                val permissionRequestIntent = ComplicationHelperActivity.createPermissionRequestHelperIntent(context, componentName)
                context.startActivity(permissionRequestIntent)
            }
        }
    }
}