package ca.joshstagg.slate

import android.graphics.Paint
import android.graphics.Typeface

internal class SlatePaints {

    val hour: Paint = Paint()
    val minute: Paint = Paint()
    val second: Paint = Paint()
    val center: Paint = Paint()
    val tick: Paint = Paint()
    val complicationText: Paint = Paint()
    val complicationMainText: Paint = Paint()
    val complicationSubText: Paint = Paint()

    var accentHandColor = Constants.ACCENT_COLOR_DEFAULT
        set(value) {
            if (field != value) {
                field = value
                second.color = value
            }
        }
    private val mTickColor = 0x64D5D5D6
    private val mPrimaryHandColor = 0xfff5f5f5.toInt()
    private val mShadowColor = 0xaa000000.toInt()
    private val mComplicationColor = 0x80FFFFFF.toInt()

    init {
        hour.color = mPrimaryHandColor
        hour.strokeWidth = 9f
        hour.isAntiAlias = true
        hour.strokeCap = Paint.Cap.BUTT
        hour.setShadowLayer(5f, 0f, 0f, mShadowColor)

        minute.color = mPrimaryHandColor
        minute.strokeWidth = 9f
        minute.isAntiAlias = true
        minute.strokeCap = Paint.Cap.BUTT
        minute.setShadowLayer(4f, 0f, 0f, mShadowColor)

        second.color = accentHandColor
        second.strokeWidth = 4f
        second.isAntiAlias = true
        second.strokeCap = Paint.Cap.BUTT
        second.setShadowLayer(6f, 0f, 0f, mShadowColor)

        center.color = mPrimaryHandColor
        center.strokeWidth = 8f
        center.isAntiAlias = true
        center.strokeCap = Paint.Cap.BUTT

        tick.color = mTickColor
        tick.strokeWidth = 4f
        tick.isAntiAlias = true
        tick.setShadowLayer(1f, 0f, 0f, mShadowColor)

        complicationText.color = mComplicationColor
        complicationText.textSize = Constants.COMPLICATION_TEXT_SIZE
        complicationText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        complicationText.textAlign = Paint.Align.CENTER
        complicationText.isAntiAlias = true

        complicationMainText.color = mComplicationColor
        complicationMainText.textSize = Constants.COMPLICATION_MAIN_TEXT_SIZE
        complicationMainText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        complicationMainText.textAlign = Paint.Align.CENTER
        complicationMainText.isAntiAlias = true

        complicationSubText.color = mComplicationColor
        complicationSubText.textSize = Constants.COMPLICATION_SUB_TEXT_SIZE
        complicationSubText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        complicationSubText.textAlign = Paint.Align.CENTER
        complicationSubText.isAntiAlias = true
    }

    fun setAntiAlias(antiAlias: Boolean) {
        hour.isAntiAlias = antiAlias
        minute.isAntiAlias = antiAlias
        second.isAntiAlias = antiAlias
        center.isAntiAlias = antiAlias
        tick.isAntiAlias = antiAlias
        complicationText.isAntiAlias = antiAlias
        complicationMainText.isAntiAlias = antiAlias
        complicationSubText.isAntiAlias = antiAlias
    }
}
