package com.bhh.tools.http

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class HttpRetryInterceptor(
    private val maxRetries: Int = 3,
    private val backoffMs: Long = 0
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var lastError: IOException? = null
        var attempt = 0

        while (attempt < maxRetries) {
            try {
                return chain.proceed(chain.request())
            } catch (e: IOException) {
                lastError = e
                attempt++
                if (attempt >= maxRetries) break
                if (backoffMs > 0) {
                    try { Thread.sleep(backoffMs) } catch (_: InterruptedException) {}
                }
            }
        }

        throw lastError ?: IOException("network retry failed")
    }
}