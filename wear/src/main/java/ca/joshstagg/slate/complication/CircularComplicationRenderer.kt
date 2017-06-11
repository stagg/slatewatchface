package ca.joshstagg.slate.complication

import android.graphics.Paint
import android.graphics.Path

internal abstract class CircularComplicationRenderer : ComplicationRenderer {

    val pathEdge = Path()
    val pathClip = Path()

    override fun render(render: Render) {
        val rect = render.rect
        val left = rect.left.toFloat()
        val top = rect.top.toFloat()
        val right = rect.right.toFloat()
        val bottom = rect.bottom.toFloat()

        val radius: Float = (rect.width() / 2).toFloat()

        val paint = Paint()
        paint.isAntiAlias = true
        paint.setARGB(80, 80, 80, 80)
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f

        pathEdge.reset()
        pathEdge.moveTo(left, top)
        pathEdge.addCircle(rect.centerX().toFloat(), rect.centerY().toFloat(), radius, Path.Direction.CW)
        render.canvas.drawPath(pathEdge, paint)

        pathClip.reset()
        pathClip.moveTo(left + 2f, top + 2f)
        pathClip.addArc(left + 2f, top + 2f, right - 2f, bottom - 2f, 0f, 360f)
        render.canvas.clipPath(pathClip)
    }
}