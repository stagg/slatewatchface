package ca.joshstagg.slate.complication

import android.content.Context
import android.graphics.drawable.Icon
import android.support.wearable.complications.ComplicationData

internal class SmallImageComplicationRender(val context: Context) : CircularComplicationRenderer() {

    override fun render(render: Render) {
        super.render(render)
        render.complicationData.smallImage?.let{
            when (render.complicationData.imageStyle) {
                ComplicationData.IMAGE_STYLE_PHOTO -> {
                    renderPhoto(render, it)
                }
                else -> { // ComplicationData.IMAGE_STYLE_ICON
                    renderIcon(render, it)
                }
            }

        }
    }

    override fun ambientRender(render: Render) {
        super.ambientRender(render)
        // No render
    }


    private fun renderPhoto(render: Render, icon: Icon) {
        val drawable = icon.loadDrawable(context)
        drawable.bounds = render.rect
        drawable.draw(render.canvas)
    }

    private fun renderIcon(render: Render, icon: Icon) {
        val drawable = icon.loadDrawable(context)
        val rect = render.rect
        val w = rect.width() / 3
        val h = rect.height() / 3
        drawable.setBounds(rect.left + w, rect.top + h, rect.right - w, rect.bottom - h)
        drawable.draw(render.canvas)
    }
}