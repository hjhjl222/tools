package com.bhh.tools.robot

import android.content.Context
import android.net.Uri
import android.os.Bundle
import com.bhh.tools.log.Logger
import java.security.SecureRandom
import kotlin.math.abs

open class ITouch(private val context: Context) {
    protected val logger by lazy { Logger.get(this::class.java.simpleName) }

    protected fun generateSequence(): Long {
        val random = SecureRandom()
        val timestamp = System.currentTimeMillis()
        val randomPart = abs((random.nextLong() % 1000000).toDouble()).toLong()
        return (timestamp.toString() + "" + randomPart).toLong()
    }

    protected fun callAction(action: String?, paramJson: String?, sequence: Long): Bundle? {
        val extras = Bundle()
        extras.putString("action", action)
        extras.putString("param", paramJson)
        extras.putLong("sequence", sequence)
        return callApi("content://com.daemon.actions", "execute_action", "", extras)
    }

    protected fun callApi(URI: String, method: String, arg: String, extras: Bundle): Bundle? {
        logger.debug("callApi $URI $method $arg $extras")
        val uri = Uri.parse(URI)
        val result = context.contentResolver?.call(uri, method, arg, extras)

        if (result != null) {
            logger.warn("result is $result")
            return result
        } else {
            logger.error("result is null")
            return null
        }
    }
}