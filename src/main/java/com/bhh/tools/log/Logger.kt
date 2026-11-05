package com.bhh.tools.log

import android.os.Environment
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.android.LogcatAppender
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy
import ch.qos.logback.core.util.FileSize
import com.bhh.tools.app.AppMode
import org.slf4j.LoggerFactory
import org.slf4j.Marker
import org.slf4j.event.Level
import kotlin.reflect.KClass
import org.slf4j.Logger as SlfLogger

object Logger {
    private val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
    private val appenders = mutableListOf<Appender<ILoggingEvent>>()
    private var baseName = "lp"

    init {
        PatternLayout.defaultConverterMap["process"] = "ProcessConverter"
        init()
    }


    fun get(name: String) = LoggerFactory.getLogger("$baseName#$name")!!

    fun get(kClass: KClass<*>) = LoggerFactory.getLogger(kClass.java)!!

    fun appendLogcat() {
        val logcatEncoder = PatternLayoutEncoder()
        logcatEncoder.context = loggerContext
        logcatEncoder.pattern = "%msg [%thread]"
        logcatEncoder.start()

        val logcatAppender = LogcatAppender()
        logcatAppender.name = "logcat"
        logcatAppender.context = loggerContext
        logcatAppender.encoder = logcatEncoder
        logcatAppender.start()

        appenders += logcatAppender
    }

    fun appendFile(
        fileName: String,
        countLimit: Int = 14,
        maxFileSize: String = "500MB",
        totalSizeCap: String = "1GB",
        pattern: String = "%d{HH:mm:ss.SSS} %process %.-1p/%c{32} %msg [%thread]%n",
        logDir: String = "${Environment.getExternalStorageDirectory().path}/loki/log",
    ) {
        baseName = fileName
        val fileEncoder = PatternLayoutEncoder()
        fileEncoder.context = loggerContext
        fileEncoder.pattern = pattern
        fileEncoder.start()

        val rollingFileAppender = RollingFileAppender<ILoggingEvent>()
        rollingFileAppender.context = loggerContext
        rollingFileAppender.isAppend = true

        val rollingPolicy = SizeAndTimeBasedRollingPolicy<ILoggingEvent>()
        rollingPolicy.fileNamePattern = "$logDir/$fileName.%d{yyyy-MM-dd,GMT+8}.%i.log"
        rollingPolicy.maxHistory = countLimit
        rollingPolicy.isCleanHistoryOnStart = true
        rollingPolicy.setMaxFileSize(FileSize.valueOf(maxFileSize))
        rollingPolicy.setTotalSizeCap(FileSize.valueOf(totalSizeCap))
        rollingPolicy.setParent(rollingFileAppender)
        rollingPolicy.context = loggerContext
        rollingPolicy.start()

        rollingFileAppender.rollingPolicy = rollingPolicy
        rollingFileAppender.encoder = fileEncoder
        rollingFileAppender.isPrudent = true
        rollingFileAppender.name = "file"
        rollingFileAppender.start()

        appenders += rollingFileAppender
    }

    fun init(level: Level = Level.TRACE) {
        loggerContext.reset()
        if (appenders.isEmpty()) {
            appendLogcat()
        }
        val root = LoggerFactory.getLogger(SlfLogger.ROOT_LOGGER_NAME) as ch.qos.logback.classic.Logger
        root.level = ch.qos.logback.classic.Level.toLevel(level.name)
        for (appender in appenders) {
            root.addAppender(appender)
        }
    }

    fun setLevel(name: String, level: Level) {
        val logger = LoggerFactory.getLogger(name) as ch.qos.logback.classic.Logger
        logger.level = ch.qos.logback.classic.Level.toLevel(level.name)
    }

    fun init(appName: String, mode: AppMode, needStore: Boolean = false) {
        try {
            appenders.clear()
            val level = when (mode) {
                AppMode.DEBUG -> {
                    appendLogcat()
                    if (needStore) {
                        appendFile(appName, totalSizeCap = "2GB")
                    }
                    Level.TRACE
                }
                else -> {
                    appendLogcat()
                    if (needStore) {
                        appendFile(appName)
                    }
                    Level.DEBUG
                }
            }
            init(level)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

inline fun SlfLogger.trace(lazyMsg: () -> String) {
    if (isTraceEnabled) {
        trace(lazyMsg())
    }
}

inline fun SlfLogger.trace(marker: Marker, lazyMsg: () -> String) {
    if (isTraceEnabled) {
        trace(marker, lazyMsg())
    }
}

inline fun SlfLogger.debug(lazyMsg: () -> String) {
    if (isDebugEnabled) {
        debug(lazyMsg())
    }
}

inline fun SlfLogger.debug(marker: Marker, lazyMsg: () -> String) {
    if (isDebugEnabled) {
        debug(marker, lazyMsg())
    }
}

inline fun SlfLogger.info(lazyMsg: () -> String) {
    if (isInfoEnabled) {
        info(lazyMsg())
    }
}

inline fun SlfLogger.info(marker: Marker, lazyMsg: () -> String) {
    if (isInfoEnabled) {
        info(marker, lazyMsg())
    }
}
