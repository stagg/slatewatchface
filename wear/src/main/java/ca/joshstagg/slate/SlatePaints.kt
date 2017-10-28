package ca.joshstagg.slate

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import ca.joshstagg.slate.config.Config

class SlatePaints(context: Context, scaleFactor: Float = 1f) {

    private val scale: Float = scaleFactor * context.resources.displayMetrics.density

    val centerRadius = 6f  * scale
    val centerSecondRadius = 4f  * scale

    val notificationOuterRadius = 5f  * scale
    val notificationInnerRadius = 3f  * scale

    val innerTickRadius = 9 * scale
    val largeInnerTickRadius = 21 * scale

    val secLength = 8 * scale
    val minLength = 13 * scale
    val hrLength = 35 * scale

    val secStart = -20 * scale
    val notificationOffset = 18 * scale

    val hour: Paint = Paint()
    val minute: Paint = Paint()
    val second: Paint = Paint()
    val center: Paint = Paint()
    val tick: Paint = Paint()
    val complicationText: Paint = Paint()
    val complicationMainText: Paint = Paint()
    val complicationSubText: Paint = Paint()
    val complicationEdge: Paint = Paint()
    val complicationSecondary: Paint = Paint()

    var accentHandColor = Config().accentColor
        set(value) {
            if (field != value) {
                field = value
                second.color = value
            }
        }
    private val mTickColor = 0x64D5D5D6
    private val mPrimaryHandColor = 0xfff5f5f5.toInt()
    private val mShadowColor = 0xaa000000.toInt()
    private val primaryComplicationColor = 0xF4FFFFFF.toInt()

    val complicationTint: Int
        get() {
            return primaryComplicationColor
        }

    init {
        hour.color = mPrimaryHandColor
        hour.strokeWidth = 6f * scale
        hour.isAntiAlias = true
        hour.strokeCap = Paint.Cap.BUTT
        hour.setShadowLayer(2.5f * scale, 0f, 0f, mShadowColor)

        minute.color = mPrimaryHandColor
        minute.strokeWidth = 5f * scale
        minute.isAntiAlias = true
        minute.strokeCap = Paint.Cap.BUTT
        minute.setShadowLayer(2f * scale, 0f, 0f, mShadowColor)

        second.color = accentHandColor
        second.strokeWidth = 3f * scale
        second.isAntiAlias = true
        second.strokeCap = Paint.Cap.BUTT
        second.setShadowLayer(3f * scale, 0f, 0f, mShadowColor)

        center.color = mPrimaryHandColor
        center.strokeWidth = 5f * scale
        center.isAntiAlias = true
        center.strokeCap = Paint.Cap.BUTT

        tick.color = mTickColor
        tick.strokeWidth = 3f * scale
        tick.isAntiAlias = true
        tick.setShadowLayer(.5f * scale, 0f, 0f, mShadowColor)

        complicationText.color = primaryComplicationColor
        complicationText.textSize = Constants.COMPLICATION_TEXT_SIZE
        complicationText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        complicationText.textAlign = Paint.Align.CENTER
        complicationText.isAntiAlias = true

        complicationMainText.color = primaryComplicationColor
        complicationMainText.textSize = Constants.COMPLICATION_MAIN_TEXT_SIZE
        complicationMainText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        complicationMainText.textAlign = Paint.Align.CENTER
        complicationMainText.isAntiAlias = true

        complicationSubText.color = primaryComplicationColor
        complicationSubText.textSize = Constants.COMPLICATION_SUB_TEXT_SIZE
        complicationSubText.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        complicationSubText.textAlign = Paint.Align.CENTER
        complicationSubText.isAntiAlias = true

        complicationEdge.setARGB(255, 30, 30, 30)
        complicationEdge.strokeCap = Paint.Cap.ROUND
        complicationEdge.strokeJoin = Paint.Join.ROUND
        complicationEdge.style = Paint.Style.STROKE
        complicationEdge.strokeWidth = 2f * scale
        complicationEdge.isAntiAlias = true

        complicationSecondary.setARGB(255, 155, 155, 155)
        complicationSecondary.strokeCap = Paint.Cap.ROUND
        complicationSecondary.strokeJoin = Paint.Join.ROUND
        complicationSecondary.style = Paint.Style.STROKE
        complicationSecondary.strokeWidth = 3f * scale
        complicationSecondary.isAntiAlias = true
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
        complicationEdge.isAntiAlias = antiAlias
        complicationSecondary.isAntiAlias = antiAlias
    }
}
