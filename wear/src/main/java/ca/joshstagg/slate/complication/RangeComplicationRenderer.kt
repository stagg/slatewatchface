package ca.joshstagg.slate.complication

import android.content.Context
import android.graphics.Path
import android.graphics.RectF

internal class RangeComplicationRenderer(private val context: Context) :
    CircularComplicationRenderer() {

    private val pathRange = Path()
    private val rectF = RectF()

    override fun Render.renderInBounds() {
        renderText(context, complicationData.icon)

        val percent = complicationData.range.let { (value, min, max) ->
            Math.abs((value - min) / (max - min))
        }
        rectF.set(rect)
        pathRange.reset()
        pathRange.moveTo(rectF.left, rectF.top)

        rectF.inset(3f, 3f)
        pathRange.addArc(rectF, 270f, percent * 360)
        canvas.drawPath(pathRange, paints.complicationSecondary)
    }

    override fun Render.ambientRenderInBounds() {
        renderText(context, complicationData.ambientIcon)
    }
}