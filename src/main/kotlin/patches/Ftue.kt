package com.evacipated.cardcrawl.mod.haberdashery.patches

import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.evacipated.cardcrawl.mod.haberdashery.Config
import com.evacipated.cardcrawl.mod.haberdashery.HaberdasheryMod
import com.evacipated.cardcrawl.mod.haberdashery.util.L10nStrings
import com.evacipated.cardcrawl.modthespire.lib.*
import com.megacrit.cardcrawl.core.Settings
import com.megacrit.cardcrawl.dungeons.AbstractDungeon
import com.megacrit.cardcrawl.ui.FtueTip
import com.megacrit.cardcrawl.ui.FtueTip.TipType
import javassist.CtBehavior
import kotlin.reflect.KMutableProperty

object Ftue {
    @JvmStatic
    @SpireEnum(name = "HABERDASHERY_EXCLUSION")
    lateinit var EXCLUSION: TipType

    @JvmField var fixDoubleAnimation = false

    private val strings by lazy { L10nStrings(HaberdasheryMod.makeID("Ftue")) }
    private val keyMap by lazy { mapOf(
        EXCLUSION to "exclusion"
    ) }

    fun canOpen(type: TipType): Boolean {
        val prop = getProperty(type)
        return !prop.getter.call()
    }

    fun open(
        type: TipType, vararg args: Any?,
        x: Float = 0.5f, y: Float = 0.5f,
        posX: Float = Settings.WIDTH * x, posY: Float = Settings.HEIGHT * y
    ) {
        if (AbstractDungeon.currMapNode?.getRoom() == null) return

        val header = strings["${keyMap[type]}_header"]
        val body = strings["${keyMap[type]}_body"]

        AbstractDungeon.ftue = FtueTip(
            header,
            body.format(*args),
            posX,
            posY,
            type,
        )

        // Stop this Ftue from showing again
        getProperty(type).setter.call(true)
    }

    private fun getProperty(type: TipType): KMutableProperty<Boolean> {
        return when (type) {
            EXCLUSION -> Config::ftueExclusionDragDrop
            else -> throw IllegalArgumentException()
        }
    }

    @SpirePatch2(
        clz = FtueTip::class,
        method = "render"
    )
    object Render {
        @JvmStatic
        @SpireInsertPatch(
            locator = Locator::class
        )
        fun insert(__instance: FtueTip, sb: SpriteBatch) {
            if (__instance.type == EXCLUSION) {
                fixDoubleAnimation = true
                AbstractDungeon.player?.render(sb)
                fixDoubleAnimation = false
            }
        }

        private class Locator : SpireInsertLocator() {
            override fun Locate(ctBehavior: CtBehavior): IntArray {
                val finalMatcher = Matcher.FieldAccessMatcher(Settings::class.java, "isControllerMode")
                return LineFinder.findInOrder(ctBehavior, finalMatcher)
            }
        }
    }
}
