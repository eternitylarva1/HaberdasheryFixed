package haberdashery.extensions

import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaMethod

val <R> KFunction<R>.inst: String
    get() = "${this.javaMethod!!.declaringClass.canonicalName}.${this.name}"

val <T> KProperty<T>.inst: String
    get() = "${this.javaField!!.declaringClass.canonicalName}.${this.name}"
