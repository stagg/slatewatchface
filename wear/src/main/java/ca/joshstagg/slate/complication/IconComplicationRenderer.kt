package ca.joshstagg.slate.complication

import android.graphics.drawable.Drawable

internal class IconComplicationRenderer : CircularComplicationRenderer() {

    override fun Render.renderInBounds() {
        complicationData.icon?.let {
            renderIcon(it)
        }
    }

    override fun Render.ambientRenderInBounds() {
        complicationData.ambientIcon?.let {
            renderIcon(it)
        }
    }

    private fun Render.renderIcon(drawable: Drawable) {
        val w = rect.width() / 3
        val h = rect.height() / 3
        drawable.apply {
            setTint(paints.complicationTint)
            setBounds(rect.left + w, rect.top + h, rect.right - w, rect.bottom - h)
            draw(canvas)
        }
    }
}