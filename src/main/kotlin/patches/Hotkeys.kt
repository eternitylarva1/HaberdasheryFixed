package com.evacipated.cardcrawl.mod.haberdashery.patches

import com.badlogic.gdx.Input
import com.evacipated.cardcrawl.mod.haberdashery.HaberdasheryMod
import com.evacipated.cardcrawl.mod.haberdashery.util.L10nStrings
import com.evacipated.cardcrawl.modthespire.lib.*
import com.megacrit.cardcrawl.core.Settings
import com.megacrit.cardcrawl.helpers.input.InputAction
import com.megacrit.cardcrawl.helpers.input.InputActionSet
import com.megacrit.cardcrawl.screens.options.InputSettingsScreen
import com.megacrit.cardcrawl.screens.options.RemapInputElement
import javassist.CtBehavior

object Hotkeys {
    object ActionSet {
        private val CUSTOMIZE_KEY = HaberdasheryMod.makeID("CUSTOMIZE")

        internal lateinit var customize: InputAction

        fun load() {
            customize = InputAction(InputActionSet.prefs.getInteger(CUSTOMIZE_KEY, Input.Keys.H))
        }

        fun save() {
            InputActionSet.prefs.putInteger(CUSTOMIZE_KEY, customize.key)
        }

        fun resetToDefault() {
            customize.remap(Input.Keys.H)
        }
    }

    @SpirePatch2(
        clz = InputSettingsScreen::class,
        method = "refreshData"
    )
    object SettingsScreen {
        @JvmStatic
        @SpireInsertPatch(
            locator = Locator::class,
            localvars = ["elements"]
        )
        fun insert(__instance: InputSettingsScreen, elements: ArrayList<RemapInputElement>) {
            if (!Settings.isControllerMode) {
                val strings = L10nStrings(HaberdasheryMod.makeID("InputActionSet"))
                elements.add(RemapInputElement(__instance, strings["customize"], ActionSet.customize))
            }
        }

        private class Locator : SpireInsertLocator() {
            override fun Locate(ctBehavior: CtBehavior): IntArray {
                val finalMatcher = Matcher.FieldAccessMatcher(InputSettingsScreen::class.java, "maxScrollAmount")
                return LineFinder.findInOrder(ctBehavior, finalMatcher)
            }
        }
    }

    private object ActionSetPatches {
        @SpirePatch2(
            clz = InputActionSet::class,
            method = "load"
        )
        object Load {
            @JvmStatic
            @SpirePrefixPatch
            fun prefix() {
                ActionSet.load()
            }
        }

        @SpirePatch2(
            clz = InputActionSet::class,
            method = "save"
        )
        object Save {
            @JvmStatic
            @SpirePrefixPatch
            fun prefix() {
                ActionSet.save()
            }
        }

        @SpirePatch2(
            clz = InputActionSet::class,
            method = "resetToDefaults"
        )
        object Reset {
            @JvmStatic
            @SpirePrefixPatch
            fun prefix() {
                ActionSet.resetToDefault()
            }
        }
    }
}
