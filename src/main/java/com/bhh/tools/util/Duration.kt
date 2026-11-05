package com.bhh.tools.util

const val MILLI_VALUE = 1L
const val SECOND_VALUE = 1000L
const val MINUTE_VALUE = 60_000L
const val HOUR_VALUE = 3600_000L
const val DAY_VALUE = 86400_000L

data class Duration(val value: Long) : Comparable<Duration> {

    companion object {
        val MILLI = Duration(MILLI_VALUE)
        val SECOND = Duration(SECOND_VALUE)
        val MINUTE = Duration(MINUTE_VALUE)
        val HOUR = Duration(HOUR_VALUE)
        val DAY = Duration(DAY_VALUE)
    }

    fun toMillis() = value
    fun toSeconds() = value / SECOND_VALUE
    fun toMinutes() = value / MINUTE_VALUE
    fun toHours() = value / HOUR_VALUE
    fun toDays() = value / DAY_VALUE

    operator fun unaryPlus() = this
    operator fun unaryMinus() = Duration(-value)

    operator fun plus(duration: Duration) = Duration(value + duration.value)

    operator fun minus(duration: Duration) = Duration(value - duration.value)

    operator fun times(count: Int) = Duration(value * count)
    operator fun times(count: Long) = Duration(value * count)

    operator fun div(count: Int) = Duration(value / count)
    operator fun div(count: Long) = Duration(value / count)
    operator fun div(duration: Duration) = value / duration.value

    operator fun rem(duration: Duration) = Duration(value % duration.value)

    override fun compareTo(other: Duration) = when {
        value > other.value -> 1
        value < other.value -> -1
        else -> 0
    }
}

operator fun Int.times(duration: Duration) = duration * this
fun Int.asMillis() = Duration(this.toLong())
fun Int.asSeconds() = Duration(this * SECOND_VALUE)
fun Int.asMinutes() = Duration(this * MINUTE_VALUE)
fun Int.asHours() = Duration(this * HOUR_VALUE)
fun Int.asDays() = Duration(this * DAY_VALUE)

operator fun Long.times(duration: Duration) = duration * this
fun Long.asMillis() = Duration(this)
fun Long.asSeconds() = Duration(this * SECOND_VALUE)
fun Long.asMinutes() = Duration(this * MINUTE_VALUE)
fun Long.asHours() = Duration(this * HOUR_VALUE)
fun Long.asDays() = Duration(this * DAY_VALUE)


fun Thread.sleep(duration: Duration) {
    Thread.sleep(duration.toMillis())
}