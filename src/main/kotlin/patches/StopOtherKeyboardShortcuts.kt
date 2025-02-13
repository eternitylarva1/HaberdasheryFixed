package com.evacipated.cardcrawl.mod.haberdashery.patches

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2
import com.evacipated.cardcrawl.modthespire.lib.SpirePatches2
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch
import com.evacipated.cardcrawl.modthespire.lib.SpireReturn

@SpirePatches2(
    SpirePatch2(
        cls = "com.badlogic.gdx.backends.lwjgl.LwjglInput",
        method = "isKeyJustPressed"
    ),
    SpirePatch2(
        cls = "com.badlogic.gdx.backends.lwjgl3.Lwjgl3Input",
        method = "isKeyJustPressed",
        optional = true
    ),
)
object StopOtherKeyboardShortcuts {
    private val stoppedKeys = mutableSetOf<Int>()

    @JvmStatic
    @SpirePrefixPatch
    fun stopDuplicateShortcuts(key: Int): SpireReturn<Boolean> {
        if (stoppedKeys.contains(key)) {
            return SpireReturn.Return(false)
        }
        return SpireReturn.Continue()
    }

    internal fun stopForOneFrame(key: Int) {
        stoppedKeys.add(key)
    }

    internal fun clear() {
        stoppedKeys.clear()
    }
}
