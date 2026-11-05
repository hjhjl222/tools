package com.bhh.tools.coroutines.event

import com.bhh.tools.coroutines.ClientDispatcher
import com.bhh.tools.coroutines.GlobalScheduler
import com.bhh.tools.coroutines.OnlyLogExceptionHandler
import com.bhh.tools.log.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

object EventBus {
    private val log = Logger.get("EventBus")

    private val print = false//BuildConfig.DEBUG
    interface Disposable {
        suspend fun dispose()
    }

    val broadcast = BroadcastChannel<Any>(Channel.BUFFERED)

    fun logEvent(operate: String, event: Any) {
        if (!print) {
            return
        }
        log.debug("$operate ${event.javaClass.name}@${event.hashCode()}")
    }

    fun logError(content: String, throwable: Throwable) {
        if (!print) {
            return
        }
        log.error(content, throwable)
    }

    suspend fun <Event: IEvent>send(event: Event) {
        logEvent("send", event)
        broadcast.send(event)
    }

    fun <Event: IEvent>post(event: Event) = ClientDispatcher.launch(GlobalScheduler.Default) {
        logEvent("send", event)
        broadcast.send(event)
    }

    fun <Event: IEvent>send(event: Event, context: CoroutineContext) {
        ClientDispatcher.launch(context) {
            send(event)
        }
    }

    fun <Return: Any, Event: CallBackEvent<Return>>sendCallbackEvent(event: Event, context: CoroutineContext) {
        ClientDispatcher.launch(context) {
            send(event)
        }
    }

    inline fun <reified T> receive(context: CoroutineContext = EmptyCoroutineContext, crossinline onEvent: suspend (T) -> Unit): Disposable {
        return receiveBase(context, onEvent)
    }

    inline fun <reified Return, reified CallbackEvent: CallBackEvent<Return>> receiveCallback(context: CoroutineContext = EmptyCoroutineContext, crossinline onEvent: suspend (CallbackEvent) -> Return?): Disposable {
        return receiveBase<CallbackEvent, Return, CallbackEvent>(context, onEvent)
    }

    inline fun <reified T, reified Return, reified CallbackEvent: CallBackEvent<Return>> receiveBase(context: CoroutineContext = EmptyCoroutineContext, crossinline onEvent: suspend (T) -> Return?): Disposable {
        val channel = broadcast.openSubscription()
        logEvent("find", channel)
        val job = GlobalScope.launch(context) {
            for (event in channel) {
                if (event is T) {
                    logEvent("find", event)
                    supervisorScope {
                        launch(OnlyLogExceptionHandler) {

                            try {
                                if (event is CallbackEvent) {
                                    logEvent("receive onEventCallback", event)
                                    event.onEventCallback(onEvent(event))
                                } else {
                                    logEvent("receive onEvent", event)
                                    onEvent(event)
                                }
                            } catch (e: Throwable) {
                                logError("receive", e)
                            }
                        }
                    }
                }
            }
        }
        val disposable = object : Disposable {
            override suspend fun dispose() {
                channel.cancel()
                job.cancelAndJoin()
            }
        }
        return disposable
    }
}