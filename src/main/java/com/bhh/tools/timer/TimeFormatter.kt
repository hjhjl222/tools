package com.bhh.tools.timer

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

enum class TimeFormatter(val patten: String) {
    yyyy_MM_dd_HH_mm_ss("yyyy-MM-dd HH:mm:ss"),
    yyyy_MM_dd_HH_mm_ss_n("yyyy-MM-dd HH:mm:ss.n"),
    yyyyMMddHHmmss("yyyyMMddHHmmss"),
    yyyy_MM_dd("yyyy-MM-dd"),
    dd_HH_mm_ss("dd HH:mm:ss"),
    ;
}

// 新增时间间隔格式化扩展函数
fun Long.formatDuration(): String {
    val seconds = this / 1000
    val days = seconds / 86400
    val hours = (seconds % 86400) / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60

    return buildString {
        append("${days}天")
        append("${hours.toString().padStart(2, '0')}时")
        append("${minutes.toString().padStart(2, '0')}分")
        append("${remainingSeconds.toString().padStart(2, '0')}秒")
    }
}

/**
 * 计算当天 0 点的时间戳
 */
fun getTodayMidnightTimestamp(): Long {
    // 获取当前日期
    val today = LocalDate.now()
    // 将当前日期转换为当天 0 点的 LocalDateTime 对象
    val midnight = today.atStartOfDay()
    // 将 LocalDateTime 对象转换为带时区的 ZonedDateTime 对象
    val zonedMidnight = midnight.atZone(ZoneId.systemDefault())
    // 获取对应的时间戳
    return zonedMidnight.toInstant().toEpochMilli()
}

@SuppressLint("NewApi")
fun Int.formatTime(patten: TimeFormatter, @SuppressLint("NewApi") zoneId: ZoneId = ZoneId.systemDefault()): String {
    val formatter = DateTimeFormatter.ofPattern(patten.patten).withZone(zoneId)
    return formatter.format(Instant.ofEpochMilli(toLong()))
}

@SuppressLint("NewApi")
fun Long.formatTime(patten: TimeFormatter, @SuppressLint("NewApi") zoneId: ZoneId = ZoneId.systemDefault()): String {
    val formatter = DateTimeFormatter.ofPattern(patten.patten).withZone(zoneId)
    return formatter.format(Instant.ofEpochMilli(toLong()))
}

//2023-12-05 00:09:20.183999981
fun String.getTimeStamp(patten: TimeFormatter): Long {
    return try {
        val sdf = SimpleDateFormat(patten.patten, Locale.getDefault())
        sdf.parse(this)?.time?:-1
    } catch (e: Exception) {
        // 如果解析出现异常，打印堆栈信息并返回 null
        e.printStackTrace()
        -1
    }
}
