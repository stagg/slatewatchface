package ca.joshstagg.slate

import android.graphics.Canvas

/**
 * Slate ca.joshstagg.slate
 * Copyright 2017  Josh Stagg
 */
class NotificationEngine(private val paints: SlatePaints) {

    private var unreadNotificationCount = 0

    fun unreadCountChanged(count: Int): Boolean {
        val config = Slate.instance.configService.config
        val changed = config.notificationDot && unreadNotificationCount != count
        if (changed) {
            unreadNotificationCount = count
        }
        return changed
    }

    fun drawUnreadIndicator(canvas: Canvas) {
        val config = Slate.instance.configService.config
        if (config.notificationDot && unreadNotificationCount > 0) {
            val width = (canvas.width / 2).toFloat()
            val height = (canvas.height - paints.notificationOffset)
            canvas.drawCircle(width, height, paints.notificationOuterRadius, paints.center)
            canvas.drawCircle(width, height, paints.notificationInnerRadius, paints.second)
        }
    }
}