package ca.joshstagg.slate.complication

import android.content.Context
import android.text.TextUtils

internal class TextComplicationRenderer(val context: Context) : CircularComplicationRenderer() {

    // Both Short Text and No Permission Types can be rendered with the same code.
    // No Permission will display "--" with an Intent to launch a permission prompt.
    // If you want to support more types, just add a "else if" below with your
    // rendering code inside.
    // Render factory here -> factory picks a complication renderer, gives it dimensions, data, and a section of canvas to draw on.
    // Keep the main location and size/positional rendering here
    override fun render(render: Render) {
        super.render(render)
        renderText(render)
    }

    // todo should also check burn-in to remove text
    override fun ambientRender(render: Render) {
        super.ambientRender(render)
        renderText(render)
    }

    private fun renderText(render: Render) {
        val mainText = render.complicationData.shortText
        val subText = render.complicationData.shortTitle

        var complicationMessage = mainText.getText(context, render.currentTimeMills)

        /* In most cases you would want the subText (Title) under the
         * mainText (Text), but to keep it simple for the code lab, we are
         * concatenating them all on one line.
         */
        if (subText != null) {
            complicationMessage = TextUtils.concat(complicationMessage, " ", subText.getText(context, render.currentTimeMills))
        }

        val textWidth = render.paints.complication.measureText(complicationMessage, 0, complicationMessage.length)
        val textHeight = render.paints.complication.textSize
        val x = render.rect.centerX() - textWidth / 2
        val y = render.rect.centerY() + textHeight / 3

        render.canvas.drawText(complicationMessage, 0, complicationMessage.length, x, y, render.paints.complication)
    }
}