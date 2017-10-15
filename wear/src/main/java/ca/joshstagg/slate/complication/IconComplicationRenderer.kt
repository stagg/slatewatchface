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
        val drawable = icon.loadDrawable(context)
        val w = rect.width() / 3
        val h = rect.height() / 3
        drawable.setTint(paints.complicationTint)
        drawable.setBounds(rect.left + w, rect.top + h, rect.right - w, rect.bottom - h)
        drawable.draw(canvas)
    }
}