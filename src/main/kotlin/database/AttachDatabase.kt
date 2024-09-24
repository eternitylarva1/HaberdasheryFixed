package haberdashery.database

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.megacrit.cardcrawl.characters.AbstractPlayer
import com.megacrit.cardcrawl.core.Settings
import com.megacrit.cardcrawl.dungeons.AbstractDungeon
import com.megacrit.cardcrawl.helpers.RelicLibrary
import com.megacrit.cardcrawl.relics.AbstractRelic
import haberdashery.HaberdasheryMod
import haberdashery.extensions.asRegion
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.Reader
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths

object AttachDatabase {
    private val logger: Logger = LogManager.getLogger(AttachDatabase::class.java)
    private val database: MutableMap<AbstractPlayer.PlayerClass, MutableMap<String, AttachInfo>> = mutableMapOf()
    private val maskTextureCache = mutableMapOf<String, TextureRegion>()

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
            .forEach {
                // removes the leading /
                val internal = Gdx.files.internal(it.subpath(0, it.nameCount).toString())
                val local = Gdx.files.local(Paths.get(HaberdasheryMod.ID).resolve(it.fileName.toString()).toString())
                if (local.exists()) {
                    logger.info("Loading ${local.name()} (LOCAL)")
                    load(local)
                } else {
                    logger.info("Loading ${internal.name()} (INTERNAL)")
                    load(internal)
                }
            }
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
        logger.info("Saving attach info...")
        val gson = GsonBuilder()
            .setPrettyPrinting()
            .create()
        database[character]?.let {
            val json = gson.toJson(mapOf(character to it))
            val filename = "${character.name.lowercase()}.json"
            logger.info(filename)
            Gdx.files.local(Paths.get(HaberdasheryMod.ID, filename).toString()).writeString(json, false)
        }
    }

    private inline fun <reified T> Gson.fromJson(reader: Reader) =
        fromJson<T>(reader, object : TypeToken<T>() {}.type)

    private fun load(file: FileHandle) {
        val gson = GsonBuilder().create()
        val reader = file.reader()
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

    fun getMaskImg(relic: AbstractRelic): TextureRegion? {
        if (maskTextureCache.contains(relic.relicId)) {
            return maskTextureCache[relic.relicId]
        }

        try {
            // Use Texture(Pixmap(FileHandle(String))) instead of Texture(String) because the latter
            // uses FileTextureData, which doesn't let us make changes to the texture/pixmap later
            return Texture(Pixmap(Gdx.files.internal(HaberdasheryMod.assetPath("attachments/masks/${relic.imgUrl}")))).asRegion().also {
                maskTextureCache[relic.relicId] = it
            }
        } catch (e: Exception) {
            logger.warn("Failed to load mask: ${e.message}")
            return null
        }
    }
}
