package ca.joshstagg.slate.complication

import android.content.Context
import android.graphics.drawable.Icon


/*
    Internal Text renderer function that supports variations in text, title and icons
    Used by RangeComplicationRenderer and TextComplicationRenderer for DRY convenience
 */
internal fun Render.renderText(context: Context, icon: Icon?) {
    val mainText = complicationData.shortText
    val subText = complicationData.shortTitle
    val textHeight = paints.complicationText.textSize
    val x = rect.centerX().toFloat()
    var y = rect.centerY().toFloat()

    when {
        icon != null -> {
            icon.loadDrawable(context)?.let { drawable->
                var widthOffset = rect.width() / 3
                var heightOffset = rect.height() / 3
                var top = heightOffset
                var bottom = heightOffset

                mainText?.let {
                    widthOffset = rect.width() * 3 / 8
                    heightOffset = rect.height() * 1 / 8
                    top = 2 * heightOffset
                    bottom = 4 * heightOffset
                    y += 2 * heightOffset
                }

                drawable.setTint(paints.complicationTint)
                drawable.setBounds(
                        rect.left + widthOffset,
                        rect.top + top,
                        rect.right - widthOffset,
                        rect.bottom - bottom)
                drawable.draw(canvas)
            }
        }
        subText != null -> {
            val subMessage = subText.getText(context, currentTimeMills)
            canvas.drawText(subMessage, 0, subMessage.length, x, y + textHeight, paints.complicationSubText)
        }
        else -> y += textHeight / 3
    }

    mainText?.getText(context, currentTimeMills)?.let {
        canvas.drawText(it, 0, it.length, x, y, paints.complicationMainText)
    }
}