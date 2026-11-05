package com.bhh.tools.system.aidl

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.IInterface
import com.bhh.tools.app.IApplicationHandler
import com.bhh.tools.coroutines.ClientDispatcher
import com.bhh.tools.coroutines.GlobalScheduler
import com.bhh.tools.log.Logger
import com.bhh.tools.util.asSeconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Semaphore

abstract class ChannelConnection<Service: IInterface>(private val clientName: String, private val targetService: String, private val targetPackage: String): ServiceConnection, IApplicationHandler, CoroutineScope {

    protected val logger by lazy { Logger.get(this::class.java.simpleName) }

    var service: Service? = null
    private val semaphore = Semaphore(1)

    fun isBinding(): Boolean {
        return service?.asBinder()?.isBinderAlive == true
    }

    fun onInit(needBind: Boolean) {
        if (!needBind) {
            return
        }
        tryBindService()
    }

    val packageName get() = application?.packageName

    private fun tryBindService() = launch(coroutineContext) {
        while (service == null) {
            try {
                semaphore.acquire()
                if (service != null) {
                    return@launch
                }
                logger.info("$clientName try bind $targetService")
                val intent = Intent(targetService)
                intent.`package` = targetPackage
                application?.bindService(intent, this@ChannelConnection, Context.BIND_AUTO_CREATE)
                com.bhh.tools.timer.delay(1.asSeconds())
            } catch (throwable: Throwable) {
                logger.info(throwable.message?:"")
            } finally {
                delay(2000)
                semaphore.release()
            }
        }
    }

    final override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
        service = initService(binder)
        logger.error("$clientName bind service success $service $this")
//        semaphore.release()
        ClientDispatcher.launch(GlobalScheduler.Default) {
            if (!isBinding()) {
                return@launch
            }
            onConnected(name)
        }
    }

    final override fun onServiceDisconnected(name: ComponentName?) {
        logger.info("unbind service $clientName")
        semaphore.acquire()
        onDisconnected(name)
        service = null
        semaphore.release()
        tryBindService()
    }

    protected abstract fun initService(binder: IBinder?): Service
    protected open suspend fun onConnected(name: ComponentName?) {}
    protected open fun onDisconnected(name: ComponentName?) {}
}
