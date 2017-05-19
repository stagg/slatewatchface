package ca.joshstagg.slate.complication

import android.content.Context
import android.support.wearable.complications.ComplicationData

internal class ComplicationRenderFactory(context: Context) {

    val mTextRender = TextComplicationRenderer(context)
    val mEmptyRender = object : ComplicationRenderer {}

    internal fun renderFor(type: Int): ComplicationRenderer {
        val render: ComplicationRenderer
        when (type) {
            ComplicationData.TYPE_SHORT_TEXT, ComplicationData.TYPE_NO_PERMISSION -> {
                render = mTextRender
            }
            else -> {
                render = mEmptyRender
            }
        }
        return render
    }
}