package com.bhh.tools.coroutines

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.*
import java.lang.Runnable
import kotlin.coroutines.CoroutineContext

class LooperDispatcher(looper: Looper = Looper.myLooper()!!) : CoroutineDispatcher() {

    private val handler = Handler(looper)

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        handler.post(block)
    }
}