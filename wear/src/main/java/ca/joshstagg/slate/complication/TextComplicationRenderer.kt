package ca.joshstagg.slate.complication

import android.content.Context
import android.graphics.drawable.Icon

internal class TextComplicationRenderer(val context: Context) : CircularComplicationRenderer() {

    override fun render(render: Render) {
        super.render(render)
        renderText(render, render.complicationData.icon)
    }

    // todo should also check burn-in to remove text
    override fun ambientRender(render: Render) {
        super.ambientRender(render)
        renderText(render, render.complicationData.burnInProtectionIcon)
    }

    private fun renderText(render: Render, icon: Icon?) {
        val mainText = render.complicationData.shortText
        val subText = render.complicationData.shortTitle

        val mainMessage = mainText.getText(context, render.currentTimeMills)

        val textHeight = render.paints.complicationText.textSize
        val x = render.rect.centerX().toFloat()

        if (icon != null) {
            val drawable = icon.loadDrawable(context)
            val rect = render.rect
            val w = rect.width() * 3 / 8
            val hOff = rect.height() * 1 / 8

            drawable.setTint(render.paints.complicationColor)
            drawable.setBounds(rect.left + w, rect.top + 2 * hOff, rect.right - w, rect.bottom - 4 * hOff)
            drawable.draw(render.canvas)

            val y = render.rect.centerY().toFloat()
            render.canvas.drawText(mainMessage, 0, mainMessage.length, x, y + 2 * hOff, render.paints.complicationMainText)
        } else if (subText != null) {
            val subMessage = subText.getText(context, render.currentTimeMills)
            val y = render.rect.centerY().toFloat()
            render.canvas.drawText(subMessage, 0, subMessage.length, x, y + textHeight, render.paints.complicationSubText)
            render.canvas.drawText(mainMessage, 0, mainMessage.length, x, y, render.paints.complicationMainText)
        } else {
            val y = render.rect.centerY() + textHeight / 3
            render.canvas.drawText(mainMessage, 0, mainMessage.length, x, y, render.paints.complicationText)
        }
    }
}