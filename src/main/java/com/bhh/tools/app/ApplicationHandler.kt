package com.bhh.tools.app

import android.content.Context

object ApplicationHandler {
    var application: Context? = null
    fun onCreate(application: Context) {
        ApplicationHandler.application = application
    }

    fun onTerminate(application: Context) {
        ApplicationHandler.application = null
    }
}