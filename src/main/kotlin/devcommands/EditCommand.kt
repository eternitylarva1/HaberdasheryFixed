package com.evacipated.cardcrawl.mod.haberdashery.devcommands

import basemod.devcommands.ConsoleCommand
import basemod.devcommands.relic.Relic
import com.evacipated.cardcrawl.mod.haberdashery.AdjustRelic
import com.evacipated.cardcrawl.mod.haberdashery.database.AttachDatabase
import com.megacrit.cardcrawl.dungeons.AbstractDungeon
import com.megacrit.cardcrawl.helpers.RelicLibrary

class EditCommand : ConsoleCommand() {
    init {
        requiresPlayer = true
        minExtraTokens = 0
        maxExtraTokens = 1
        simpleCheck = true
    }

    override fun execute(tokens: Array<out String>, depth: Int) {
        if (tokens.size <= 2) {
            AdjustRelic.setRelic(null)
            return
        }
        val relicId = Relic.getRelicName(tokens.copyOfRange(2, tokens.size))
        if (!AbstractDungeon.player.hasRelic(relicId)) {
            RelicLibrary.getRelic(relicId)
                ?.makeCopy()
                ?.instantObtain(AbstractDungeon.player, AbstractDungeon.player.relics.size, false)
        }
        AdjustRelic.setRelic(relicId)
    }

    override fun extraOptions(tokens: Array<out String>?, depth: Int): ArrayList<String> {
        val ret = arrayListOf<String>()
        return AttachDatabase.getRelicsDevCommand(AbstractDungeon.player.chosenClass)
            .toCollection(ret)
    }
}
