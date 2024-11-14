package haberdashery

import basemod.BaseMod
import basemod.abstracts.CustomSavable
import basemod.devcommands.ConsoleCommand
import basemod.interfaces.*
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer
import com.megacrit.cardcrawl.dungeons.AbstractDungeon
import com.megacrit.cardcrawl.helpers.ImageMaster
import com.megacrit.cardcrawl.localization.UIStrings
import com.megacrit.cardcrawl.relics.AbstractRelic
import haberdashery.devcommands.HaberdasheryCommand
import haberdashery.extensions.chosenExclusions
import haberdashery.extensions.panel

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
        // TODO
        BaseMod.loadCustomStringsFile(UIStrings::class.java, assetPath("localization/eng/UIStrings.json"))
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
        AdjustRelic.update()
    }

    override fun receivePostRender(sb: SpriteBatch) {
        AdjustRelic.render(sb)
    }
}
