package ca.joshstagg.slate.complication

import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect

internal abstract class CircularComplicationRenderer : ComplicationRenderer {

    val pathClip = Path()
    val paint = Paint()

    init {
        paint.isAntiAlias = true
        paint.setARGB(255, 80, 80, 80)
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f
    }

    override final fun render(render: Render) {
        // Set the circular clip
        clip(render.rect)

        // Clip and do the complication drawing
        render.canvas.save()
        render.canvas.clipPath(pathClip)
        renderInBounds(render)
        render.canvas.restore()

        // Draw bounding edge
        render.canvas.drawPath(pathClip, paint)
    }

    protected open fun renderInBounds(render: Render) {}

    override final fun ambientRender(render: Render) {
        clip(render.rect)

        // Clip for complication drawing
        render.canvas.save()
        render.canvas.clipPath(pathClip)
        ambientRenderInBounds(render)
        render.canvas.restore()
    }

    protected open fun ambientRenderInBounds(render: Render) {}

    private fun clip(rect: Rect) {
        val left = rect.left.toFloat()
        val top = rect.top.toFloat()
        val right = rect.right.toFloat()
        val bottom = rect.bottom.toFloat()
        pathClip.reset()
        pathClip.moveTo(left, top)
        pathClip.addArc(left, top, right, bottom, 0f, 360f)
    }
}