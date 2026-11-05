package com.bhh.tools.http.download

import android.annotation.SuppressLint
import com.bhh.tools.cypher.md5
import com.bhh.tools.log.Logger
import com.bhh.tools.util.asSeconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.*
import java.time.Duration

@SuppressLint("NewApi")
class DownloadManager(private val subscribeOn: CoroutineDispatcher, private val observeOn: CoroutineDispatcher, baseUrl: String) {
    private val log = Logger.get("DownloadManager2")
    private val BUFFER_SIZE = 4096 // 缓冲区大小

    private val downloadServer by lazy { DownloadServer(baseUrl = baseUrl, connect = 10.asSeconds(), read = 600.asSeconds()) }
    private var retrofit: Retrofit
    private var fileDownloadService: IServer

    init {
        // 创建 OkHttpClient 以支持自定义 Header
        val httpClientBuilder = OkHttpClient.Builder()
        httpClientBuilder.addInterceptor { chain ->
            val originalRequest: Request = chain.request()
            val newRequestBuilder: Request.Builder = originalRequest.newBuilder()
            val newRequest: Request = newRequestBuilder.build()
            chain.proceed(newRequest)
        }
        val httpClient = httpClientBuilder.build()

        // 创建 Retrofit 实例
        retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(httpClient)
            .build()

        // 创建 Retrofit 服务
        fileDownloadService = retrofit.create(IServer::class.java)
    }

    fun getTotalLength(url: String, callback:(Long) -> Unit) {
        downloadServer.totalLength(url, onSuccess = {
            callback(it)
        }, onError = { code,_ ->
            log.error( "get file header failed error2: $code")
        }, subscribeOn, observeOn)
    }

    fun startDownload(url: String, md5: String, file: File, callback: DownloadCallback) {
        downloadServer.totalLength(url, onSuccess = { total ->
            var start = 0L
            if (file.exists()) {
                val length = file.length()
                if (total > length) {
                    start = length
                } else {
                    if (total == length && checkMd5(file,md5)) {
                        callback.onDownloading(file, 100)
                        return@totalLength
                    }
                    file.delete()
                }
            }


            suspend fun <T>onAction(value:T, action:(value: T)->Unit) {
                withContext(observeOn) {
                    action(value)
                }
            }

            suspend fun onFailed(msg: String) = onAction(msg){
                callback.onFailed(file, msg)
            }
            suspend fun onDownloading(index: Int) = onAction(index) {
                callback.onDownloading(file, index)
            }


            val call: Call<ResponseBody> = fileDownloadService.download(url,"bytes=$start-") // Range 头部为空表示从头开始下载
            call.enqueue(object : Callback<ResponseBody?> {
                override fun onResponse(call: Call<ResponseBody?>?, response: Response<ResponseBody?>) {
                    GlobalScope.launch(subscribeOn) {
                        if (response.isSuccessful) {
                            val body = response.body()
                            if (body != null) {
                                try {
                                    var downloadedSize = if (response.code() == 206) start else 0
                                    // 获取输入流和输出流
                                    val inputStream: InputStream = body.byteStream()
                                    val outputStream: OutputStream = FileOutputStream(file, true)
                                    val buffer = ByteArray(BUFFER_SIZE)
                                    var bytesRead: Int
                                    var count = 0
                                    var current = 0
                                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                        // 写入文件
                                        outputStream.write(buffer, 0, bytesRead)
                                        downloadedSize += bytesRead.toLong()
                                        current = ((downloadedSize.toFloat() / total) * 100).toInt()
                                        if (count != current) {
                                            count = current
                                            onDownloading(count)
                                        }
                                    }
                                    // 下载完成回调
                                    withContext(observeOn) {
                                        callback.onComplete(file)
                                    }
                                    outputStream.flush()
                                    outputStream.close()
                                    inputStream.close()
                                } catch (e: IOException) {
                                    onFailed(e.message?:"")
                                }
                            } else {
                                onFailed("Response body is null.")
                            }
                        } else {
                            onFailed("Server returned an error: " + response.code())
                        }
                    }

                }

                override fun onFailure(call: Call<ResponseBody?>?, t: Throwable) {
                    GlobalScope.launch(subscribeOn) {
                        onFailed(t.message?:"")
                    }
                }
            })
            // Range 头部为空表示从头开始下载
        }, onError = { code,_ ->
            log.error( "get file header failed error2")
        }, subscribeOn, observeOn)
    }

    private fun checkMd5(file: File, md5: String): Boolean {
        return file.md5 == md5
    }

    interface DownloadCallback {
        fun onStart(file: File)
        fun onDownloading(file: File,present: Int)
        fun onComplete(file: File)
        fun onFailed(file: File,message: String?)
    }
}