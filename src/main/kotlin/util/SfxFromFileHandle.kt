package com.evacipated.cardcrawl.mod.haberdashery.util

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.files.FileHandle
import com.evacipated.cardcrawl.mod.haberdashery.extensions.privateMethod
import com.evacipated.cardcrawl.mod.haberdashery.extensions.setPrivate
import com.evacipated.cardcrawl.modthespire.lib.ByRef
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch
import com.megacrit.cardcrawl.audio.Sfx

class SfxFromFileHandle(
    fileHandle: FileHandle,
    preload: Boolean,
) : Sfx("", false) {
    constructor(fileHandle: FileHandle) : this(fileHandle, false)

    private var fileHandle: FileHandle? = null

    init {
        if (preload) {
            val sound = privateMethod("initSound", FileHandle::class.java, clazz = Sfx::class.java)
                .invoke<Sound>(this, fileHandle)
            setPrivate("sound", sound, Sfx::class.java)
        } else {
            this.fileHandle = fileHandle
        }
    }

    @SpirePatch2(
        clz = Sfx::class,
        method = "initSound"
    )
    object Patch {
        @JvmStatic
        @SpirePrefixPatch
        fun loadFromFileHandle(__instance: Sfx, @ByRef file: Array<FileHandle?>, @ByRef ___sound: Array<Sound?>) {
            if (__instance is SfxFromFileHandle && ___sound[0] == null) {
                if (__instance.fileHandle != null) {
                    file[0] = __instance.fileHandle
                }
            }
        }
    }
}
