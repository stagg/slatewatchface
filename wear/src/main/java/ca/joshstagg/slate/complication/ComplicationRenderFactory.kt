package ca.joshstagg.slate.complication

import android.content.Context
import android.support.wearable.complications.ComplicationData

internal class ComplicationRenderFactory(context: Context) {

    private val textRender = TextComplicationRenderer(context)
    private val iconRender = IconComplicationRenderer()
    private val smallImageRender = SmallImageComplicationRender()
    private val rangeRender = RangeComplicationRenderer(context)

    internal fun rendererFor(type: Int): ComplicationRenderer = when (type) {
        ComplicationData.TYPE_SHORT_TEXT,
        ComplicationData.TYPE_NO_PERMISSION,
        ComplicationData.TYPE_NO_DATA -> textRender
        ComplicationData.TYPE_SMALL_IMAGE -> smallImageRender
        ComplicationData.TYPE_ICON -> iconRender
        ComplicationData.TYPE_RANGED_VALUE -> rangeRender
        else -> EMPTY_RENDER
    }

    companion object {
        val EMPTY_RENDER = object : ComplicationRenderer {}
    }
}