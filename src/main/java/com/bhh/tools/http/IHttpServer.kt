package com.bhh.tools.http

import com.bhh.tools.log.Logger
import com.bhh.tools.util.asSeconds
import okhttp3.Request
import kotlin.reflect.KClass

interface IHttpServer

abstract class BaseServer<Server: IHttpServer>(val connect: Long = 60, val read: Long = 60) {
    protected val logger by lazy { Logger.get(javaClass.simpleName) }
    abstract val server: Server
    val okHttpClient:HttpClient by lazy { ServerHttpClient(connectTimeout = connect.asSeconds(), readConnect = read.asSeconds(),server = this) }

    private val retrofit by lazy { IRetrofit.factory(getBaseHttpUrl(), okHttpClient.httpBuilder.build()) }

    open fun getHeaders(request: Request): HashMap<String, String>{
        return HashMap()
    }

    protected fun <T : Any> create(kClass: KClass<T>): T {
        return retrofit.create(kClass.java)
    }
    abstract fun getBaseHttpUrl(): String

    inline fun <Params, reified Result>startPostRequest(params: Params):Result? {
        return okHttpClient.startPostRequest(getBaseHttpUrl(), params)
    }
}
