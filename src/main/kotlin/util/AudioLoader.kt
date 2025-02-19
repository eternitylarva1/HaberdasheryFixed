package com.evacipated.cardcrawl.mod.haberdashery.util

import basemod.BaseMod
import basemod.ReflectionHacks
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.utils.StreamUtils
import com.evacipated.cardcrawl.mod.haberdashery.HaberdasheryMod
import com.evacipated.cardcrawl.mod.haberdashery.database.AttachDatabase
import com.evacipated.cardcrawl.mod.haberdashery.spine.FSFileHandle
import com.evacipated.cardcrawl.modthespire.Loader
import com.google.gson.GsonBuilder
import com.megacrit.cardcrawl.audio.Sfx
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.notExists

object AudioLoader {
    fun loadAll() {
        for (modInfo in Loader.MODINFOS) {
            val fs = AttachDatabase.getModFileSystem(modInfo) ?: continue
            val path = fs.getPath("/${HaberdasheryMod.ID}/audio")
            if (path.notExists()) continue
            Files.walk(path)
                .filter(Files::isRegularFile)
                .filter { it.fileName?.toString()?.substringAfterLast(".", "") == "json" }
                .forEach {
                    load(it)
                }
        }
    }
    fun load(path: Path) {
        load(FSFileHandle(path))
    }

    fun load(fileHandle: FileHandle) {
        if (!fileHandle.exists()) return

        val reader = BufferedReader(InputStreamReader(fileHandle.read()), 64)

        try {
            val data = gson.fromJson(reader, LoadData::class.java)

            if (data.id != null && data.file != null) {
                val file = fileHandle.parent().child(data.file)
                if (file.exists()) {
                    val audioToAdd = ReflectionHacks.getPrivateStatic<HashMap<String, Sfx>>(BaseMod::class.java, "audioToAdd")
                    audioToAdd[data.id] = SfxFromFileHandle(file, data.preload)
                }
            }
        } catch (e: Exception) {
            throw GdxRuntimeException("Error reading audio file: $fileHandle", e)
        } finally {
            StreamUtils.closeQuietly(reader)
        }
    }

    private val gson = GsonBuilder().create()

    private data class LoadData(
        val id: String?,
        val file: String?,
        val preload: Boolean = false,
    )
}
