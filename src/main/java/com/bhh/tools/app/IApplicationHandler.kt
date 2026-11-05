package com.bhh.tools.app

interface IApplicationHandler {
    val application get() = ApplicationHandler.application
    val packageManager get() = application?.packageManager
}