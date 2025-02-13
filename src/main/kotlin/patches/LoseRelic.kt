package com.evacipated.cardcrawl.mod.haberdashery.patches

import com.evacipated.cardcrawl.mod.haberdashery.AttachRelic
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2
import com.megacrit.cardcrawl.characters.AbstractPlayer

@SpirePatch2(
    clz = AbstractPlayer::class,
    method = "loseRelic"
)
object LoseRelic {
    var losingRelic = false

    @JvmStatic
    fun Postfix(__result: Boolean, targetID: String) {
        if (__result) {
            AttachRelic.lose(targetID)
        }
        losingRelic = false
    }

    @JvmStatic
    fun Prefix() {
        losingRelic = true
    }
}
