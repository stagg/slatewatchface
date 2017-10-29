package ca.joshstagg.slate.complication

import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF

internal abstract class CircularComplicationRenderer : ComplicationRenderer {

    private val pathClip = Path()
    private val rectF = RectF()

    override final fun render(render: Render) = with(render) {
        // Set the circular clip
        clip(rect)

        // Clip and do the complication drawing
        canvas.save()
        canvas.clipPath(pathClip)
        // Fill
        canvas.drawColor(paints.complicationFill.color)
        renderInBounds()
        canvas.restore()
        // Draw bounding edge
        canvas.drawPath(pathClip, paints.complicationEdge)
    }

    protected open fun Render.renderInBounds() {}

    override final fun ambientRender(render: Render) = with(render) {
        // Set the circular clip
        clip(rect)
        // Clip for complication drawing
        canvas.save()
        canvas.clipPath(pathClip)
        ambientRenderInBounds()
        // Restore previous clip state
        canvas.restore()
    }

    protected open fun Render.ambientRenderInBounds() {}

    private fun clip(rect: Rect) {
        rectF.set(rect)
        pathClip.reset()
        pathClip.moveTo(rectF.left, rectF.top)
        pathClip.addArc(rectF, 0f, 360f)
    }
}