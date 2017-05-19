package ca.joshstagg.slate.complication

import android.content.Context

internal class TextComplicationRenderer(val context: Context) : CircularComplicationRenderer() {

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
        val mainMessage = mainText.getText(context, render.currentTimeMills)

        val textHeight = render.paints.complicationText.textSize
        val x = render.rect.centerX().toFloat()
        if (subText != null) {
            val subMessage = subText.getText(context, render.currentTimeMills)
            val y = render.rect.centerY().toFloat()
            render.canvas.drawText(mainMessage, 0, mainMessage.length, x, y, render.paints.complicationMainText)
            render.canvas.drawText(subMessage, 0, subMessage.length, x, y + textHeight, render.paints.complicationSubText)
        } else {
            val y = render.rect.centerY() + textHeight / 3
            render.canvas.drawText(mainMessage, 0, mainMessage.length, x, y, render.paints.complicationText)
        }
    }
}