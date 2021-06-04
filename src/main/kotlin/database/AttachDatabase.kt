package haberdashery.database

import com.badlogic.gdx.Gdx
import com.google.gson.GsonBuilder
import com.megacrit.cardcrawl.characters.AbstractPlayer
import com.megacrit.cardcrawl.core.Settings
import com.megacrit.cardcrawl.dungeons.AbstractDungeon
import com.megacrit.cardcrawl.helpers.RelicLibrary

object AttachDatabase {
    private val database: MutableMap<AbstractPlayer.PlayerClass, MutableMap<String, AttachInfo>> = mutableMapOf()

    init {
        Ironclad.initialize()
        Silent.initialize()
        Defect.initialize()
        Watcher.initialize()
    }

    fun test() {
        val character = AbstractDungeon.player?.chosenClass ?: return
        database.getOrDefault(character, mutableMapOf()).keys.forEach { id ->
            RelicLibrary.getRelic(id)?.makeCopy()?.let { relic ->
                AbstractDungeon.getCurrRoom().spawnRelicAndObtain(
                    Settings.WIDTH / 2f, Settings.HEIGHT / 2f,
                    relic
                )
            }
        }
    }

    fun save(character: AbstractPlayer.PlayerClass) {
        println("Saving attach info...")
        val gson = GsonBuilder()
            .setPrettyPrinting()
            .create()
        database[character]?.let {
            val json = gson.toJson(mapOf(character to it))
            val filename = "${character.name.toLowerCase()}.json"
            println(filename)
            Gdx.files.local(filename).writeString(json, false)
            println("Done")
        }
    }

    fun getInfo(character: AbstractPlayer.PlayerClass, relicID: String): AttachInfo? {
        return database[character]?.get(relicID)
    }

    fun relic(character: AbstractPlayer.PlayerClass, relicID: String, info: AttachInfo) {
        database.getOrPut(character) { mutableMapOf() }[relicID] = info.finalize()
    }

    fun character(character: AbstractPlayer.PlayerClass): CharacterState {
        return CharacterState(character)
    }

    data class CharacterState(val character: AbstractPlayer.PlayerClass) {
        fun relic(relicID: String, info: AttachInfo): CharacterState {
            relic(character, relicID, info)
            return this
        }
    }
}
