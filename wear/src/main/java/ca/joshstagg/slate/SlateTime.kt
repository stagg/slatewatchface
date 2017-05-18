package ca.joshstagg.slate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.icu.util.Calendar
import android.icu.util.TimeZone

import java.util.concurrent.atomic.AtomicBoolean

internal class SlateTime(private val mContext: Context) {
    private val mCalendar = Calendar.getInstance()
    private val mRegisteredReceivers = AtomicBoolean(false)

    private val mTimeZoneReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            mCalendar.timeZone = TimeZone.getTimeZone(intent.getStringExtra("time-zone"))
        }
    }

    private val mDayChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            mCalendar.timeInMillis = System.currentTimeMillis()
        }
    }

    val timeNow: Calendar
        get() {
            mCalendar.timeInMillis = System.currentTimeMillis()
            return mCalendar
        }

    fun reset() {
        mCalendar.timeZone = TimeZone.getDefault()
        mCalendar.timeInMillis = System.currentTimeMillis()
    }

    fun registerReceiver() {
        if (!mRegisteredReceivers.getAndSet(true)) {
            mContext.registerReceiver(mTimeZoneReceiver, IntentFilter(Intent.ACTION_TIMEZONE_CHANGED))
            mContext.registerReceiver(mDayChangeReceiver, IntentFilter(Intent.ACTION_DATE_CHANGED))
        }
    }

    fun unregisterReceiver() {
        if (mRegisteredReceivers.getAndSet(false)) {
            mContext.unregisterReceiver(mTimeZoneReceiver)
            mContext.unregisterReceiver(mDayChangeReceiver)
        }
    }
}
