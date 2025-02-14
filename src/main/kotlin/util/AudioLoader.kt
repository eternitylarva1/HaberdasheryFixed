package com.evacipated.cardcrawl.mod.haberdashery.util

import basemod.BaseMod
import basemod.ReflectionHacks
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.utils.StreamUtils
import com.evacipated.cardcrawl.mod.haberdashery.spine.FSFileHandle
import com.megacrit.cardcrawl.audio.Sfx
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.file.Path

object AudioLoader {
    fun load(path: Path) {
        load(FSFileHandle(path))
    }

    fun load(fileHandle: FileHandle) {
        if (!fileHandle.exists()) return

        var id: String? = null
        var file: FileHandle? = null
        var preload = false

        val reader = BufferedReader(InputStreamReader(fileHandle.read()), 64)

        try {
            while (true) {
                val line = reader.readLine() ?: break

                if (line.trim().isEmpty()) {
                    continue
                }

                with (FileReadUtil.readTuple(line)) {
                    when (name) {
                        "id" -> id = data[0]
                        "file" -> file = fileHandle.parent().child(data[0])
                        "preload" -> preload = data[0].toBoolean()
                    }
                }
            }
        } catch (e: Exception) {
            throw GdxRuntimeException("Error reading audio file: $fileHandle", e)
        } finally {
            StreamUtils.closeQuietly(reader)
        }

        @Suppress("KotlinConstantConditions")
        if (id != null && file != null && file?.exists() == true) {
            val audioToAdd = ReflectionHacks.getPrivateStatic<HashMap<String, Sfx>>(BaseMod::class.java, "audioToAdd")
            audioToAdd[id!!] = SfxFromFileHandle(file!!, preload)
        }
    }
}
