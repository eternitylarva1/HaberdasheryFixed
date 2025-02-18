package com.evacipated.cardcrawl.mod.haberdashery.util

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.utils.StreamUtils
import com.evacipated.cardcrawl.mod.haberdashery.HaberdasheryMod
import com.evacipated.cardcrawl.mod.haberdashery.database.AttachDatabase
import com.evacipated.cardcrawl.mod.haberdashery.patches.NewCursors
import com.evacipated.cardcrawl.mod.haberdashery.spine.FSFileHandle
import com.evacipated.cardcrawl.mod.haberdashery.ui.CustomCursor
import com.evacipated.cardcrawl.modthespire.Loader
import com.google.gson.GsonBuilder
import com.megacrit.cardcrawl.characters.AbstractPlayer
import com.megacrit.cardcrawl.core.GameCursor.CursorType
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.notExists

object CursorLoader {
    fun loadAll() {
        for (modInfo in Loader.MODINFOS) {
            val fs = AttachDatabase.getModFileSystem(modInfo) ?: continue
            val path = fs.getPath("/${HaberdasheryMod.ID}/cursors")
            if (path.notExists()) continue
            Files.walk(path)
                .filter(Files::isRegularFile)
                .filter { it.fileName?.toString()?.substringAfterLast(".", "") == "json" }
                .forEach {
                    load(it)
                }
        }
    }

    fun load(path: Path): CustomCursor? {
        return load(FSFileHandle(path))
    }

    fun load(path: String): CustomCursor? {
        return load(Gdx.files.internal(path))
    }

    fun load(fileHandle: FileHandle): CustomCursor? {
        if (!fileHandle.exists()) return null

        val imagesDir = fileHandle.parent()
        val reader = BufferedReader(InputStreamReader(fileHandle.read()), 64)

        try {
            val data = gson.fromJson(reader, LoadData::class.java)

            val tex = Texture(imagesDir.child(data.image)).apply {
                setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            }

            val cursor = CustomCursor(
                tex,
                data.size?.get(0) ?: tex.width.toFloat(),
                data.size?.get(1) ?: tex.height.toFloat(),
                data.hotspot?.get(0) ?: 0f,
                data.hotspot?.get(1) ?: 0f,
            )

            if (data.character != null) {
                NewCursors.addCursor(data.character, data.type, cursor)
            }

            return cursor
        } catch (e: Exception) {
            throw GdxRuntimeException("Error reading cursor file: $fileHandle", e)
        } finally {
            StreamUtils.closeQuietly(reader)
        }
    }

    private val gson = GsonBuilder().create()

    private data class LoadData(
        val character: AbstractPlayer.PlayerClass?,
        val image: String,
        val type: CursorType,
        val size: FloatArray?,
        val hotspot: FloatArray?,
    )
}
