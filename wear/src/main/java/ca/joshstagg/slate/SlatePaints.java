package ca.joshstagg.slate;

import android.graphics.Paint;

class SlatePaints {

    final Paint hourPaint;
    final Paint minutePaint;
    final Paint centerPaint;
    final Paint secondPaint;
    final Paint tickPaint;

    private int mInteractiveSecondHandColor;

    SlatePaints() {
        hourPaint = new Paint();
        minutePaint = new Paint();
        secondPaint = new Paint();
        centerPaint = new Paint();
        tickPaint = new Paint();
    }

    void onCreate() {
        hourPaint.setARGB(255, 245, 245, 245);
        hourPaint.setStrokeWidth(9.f);
        hourPaint.setAntiAlias(true);
        hourPaint.setStrokeCap(Paint.Cap.BUTT);
        hourPaint.setShadowLayer(5f, 0, 0, 0xaa000000);

        minutePaint.setARGB(255, 245, 245, 245);
        minutePaint.setStrokeWidth(9.f);
        minutePaint.setAntiAlias(true);
        minutePaint.setStrokeCap(Paint.Cap.BUTT);
        minutePaint.setShadowLayer(4f, 0, 0, 0xaa000000);

        secondPaint.setColor(mInteractiveSecondHandColor);
        secondPaint.setStrokeWidth(4.f);
        secondPaint.setAntiAlias(true);
        secondPaint.setStrokeCap(Paint.Cap.BUTT);
        secondPaint.setShadowLayer(6f, 0, 0, 0xaa000000);

        centerPaint.setARGB(255, 245, 245, 245);
        centerPaint.setStrokeWidth(8.f);
        centerPaint.setAntiAlias(true);
        centerPaint.setStrokeCap(Paint.Cap.BUTT);

        tickPaint.setARGB(100, 213, 213, 213);
        tickPaint.setStrokeWidth(4.f);
        tickPaint.setAntiAlias(true);
        tickPaint.setShadowLayer(1f, 0, 0, 0xaa000000);
    }

    void setAntiAlias(boolean antiAlias) {
        hourPaint.setAntiAlias(antiAlias);
        minutePaint.setAntiAlias(antiAlias);
        secondPaint.setAntiAlias(antiAlias);
        tickPaint.setAntiAlias(antiAlias);
    }

    void setAccentColor(int accentColor) {
        if (mInteractiveSecondHandColor != accentColor) {
            mInteractiveSecondHandColor = accentColor;
            secondPaint.setColor(mInteractiveSecondHandColor);
        }
    }
}
