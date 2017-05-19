package ca.joshstagg.slate.complication

import android.graphics.Paint

internal abstract class CircularComplicationRenderer : ComplicationRenderer {

    override fun render(render: Render) {
        val radius: Float = render.center.x - render.origin.x
        val temp2 = Paint()
        temp2.isAntiAlias = true
        temp2.setARGB(80, 80, 80, 80)
        render.canvas.drawCircle(render.center.x, render.center.y, radius, temp2)

        //todo convert to paths
        val temp = Paint()
        temp.isAntiAlias = true
        temp.setARGB(60, 0, 0, 0)
        render.canvas.drawCircle(render.center.x, render.center.y, radius - 4f, temp)
    }
}