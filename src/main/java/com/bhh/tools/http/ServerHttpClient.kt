package com.bhh.tools.http

import com.bhh.tools.util.Duration
import okhttp3.Request
import java.net.Proxy

open class ServerHttpClient(connectTimeout: Duration, readConnect: Duration, writeConnect: Duration = readConnect, proxyConfig: Proxy? = null, retryCount: Int = 0, protected val server: BaseServer<*>? = null): HttpClient(connectTimeoutSeconds = connectTimeout.toSeconds(), readTimeoutSeconds = readConnect.toSeconds(), writeTimeoutSeconds = writeConnect.toSeconds(), proxyConfig = proxyConfig, retryCount = retryCount) {

    override fun headers(request: Request): HashMap<String, String>{
        return server?.getHeaders(request)?: HashMap()
    }
}