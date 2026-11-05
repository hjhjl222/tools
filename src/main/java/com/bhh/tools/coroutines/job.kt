package com.bhh.tools.coroutines

import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.runBlocking

private val onDestroys = mutableListOf<() -> Unit>()

private fun <T : Any> T.addDestroy(onDestroy: suspend T.() -> Unit): () -> Unit {
    val function: () -> Unit = {
        runBlocking {
            onDestroy()
        }
    }
    synchronized(onDestroys) {
        onDestroys.add(0, function)
    }
    return function
}


fun Job.onGlobalDestroy(onDestroy: suspend Job.() -> Unit): Job {
    val function = addDestroy(onDestroy)
    invokeOnCompletion {
        synchronized(onDestroys) {
            onDestroys.remove(function)
        }
    }
    return this
}

fun Job.cancelAndJoinOnGlobalDestroy() = onGlobalDestroy { cancelAndJoin() }