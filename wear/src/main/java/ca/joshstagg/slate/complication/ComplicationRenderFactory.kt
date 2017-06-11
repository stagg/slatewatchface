package ca.joshstagg.slate.complication

import android.content.Context
import android.support.wearable.complications.ComplicationData

internal class ComplicationRenderFactory(context: Context) {

    private val mTextRender = TextComplicationRenderer(context)
    private val mIconRender = IconComplicationRenderer(context)
    private val mSmallImageRender = SmallImageComplicationRender(context)
    private val mEmptyRender = object : ComplicationRenderer {}

    internal fun renderFor(type: Int): ComplicationRenderer {
        val render: ComplicationRenderer
        when (type) {
            ComplicationData.TYPE_SHORT_TEXT, ComplicationData.TYPE_NO_PERMISSION -> {
                render = mTextRender
            }
            ComplicationData.TYPE_SMALL_IMAGE -> {
                render = mSmallImageRender
            }
            ComplicationData.TYPE_ICON -> {
                render = mIconRender
            }
            else -> {
                render = mEmptyRender
            }
        }
        return render
    }
}