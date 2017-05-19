package ca.joshstagg.slate.complication

import android.graphics.Paint

internal abstract class CircularComplicationRenderer : ComplicationRenderer {

    override fun render(render: Render) {
        val radius: Float = (render.rect.width() / 2).toFloat()
        val cx = render.rect.centerX().toFloat()
        val cy = render.rect.centerY().toFloat()

        val temp2 = Paint()
        temp2.isAntiAlias = true
        temp2.setARGB(80, 80, 80, 80)
        render.canvas.drawCircle(cx, cy, radius, temp2)

        //todo convert to paths
        val temp = Paint()
        temp.isAntiAlias = true
        temp.setARGB(60, 0, 0, 0)
        render.canvas.drawCircle(cx, cy, radius - 4f, temp)
    }
}