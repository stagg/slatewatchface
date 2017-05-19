package ca.joshstagg.slate.complication

import android.graphics.Canvas
import android.graphics.PointF
import android.support.wearable.complications.ComplicationData
import ca.joshstagg.slate.SlatePaints

internal class Render(val canvas: Canvas,
                      val origin: PointF,
                      val center: PointF,
                      val currentTimeMills: Long,
                      val paints: SlatePaints,
                      val complicationData: ComplicationData)