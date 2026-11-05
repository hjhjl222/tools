package com.bhh.tools.util

import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import java.lang.reflect.Field
import kotlin.reflect.KClass

fun <T: Activity>KClass<T>.startActivity(context: Context, bundle: Bundle? = null) {
    val intent = Intent(context, this.java)
    if (bundle != null) {
        intent.putExtras(bundle)
    }
    context.startActivity(intent)
}

fun <T: Service>KClass<T>.startService(context: Context, bundle: Bundle? = null) {
    val intent = Intent(context, this.java)
    if (bundle != null) {
        intent.putExtras(bundle)
    }
    context.startService(intent)
}


inline fun <reified T : Any> KClass<T>.clone(instance: T): T {
    val constructor = java.getDeclaredConstructor()
        ?: throw IllegalArgumentException("Class ${java.simpleName} does not have a default constructor")
    val newInstance = constructor.newInstance()
    val properties = java.declaredFields
    properties.forEach { property ->
        if (property.isAccessible) {
            val value = property.get(instance)
            property.set(newInstance, value)
        } else {
            property.isAccessible = true
            val value = property.get(instance)
            property.set(newInstance, value)
            property.isAccessible = false
        }
    }
    return newInstance
}

/*fun Any.toFieldValueString(): String {
    val properties = mutableListOf<Field>()
    var clazz: Class<*>? = this::class.java
    while (clazz != null) {
        properties.addAll(clazz.declaredFields)
        clazz = clazz.superclass
    }
    return properties.filter { it.name != "shadow\$_klass_" && it.name != "shadow\$_monitor_"}.joinToString("\n") { property ->
        val value = if (property.isAccessible) {
            property.get(this)
        } else {
            property.isAccessible = true
            val result = property.get(this)
            property.isAccessible = false
            result
        }
        val formattedValue = when (value) {
            is Array<*> -> value.joinToString("\n") { it.toString() }
            is Collection<*> -> value.joinToString("\n") { it.toString() }
            is Map<*, *> -> value.entries.joinToString("\n") { "${it.key}: ${it.value}" }
            else -> value.toString()
        }
        "${property.name}: $formattedValue"
    }
}*/


fun Any.toFieldValueString(vararg excludes: String): String {
    val properties = mutableListOf<Field>()
    var clazz: Class<*>? = this::class.java
    while (clazz != null) {
        properties.addAll(clazz.declaredFields)
        clazz = clazz.superclass
    }
    return properties.filter { it.name != "shadow\$_klass_" && it.name != "shadow\$_monitor_" && !excludes.contains(it.name) }.joinToString("\n") { property ->
        val value = if (property.isAccessible) {
            property.get(this)
        } else {
            property.isAccessible = true
            val result = property.get(this)
            property.isAccessible = false
            result
        }
        val formattedValue = when (value) {
            is Array<*> -> "\n  [\n ${value.joinToString("\n   ") { it?.toString() ?: "null" }}\n   ]"
            is Collection<*> -> "   \n  [\n${value.joinToString("\n  ") { it?.toString() ?: "null" }}\n ]"
            is Map<*, *> -> "\n {\n ${value.entries.joinToString("\n  ") { "${it.key}: ${it.value?.toString() ?: "null"}" }}\n  }"
            else -> value?.toString() ?: "null"
        }
        "【${property.name}】: $formattedValue"
    }
}


