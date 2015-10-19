package ca.joshstagg.slate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.view.Gravity;
import android.view.SurfaceHolder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Slate ca.joshstagg.slate
 * Copyright 2014  Josh Stagg
 */
public class SlateWatchFaceService extends CanvasWatchFaceService {
    private static final String TAG = "SlateWatchFaceService";

    /**
     * Update rate in milliseconds for interactive mode. We update once a second to advance the
     * second hand.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);
    private static final long INTERACTIVE_SMOOTH_UPDATE_RATE_MS = 4;

    @Override
    public Engine onCreateEngine() {
        /* provide your watch face implementation */
        return new Engine();
    }

    /* implement service callback methods */
    private class Engine extends CanvasWatchFaceService.Engine implements DataApi.DataListener,
            GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener  {
        static final int MSG_UPDATE_TIME = 0;

        Paint mHourPaint;
        Paint mMinutePaint;
        Paint mCenterPaint;
        Paint mSecondPaint;
        Paint mTickPaint;
        Paint mTextPaint;
        boolean mMute;
        Time mTime;
        private long mUpdateRate = INTERACTIVE_UPDATE_RATE_MS;

        /** Handler to update the time once a second in interactive mode. */
        public final Handler mUpdateTimeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                switch (message.what) {
                    case MSG_UPDATE_TIME:
                        invalidate();
                        if (shouldTimerBeRunning()) {
                            long delayMs = mUpdateRate - (System.currentTimeMillis() % mUpdateRate);
                            mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
                        }
                        break;
                }
            }
        };

        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };
        final BroadcastReceiver mDayChangeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.setToNow();
                date = Integer.toString(mTime.monthDay);
                mTextPaint.getTextBounds(date, 0, date.length(), textBounds);
            }
        };
        boolean mRegisteredReceivers = false;

        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(SlateWatchFaceService.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();


        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;

        Bitmap mBackgroundBitmap;
        Bitmap mBackgroundScaledBitmap;
        int mInteractiveSecondHandColor = SlateWatchFaceUtil.COLOR_VALUE_DEFAULT;
        boolean mSmoothMode = SlateWatchFaceUtil.SMOOTH_MODE_VALUE_DEFAULT;
        boolean mShowDate = SlateWatchFaceUtil.SHOW_DATE_VALUE_DEFAULT;

        String date;
        Rect textBounds = new Rect();
        @Override
        public void onCreate(SurfaceHolder holder) {
            /* initialize your watch face */
            Logger.d(TAG, "onCreate");
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(SlateWatchFaceService.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_SHORT)
                    .setStatusBarGravity(Gravity.CENTER_HORIZONTAL| Gravity.TOP)
                    .setHotwordIndicatorGravity(Gravity.CENTER_HORIZONTAL| Gravity.BOTTOM)
                    .setViewProtection(0)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .build());

            Resources resources = SlateWatchFaceService.this.getResources();
            Drawable backgroundDrawable = resources.getDrawable(R.drawable.bg);
            mBackgroundBitmap = ((BitmapDrawable) backgroundDrawable).getBitmap();

            mHourPaint = new Paint();
            mHourPaint.setARGB(255, 245, 245, 245);
            mHourPaint.setStrokeWidth(9.f);
            mHourPaint.setAntiAlias(true);
            mHourPaint.setStrokeCap(Paint.Cap.BUTT);
            mHourPaint.setShadowLayer(5f, 0, 0, 0xaa000000);

            mMinutePaint = new Paint();
            mMinutePaint.setARGB(255, 245, 245, 245);
            mMinutePaint.setStrokeWidth(9.f);
            mMinutePaint.setAntiAlias(true);
            mMinutePaint.setStrokeCap(Paint.Cap.BUTT);
            mMinutePaint.setShadowLayer(4f, 0, 0, 0xaa000000);

            mSecondPaint = new Paint();
            mSecondPaint.setColor(mInteractiveSecondHandColor); //setARGB(255, 102, 45, 145);
            mSecondPaint.setStrokeWidth(4.f);
            mSecondPaint.setAntiAlias(true);
            mSecondPaint.setStrokeCap(Paint.Cap.BUTT);
            mSecondPaint.setShadowLayer(6f, 0, 0, 0xaa000000);

            mCenterPaint = new Paint();
            mCenterPaint.setARGB(255, 245, 245, 245);
            mCenterPaint.setStrokeWidth(8.f);
            mCenterPaint.setAntiAlias(true);
            mCenterPaint.setStrokeCap(Paint.Cap.BUTT);

            mTickPaint = new Paint();
            mTickPaint.setARGB(100, 213, 213, 213);
            mTickPaint.setStrokeWidth(4.f);
            mTickPaint.setAntiAlias(true);
            mTickPaint.setShadowLayer(1f, 0, 0, 0xaa000000);

            mTextPaint = new Paint();
            mTextPaint.setARGB(100, 213, 213, 213);
            mTextPaint.setTextSize(30f);
            mTextPaint.setStrokeWidth(3.f);
            mTextPaint.setAntiAlias(true);

            mTime = new Time();
            mTime.setToNow();
            date = Integer.toString(mTime.monthDay);
            mTextPaint.getTextBounds(date, 0, date.length(), textBounds);

            mUpdateRate = mSmoothMode ? INTERACTIVE_SMOOTH_UPDATE_RATE_MS : INTERACTIVE_UPDATE_RATE_MS;
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            /* get device features (burn-in, low-bit ambient) */
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            Logger.d(TAG, "onPropertiesChanged: low-bit ambient = " + mLowBitAmbient);
        }

        @Override
        public void onTimeTick() {
            /* the time changed */
            super.onTimeTick();
            Logger.d(TAG, "onTimeTick: ambient = " + isInAmbientMode());
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            /* the wearable switched between modes */
            super.onAmbientModeChanged(inAmbientMode);
            Logger.d(TAG, "onAmbientModeChanged: " + inAmbientMode);
            if (mLowBitAmbient) {
                boolean antiAlias = !inAmbientMode;
                mHourPaint.setAntiAlias(antiAlias);
                mMinutePaint.setAntiAlias(antiAlias);
                mSecondPaint.setAntiAlias(antiAlias);
                mTickPaint.setAntiAlias(antiAlias);
            }
            invalidate();

            // Whether the timer should be running depends on whether we're in ambient mode (as well
            // as whether we're visible), so we may need to start or stop the timer.
            updateTimer();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {
            /* draw your watch face */
            long now = System.currentTimeMillis();
            mTime.set(now);

            int width = bounds.width();
            int height = bounds.height();

            // Draw the background, scaled to fit.
            if (mBackgroundScaledBitmap == null
                    || mBackgroundScaledBitmap.getWidth() != width
                    || mBackgroundScaledBitmap.getHeight() != height) {
                mBackgroundScaledBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap,
                        width, height, true /* filter */);
            }
            // Find the center. Ignore the window insets so that, on round watches with a
            // "chin", the watch face is centered on the entire screen, not just the usable
            // portion.
            float centerX = width / 2f;
            float centerY = height / 2f;

            // Draw the ticks.
            if (!isInAmbientMode()) {
                canvas.drawBitmap(mBackgroundScaledBitmap, 0, 0, null);
                float innerTickRadius = centerX - 18;
                float largeInnerTickRadius = centerX - 42;
                float outerTickRadius = centerX;
                for (int tickIndex = 0; tickIndex < 12; tickIndex++) {
                    float tickRot = (float) (tickIndex * Math.PI * 2 / 12);
                    float innerX;
                    float innerY;
                    if (mShowDate && tickIndex == 3) {
                        continue;
                    } else if (tickIndex == 0 || tickIndex == 3 || tickIndex == 6 || tickIndex == 9) {
                        innerX = (float) Math.sin(tickRot) * largeInnerTickRadius;
                        innerY = (float) -Math.cos(tickRot) * largeInnerTickRadius;
                    } else {
                        innerX = (float) Math.sin(tickRot) * innerTickRadius;
                        innerY = (float) -Math.cos(tickRot) * innerTickRadius;
                    }
                    float outerX = (float) Math.sin(tickRot) * outerTickRadius;
                    float outerY = (float) -Math.cos(tickRot) * outerTickRadius;
                    canvas.drawLine(centerX + innerX, centerY + innerY,
                            centerX + outerX, centerY + outerY, mTickPaint);
                }
            } else {
                canvas.drawColor(Color.BLACK);
            }

            float millRot = ((now % 60000) / 1000f) / 30f * (float) Math.PI;
            float secRot = mTime.second / 30f * (float) Math.PI;
            int minutes = mTime.minute;
            float minRot = minutes / 30f * (float) Math.PI;
            float hrRot = ((mTime.hour + (minutes / 60f)) / 6f ) * (float) Math.PI;

            if (!isInAmbientMode() && mShowDate) {
                canvas.drawText(date, centerX*2-(textBounds.width()*1.5f), (centerY + (textBounds.height()/2)), mTextPaint);
            }

            float secLength = centerX - 16;
            float minLength = centerX - 26;
            float hrLength = centerX - 70;

            float hrX = (float) Math.sin(hrRot) * hrLength;
            float hrY = (float) -Math.cos(hrRot) * hrLength;
            canvas.drawLine(centerX, centerY, centerX + hrX, centerY + hrY, mHourPaint);

            float minX = (float) Math.sin(minRot) * minLength;
            float minY = (float) -Math.cos(minRot) * minLength;
            canvas.drawCircle(centerX, centerY, 10f, mMinutePaint);
            canvas.drawLine(centerX, centerY, centerX + minX, centerY + minY, mMinutePaint);
            canvas.drawCircle(centerX, centerY, 10f, mCenterPaint);

            if (!isInAmbientMode()) {
                float secRotate =  mSmoothMode ? millRot : secRot;
                float secStartX = (float) Math.sin(secRotate) * -40;
                float secStartY = (float) -Math.cos(secRotate) * -40;
                float secX = (float) Math.sin(secRotate) * secLength;
                float secY = (float) -Math.cos(secRotate) * secLength;
                canvas.drawLine(centerX + secStartX, centerY + secStartY, centerX + secX, centerY + secY, mSecondPaint);
                canvas.drawCircle(centerX, centerY, 6f, mSecondPaint);
            }
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            /* the watch face became visible or invisible */
            super.onVisibilityChanged(visible);
            Logger.d(TAG, "onVisibilityChanged: " + visible);
            if (visible) {
                mGoogleApiClient.connect();
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
            } else {
                unregisterReceiver();
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    Wearable.DataApi.removeListener(mGoogleApiClient, this);
                    mGoogleApiClient.disconnect();
                }
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredReceivers) {
                return;
            }
            mRegisteredReceivers = true;
            IntentFilter tzFilter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            SlateWatchFaceService.this.registerReceiver(mTimeZoneReceiver, tzFilter);
            IntentFilter dFilter = new IntentFilter(Intent.ACTION_DATE_CHANGED);
            SlateWatchFaceService.this.registerReceiver(mDayChangeReceiver, dFilter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredReceivers) {
                return;
            }
            mRegisteredReceivers = false;
            SlateWatchFaceService.this.unregisterReceiver(mTimeZoneReceiver);
            SlateWatchFaceService.this.unregisterReceiver(mDayChangeReceiver);
        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            Logger.d(TAG, "updateTimer");
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        private void updateConfigDataItemAndUiOnStartup() {
            SlateWatchFaceUtil.fetchConfigDataMap(mGoogleApiClient,
                    new SlateWatchFaceUtil.FetchConfigDataMapCallback() {
                        @Override
                        public void onConfigDataMapFetched(DataMap startupConfig) {
                            // If the DataItem hasn't been created yet or some keys are missing,
                            // use the default values.
                            setDefaultValuesForMissingConfigKeys(startupConfig);
                            SlateWatchFaceUtil.putConfigDataItem(mGoogleApiClient, startupConfig);
                            updateUiForConfigDataMap(startupConfig);
                        }
                    }
            );
        }

        private void setDefaultValuesForMissingConfigKeys(DataMap config) {
            addStringKeyIfMissing(config, SlateWatchFaceUtil.KEY_SECONDS_COLOR,
                    SlateWatchFaceUtil.COLOR_VALUE_STRING_DEFAULT);
            addBoolKeyIfMissing(config, SlateWatchFaceUtil.KEY_SMOOTH_MODE,
                    SlateWatchFaceUtil.SMOOTH_MODE_VALUE_DEFAULT);
            addBoolKeyIfMissing(config, SlateWatchFaceUtil.KEY_SHOW_DATE,
                    SlateWatchFaceUtil.SHOW_DATE_VALUE_DEFAULT);
        }

        private void addStringKeyIfMissing(DataMap config, String key, String value) {
            if (!config.containsKey(key)) {
                config.putString(key, value);
            }
        }

        private void addBoolKeyIfMissing(DataMap config, String key, boolean value) {
            if (!config.containsKey(key)) {
                config.putBoolean(key, value);
            }
        }

        @Override // DataApi.DataListener
        public void onDataChanged
                (DataEventBuffer dataEvents) {
            try {
                for (DataEvent dataEvent : dataEvents) {
                    if (dataEvent.getType() != DataEvent.TYPE_CHANGED) {
                        continue;
                    }

                    DataItem dataItem = dataEvent.getDataItem();
                    if (!dataItem.getUri().getPath().equals(
                            SlateWatchFaceUtil.PATH_WITH_FEATURE)) {
                        continue;
                    }

                    DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
                    DataMap config = dataMapItem.getDataMap();
                    Logger.d(TAG, "Config DataItem updated:" + config);
                    updateUiForConfigDataMap(config);
                }
            } finally {
                dataEvents.close();
            }
        }

        private void updateUiForConfigDataMap(final DataMap config) {
            boolean uiUpdated = false;
            for (String configKey : config.keySet()) {
                if (!config.containsKey(configKey)) {
                    continue;
                }
                uiUpdated |= updateUiForKey(configKey, config);
            }
            if (uiUpdated) {
                invalidate();
            }
        }

        /**
         * Updates the color of a UI item according to the given {@code configKey}. Does nothing if
         * {@code configKey} isn't recognized.
         *
         * @return whether UI has been updated
         */
        private boolean updateUiForKey(String key, DataMap config) {
            switch (key) {
                case SlateWatchFaceUtil.KEY_SECONDS_COLOR:
                    String colorStr = config.getString(key);
                    int color = mInteractiveSecondHandColor;
                    if (null != colorStr) {
                        color = Color.parseColor(config.getString(key));
                        Logger.d(TAG, "Found watch face config key: " + key + " -> "
                                + Integer.toHexString(color));
                        mInteractiveSecondHandColor = color;
                    }
                    if (!isInAmbientMode() && mSecondPaint != null) {
                        mSecondPaint.setColor(color);
                    }
                    break;
                case SlateWatchFaceUtil.KEY_SMOOTH_MODE:
                    mSmoothMode = config.getBoolean(key);
                    mUpdateRate = mSmoothMode ? INTERACTIVE_SMOOTH_UPDATE_RATE_MS : INTERACTIVE_UPDATE_RATE_MS;
                    break;
                case SlateWatchFaceUtil.KEY_SHOW_DATE:
                    mShowDate = config.getBoolean(key);
                    break;
                default:
                    Logger.w(TAG, "Ignoring unknown config key: " + key);
                    return false;
            }
            return true;
        }

        @Override  // GoogleApiClient.ConnectionCallbacks
        public void onConnected(Bundle connectionHint) {
            Logger.d(TAG, "onConnected: " + connectionHint);
            Wearable.DataApi.addListener(mGoogleApiClient, Engine.this);
            updateConfigDataItemAndUiOnStartup();
        }

        @Override  // GoogleApiClient.ConnectionCallbacks
        public void onConnectionSuspended(int cause) {
            Logger.d(TAG, "onConnectionSuspended: " + cause);
        }

        @Override  // GoogleApiClient.OnConnectionFailedListener
        public void onConnectionFailed(ConnectionResult result) {
            Logger.d(TAG, "onConnectionFailed: " + result);
        }
    }
}
