package haberdashery

import basemod.BaseMod
import basemod.abstracts.CustomSavable
import basemod.devcommands.ConsoleCommand
import basemod.interfaces.*
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer
import com.megacrit.cardcrawl.core.Settings
import com.megacrit.cardcrawl.dungeons.AbstractDungeon
import com.megacrit.cardcrawl.helpers.ImageMaster
import com.megacrit.cardcrawl.localization.UIStrings
import com.megacrit.cardcrawl.relics.AbstractRelic
import haberdashery.devcommands.HaberdasheryCommand
import haberdashery.extensions.chosenExclusions
import haberdashery.extensions.panel
import haberdashery.util.AssetLoader
import haberdashery.util.Assets
import java.nio.charset.StandardCharsets
import java.util.*

@SpireInitializer
class HaberdasheryMod :
    PostInitializeSubscriber,
    EditStringsSubscriber,
    AddAudioSubscriber,
    RelicGetSubscriber,
    PreUpdateSubscriber,
    PostRenderSubscriber
{
    companion object Statics {
        val ID: String = "haberdashery"
        val NAME: String = "Haberdashery"

        @Suppress("unused")
        @JvmStatic
        fun initialize() {
            BaseMod.subscribe(HaberdasheryMod())
        }

        fun makeID(id: String) = "$ID:$id"
        fun assetPath(path: String) = "${ID}Assets/$path"
    }

    override fun receivePostInitialize() {
        Assets.preload()

        val settingsPanel = panel {
            loadStrings(makeID("Config"))
            spacing = 5f
            title {
                textID = "title"
            }
            h2 {
                textID = "settings"
            }
            indent {
                fromConfig(Config)
            }
        }

        BaseMod.registerModBadge(
            ImageMaster.loadImage(assetPath("images/modBadge.png")),
            NAME,
            "kiooeht",
            "TODO",
            settingsPanel
        )

        ConsoleCommand.addCommand("haberdashery", HaberdasheryCommand::class.java)

        BaseMod.addSaveField<Any>(makeID("chosenExclusions"), object : CustomSavable<Map<String, String>> {
            override fun onSave(): Map<String, String>? {
                return AbstractDungeon.player?.chosenExclusions
            }

            override fun onLoad(save: Map<String, String>?) {
                if (save != null) {
                    AbstractDungeon.player?.chosenExclusions?.putAll(save)
                    AttachRelic.onChange()
                }
            }
        })
    }

    override fun receiveEditStrings() {
        loadStrings(UIStrings::class.java)
    }

    private fun loadStrings(clazz: Class<*>, lang: Settings.GameLanguage = Settings.language) {
        val path = assetPath("localization/${lang.name.lowercase(locale = Locale.ENGLISH)}/${clazz.simpleName}.json")
        val file = Gdx.files.internal(path)
        if (file.exists()) {
            BaseMod.loadCustomStrings(clazz, file.readString(StandardCharsets.UTF_8?.toString()))
        } else if (lang != Settings.GameLanguage.ENG) {
            loadStrings(clazz, Settings.GameLanguage.ENG)
        } else {
            throw RuntimeException("Failed to load localization: ${clazz.simpleName}")
        }
    }

    override fun receiveAddAudio() {
        BaseMod.addAudio(makeID("SOZU"), assetPath("audio/sozu.ogg"))
    }

    override fun receiveRelicGet(relic: AbstractRelic?) {
        if (relic != null) {
            AttachRelic.receive(relic)
        }
    }

    override fun receivePreUpdate() {
        AssetLoader.update()
        AdjustRelic.update()
    }

    override fun receivePostRender(sb: SpriteBatch) {
        AdjustRelic.render(sb)
    }
}
