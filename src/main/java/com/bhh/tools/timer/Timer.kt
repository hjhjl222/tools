package com.bhh.tools.timer

import android.os.SystemClock
import com.bhh.tools.coroutines.ClientDispatcher
import com.bhh.tools.coroutines.cancelAndJoinOnGlobalDestroy
import com.bhh.tools.util.Duration
import com.bhh.tools.util.asSeconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.delay as kc_delay

suspend fun delay(delay: Duration) {
    kc_delay(delay.toMillis())
}

fun timer(initialDelay: Duration, period: Duration, context: CoroutineContext = EmptyCoroutineContext, onTime: suspend CoroutineScope.(Job) -> Unit) =
    ClientDispatcher.launch(context) {
        delay(initialDelay)
        var fireTime = SystemClock.elapsedRealtime()
        while (true) {
            try {
                onTime(coroutineContext[Job]!!)
            } finally {
                fireTime += period.toMillis()
                kc_delay(fireTime - SystemClock.elapsedRealtime())
            }
        }
    }.cancelAndJoinOnGlobalDestroy()

fun timer(period: Duration, context: CoroutineContext = EmptyCoroutineContext, onTime: suspend CoroutineScope.(Job) -> Unit) =
    timer(0.asSeconds(), period, context, onTime)