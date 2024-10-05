package haberdashery.database

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.esotericsoftware.spine.Skeleton
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.megacrit.cardcrawl.characters.AbstractPlayer
import com.megacrit.cardcrawl.core.AbstractCreature
import com.megacrit.cardcrawl.core.CardCrawlGame
import com.megacrit.cardcrawl.dungeons.AbstractDungeon
import com.megacrit.cardcrawl.helpers.RelicLibrary
import com.megacrit.cardcrawl.relics.AbstractRelic
import haberdashery.HaberdasheryMod
import haberdashery.extensions.asRegion
import haberdashery.extensions.getPrivate
import haberdashery.spine.attachments.MaskedRegionAttachment
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.awt.image.Raster
import java.io.Reader
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import javax.imageio.ImageIO
import kotlin.io.path.createDirectories
import kotlin.streams.asSequence

object AttachDatabase {
    private val logger: Logger = LogManager.getLogger(AttachDatabase::class.java)
    private val internalFS: FileSystem
    private val localFS: FileSystem
    private val database: MutableMap<AbstractPlayer.PlayerClass, MutableMap<String, AttachInfo>> = mutableMapOf()
    private val maskTextureCache = mutableMapOf<String, TextureRegion>()

    init {
        val uri = AttachDatabase::class.java.getResource("/" + HaberdasheryMod.assetPath("attachments")).toURI()
        internalFS = FileSystems.newFileSystem(uri, emptyMap<String, Any?>())
        val path = if (uri.scheme == "jar") {
            internalFS.getPath("/" + HaberdasheryMod.assetPath("attachments"))
        } else {
            Paths.get(uri)
        }
        localFS = FileSystems.getDefault()

        // Load internal json
        Files.walk(path, 1)
            .filter(Files::isRegularFile)
            .forEach {
                // removes the leading /
                val internal = Gdx.files.internal(it.subpath(0, it.nameCount).toString())
                logger.info("Loading ${internal.name()} (INTERNAL)")
                load(internal)
            }

        // Load local json
        Files.walk(localFS.getPath(HaberdasheryMod.ID), 1)
            .filter(Files::isRegularFile)
            .forEach {
                val local = Gdx.files.local(it.toString())
                logger.info("Loading ${local.name()} (LOCAL)")
                load(local)
            }
    }

    fun test() {
        val character = AbstractDungeon.player?.chosenClass ?: return
        database.getOrDefault(character, mutableMapOf()).keys.forEach { id ->
            RelicLibrary.getRelic(id)?.makeCopy()?.let { relic ->
                relic.instantObtain(AbstractDungeon.player, AbstractDungeon.player.relics.size, false)
            }
        }
    }

    fun saveAll() {
        for (player in CardCrawlGame.characterManager.allCharacters) {
            val skeleton = player.getPrivate<Skeleton?>("skeleton", clazz = AbstractCreature::class.java) ?: continue
            save(player.chosenClass, skeleton)
        }
    }

    fun save(character: AbstractPlayer.PlayerClass, skeleton: Skeleton) {
        logger.info("Saving attach info...")
        val gson = GsonBuilder()
            .setPrettyPrinting()
            .create()
        database[character]?.let {
            val json = gson.toJson(mapOf(character to it))
            val filename = "${character.name.lowercase()}.json"
            logger.info(filename)
            Gdx.files.local(Paths.get(HaberdasheryMod.ID, filename).toString()).writeString(json, false)

            logger.info("Saving masks...")
            it.forEach { (relicId, info) ->
                if (info.mask != null && info.maskRequiresSave) {
                    val relicSlotName = HaberdasheryMod.makeID(relicId)
                    val slot = skeleton.findSlot(relicSlotName)
                    (slot.attachment as? MaskedRegionAttachment)?.also { attachment ->
                        val pixmap = attachment.getMask().texture.textureData.consumePixmap()
                        try {
                            val img = BufferedImage(pixmap.width, pixmap.height, BufferedImage.TYPE_BYTE_GRAY)
                            val pixels = pixmap.pixels
                            val bytes = ByteArray(pixels.limit())
                            pixels.position(0)
                            pixels.get(bytes)
                            img.data = Raster.createRaster(img.sampleModel, DataBufferByte(bytes, bytes.size), null)
                            val imgUrl = info.mask!!
                            val filepath = Paths.get(HaberdasheryMod.ID, "masks", imgUrl)
                            filepath.parent.createDirectories()
                            if (ImageIO.write(img, "png", filepath.toFile())) {
                                logger.info("  $relicId: $imgUrl")
                                info.maskSaved()
                                pixels.position(0)
                            } else {
                                logger.warn("  $relicId: Couldn't write png")
                            }
                        } catch (e: Exception) {
                            logger.error("  $relicId: Failed to save mask", e)
                        }
                    }
                } else {
                    // Reset maskChanged and maskRequiresSave
                    info.maskSaved()
                }
            }
        }
    }

    private inline fun <reified T> Gson.fromJson(reader: Reader) =
        fromJson<T>(reader, object : TypeToken<T>() {}.type)

    private fun load(file: FileHandle) {
        val gson = GsonBuilder().create()
        val reader = file.reader()
        val data = gson.fromJson<LinkedHashMap<AbstractPlayer.PlayerClass?, LinkedHashMap<String, AttachInfo>>>(reader)

        data.forEach { (character, relics) ->
            if (character != null) {
                val state = character(character)
                relics.forEach { (relicId, info) ->
                    state.relic(relicId, info)
                }
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

    fun getMaskImg(relic: AbstractRelic, info: AttachInfo): TextureRegion? {
        val filename = info.mask ?: return null

        if (maskTextureCache.contains(relic.relicId)) {
            return maskTextureCache[relic.relicId]
        }

        try {
            val internal = Gdx.files.internal(HaberdasheryMod.assetPath("attachments/masks/${filename}"))
            val local = Gdx.files.local(Paths.get(HaberdasheryMod.ID, "masks", filename).toString())
            val file = newestFile(internal, local)

            logger.info("Loading mask $filename (${if (file == local) "LOCAL" else "INTERNAL"})")

            // Use Texture(Pixmap(FileHandle(String))) instead of Texture(String) because the latter
            // uses FileTextureData, which doesn't let us make changes to the texture/pixmap later
            return Texture(Pixmap(file)).asRegion().also {
                maskTextureCache[relic.relicId] = it
            }
        } catch (e: Exception) {
            logger.warn("Failed to load mask", e)
            return null
        }
    }

    private fun newestFile(internal: FileHandle, local: FileHandle): FileHandle {
        if (!local.exists()) {
            return internal
        }
        if (!internal.exists()) {
            return local
        }

        val internalTime = Files.getLastModifiedTime(internalFS.getPath(internal.path())).toInstant()
        val localTime = Files.getLastModifiedTime(local.file().toPath()).toInstant()

        return if (localTime.isAfter(internalTime)) local else internal
    }

    fun makeRelicMaskFilename(id: String): String {
        // Generate random suffix to reduce possible filename conflicts
        val randChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        val suffix = Random().ints(4, 0, randChars.size)
            .asSequence()
            .map(randChars::get)
            .joinToString("")
        // Remove illegal characters from filename
        return id.replace(Regex("""[<>:"/\\|?*]"""), "_") + "_$suffix.png"
    }
}
