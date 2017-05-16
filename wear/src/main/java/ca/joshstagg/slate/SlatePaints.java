package ca.joshstagg.slate;

import android.graphics.Paint;
import android.graphics.Typeface;

class SlatePaints {

    final Paint hourPaint;
    final Paint minutePaint;
    final Paint secondPaint;
    final Paint centerPaint;
    final Paint tickPaint;
    final Paint complicationPaint;

    private int mTickColor = 0x64D5D5D6;
    private int mAccentHandColor = Constants.ACCENT_COLOR_DEFAULT;
    private int mPrimaryHandColor = 0xfff5f5f5;
    private int mShadowColor = 0xaa000000;
    private int mComplicationColor = 0x80FFFFFF;

    SlatePaints() {
        hourPaint = new Paint();
        hourPaint.setColor(mPrimaryHandColor);
        hourPaint.setStrokeWidth(9.f);
        hourPaint.setAntiAlias(true);
        hourPaint.setStrokeCap(Paint.Cap.BUTT);
        hourPaint.setShadowLayer(5f, 0, 0, mShadowColor);

        minutePaint = new Paint();
        minutePaint.setColor(mPrimaryHandColor);
        minutePaint.setStrokeWidth(9.f);
        minutePaint.setAntiAlias(true);
        minutePaint.setStrokeCap(Paint.Cap.BUTT);
        minutePaint.setShadowLayer(4f, 0, 0, mShadowColor);

        secondPaint = new Paint();
        secondPaint.setColor(mAccentHandColor);
        secondPaint.setStrokeWidth(4.f);
        secondPaint.setAntiAlias(true);
        secondPaint.setStrokeCap(Paint.Cap.BUTT);
        secondPaint.setShadowLayer(6f, 0, 0, mShadowColor);

        centerPaint = new Paint();
        centerPaint.setColor(mPrimaryHandColor);
        centerPaint.setStrokeWidth(8.f);
        centerPaint.setAntiAlias(true);
        centerPaint.setStrokeCap(Paint.Cap.BUTT);

        tickPaint = new Paint();
        tickPaint.setColor(mTickColor);
        tickPaint.setStrokeWidth(4.f);
        tickPaint.setAntiAlias(true);
        tickPaint.setShadowLayer(1f, 0, 0, mShadowColor);

        complicationPaint = new Paint();
        complicationPaint.setColor(mComplicationColor);
        complicationPaint.setTextSize(Constants.COMPLICATION_TEXT_SIZE);
        complicationPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
        complicationPaint.setAntiAlias(true);
    }

    void setAntiAlias(boolean antiAlias) {
        hourPaint.setAntiAlias(antiAlias);
        minutePaint.setAntiAlias(antiAlias);
        secondPaint.setAntiAlias(antiAlias);
        centerPaint.setAntiAlias(antiAlias);
        tickPaint.setAntiAlias(antiAlias);
        complicationPaint.setAntiAlias(antiAlias);
    }

    void setAccentColor(int accentColor) {
        if (mAccentHandColor != accentColor) {
            mAccentHandColor = accentColor;
            secondPaint.setColor(mAccentHandColor);
        }
    }
}
