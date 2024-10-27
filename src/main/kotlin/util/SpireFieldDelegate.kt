package haberdashery.util

import com.evacipated.cardcrawl.modthespire.lib.SpireField
import kotlin.reflect.KProperty

class SpireFieldDelegate<Ref : Any, T : Any?>(
    private val field: SpireField<T>
) {
    operator fun getValue(thisRef: Ref, property: KProperty<*>): T {
        return field.get(thisRef)
    }

    operator fun setValue(thisRef: Ref, property: KProperty<*>, value: T) {
        return field.set(thisRef, value)
    }
}
