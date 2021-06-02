package haberdashery

import basemod.devcommands.ConsoleCommand
import basemod.devcommands.relic.Relic
import com.megacrit.cardcrawl.dungeons.AbstractDungeon

class AdjustCommand : ConsoleCommand() {
    init {
        requiresPlayer = true
        minExtraTokens = 1
        maxExtraTokens = 1
        simpleCheck = true
    }

    override fun execute(tokens: Array<out String>, depth: Int) {
        val relicId = Relic.getRelicName(tokens.copyOfRange(1, tokens.size))
        AdjustRelic.setRelic(relicId)
    }

    override fun extraOptions(tokens: Array<out String>?, depth: Int): ArrayList<String> {
        val ret = arrayListOf<String>()
        AbstractDungeon.player.relics
            .map { it.relicId.replace(' ', '_') }
            .toCollection(ret)
        return ret
    }
}
