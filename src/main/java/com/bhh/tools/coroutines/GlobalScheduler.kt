package com.bhh.tools.coroutines

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi

object GlobalScheduler {
    private val dispatcher by lazy { Dispatchers }

    val IO get() = dispatcher.IO
    val Main get() = dispatcher.Main
    val Default get() = dispatcher.Default
    @OptIn(ExperimentalCoroutinesApi::class)
    val Init by lazy { dispatcher.IO.limitedParallelism(1) }
    @OptIn(ExperimentalCoroutinesApi::class)
    val Aidl by lazy { dispatcher.IO.limitedParallelism(1) }
    @OptIn(ExperimentalCoroutinesApi::class)
    val Executor by lazy { dispatcher.IO.limitedParallelism(1) }
    @OptIn(ExperimentalCoroutinesApi::class)
    val Task by lazy { dispatcher.IO.limitedParallelism(1) }
    val TaskDispatch by lazy { dispatcher.IO.limitedParallelism(1) }
    val TaskExec by lazy { dispatcher.IO.limitedParallelism(1) }
    val Check by lazy { dispatcher.IO.limitedParallelism(1) }
    val Test by lazy { dispatcher.IO.limitedParallelism(1) }
    val TaskTimeOutChecker by lazy { dispatcher.IO.limitedParallelism(1) }
}