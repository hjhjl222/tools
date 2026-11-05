package com.bhh.tools.timer

import com.bhh.tools.util.Duration
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class Timestamp(val value: Int = now()) {
    companion object {
        fun now() = (System.currentTimeMillis() / 1000).toInt()

        fun day(time: Int = 0): Timestamp {
            val calendar = Calendar.getInstance()
            if (time > 0) {
                calendar.timeInMillis = time * 1000L
            }
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return Timestamp((calendar.timeInMillis / 1000).toInt())
        }

        fun month(time: Int = 0): Timestamp {
            val calendar = Calendar.getInstance()
            if (time > 0) {
                calendar.timeInMillis = time * 1000L
            }
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return Timestamp((calendar.timeInMillis / 1000).toInt())
        }

        fun year(time: Int = 0): Timestamp {
            val calendar = Calendar.getInstance()
            if (time > 0) {
                calendar.timeInMillis = time * 1000L
            }
            calendar.set(Calendar.MONTH, 0)
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            return Timestamp((calendar.timeInMillis / 1000).toInt())
        }

        operator fun invoke(time: String, pattern: String): Timestamp {
            return Timestamp((SimpleDateFormat(pattern, Locale.getDefault()).parse(time).time / 1000).toInt())
        }
    }

    val time: Long
        get() = value * 1000L

    fun format(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(value * 1000L)
    }

    operator fun minus(duration: Duration): Timestamp {
        return Timestamp(value - duration.toSeconds().toInt())
    }

    operator fun plus(duration: Duration): Timestamp {
        return Timestamp(value + duration.toSeconds().toInt())
    }

    operator fun minus(timestamp: Timestamp): Duration {
        return Duration((value - timestamp.value) * 1000L)
    }
}

fun Int.toTimestamp() = Timestamp(this)