package com.bhh.tools.coroutines

import com.bhh.tools.util.Duration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


object ClientDispatcher {
    fun launch(context: CoroutineContext, block: suspend CoroutineScope.() -> Unit) =
        GlobalScope.launch(context, block = block).cancelAndJoinOnGlobalDestroy()

    fun <T> async(context: CoroutineContext, block: suspend CoroutineScope.() -> T) =
        GlobalScope.async(context, block = block)

    fun timer(name: String, initialDelay: Duration, period: Duration, context: CoroutineContext = GlobalScheduler.IO, onTime: suspend CoroutineScope.(Job) -> Unit) =
        com.bhh.tools.timer.timer(initialDelay, period, context, onTime)
}