package ca.joshstagg.slate.complication

import android.graphics.Canvas
import android.graphics.Rect
import ca.joshstagg.slate.SlatePaints

internal class Render(
    var canvas: Canvas,
    var rect: Rect,
    var currentTimeMills: Long,
    val paints: SlatePaints,
    var complicationData: ComplicationRenderData
)