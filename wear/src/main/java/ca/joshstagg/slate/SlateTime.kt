package ca.joshstagg.slate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.icu.util.Calendar
import android.icu.util.TimeZone

import java.util.concurrent.atomic.AtomicBoolean

internal class SlateTime(private val context: Context) {
    private val calendar = Calendar.getInstance()
    private val registeredReceivers = AtomicBoolean(false)

    private val timeZoneReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            calendar.timeZone = TimeZone.getTimeZone(intent.getStringExtra("time-zone"))
        }
    }

    private val dayChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            calendar.timeInMillis = System.currentTimeMillis()
        }
    }

    val timeNow: Calendar
        get() {
            calendar.timeInMillis = System.currentTimeMillis()
            return calendar
        }

    fun reset() {
        calendar.timeZone = TimeZone.getDefault()
        calendar.timeInMillis = System.currentTimeMillis()
    }

    fun registerReceiver() {
        if (!registeredReceivers.getAndSet(true)) {
            context.registerReceiver(timeZoneReceiver, IntentFilter(Intent.ACTION_TIMEZONE_CHANGED))
            context.registerReceiver(dayChangeReceiver, IntentFilter(Intent.ACTION_DATE_CHANGED))
        }
    }

    fun unregisterReceiver() {
        if (registeredReceivers.getAndSet(false)) {
            context.unregisterReceiver(timeZoneReceiver)
            context.unregisterReceiver(dayChangeReceiver)
        }
    }
}
