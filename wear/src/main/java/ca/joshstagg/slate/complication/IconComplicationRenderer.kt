package ca.joshstagg.slate.complication

import android.content.Context
import android.graphics.drawable.Icon

internal class IconComplicationRenderer(val context: Context) : CircularComplicationRenderer() {

    override fun renderInBounds(render: Render) {
        render.complicationData.icon?.let {
            renderIcon(render, it)
        }
    }

    override fun ambientRenderInBounds(render: Render) {
        render.complicationData.burnInProtectionIcon?.let {
            renderIcon(render, it)
        }
    }

    private fun renderIcon(render: Render, icon: Icon) {
        val drawable = icon.loadDrawable(context)
        val rect = render.rect
        val w = rect.width() / 3
        val h = rect.height() / 3
        drawable.setTint(render.paints.complicationColor)
        drawable.setBounds(rect.left + w, rect.top + h, rect.right - w, rect.bottom - h)
        drawable.draw(render.canvas)
    }
}