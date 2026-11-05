package com.bhh.tools.http

import com.google.gson.Gson
import com.bhh.tools.log.Logger
import com.bhh.tools.util.toJSON
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.net.Proxy
import java.util.HashMap
import java.util.concurrent.TimeUnit

open class HttpClient(private val connectTimeoutSeconds: Long = 20, private val readTimeoutSeconds: Long = 20, private val writeTimeoutSeconds: Long = 20,private val proxyConfig: Proxy? = null, private val retryCount: Int = -1) {
    val logger by lazy { Logger.get(this::class.java.name) }
    protected open fun headers(request: Request): HashMap<String, String> {
        return HashMap()
    }

    val httpBuilder by lazy {
        val builder = OkHttpClient.Builder()
        .connectTimeout(connectTimeoutSeconds, TimeUnit.SECONDS)
        .readTimeout(readTimeoutSeconds, TimeUnit.SECONDS)
        .writeTimeout(writeTimeoutSeconds, TimeUnit.SECONDS)
        .proxy(proxyConfig?:Proxy.NO_PROXY)
        .addInterceptor { chain ->
            val newBuilder = chain.request().newBuilder()
            headers(newBuilder.build()).entries.forEach {
                newBuilder.addHeader(it.key, it.value)
            }
            chain.proceed(newBuilder.build())
        }
        if (retryCount > 0) {
            builder.addInterceptor(HttpRetryInterceptor(retryCount, 1000))
        } else {
            builder.retryOnConnectionFailure(true)
        }
        builder
    }

    inline fun <reified Result>startGetRequest(url: String):Result? {
        return startRequest(url, null, false)
    }

    inline fun <Params, reified Result>startPostRequest(url: String, params: Params):Result? {
        return startRequest(url, params, true)
    }

    inline fun <Params, reified Result>startRequest(url: String, params: Params?, isPost: Boolean = true):Result? {
        try {
            val builder = Request.Builder().url(url)
            val request = if (isPost) {
                val requestBody = when(params!!::class) {
                    String::class -> params as String
                    JSONObject::class -> params.toString()
                    else -> params.toJSON()!!
                }.toRequestBody("application/json; charset=utf-8".toMediaType())
                builder.post(requestBody)
            } else {
                builder.get()
            }.build()
            val response: Response = httpBuilder.build().newCall(request).execute()
            if (response.isSuccessful) {
                val body = response.body?:return null

                val result: Result = when(Result::class) {
                    Response::class -> response as Result
                    String::class -> body.string() as Result
                    else -> Gson().fromJson(body.string(), Result::class.java)
                }

                if (result !is Response) {
                    logger.debug(
                        "网络请求 \nurl:{}, \nisPost:{}, \nresponse:{}",
                        url,
                        isPost,
                        result?.toJSON()
                    )
                }
                return result
            }
        } catch (e: Exception) {
            logger.error("startRequest: $url, $params, $isPost, $e")
        }
        return null
    }
}