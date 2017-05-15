package ca.joshstagg.slate;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.view.Gravity;
import android.view.SurfaceHolder;

/**
 * Slate ca.joshstagg.slate
 * Copyright 2017 Josh Stagg
 */
public class SlateWatchFaceService extends CanvasWatchFaceService {

    @Override
    public Engine onCreateEngine() {
        Context context = this.getApplicationContext();
        ConfigService configService = Slate.getInstance().getConfigService();
        return new Engine(this, configService, new SlateTime(context), new SlatePaints());
    }

    private class Engine extends CanvasWatchFaceService.Engine {

        private final Context mContext;
        private final ConfigService mConfigService;
        private final SlateTime mSlateTime;
        private final SlatePaints mPaints;
        private final Handler mUpdateTimeHandler;

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        private boolean mLowBitAmbient;
        private Bitmap mBackgroundBitmap;
        private Bitmap mBackgroundScaledBitmap;

        Engine(Context context, ConfigService slateConfigService, SlateTime slateTime, SlatePaints paints) {
            mContext = context;
            mConfigService = slateConfigService;
            mSlateTime = slateTime;
            mPaints = paints;
            mUpdateTimeHandler = new EngineHandler(this, Looper.getMainLooper());
        }

        /**
         * Initialize the watch face
         *
         * @param holder SurfaceHolder that the watch face will use
         */
        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);
            setWatchFaceStyle(new WatchFaceStyle.Builder(SlateWatchFaceService.this)
                    .setStatusBarGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP)
                    .build());

            Drawable backgroundDrawable = ContextCompat.getDrawable(mContext, R.drawable.bg);
            mBackgroundBitmap = ((BitmapDrawable) backgroundDrawable).getBitmap();
            mPaints.onCreate();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            /* getTimeNow device features (burn-in, low-bit ambient) */
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
        }

        @Override
        public void onTimeTick() {
            /* the time changed */
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            /* the wearable switched between modes */
            super.onAmbientModeChanged(inAmbientMode);
            if (mLowBitAmbient) {
                boolean antiAlias = !inAmbientMode;
                mPaints.setAntiAlias(antiAlias);
            }
            invalidate();
            restartTimer();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            int width = bounds.width();
            int height = bounds.height();

            // Draw the background, scaled to fit.
            if (mBackgroundScaledBitmap == null
                    || mBackgroundScaledBitmap.getWidth() != width
                    || mBackgroundScaledBitmap.getHeight() != height) {
                mBackgroundScaledBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap, width, height, true /* filter */);
            }

            // Find the center. Ignore the window insets so that, on round watches with a
            // "chin", the watch face is centered on the entire screen, not just the usable
            // portion.
            float centerX = width / 2f;
            float centerY = height / 2f;

            drawTicks(canvas, centerX, centerY);
            drawHands(canvas, centerX, centerY);
        }

        private void drawTicks(Canvas canvas, float centerX, float centerY) {
            if (!isInAmbientMode()) {
                canvas.drawBitmap(mBackgroundScaledBitmap, 0, 0, null);
                float innerTickRadius = centerX - 18;
                float largeInnerTickRadius = centerX - 42;
                for (int tickIndex = 0; tickIndex < 12; tickIndex++) {
                    float tickRot = (float) (tickIndex * Math.PI * 2 / 12);
                    float innerX;
                    float innerY;
                    if (tickIndex == 0 || tickIndex == 3 || tickIndex == 6 || tickIndex == 9) {
                        innerX = (float) Math.sin(tickRot) * largeInnerTickRadius;
                        innerY = (float) -Math.cos(tickRot) * largeInnerTickRadius;
                    } else {
                        innerX = (float) Math.sin(tickRot) * innerTickRadius;
                        innerY = (float) -Math.cos(tickRot) * innerTickRadius;
                    }
                    float outerX = (float) Math.sin(tickRot) * centerX;
                    float outerY = (float) -Math.cos(tickRot) * centerX;
                    canvas.drawLine(centerX + innerX, centerY + innerY,
                            centerX + outerX, centerY + outerY, mPaints.tickPaint);
                }
            } else {
                canvas.drawColor(Color.BLACK);
            }
        }

        private void drawHands(Canvas canvas, float centerX, float centerY) {
            Calendar calendar = mSlateTime.getTimeNow();
            Config config = mConfigService.getConfig();

            float millRot = ((calendar.getTimeInMillis() % 60000) / 1000f) / 30f * (float) Math.PI;
            float secRot = calendar.get(Calendar.SECOND) / 30f * (float) Math.PI;
            int minutes = calendar.get(Calendar.MINUTE);
            float minRot = minutes / 30f * (float) Math.PI;
            float hrRot = ((calendar.get(Calendar.HOUR) + (minutes / 60f)) / 6f) * (float) Math.PI;

            float secLength = centerX - 16;
            float minLength = centerX - 26;
            float hrLength = centerX - 70;

            float hrX = (float) Math.sin(hrRot) * hrLength;
            float hrY = (float) -Math.cos(hrRot) * hrLength;
            canvas.drawLine(centerX, centerY, centerX + hrX, centerY + hrY, mPaints.hourPaint);

            float minX = (float) Math.sin(minRot) * minLength;
            float minY = (float) -Math.cos(minRot) * minLength;
            canvas.drawCircle(centerX, centerY, 10f, mPaints.minutePaint);
            canvas.drawLine(centerX, centerY, centerX + minX, centerY + minY, mPaints.minutePaint);
            canvas.drawCircle(centerX, centerY, 10f, mPaints.centerPaint);

            if (!isInAmbientMode()) {
                mPaints.setAccentColor(config.getAccentColor());

                float secRotate = config.isSmoothMovement() ? millRot : secRot;
                float secStartX = (float) Math.sin(secRotate) * -40;
                float secStartY = (float) -Math.cos(secRotate) * -40;
                float secX = (float) Math.sin(secRotate) * secLength;
                float secY = (float) -Math.cos(secRotate) * secLength;

                canvas.drawLine(centerX + secStartX, centerY + secStartY, centerX + secX, centerY + secY, mPaints.secondPaint);
                canvas.drawCircle(centerX, centerY, 6f, mPaints.secondPaint);
            }
        }

        /**
         * The watch face became visible or invisible
         *
         * @param visible If the watch face is visible or not
         */
        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);
            if (visible) {
                mConfigService.connect();
                mSlateTime.registerReceiver();
                mSlateTime.reset();
            } else {
                mSlateTime.unregisterReceiver();
                mConfigService.disconnect();
            }
            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            restartTimer();
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(Constants.MSG_UPDATE_TIME);
            super.onDestroy();
        }

        private void updateTime() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long updateRate = mConfigService.getConfig().getUpdateRate();
                long delayMs = updateRate - (System.currentTimeMillis() % updateRate);
                mUpdateTimeHandler.sendEmptyMessageDelayed(Constants.MSG_UPDATE_TIME, delayMs);
            }
        }

        /**
         * Whether the timer should be running depends on whether we're in ambient mode
         * (as well as whether we're visible), so we may need to start or stop the timer.
         * <p>
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void restartTimer() {
            mUpdateTimeHandler.removeMessages(Constants.MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(Constants.MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }
    }

    private static class EngineHandler extends Handler {

        private final Engine mEngine;

        EngineHandler(Engine engine, Looper looper) {
            super(looper);
            mEngine = engine;
        }

        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case Constants.MSG_UPDATE_TIME:
                    mEngine.updateTime();
                    break;
            }
        }
    }
}
