package com.flatcode.littletasks.Unit

object GetTimeAgo {
    private const val SECOND_MILLIS = 1000
    private const val MINUTE_MILLIS = 60 * SECOND_MILLIS
    private const val HOUR_MILLIS = 60 * MINUTE_MILLIS
    private const val DAY_MILLIS = 24 * HOUR_MILLIS

    fun getTimeAgo(time: Long): String? {
        val normalizedTime = if (time < 1000000000000L) time * 1000 else time
        val now = System.currentTimeMillis()

        if (normalizedTime !in 1..now) return null

        val diff = now - normalizedTime
        return when {
            diff < MINUTE_MILLIS -> "just now"
            diff < 2 * MINUTE_MILLIS -> "a minute ago"
            diff < 50 * MINUTE_MILLIS -> "${diff / MINUTE_MILLIS} minutes ago"
            diff < 90 * MINUTE_MILLIS -> "an hour ago"
            diff < 24 * HOUR_MILLIS -> "${diff / HOUR_MILLIS} hours ago"
            diff < 48 * HOUR_MILLIS -> "yesterday"
            else -> "${diff / DAY_MILLIS} days ago"
        }
    }

    fun getMessageAgo(time: Long): String? {
        val normalizedTime = if (time < 1000000000000L) time * 1000 else time
        val now = System.currentTimeMillis()

        if (normalizedTime !in 1..now) return null

        val diff = now - normalizedTime
        return when {
            diff < MINUTE_MILLIS -> "1 s"
            diff < 2 * MINUTE_MILLIS -> "1 m"
            diff < 50 * MINUTE_MILLIS -> "${diff / MINUTE_MILLIS} m"
            diff < 90 * MINUTE_MILLIS -> "1 h"
            diff < 24 * HOUR_MILLIS -> "${diff / HOUR_MILLIS} h"
            diff < 48 * HOUR_MILLIS -> "1 d"
            else -> "${diff / DAY_MILLIS} d"
        }
    }
}