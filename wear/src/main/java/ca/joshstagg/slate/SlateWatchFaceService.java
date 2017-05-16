package ca.joshstagg.slate;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.wearable.complications.ComplicationData;
import android.support.wearable.complications.ComplicationHelperActivity;
import android.support.wearable.complications.ComplicationText;
import android.support.wearable.complications.SystemProviders;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.SurfaceHolder;

/**
 * Slate ca.joshstagg.slate
 * Copyright 2017 Josh Stagg
 */
public class SlateWatchFaceService extends CanvasWatchFaceService {
    private static final String TAG = "SlateWatchFaceService";

    @Override
    public Engine onCreateEngine() {
        Context context = this.getApplicationContext();
        ConfigManager configService = Slate.getInstance().getConfigService();
        return new Engine(context, configService);
    }

    private class Engine extends CanvasWatchFaceService.Engine {

        private final Context mContext;
        private final ConfigManager mConfigService;
        private final Handler mUpdateTimeHandler;
        private final float[][] mTicks = new float[12][];

        private SlatePaints mPaints;
        private SlateTime mSlateTime;
        private Bitmap mBackgroundBitmap;
        private Bitmap mBackgroundScaledBitmap;
        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        private boolean mLowBitAmbient;
        private boolean mBurnInProtection;

        private int mWidth;
        private int mHeight;
        private int mComplicationsY;

        private SparseArray<ComplicationData> mActiveComplicationDataSparseArray;

        Engine(Context context, ConfigManager slateConfigService) {
            mContext = context;
            mConfigService = slateConfigService;
            mUpdateTimeHandler = new EngineHandler(this);
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

            mSlateTime = new SlateTime(mContext);
            mPaints = new SlatePaints();

            Drawable backgroundDrawable = ContextCompat.getDrawable(mContext, R.drawable.bg);
            mBackgroundBitmap = ((BitmapDrawable) backgroundDrawable).getBitmap();

            initializeComplication();
        }

        private void initializeComplication() {
            mActiveComplicationDataSparseArray = new SparseArray<>(Constants.COMPLICATION_IDS.length);
            setDefaultSystemComplicationProvider(Constants.LEFT_DIAL_COMPLICATION, SystemProviders.WATCH_BATTERY, ComplicationData.TYPE_SHORT_TEXT);
            setDefaultSystemComplicationProvider(Constants.RIGHT_DIAL_COMPLICATION, SystemProviders.DATE, ComplicationData.TYPE_SHORT_TEXT);
            setActiveComplications(Constants.COMPLICATION_IDS);
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
        }

        /*
         * Called when there is updated data for a complication id.
         */
        @Override
        public void onComplicationDataUpdate(int complicationId, ComplicationData complicationData) {
            // Adds/updates active complication data in the array.
            mActiveComplicationDataSparseArray.put(complicationId, complicationData);
            invalidate();
        }

        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            switch (tapType) {
                case TAP_TYPE_TAP:
                    int tappedComplicationId = getTappedComplicationId(x, y);
                    if (tappedComplicationId != -1) {
                        onComplicationTap(tappedComplicationId);
                    }
                    break;
            }
        }

        private int getTappedComplicationId(int x, int y) {
            ComplicationData complicationData;
            long currentTimeMillis = System.currentTimeMillis();

            for (int i = 0; i < Constants.COMPLICATION_IDS.length; i++) {
                complicationData = mActiveComplicationDataSparseArray.get(Constants.COMPLICATION_IDS[i]);

                if ((complicationData != null)
                        && (complicationData.isActive(currentTimeMillis))
                        && (complicationData.getType() != ComplicationData.TYPE_NOT_CONFIGURED)
                        && (complicationData.getType() != ComplicationData.TYPE_EMPTY)) {

                    Rect complicationBoundingRect = new Rect(0, 0, 0, 0);

                    switch (Constants.COMPLICATION_IDS[i]) {
                        case Constants.LEFT_DIAL_COMPLICATION:
                            complicationBoundingRect.set(
                                    0,                                          // left
                                    mComplicationsY - Constants.COMPLICATION_TAP_BUFFER,  // top
                                    (mWidth / 2),                               // right
                                    ((int) Constants.COMPLICATION_TEXT_SIZE               // bottom
                                            + mComplicationsY
                                            + Constants.COMPLICATION_TAP_BUFFER));
                            break;

                        case Constants.RIGHT_DIAL_COMPLICATION:
                            complicationBoundingRect.set(
                                    (mWidth / 2),                               // left
                                    mComplicationsY - Constants.COMPLICATION_TAP_BUFFER,  // top
                                    mWidth,                                     // right
                                    ((int) Constants.COMPLICATION_TEXT_SIZE               // bottom
                                            + mComplicationsY
                                            + Constants.COMPLICATION_TAP_BUFFER));
                            break;
                    }

                    if (complicationBoundingRect.width() > 0) {
                        if (complicationBoundingRect.contains(x, y)) {
                            return Constants.COMPLICATION_IDS[i];
                        }
                    } else {
                        Log.e(TAG, "Not a recognized complication id.");
                    }
                }
            }
            return -1;
        }

        /*
         * Fires PendingIntent associated with complication (if it has one).
         */
        private void onComplicationTap(int complicationId) {
            ComplicationData complicationData = mActiveComplicationDataSparseArray.get(complicationId);

            if (complicationData != null) {
                if (complicationData.getTapAction() != null) {
                    try {
                        complicationData.getTapAction().send();
                    } catch (PendingIntent.CanceledException e) {
                        Log.e(TAG, "On complication tap action error " + e);
                    }
                } else if (complicationData.getType() == ComplicationData.TYPE_NO_PERMISSION) {

                    // Watch face does not have permission to receive complication data, so launch
                    // permission request.
                    ComponentName componentName = new ComponentName(
                            getApplicationContext(),
                            SlateWatchFaceService.class);

                    Intent permissionRequestIntent =
                            ComplicationHelperActivity.createPermissionRequestHelperIntent(
                                    getApplicationContext(), componentName);

                    startActivity(permissionRequestIntent);
                }

            }
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            /* the wearable switched between modes */
            super.onAmbientModeChanged(inAmbientMode);
            if (mLowBitAmbient) {
                mPaints.setAntiAlias(!inAmbientMode);
            }
            invalidate();
            restartTimer();
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);

            mWidth = width;
            mHeight = height;

            /*
             * Since the height of the complications text does not change, we only have to
             * recalculate when the surface changes.
             */
            mComplicationsY = (int) ((mHeight / 2) + (mPaints.complicationPaint.getTextSize() / 3));

            initializeBackground(width, height);
            initializeTicks(width, height);
        }

        // Scale the background to fit.
        private void initializeBackground(int width, int height) {
            if (null == mBackgroundScaledBitmap || mBackgroundScaledBitmap.getWidth() != width || mBackgroundScaledBitmap.getHeight() != height) {
                mBackgroundScaledBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap, width, height, true);
            }
        }

        private void initializeTicks(int width, int height) {
            float centerX = width / 2f;
            float centerY = height / 2f;

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
                mTicks[tickIndex] = new float[]{centerX + innerX, centerY + innerY, centerX + outerX, centerY + outerY};
            }
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            int width = bounds.width();
            int height = bounds.height();

            float centerX = width / 2f;
            float centerY = height / 2f;

            boolean isAmbient = isInAmbientMode();
            Calendar calendar = mSlateTime.getTimeNow();

            drawBackground(canvas, isAmbient);
            drawComplications(canvas, isAmbient, calendar.getTimeInMillis());
            drawTicks(canvas, isAmbient);
            drawHands(canvas, isAmbient, calendar, centerX, centerY);
        }

        private void drawBackground(Canvas canvas, boolean isAmbient) {
            if (isAmbient) {
                canvas.drawColor(Color.BLACK);
            } else {
                canvas.drawBitmap(mBackgroundScaledBitmap, 0, 0, null);
            }
        }

        private void drawComplications(Canvas canvas, boolean isAmbient, long currentTimeMillis) {
            ComplicationData complicationData;
            for (int i = 0; i < Constants.COMPLICATION_IDS.length; i++) {
                complicationData = mActiveComplicationDataSparseArray.get(Constants.COMPLICATION_IDS[i]);
                if (complicationData != null && complicationData.isActive(currentTimeMillis)) {

                    // Both Short Text and No Permission Types can be rendered with the same code.
                    // No Permission will display "--" with an Intent to launch a permission prompt.
                    // If you want to support more types, just add a "else if" below with your
                    // rendering code inside.
                    if (complicationData.getType() == ComplicationData.TYPE_SHORT_TEXT
                            || complicationData.getType() == ComplicationData.TYPE_NO_PERMISSION) {

                        ComplicationText mainText = complicationData.getShortText();
                        ComplicationText subText = complicationData.getShortTitle();

                        CharSequence complicationMessage =
                                mainText.getText(getApplicationContext(), currentTimeMillis);

                        /* In most cases you would want the subText (Title) under the
                         * mainText (Text), but to keep it simple for the code lab, we are
                         * concatenating them all on one line.
                         */
                        if (subText != null) {
                            complicationMessage = TextUtils.concat(complicationMessage, " ",
                                    subText.getText(getApplicationContext(), currentTimeMillis));
                        }

                        //Log.d(TAG, "Com id: " + COMPLICATION_IDS[i] + "\t" + complicationMessage);
                        double textWidth = mPaints.complicationPaint.measureText(
                                complicationMessage,
                                0,
                                complicationMessage.length());

                        int complicationsX;

                        if (Constants.COMPLICATION_IDS[i] == Constants.LEFT_DIAL_COMPLICATION) {
                            complicationsX = (int) ((mWidth / 2) - textWidth) / 2;
                        } else {
                            // RIGHT_DIAL_COMPLICATION calculations
                            int offset = (int) ((mWidth / 2) - textWidth) / 2;
                            complicationsX = (mWidth / 2) + offset;
                        }

                        float xOffset = (float) (textWidth / 2);
                        float radius = mHeight / 8;

                        if (!isAmbient) {
                            Paint temp = new Paint();
                            temp.setAntiAlias(true);
                            temp.setARGB(60, 0, 0, 0);

                            canvas.drawCircle(complicationsX + xOffset, mHeight / 2, radius, temp);

                            Paint temp2 = new Paint();
                            temp2.setAntiAlias(true);
                            temp2.setARGB(40, 80, 80, 80);

                            canvas.drawCircle(complicationsX + xOffset, mHeight / 2, radius - 4f, temp2);
                        }
                        canvas.drawText(
                                complicationMessage,
                                0,
                                complicationMessage.length(),
                                complicationsX,
                                mComplicationsY,
                                mPaints.complicationPaint);
                    }
                }
            }
        }

        private void drawTicks(Canvas canvas, boolean isAmbient) {
            if (!isAmbient) {
                for (float[] tick : mTicks) {
                    canvas.drawLines(tick, mPaints.tickPaint);
                }
            }
        }

        private void drawHands(Canvas canvas, boolean isAmbient, Calendar calendar, float centerX, float centerY) {
            Config config = mConfigService.getConfig();

            float milliRotate = ((calendar.getTimeInMillis() % 60000) / 1000f) / 30f * (float) Math.PI;
            float secRotate = calendar.get(Calendar.SECOND) / 30f * (float) Math.PI;
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

            if (!isAmbient) {
                mPaints.setAccentColor(config.getAccentColor());

                float rotate = config.isSmoothMovement() ? milliRotate : secRotate;
                float secStartX = (float) Math.sin(rotate) * -40;
                float secStartY = (float) -Math.cos(rotate) * -40;
                float secX = (float) Math.sin(rotate) * secLength;
                float secY = (float) -Math.cos(rotate) * secLength;

                canvas.drawLine(centerX + secStartX,
                        centerY + secStartY,
                        centerX + secX,
                        centerY + secY, mPaints.secondPaint);

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
            restartTimer();
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

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(Constants.MSG_UPDATE_TIME);
            super.onDestroy();
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

        EngineHandler(Engine engine) {
            mEngine = engine;
        }

        @Override
        public void handleMessage(Message message) {
            switch (message.what) {
                case Constants.MSG_UPDATE_TIME:
                    mEngine.invalidate();
                    if (mEngine.shouldTimerBeRunning()) {
                        long updateRate = mEngine.mConfigService.getConfig().getUpdateRate();
                        long delayMs = updateRate - (System.currentTimeMillis() % updateRate);
                        mEngine.mUpdateTimeHandler.sendEmptyMessageDelayed(Constants.MSG_UPDATE_TIME, delayMs);
                    }
                    break;
            }
        }
    }
}
