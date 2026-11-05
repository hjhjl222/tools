package com.bhh.tools.http

import androidx.annotation.Keep
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.bhh.tools.coroutines.GlobalScheduler
import com.bhh.tools.http.BaseErrorResult
import com.bhh.tools.log.Logger
import com.bhh.tools.util.toJSON
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class IRetrofit {

    companion object {
        fun factory(baseUrl: String, httpClient: OkHttpClient): Retrofit {
            return Retrofit.Builder().apply {
                baseUrl(baseUrl)
                client(httpClient)
                addConverterFactory(GsonConverterFactory.create())
            }.build()
        }

        fun factory(httpClient: OkHttpClient): Retrofit {
            return Retrofit.Builder().apply {
                client(httpClient)
            }.build()
        }

    }
}

private val log = Logger.get("OkHttpClient")

fun <T: Any> Call<T>.translateIO(subscribeOn: CoroutineDispatcher = GlobalScheduler.IO, observeOn: CoroutineDispatcher = GlobalScheduler.Main): ResponseInfo<T> {
    return runBlocking(observeOn) {
        val response = withContext<Response<T>>(subscribeOn) {
            try {
                log.debug( "request:${request().method} url:${request().url.toUrl().path}")
                this@translateIO.execute()
            } catch (e: Exception) {
                log.error("${e.message} -> \r\n${e.stackTraceToString()}")
                Response.error(10000, ResponseBody.create(null, ""))
            }
        }
        ResponseInfo(request().url.toUrl().path, request().headers, response)
    }
}

data class ResponseInfo<T>(val url: String, val headers: Headers, val response: Response<T>)

fun <T: Any> Response<T>.handleBySelf(onSuccess:(Response<T>) -> Unit, onError: (Int, T?) -> Unit) {
    if (!isSuccessful) {
        log.error( "unknown error")
        onError(-1, null)
        return
    }

    val code = code()
    if (code < 200 || code >= 300) {
        log.error("connect error ${code()} ${message()}")
        onError(code(), null)
        return
    }
    onSuccess(this)
}


fun <T> Response<BaseResult<T>>.handleResponse(): BaseResult<*> {
    if (code() < 200 || code() >= 300) {
        return  getEmptyResult(code(), message())
    }
    return body() ?: getEmptyResult(code(), message())
}

fun getEmptyResult(code: Int, message: String?): BaseErrorResult {
    return BaseErrorResult().apply {
        this.err_code = code
        this.message = message
    }
}

class BaseErrorResult(): BaseResult<String>()

@Keep
open class BaseResult<T> {
    var is_success: Boolean = false
    var err_code: Int = 0
    var message: T? = null
}
/*
fun <T: Any> Response<T>.handleResponse(onSuccess:(T) -> Unit, onError: ((Int, String?) -> Unit)? = null, params: String? = null, headers: Headers? = null) {

    val code = code()
    if (code < 200 || code >= 300) {
        log.error( "connect error ${code()} ${message()}")
        onError?.invoke(code(), message())
        return
    }

    val result = body()
    if (result == null) {
        log.error( "$params\r\n connect success, but without data")
        onError?.invoke(code(), message())
        return
    }

    log.debug("----------------------http start---------------------------\r\nheader:${headers?.toJSON()}\r\nparams:$params \r\nresult:${result.toJSON()}\r\n----------------------http finish---------------------------")
    if (result is IResponseData) {
        if (result.isResultOk()) {
            onSuccess(result)
        } else {
            onError?.invoke(code, message())
        }
    } else {
        onSuccess(result)
    }
}*/
