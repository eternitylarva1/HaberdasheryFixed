package haberdashery

import com.evacipated.cardcrawl.modthespire.lib.SpireConfig
import java.io.IOException
import java.util.*
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible

object Config {
    private val config: SpireConfig?

    // Config properties
    var animatedRelics by BooleanValue(true)
    var playSfx by BooleanValue(false)

    init {
        config = try {
            SpireConfig(HaberdasheryMod.ID, "config", defaults())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun defaults(): Properties {
        val ret = Properties()
        Config::class.declaredMemberProperties.forEach {
            it.isAccessible = true
            val delegate = it.getDelegate(this)
            if (delegate is ConfigValue<*>) {
                ret.setProperty(it.name, delegate.default.toString())
            }
        }
        return ret
    }

    sealed class ConfigValue<T: Any>(val default: T) {
        abstract operator fun getValue(thisRef: Config, property: KProperty<*>): T
        abstract operator fun setValue(thisRef: Config, property: KProperty<*>, value: T)
    }

    class BooleanValue(default: Boolean) : ConfigValue<Boolean>(default) {
        override fun getValue(thisRef: Config, property: KProperty<*>): Boolean {
            return thisRef.config?.getBool(property.name) ?: default
        }

        override fun setValue(thisRef: Config, property: KProperty<*>, value: Boolean) {
            thisRef.config?.setBool(property.name, value)
            try {
                thisRef.config?.save()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
