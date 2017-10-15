package ca.joshstagg.slate.complication

import android.content.Context

internal class TextComplicationRenderer(private val context: Context) : CircularComplicationRenderer() {

    override fun Render.renderInBounds() {
        renderText(context, complicationData.icon)
    }

    override fun Render.ambientRenderInBounds() {
        renderText(context, complicationData.burnInProtectionIcon)
    }
}