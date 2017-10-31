package ca.joshstagg.slate.complication

import android.content.Context
import android.graphics.drawable.Icon

internal class IconComplicationRenderer(private val context: Context) : CircularComplicationRenderer() {

    override fun Render.renderInBounds() {
        complicationData.icon?.let {
            renderIcon(it)
        }
    }

    override fun Render.ambientRenderInBounds() {
        complicationData.burnInProtectionIcon?.let {
            renderIcon(it)
        }
    }

    private fun Render.renderIcon(icon: Icon) {
        icon.loadDrawable(context)?.let {
            val w = rect.width() / 3
            val h = rect.height() / 3
            it.setTint(paints.complicationTint)
            it.setBounds(rect.left + w, rect.top + h, rect.right - w, rect.bottom - h)
            it.draw(canvas)
        }
    }
}