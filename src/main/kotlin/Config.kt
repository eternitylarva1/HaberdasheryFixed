package haberdashery

import com.evacipated.cardcrawl.modthespire.lib.SpireConfig
import java.io.IOException
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmErasure

object Config {
    private val config: SpireConfig?
    private val autocompleteInfo: MutableList<Pair<String, KClass<*>>> = mutableListOf()

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

        autocompleteInfo.sortBy { it.first }
    }

    private fun defaults(): Properties {
        val ret = Properties()
        Config::class.declaredMemberProperties.forEach {
            it.isAccessible = true
            val delegate = it.getDelegate(this)
            if (delegate is ConfigValue<*>) {
                autocompleteInfo.add(Pair(it.name, it.returnType.jvmErasure))
                ret.setProperty(it.name, delegate.default.toString())
            }
        }
        return ret
    }

    internal fun autocompleteInfo(): List<Pair<String, KClass<*>>> {
        return autocompleteInfo
    }

    internal fun setFromCommand(name: String, value: String): String? {
        println("$name=$value")
        val property = Config::class.declaredMemberProperties
            .filterIsInstance<KMutableProperty1<Config, Any>>()
            .firstOrNull { it.name == name }
        if (property == null) {
            return "No option named \"$name\""
        }
        when (property.returnType.jvmErasure) {
            Boolean::class -> property.set(this, value.toBoolean())
            else -> return "Unknown type"
        }
        return null
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
