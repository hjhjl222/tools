package com.bhh.tools.util

import com.bhh.tools.log.Logger
import java.lang.reflect.Field
import java.lang.reflect.Method


fun <T : Any> Any.getFieldValue(name: String): T? = getValue(javaClass, this, name)

fun <T : Any> Class<*>.getStaticFieldValue(name: String): T? = getValue(this, null, name)

@Suppress("UNCHECKED_CAST")
private fun <T : Any> getValue(clazz: Class<*>, any: Any?, fieldName: String): T? {
    return try {
        val field = clazz.getDeclaredField(fieldName)
        field.isAccessible = true
        field.get(any) as T?
    } catch (e: NoSuchFieldException) {
        getValue(clazz.superclass ?: throw e, any, fieldName)
    }
}

fun Class<*>.getFieldFromParent(fieldType: String): Field? {
    val field = getSelfField(fieldType)
    if (field == null && superclass != null) {
        return superclass.getFieldFromParent(fieldType)
    }
    return field
}

@Suppress("UNCHECKED_CAST")
private fun Class<*>.getSelfField(fieldType: String): Field? {
    val fields = this.declaredFields
    return fields.find { it.type.name.contains(fieldType) }
}

fun Any.setFieldValue(name: String, value: Any?) {
    setValue(javaClass, this, name, value)
}

fun Any.printAllFieldValue() {
    this::class.java.declaredFields
}

fun Class<*>.printAllMethods() {
    this.declaredMethods.forEach {
        log.debug("method info: ${it.name} -> ${it.parameterTypes.contentToString()}")
    }
}

private val log = Logger.get("reflect")
fun Any.printObject(cl: Class<*>? = null, printSuper: Boolean = false, tag: String? = null) {

    var c = cl
    if (c == null) {
        c = this::class.java
    }

    val fields = c.declaredFields
    for (field in fields) {
        val value = getValue<Any>(c, this, field.name)
        if (field.name == "lastText") {
            if (value is Collection<*>) {
                value.forEach {
                    it?.printObject(tag = "${it::class.java.name} Collection lastText", printSuper = true)
                }
            } else if (value is Array<*>) {
                value.forEach {
                    it?.printObject(tag = "${it::class.java.name}  Array lastText", printSuper = true)
                }
            }
        }
        log.debug("$tag field info: ${field.name} -> $value")
    }

    if (c.superclass != null && printSuper) {
        printObject(c.superclass, printSuper)
    }
}

fun Class<*>.setStaticFieldValue(name: String, value: Any?) {
    setValue(this, null, name, value)
}

private fun setValue(clazz: Class<*>, any: Any?, fieldName: String, value: Any?) {
    try {
        val field = clazz.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(any, value)
    } catch (e: NoSuchFieldException) {
        setValue(clazz.superclass ?: throw e, any, fieldName, value)
    }
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> Any.invokeMethod(name: String, vararg args: Any): T? {
    return invoke(javaClass, this, name, args.map { it.javaClass }.toTypedArray() as Array<Class<*>>, *args)
}


fun <T : Any> Any.invokeMethodWithType(name: String, parameterTypes: Array<Class<*>>, args: Array<Any?>): T? {
    return invoke(javaClass, this, name, parameterTypes, *args)
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> Class<*>.invokeStaticMethod(name: String, vararg args: Any): T? {
    return invoke(this, null, name, args.map { it.javaClass }.toTypedArray() as Array<Class<*>>, *args)
}

fun <T : Any> Class<*>.invokeStaticMethodWithType(name: String, parameterTypes: Array<Class<*>>, args: Array<Any?>): T? {
    return invoke(this, null, name, parameterTypes, *args)
}

@Suppress("UNCHECKED_CAST")
private fun <T : Any> invoke(clazz: Class<*>, any: Any?, methodName: String, parameterTypes: Array<Class<*>>, vararg args: Any?): T? {
    return try {
        val method = clazz.getDeclaredMethod(methodName, *parameterTypes)
        method.isAccessible = true
        method.invoke(any, *args) as T?
    } catch (e: NoSuchMethodException) {
        invoke(clazz.superclass ?: throw e, any, methodName, parameterTypes, *args)
    }
}

val Class<*>.allFields: List<Field>
    get() {
        val fields = declaredFields.toMutableList()
        if (superclass != null) {
            fields.addAll(superclass!!.allFields)
        }
        return fields
    }

val Class<*>.allMethods: List<Method>
    get() {
        val methods = declaredMethods.toMutableList()
        if (superclass != null) {
            methods.addAll(superclass!!.allMethods)
        }
        return methods
    }

/**
 * 反射调用 suspend 方法
 */
suspend fun <T : Any> Any.invokeSuspendMethod(name: String, vararg args: Any?): T? {
    return invokeSuspend(javaClass, this, name, args)
}

suspend fun <T : Any> Class<*>.invokeStaticSuspendMethod(name: String, vararg args: Any?): T? {
    return invokeSuspend(this, null, name, args)
}

@Suppress("UNCHECKED_CAST")
private suspend fun <T : Any> invokeSuspend(clazz: Class<*>, any: Any?, methodName: String, args: Array<out Any?>): T? {
    // 1. 获取参数类型列表
    // 注意：这里我们假设传入的 args 类型就是方法签名的类型（除了最后的 Continuation）
    // 对于 null 值或者子类类型，这种简单推断可能会失败，严谨的做法需要用户传入参数类型
    // 但为了易用性，我们先尝试通过 args 推断，并在查找方法时允许一定的模糊匹配
    
    return kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn<T> { continuation ->
        try {
            // 2. 构造完整的参数列表 (args + continuation)
            val fullArgs = arrayOf(*args, continuation)
            
            // 3. 查找方法
            // 由于 suspend 函数的最后一个参数是 Continuation，我们需要找到这样的方法
            // 这里为了简化，遍历所有同名方法，检查参数数量和类型兼容性
            val method = findSuspendMethod(clazz, methodName, args) 
                ?: throw NoSuchMethodException("Suspend method $methodName not found in $clazz")
            
            method.isAccessible = true
            
            // 4. 调用
            val result = method.invoke(any, *fullArgs)
            
            // 5. 处理返回值
            // 如果结果是 COROUTINE_SUSPENDED，说明函数挂起了，结果会通过 continuation 回调
            // 如果结果是普通值，说明函数没有挂起直接返回了
            result
        } catch (e: Exception) {
            throw e
        }
    } as T?
}

private fun findSuspendMethod(clazz: Class<*>, methodName: String, args: Array<out Any?>): Method? {
    // 遍历当前类及其父类
    var currentClass: Class<*>? = clazz
    while (currentClass != null) {
        val methods = currentClass.declaredMethods
        for (method in methods) {
            if (method.name != methodName) continue
            
            val paramTypes = method.parameterTypes
            // suspend 函数至少有一个参数 (Continuation)，且最后一个参数必须是 Continuation
            if (paramTypes.isEmpty()) continue
            if (!kotlin.coroutines.Continuation::class.java.isAssignableFrom(paramTypes.last())) continue
            
            // 参数数量检查 (args.size + 1 == paramTypes.size)
            if (paramTypes.size != args.size + 1) continue
            
            // 参数类型检查 (忽略最后一个 Continuation)
            var isMatch = true
            for (i in args.indices) {
                val arg = args[i]
                val paramType = paramTypes[i]
                if (arg != null && !paramType.isAssignableFrom(arg.javaClass)) {
                    // 基本类型拆箱检查
                    if (!isPrimitiveWrapperMatch(paramType, arg.javaClass)) {
                        isMatch = false
                        break
                    }
                }
            }
            
            if (isMatch) return method
        }
        currentClass = currentClass.superclass
    }
    return null
}

private fun isPrimitiveWrapperMatch(targetType: Class<*>, sourceType: Class<*>): Boolean {
    if (!targetType.isPrimitive) return false
    return when (targetType) {
        Boolean::class.javaPrimitiveType -> sourceType == java.lang.Boolean::class.java
        Byte::class.javaPrimitiveType -> sourceType == java.lang.Byte::class.java
        Char::class.javaPrimitiveType -> sourceType == java.lang.Character::class.java
        Short::class.javaPrimitiveType -> sourceType == java.lang.Short::class.java
        Int::class.javaPrimitiveType -> sourceType == java.lang.Integer::class.java
        Long::class.javaPrimitiveType -> sourceType == java.lang.Long::class.java
        Float::class.javaPrimitiveType -> sourceType == java.lang.Float::class.java
        Double::class.javaPrimitiveType -> sourceType == java.lang.Double::class.java
        else -> false
    }
}