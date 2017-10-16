package ca.joshstagg.slate

import android.graphics.Canvas

/**
 * Slate ca.joshstagg.slate
 * Copyright 2017  Josh Stagg
 */
class NotificationEngine(private val paints: SlatePaints) {

    private var unreadNotificationCount = 0

    fun unreadCountChanged(count: Int) : Boolean {
        val config = Slate.instance.configService.config
        val changed = config.notificationDot && unreadNotificationCount != count
        if (changed) {
            unreadNotificationCount = count
        }
        return changed
    }

    fun drawUnreadIndicator(canvas: Canvas, isAmbient: Boolean) {
        val config = Slate.instance.configService.config
        if (!isAmbient && config.notificationDot && unreadNotificationCount > 0) {
            val width = canvas.width
            val height = canvas.height
            canvas.drawCircle((width / 2).toFloat(), (height - 40).toFloat(), 10f, paints.center)
            canvas.drawCircle((width / 2).toFloat(), (height - 40).toFloat(), 4f, paints.second)
        }
    }
}