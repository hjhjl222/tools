package com.bhh.tools.coroutines.event

interface IEvent

abstract class CallBackEvent<Return: Any>(val onEventCallback: (Return?) -> Unit): IEvent