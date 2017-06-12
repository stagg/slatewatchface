package ca.joshstagg.slate.complication

import android.content.Context
import android.support.wearable.complications.ComplicationData

internal class ComplicationRenderFactory(context: Context) {

    private val mTextRender = TextComplicationRenderer(context)
    private val mIconRender = IconComplicationRenderer(context)
    private val mSmallImageRender = SmallImageComplicationRender(context)
    private val mRangeRender = RangeComplicationRenderer(context)
    private val mEmptyRender = object : ComplicationRenderer {}

    internal fun rendererFor(type: Int): ComplicationRenderer = when (type) {
        ComplicationData.TYPE_SHORT_TEXT, ComplicationData.TYPE_NO_PERMISSION -> mTextRender
        ComplicationData.TYPE_SMALL_IMAGE -> mSmallImageRender
        ComplicationData.TYPE_ICON -> mIconRender
        ComplicationData.TYPE_RANGED_VALUE -> mRangeRender
        else -> mEmptyRender
    }
}