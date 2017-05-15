package ca.joshstagg.slate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.icu.util.Calendar;
import android.icu.util.TimeZone;

import java.util.concurrent.atomic.AtomicBoolean;

class SlateTime {

    private final Context mContext;
    private final Calendar mCalendar = Calendar.getInstance();
    private final AtomicBoolean mRegisteredReceivers = new AtomicBoolean(false);

    SlateTime(Context context) {
        mContext = context;
    }

    private final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            TimeZone tz = TimeZone.getTimeZone(intent.getStringExtra("time-zone"));
            mCalendar.setTimeZone(tz);
        }
    };

    private final BroadcastReceiver mDayChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mCalendar.setTimeInMillis(System.currentTimeMillis());
        }
    };

    Calendar getTimeNow() {
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        return mCalendar;
    }

    void reset() {
        mCalendar.setTimeZone(TimeZone.getDefault());
        mCalendar.setTimeInMillis(System.currentTimeMillis());
    }

    void registerReceiver() {
        if (!mRegisteredReceivers.getAndSet(true)) {
            IntentFilter tzFilter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            mContext.registerReceiver(mTimeZoneReceiver, tzFilter);
            IntentFilter dFilter = new IntentFilter(Intent.ACTION_DATE_CHANGED);
            mContext.registerReceiver(mDayChangeReceiver, dFilter);
        }
    }

    void unregisterReceiver() {
        if (mRegisteredReceivers.getAndSet(false)) {
            mContext.unregisterReceiver(mTimeZoneReceiver);
            mContext.unregisterReceiver(mDayChangeReceiver);
        }
    }
}
