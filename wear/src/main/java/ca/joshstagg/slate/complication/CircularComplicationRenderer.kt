package ca.joshstagg.slate.complication

import android.graphics.Paint
import android.graphics.Path

internal abstract class CircularComplicationRenderer : ComplicationRenderer {

    override fun render(render: Render) {
        val radius: Float = (render.rect.width() / 2).toFloat()

        val paint = Paint()
        paint.isAntiAlias = true
        paint.setARGB(80, 80, 80, 80)
        paint.isAntiAlias = true;
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f

        val path = Path()
        path.moveTo(render.rect.left.toFloat(), render.rect.top.toFloat())
        path.addCircle(render.rect.centerX().toFloat(), render.rect.centerY().toFloat(), radius, Path.Direction.CW)
        render.canvas.drawPath(path, paint)
    }
}