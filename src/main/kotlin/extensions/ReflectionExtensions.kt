package haberdashery.extensions

import basemod.ReflectionHacks
import kotlin.reflect.KClass

// Field access helpers
fun <T> Any.getPrivate(
    fieldName: String,
    clazz: Class<*> = this::class.java
): T {
    return ReflectionHacks.getPrivate(this, clazz, fieldName)
}

fun <T> Any.setPrivate(
    fieldName: String,
    value: T,
    clazz: Class<*> = this::class.java
) {
    ReflectionHacks.setPrivate(this, clazz, fieldName, value)
}

fun <T> Any.setPrivateFinal(
    fieldName: String,
    value: T,
    clazz: Class<*> = this::class.java
) {
    ReflectionHacks.setPrivateFinal(this, clazz, fieldName, value)
}

fun <T> KClass<*>.getPrivate(fieldName: String): T {
    return ReflectionHacks.getPrivateStatic(this.java, fieldName)
}

fun <T> KClass<*>.setPrivate(fieldName: String, value: T) {
    ReflectionHacks.setPrivateStatic(this.java, fieldName, value)
}

fun <T> Class<*>.getPrivate(fieldName: String): T {
    return ReflectionHacks.getPrivateStatic(this, fieldName)
}

fun <T> Class<*>.setPrivate(fieldName: String, value: T) {
    ReflectionHacks.setPrivateStatic(this, fieldName, value)
}

// Method call helpers
fun Any.privateMethod(
    methodName: String,
    vararg argTypes: KClass<*>,
    clazz: KClass<*> = this::class
): ReflectionHacks.RMethod {
    return ReflectionHacks.privateMethod(clazz.java, methodName, *argTypes.map { it.java}.toTypedArray())
}

fun Any.privateMethod(
    methodName: String,
    vararg argTypes: Class<*>,
    clazz: Class<*> = this::class.java
): ReflectionHacks.RMethod {
    return ReflectionHacks.privateMethod(clazz, methodName, *argTypes)
}

fun KClass<*>.privateMethod(
    methodName: String,
    vararg argTypes: KClass<*>
): ReflectionHacks.RMethod {
    return ReflectionHacks.privateStaticMethod(this.java, methodName, *argTypes.map { it.java }.toTypedArray())
}

fun Class<*>.privateMethod(
    methodName: String,
    vararg argTypes: Class<*>
): ReflectionHacks.RMethod {
    return ReflectionHacks.privateStaticMethod(this, methodName, *argTypes)
}
