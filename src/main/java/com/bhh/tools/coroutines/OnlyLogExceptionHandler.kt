package com.bhh.tools.coroutines

import com.bhh.tools.log.Logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.coroutines.CoroutineContext

object OnlyLogExceptionHandler  : CoroutineExceptionHandler {
    private val log = Logger.get("OnlyLogExceptionHandler")
    override val key = CoroutineExceptionHandler

    override fun handleException(context: CoroutineContext, e: Throwable) {
        log.error("${e.message} -> \r\n${e.stackTraceToString()}")
    }
}