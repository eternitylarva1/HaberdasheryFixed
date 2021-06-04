package haberdashery.database

import com.badlogic.gdx.Gdx
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.megacrit.cardcrawl.characters.AbstractPlayer
import com.megacrit.cardcrawl.core.Settings
import com.megacrit.cardcrawl.dungeons.AbstractDungeon
import com.megacrit.cardcrawl.helpers.RelicLibrary
import haberdashery.HaberdasheryMod
import java.io.Reader
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

object AttachDatabase {
    private val database: MutableMap<AbstractPlayer.PlayerClass, MutableMap<String, AttachInfo>> = mutableMapOf()

    init {
        val uri = AttachDatabase::class.java.getResource("/" + HaberdasheryMod.assetPath("attachments")).toURI()
        val path = if (uri.scheme == "jar") {
            val fs = FileSystems.newFileSystem(uri, emptyMap<String, Any?>())
            fs.getPath("/" + HaberdasheryMod.assetPath("attachments"))
        } else {
            Paths.get(uri)
        }
        Files.walk(path, 1)
            .filter { Files.isRegularFile(it) }
            .forEach { load(it) }

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

    private inline fun <reified T> Gson.fromJson(reader: Reader) =
        fromJson<T>(reader, object : TypeToken<T>() {}.type)

    private fun load(path: Path) {
        val gson = GsonBuilder().create()
        val reader = Gdx.files.internal(path.subpath(0, path.nameCount).toString()).reader()
        val data = gson.fromJson<LinkedHashMap<AbstractPlayer.PlayerClass, LinkedHashMap<String, AttachInfo>>>(reader)

        data.forEach { (character, relics) ->
            val state = character(character)
            relics.forEach { (relicId, info) ->
                state.relic(relicId, info)
            }
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
