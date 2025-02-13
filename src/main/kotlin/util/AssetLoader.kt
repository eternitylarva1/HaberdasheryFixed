package com.evacipated.cardcrawl.mod.haberdashery.util

import com.badlogic.gdx.assets.AssetLoaderParameters
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.TextureLoader
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.GdxRuntimeException

object AssetLoader {
    private val manager = AssetManager().apply {
        setErrorListener { descriptor, e ->
            println(descriptor.fileName)
            e.printStackTrace()
        }
    }

    inline fun <reified T> get(filename: String): T? {
        return get(filename, T::class.java)
    }

    fun <T> get(filename: String, clazz: Class<T>): T? {
        if (!manager.isLoaded(filename, clazz)) {
            manager.load(filename, clazz, getParam(clazz))
            try {
                manager.finishLoadingAsset(filename)
            } catch (e: GdxRuntimeException) {
                return null
            }
        }
        return manager.get(filename, clazz)
    }

    inline fun <reified T> preload(filename: String) {
        preload(filename, T::class.java)
    }

    fun <T> preload(filename: String, clazz: Class<T>) {
        manager.load(filename, clazz, getParam(clazz))
    }

    private fun <T> getParam(clazz: Class<T>): AssetLoaderParameters<T>? {
        val ret: AssetLoaderParameters<*>? = when (clazz) {
            Texture::class.java -> TextureLoader.TextureParameter().apply {
                minFilter = Texture.TextureFilter.Linear
                magFilter = Texture.TextureFilter.Linear
            }
            else -> null
        }
        return ret as AssetLoaderParameters<T>?
    }

    internal fun update() {
        manager.update()
    }
}
