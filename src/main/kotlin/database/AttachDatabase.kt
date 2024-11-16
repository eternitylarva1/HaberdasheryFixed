package haberdashery.database

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.esotericsoftware.spine.Skeleton
import com.evacipated.cardcrawl.modthespire.Loader
import com.evacipated.cardcrawl.modthespire.lib.SpireEnum
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
import haberdashery.database.adapters.AnimationInfoTypeAdapterFactory
import haberdashery.extensions.asRegion
import haberdashery.extensions.getPrivate
import haberdashery.extensions.skeleton
import haberdashery.spine.attachments.MaskedRegionAttachment
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.awt.image.Raster
import java.io.FileNotFoundException
import java.io.Reader
import java.net.URI
import java.nio.file.*
import java.util.*
import javax.imageio.ImageIO
import kotlin.io.path.*
import kotlin.streams.asSequence

object AttachDatabase {
    private val logger: Logger = LogManager.getLogger(AttachDatabase::class.java)
    private val database: MutableMap<AbstractPlayer.PlayerClass, MutableMap<String, AttachInfo>> = mutableMapOf()
    private val maskTextureCache = mutableMapOf<String, TextureRegion>()
    private val paths: LinkedHashSet<Path> = linkedSetOf()

    init {
        load()
    }

    fun test() {
        val character = AbstractDungeon.player?.chosenClass ?: return
        val ids = mutableSetOf<String>()
        ids.addAll(database.getOrDefault(Enums.COMMON, mutableMapOf()).keys)
        ids.addAll(database.getOrDefault(character, mutableMapOf()).keys)
        ids.forEach { id ->
            RelicLibrary.getRelic(id)
                ?.makeCopy()
                ?.instantObtain(AbstractDungeon.player, AbstractDungeon.player.relics.size, false)
        }
    }

    fun load() {
        database.clear()

        // Load local json
        val localFS = FileSystems.getDefault()
        Files.walk(localFS.getPath(HaberdasheryMod.ID), 1)
            .filter(Files::isRegularFile)
            .forEach {
                logger.info("Loading ${it.fileName} (LOCAL)")
                load(it)
            }

        // Load mod jsons
        for (modInfo in Loader.MODINFOS) {
            val uri = modInfo.jarURL?.toURI()?.let { URI.create("jar:$it") } ?: continue
            val fs = try {
                FileSystems.newFileSystem(uri, emptyMap<String, Any?>())
            } catch (e: FileSystemAlreadyExistsException) {
                FileSystems.getFileSystem(uri)
            } catch (e: Exception) {
                logger.error("Failed to make FileSystem: $uri", e)
                continue
            }

            val path = fs.getPath("/${HaberdasheryMod.ID}")
            if (path.notExists()) continue
            Files.walk(path, 1)
                .filter(Files::isRegularFile)
                .filter { it.fileName?.toString()?.substringAfterLast(".", "") == "json" }
                .forEach {
                    logger.info("Loading ${it.fileName} (MOD:${modInfo.ID})")
                    load(it)
                }
        }
    }

    fun saveAll() {
        save(Enums.COMMON, AbstractDungeon.player?.getPrivate("skeleton", clazz = AbstractCreature::class.java))
        for (player in CardCrawlGame.characterManager.allCharacters) {
            val skeleton = player.skeleton ?: continue
            save(player.chosenClass, skeleton)
        }
    }

    fun save(character: AbstractPlayer.PlayerClass, skeleton: Skeleton?) {
        logger.info("Saving attach info...")
        val gson = GsonBuilder()
            .registerTypeAdapterFactory(AnimationInfoTypeAdapterFactory())
            .setPrettyPrinting()
            .create()
        database[character]?.let {
            val json = gson.toJson(mapOf(character to it))
            val filename = if (character == Enums.COMMON) {
                "common.json"
            } else {
                "${character.name.lowercase()}.json"
            }
            logger.info(filename)
            val path = Paths.get(HaberdasheryMod.ID, filename)
            Gdx.files.local(path.toString()).writeString(json, false)

            logger.info("Saving masks...")
            it.forEach { (relicId, info) ->
                info.path = path
                if (skeleton == null) return@forEach
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

    private fun load(path: Path) {
        val gson = GsonBuilder()
            .registerTypeAdapterFactory(AnimationInfoTypeAdapterFactory())
            .create()
        val reader = path.reader()
        val data = gson.fromJson<LinkedHashMap<AbstractPlayer.PlayerClass?, LinkedHashMap<String, AttachInfo>>>(reader)
        reader.close()

        paths.add(path.parent)

        data.forEach { (character, relics) ->
            if (character != null) {
                val state = character(character)
                relics.forEach { (relicId, info) ->
                    info.path = path
                    state.relic(relicId, info)
                }
            }
        }
    }

    fun getInfo(character: AbstractPlayer.PlayerClass, relicID: String): AttachInfo? {
        return database[character]?.get(relicID)
            ?: database[Enums.COMMON]?.get(relicID)
    }

    fun relic(character: AbstractPlayer.PlayerClass, relicID: String, info: AttachInfo) {
        val map = database.getOrPut(character) { mutableMapOf() }
        map.putIfAbsent(relicID, info.finalize())
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

    fun getPaths(info: AttachInfo?): Iterable<Path> {
        return info?.path?.let { listOf(it.parent) + paths } ?: paths
    }

    fun getMaskImg(relic: AbstractRelic, info: AttachInfo): TextureRegion? {
        val filename = info.mask ?: return null

        if (maskTextureCache.contains(relic.relicId)) {
            return maskTextureCache[relic.relicId]
        }

        try {
            for (path in getPaths(info)) {
                val internal = path.fileSystem.getPath(HaberdasheryMod.ID, "masks", filename)
                val local = Paths.get(HaberdasheryMod.ID, "masks", filename)
                val file = newestFile(internal, local)
                if (!file.exists()) continue

                logger.info("Loading mask $filename (${if (file == local) "LOCAL" else "INTERNAL"})")

                val bytes = file.readBytes()
                val pix2d = Gdx2DPixmap(bytes, 0, bytes.size, 0)
                // Use Texture(Pixmap(...)) instead of Texture(String) because the latter
                // uses FileTextureData, which doesn't let us make changes to the texture/pixmap later
                return Texture(Pixmap(pix2d)).asRegion().also {
                    maskTextureCache[relic.relicId] = it
                }
            }
            throw FileNotFoundException()
        } catch (e: Exception) {
            logger.warn("Failed to load mask", e)
            return null
        }
    }

    private fun newestFile(internal: Path, local: Path): Path {
        if (local.notExists()) {
            return internal
        }
        if (internal.notExists()) {
            return local
        }

        val internalTime = internal.getLastModifiedTime().toInstant()
        val localTime = local.getLastModifiedTime().toInstant()

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

    internal object Enums {
        @JvmStatic
        @SpireEnum(name = "HABERDASHERY_COMMON")
        lateinit var COMMON: AbstractPlayer.PlayerClass
    }
}
