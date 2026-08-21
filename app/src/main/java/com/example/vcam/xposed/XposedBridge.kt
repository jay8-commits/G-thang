package com.example.vcam.xposed

import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Member
import java.lang.reflect.Method
import java.lang.reflect.Modifier

/**
 * Xposed Bridge interface definitions and helpers.
 * Ensures the module compiles without external binary stub requirements
 * while executing properly inside LSPosed / EdXposed / Xposed runtimes.
 */
interface IXposedHookLoadPackage {
    @Throws(Throwable::class)
    fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam)
}

interface IXposedHookZygoteInit {
    @Throws(Throwable::class)
    fun initZygote(startupParam: StartupParam)

    class StartupParam {
        var modulePath: String? = null
        var startsSystemServer: Boolean = false
    }
}

abstract class XC_MethodHook {
    open class MethodHookParam {
        @JvmField var method: Member? = null
        @JvmField var thisObject: Any? = null
        @JvmField var args: Array<Any?> = emptyArray()
        @JvmField var result: Any? = null
        @JvmField var throwable: Throwable? = null
        @JvmField var returnEarly: Boolean = false

        fun setResult(result: Any?) {
            this.result = result
            this.throwable = null
            this.returnEarly = true
        }

        fun setThrowable(throwable: Throwable?) {
            this.throwable = throwable
            this.result = null
            this.returnEarly = true
        }
    }

    class Unhook internal constructor(
        val hook: XC_MethodHook,
        val method: Member
    ) {
        fun unhook() {
            XposedBridge.unhookMethod(method, hook)
        }
    }

    @Throws(Throwable::class)
    open fun beforeHookedMethod(param: MethodHookParam) {}

    @Throws(Throwable::class)
    open fun afterHookedMethod(param: MethodHookParam) {}
}

class XC_LoadPackage {
    class LoadPackageParam {
        var packageName: String = ""
        var processName: String = ""
        var classLoader: ClassLoader? = null
        var isFirstApplication: Boolean = false
        var appInfo: android.content.pm.ApplicationInfo? = null
    }
}

object XposedBridge {
    private val hookedMethods = mutableMapOf<Member, MutableList<XC_MethodHook>>()

    fun log(text: String) {
        android.util.Log.i("GThang-Xposed", text)
    }

    fun log(t: Throwable) {
        android.util.Log.e("GThang-Xposed", "Xposed error", t)
    }

    fun hookMethod(hookMethod: Member, callback: XC_MethodHook): XC_MethodHook.Unhook {
        synchronized(hookedMethods) {
            val list = hookedMethods.getOrPut(hookMethod) { mutableListOf() }
            list.add(callback)
        }
        return XC_MethodHook.Unhook(callback, hookMethod)
    }

    fun unhookMethod(hookMethod: Member, callback: XC_MethodHook) {
        synchronized(hookedMethods) {
            hookedMethods[hookMethod]?.remove(callback)
        }
    }

    fun hookAllMethods(hookClass: Class<*>, methodName: String, callback: XC_MethodHook): Set<XC_MethodHook.Unhook> {
        val result = mutableSetOf<XC_MethodHook.Unhook>()
        for (method in hookClass.declaredMethods) {
            if (method.name == methodName) {
                result.add(hookMethod(method, callback))
            }
        }
        return result
    }

    fun hookAllConstructors(hookClass: Class<*>, callback: XC_MethodHook): Set<XC_MethodHook.Unhook> {
        val result = mutableSetOf<XC_MethodHook.Unhook>()
        for (constructor in hookClass.declaredConstructors) {
            result.add(hookMethod(constructor, callback))
        }
        return result
    }
}

object XposedHelpers {
    fun findClass(className: String, classLoader: ClassLoader?): Class<*> {
        val cl = classLoader ?: ClassLoader.getSystemClassLoader()
        return Class.forName(className, false, cl)
    }

    fun findClassIfExists(className: String, classLoader: ClassLoader?): Class<*>? {
        return try {
            findClass(className, classLoader)
        } catch (e: Throwable) {
            null
        }
    }

    fun findMethodExact(clazz: Class<*>, methodName: String, vararg parameterTypes: Class<*>): Method {
        val method = clazz.getDeclaredMethod(methodName, *parameterTypes)
        method.isAccessible = true
        return method
    }

    fun findMethodExactIfExists(clazz: Class<*>, methodName: String, vararg parameterTypes: Class<*>): Method? {
        return try {
            findMethodExact(clazz, methodName, *parameterTypes)
        } catch (e: Throwable) {
            null
        }
    }

    fun findConstructorExact(clazz: Class<*>, vararg parameterTypes: Class<*>): Constructor<*> {
        val constructor = clazz.getDeclaredConstructor(*parameterTypes)
        constructor.isAccessible = true
        return constructor
    }

    fun findAndHookMethod(
        clazz: Class<*>,
        methodName: String,
        vararg parameterTypesAndCallback: Any
    ): XC_MethodHook.Unhook? {
        if (parameterTypesAndCallback.isEmpty()) return null
        val callback = parameterTypesAndCallback.last() as? XC_MethodHook ?: return null
        val paramCount = parameterTypesAndCallback.size - 1
        val paramTypes = Array(paramCount) { i ->
            when (val param = parameterTypesAndCallback[i]) {
                is Class<*> -> param
                is String -> findClass(param, clazz.classLoader)
                else -> throw IllegalArgumentException("parameter type must be Class or String")
            }
        }

        return try {
            val method = findMethodExact(clazz, methodName, *paramTypes)
            XposedBridge.hookMethod(method, callback)
        } catch (t: Throwable) {
            XposedBridge.log("Failed to hook method $methodName on ${clazz.name}: ${t.message}")
            null
        }
    }

    fun findAndHookMethod(
        className: String,
        classLoader: ClassLoader?,
        methodName: String,
        vararg parameterTypesAndCallback: Any
    ): XC_MethodHook.Unhook? {
        val clazz = findClass(className, classLoader)
        return findAndHookMethod(clazz, methodName, *parameterTypesAndCallback)
    }

    fun findAndHookConstructor(
        clazz: Class<*>,
        vararg parameterTypesAndCallback: Any
    ): XC_MethodHook.Unhook? {
        if (parameterTypesAndCallback.isEmpty()) return null
        val callback = parameterTypesAndCallback.last() as? XC_MethodHook ?: return null
        val paramCount = parameterTypesAndCallback.size - 1
        val paramTypes = Array(paramCount) { i ->
            when (val param = parameterTypesAndCallback[i]) {
                is Class<*> -> param
                is String -> findClass(param, clazz.classLoader)
                else -> throw IllegalArgumentException("parameter type must be Class or String")
            }
        }

        return try {
            val constructor = findConstructorExact(clazz, *paramTypes)
            XposedBridge.hookMethod(constructor, callback)
        } catch (t: Throwable) {
            XposedBridge.log("Failed to hook constructor on ${clazz.name}: ${t.message}")
            null
        }
    }

    fun getObjectField(obj: Any, fieldName: String): Any? {
        var clazz: Class<*>? = obj.javaClass
        while (clazz != null) {
            try {
                val field = clazz.getDeclaredField(fieldName)
                field.isAccessible = true
                return field.get(obj)
            } catch (e: NoSuchFieldException) {
                clazz = clazz.superclass
            }
        }
        throw NoSuchFieldError(fieldName)
    }

    fun setObjectField(obj: Any, fieldName: String, value: Any?) {
        var clazz: Class<*>? = obj.javaClass
        while (clazz != null) {
            try {
                val field = clazz.getDeclaredField(fieldName)
                field.isAccessible = true
                field.set(obj, value)
                return
            } catch (e: NoSuchFieldException) {
                clazz = clazz.superclass
            }
        }
        throw NoSuchFieldError(fieldName)
    }

    fun getStaticObjectField(clazz: Class<*>, fieldName: String): Any? {
        val field = clazz.getDeclaredField(fieldName)
        field.isAccessible = true
        return field.get(null)
    }

    fun setStaticObjectField(clazz: Class<*>, fieldName: String, value: Any?) {
        val field = clazz.getDeclaredField(fieldName)
        field.isAccessible = true
        field.set(null, value)
    }

    fun callMethod(obj: Any, methodName: String, vararg args: Any?): Any? {
        val argTypes = Array(args.size) { i -> args[i]?.javaClass ?: Any::class.java }
        val method = obj.javaClass.getMethod(methodName, *argTypes)
        method.isAccessible = true
        return method.invoke(obj, *args)
    }
}
