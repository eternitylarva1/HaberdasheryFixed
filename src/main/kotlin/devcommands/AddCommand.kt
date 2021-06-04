package haberdashery.devcommands

import basemod.devcommands.ConsoleCommand
import basemod.devcommands.relic.Relic
import com.esotericsoftware.spine.Skeleton
import com.megacrit.cardcrawl.core.AbstractCreature
import com.megacrit.cardcrawl.core.Settings
import com.megacrit.cardcrawl.dungeons.AbstractDungeon
import com.megacrit.cardcrawl.helpers.RelicLibrary
import haberdashery.AdjustRelic
import haberdashery.database.AttachDatabase
import haberdashery.database.AttachInfo
import haberdashery.extensions.getPrivate

class AddCommand : ConsoleCommand() {
    init {
        requiresPlayer = true
        minExtraTokens = 2
        maxExtraTokens = 2
    }

    override fun execute(tokens: Array<out String>, depth: Int) {
        val bone = tokens[depth]
        val relicId = Relic.getRelicName(tokens.copyOfRange(depth + 1, tokens.size))

        val relic = RelicLibrary.getRelic(relicId)?.makeCopy()
        if (relic != null) {
            AttachDatabase.relic(
                AbstractDungeon.player.chosenClass,
                relicId,
                AttachInfo(bone)
            )

            AbstractDungeon.getCurrRoom().spawnRelicAndObtain(
                Settings.WIDTH / 2f, Settings.HEIGHT / 2f,
                relic
            )

            AdjustRelic.setRelic(relicId)
        }
    }

    override fun extraOptions(tokens: Array<out String>, depth: Int): ArrayList<String> {
        return if (tokens.size > depth + 1) {
            getRelicOptions().also {
                if (it.contains(tokens[depth + 1])) {
                    complete = true
                }
            }
        } else {
            val list = AbstractDungeon.player.getPrivate<Skeleton?>("skeleton", clazz = AbstractCreature::class.java)?.let { skeleton ->
                skeleton.bones.map { it.data.name }
            } ?: emptyList()
            ArrayList(list)
        }
    }
}
