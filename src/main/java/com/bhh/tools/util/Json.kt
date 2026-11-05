package com.bhh.tools.util

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.bhh.tools.log.Logger
import java.lang.reflect.Type
import kotlin.reflect.KClass

object Json {
    private val logger = Logger.get("Json")
    private val gson = GsonBuilder().serializeNulls().disableHtmlEscaping().create()

    fun <T : Any> from(json: String, classOfT: KClass<T>): T? {
        return try {
            gson.fromJson(json, classOfT.java)
        } catch (t: Throwable) {
            null
        }
    }

    fun <T : Any> from(json: String, typeOfT: Type): T? {
        return try {
            gson.fromJson(json, typeOfT)
        } catch (t: Throwable) {
            logger.error(t.message,t)
            null
        }
    }

    fun to(value: Any): String = gson.toJson(value)
}


fun Any.toPrintJsonString(): String? {
    return try {
        val gson = GsonBuilder().setPrettyPrinting().create()
        gson.toJson(this)
    } catch (t: Throwable) {
        null
    }
}

fun <T : Any> KClass<T>.newInstanceFromJSON(json: String?): T? =
    Json.from(json.let { if (it.isNullOrBlank()) "{}" else it }, this)

inline fun <reified T : Any> newInstanceFromJSON(json: String?, kClass: KClass<T>): T? =
    Json.from(json.let { if (it.isNullOrBlank()) "{}" else it }, kClass)

fun Any.toJSON() = try {
    Json.to(this)
} catch (t: Throwable) {
    null
}