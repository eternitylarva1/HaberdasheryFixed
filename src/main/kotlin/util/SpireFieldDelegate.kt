package com.evacipated.cardcrawl.mod.haberdashery.util

import com.evacipated.cardcrawl.modthespire.lib.SpireField
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class SpireFieldDelegate<T : Any, V : Any?>(
    private val field: SpireField<V>
) : ReadWriteProperty<T, V> {
    override operator fun getValue(thisRef: T, property: KProperty<*>): V {
        return field.get(thisRef)
    }

    override operator fun setValue(thisRef: T, property: KProperty<*>, value: V) {
        return field.set(thisRef, value)
    }
}
