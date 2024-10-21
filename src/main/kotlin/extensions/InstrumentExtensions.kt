package haberdashery.extensions

import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

val <R> KFunction<R>.inst: String
    get() = "${this.javaMethod!!.declaringClass.canonicalName}.${this.name}"
