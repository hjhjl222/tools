package com.bhh.tools.http.download

import android.annotation.SuppressLint
import com.bhh.tools.coroutines.GlobalScheduler
import com.bhh.tools.http.ServerHttpClient
import com.bhh.tools.http.IRetrofit
import com.bhh.tools.http.handleBySelf
import com.bhh.tools.http.translateIO
import com.bhh.tools.util.Duration
import com.bhh.tools.util.asSeconds
import kotlinx.coroutines.CoroutineDispatcher
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

internal interface IServer {
    @HEAD
    fun getHeader(@Url url: String): Call<Void>

    @Streaming
    @GET
    fun download(@Url url: String, @Header("RANGE") start: String): Call<ResponseBody>
}

class DownloadServer(baseUrl: String, @SuppressLint("NewApi") connect: Duration = 10.asSeconds(), @SuppressLint(
    "NewApi"
) read:Duration = 1200.asSeconds()){

    private val server by lazy {
        val retrofit = IRetrofit.factory(baseUrl, ServerHttpClient(connect, read).httpBuilder.build())
        retrofit.create(IServer::class.java)
    }

    fun totalLength(url: String, onSuccess: (Long) -> Unit, onError: (Int, Unit) -> Unit, subscribeOn: CoroutineDispatcher = GlobalScheduler.IO, observeOn: CoroutineDispatcher = GlobalScheduler.Main) {
        val responseInfo = server.getHeader(url).translateIO(subscribeOn, observeOn)
        responseInfo.response.handleBySelf(
                onSuccess = {
                    onSuccess(it.headers()["Content-Length"]!!.toLong())
                },
                onError = { code, _ ->
                    onError(code, Unit)
                }
            )
    }

    fun download(url: String, start: Long, onSuccess: (ResponseBody, Boolean) -> Unit, onError: (Int) -> Unit, subscribeOn: CoroutineDispatcher = GlobalScheduler.IO, observeOn: CoroutineDispatcher = GlobalScheduler.Main) {
        val responseInfo = server.download(url, "bytes=$start-").translateIO(subscribeOn, observeOn)
        responseInfo.response.handleBySelf(
                onSuccess = {
                    onSuccess(it.body()!!, it.code() == 206)
                },
                onError = { code, _ ->
                    onError(code)
                }
            )
    }
}