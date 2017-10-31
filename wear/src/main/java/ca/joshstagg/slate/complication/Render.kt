package ca.joshstagg.slate.complication

import android.graphics.Canvas
import android.graphics.Rect
import android.support.wearable.complications.ComplicationData
import ca.joshstagg.slate.SlatePaints

internal class Render(val canvas: Canvas,
                      val rect: Rect,
                      val currentTimeMills: Long,
                      val paints: SlatePaints,
                      val complicationData: ComplicationData)