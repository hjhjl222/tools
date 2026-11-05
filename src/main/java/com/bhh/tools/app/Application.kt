package com.bhh.tools.app

import com.bhh.tools.log.Logger
import android.app.Application as AndroidApplication
import java.text.SimpleDateFormat
import java.util.*

private val log = Logger.get("com.bhh.tools.app.Application")

enum class AppMode {
    DEBUG, TEST, RELEASE;

    val isDebug get() = this === DEBUG

    val isTest get() = this === TEST

    val isRelease get() = this === RELEASE
}

const val TAG_APP_NAME = "appName"
const val TAG_BUILD_TIME = "buildTime"
const val TAG_VERSION_NAME = "versionName"
const val TAG_VERSION_CODE = "versionCode"

@Suppress("LeakingThis")
open class Application(name: String, mode: AppMode, versionName: String, versionCode: Int, buildTime: Long) : AndroidApplication() {

    companion object {
        lateinit var name: String
        lateinit var mode: AppMode
        lateinit var versionName: String
        var versionCode = 0
        var buildTime = 0L
    }

    init {
        Application.name = name
        Application.mode = mode
        Application.versionName = versionName
        Application.versionCode = versionCode
        Application.buildTime = buildTime
    }

    override fun onCreate() {
        log.info("$name($versionName) start in $mode mode, buildTime ${SimpleDateFormat.getDateTimeInstance().format(Date(buildTime))}")
        super.onCreate()
    }

}