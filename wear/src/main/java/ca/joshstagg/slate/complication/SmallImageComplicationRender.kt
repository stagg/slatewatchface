package ca.joshstagg.slate.complication

import android.graphics.drawable.Drawable
import android.support.wearable.complications.ComplicationData

internal class SmallImageComplicationRender :
    CircularComplicationRenderer() {

    override fun Render.renderInBounds() {
        complicationData.smallImage?.let {
            when (complicationData.imageStyle) {
                ComplicationData.IMAGE_STYLE_PHOTO -> renderPhoto(it)
                ComplicationData.IMAGE_STYLE_ICON -> renderIcon(it)
            }
        }
    }

    private fun Render.renderPhoto(drawable: Drawable) {
        drawable.bounds = rect
        drawable.draw(canvas)
    }

    private fun Render.renderIcon(drawable: Drawable) {
        val w = rect.width() / 5
        val h = rect.height() / 5
        drawable.setBounds(rect.left + w, rect.top + h, rect.right - w, rect.bottom - h)
        drawable.draw(canvas)
    }
}