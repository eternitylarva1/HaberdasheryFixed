package haberdashery.util

import haberdashery.HaberdasheryMod

class AssetDelegate<T : Any?>(
    private val filename: String,
    private val clazz: Class<T>,
    val preload: Boolean,
) : Lazy<T> {
    @Volatile private var  _value: T? = null
    @Volatile private var initialized = false

    private val lock = this

    override val value: T
        get() {
            if (initialized) {
                return _value!!
            }

            return synchronized(lock) {
                _value = load()
                initialized = true
                _value!!
            }
        }

    override fun isInitialized(): Boolean =
        initialized

    private fun load(): T {
        return AssetLoader.get(filename, clazz)!!
    }

    fun preload() {
        AssetLoader.preload(filename, clazz)
    }
}

inline fun <reified T> asset(filename: String) =
    AssetDelegate(HaberdasheryMod.assetPath(filename), T::class.java, false)

inline fun <reified T> preload(filename: String) =
    AssetDelegate(HaberdasheryMod.assetPath(filename), T::class.java, true)
