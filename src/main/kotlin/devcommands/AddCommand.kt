package com.evacipated.cardcrawl.mod.haberdashery.devcommands

import basemod.devcommands.ConsoleCommand
import basemod.devcommands.relic.Relic
import com.evacipated.cardcrawl.mod.haberdashery.AdjustRelic
import com.megacrit.cardcrawl.helpers.RelicLibrary

class AddCommand : ConsoleCommand() {
    init {
        requiresPlayer = true
        minExtraTokens = 1
        maxExtraTokens = 1
        simpleCheck = true
    }

    override fun execute(tokens: Array<out String>, depth: Int) {
        val relicId = Relic.getRelicName(tokens.copyOfRange(depth, tokens.size))

        val relic = RelicLibrary.getRelic(relicId)?.makeCopy()
        if (relic != null) {
            AdjustRelic.addRelic(relicId)
        }
    }

    override fun extraOptions(tokens: Array<out String>, depth: Int): ArrayList<String> {
        return getRelicOptions()
    }
}
