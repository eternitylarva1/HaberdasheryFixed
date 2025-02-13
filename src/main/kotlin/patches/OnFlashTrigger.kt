package com.evacipated.cardcrawl.mod.haberdashery.patches

import com.badlogic.gdx.utils.Array
import com.esotericsoftware.spine.AnimationState
import com.esotericsoftware.spine.AnimationState.AnimationStateListener
import com.evacipated.cardcrawl.mod.haberdashery.Config
import com.evacipated.cardcrawl.mod.haberdashery.database.AttachDatabase
import com.evacipated.cardcrawl.mod.haberdashery.extensions.getPrivate
import com.evacipated.cardcrawl.mod.haberdashery.extensions.subSkeletons
import com.evacipated.cardcrawl.mod.haberdashery.spine.AnimationEventListener
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch
import com.megacrit.cardcrawl.actions.AbstractGameAction
import com.megacrit.cardcrawl.dungeons.AbstractDungeon
import com.megacrit.cardcrawl.relics.AbstractRelic

@SpirePatch2(
    clz = AbstractRelic::class,
    method = "flash"
)
object OnFlashTrigger {
    @JvmStatic
    @SpirePrefixPatch
    fun onFlash(__instance: AbstractRelic) {
        if (!Config.animatedRelics) return
        if (!__instance.isObtained || !__instance.isDone) return
        val player = AbstractDungeon.player ?: return
        val info = AttachDatabase.getInfo(player.chosenClass, __instance.relicId)?.skeletonInfo ?: return
        if (info.onFlash == null) return
        val subSkeleton = player.subSkeletons[__instance.relicId] ?: return

        subSkeleton.skeleton.data.findAnimation(info.onFlash.animation.name)?.let { animation ->
            val e = subSkeleton.anim.addAnimation(lastEmptyTrackIndex(subSkeleton.anim), animation, false, 0f)
            info.onFlash.animation.speed?.let { e.timeScale = it }
            if (info.onFlash.beforeAction != null) {
                val listeners = subSkeleton.anim.getPrivate<Array<AnimationStateListener>>("listeners", clazz = AnimationState::class.java)
                val action = object : AbstractGameAction() {
                    override fun update() {
                        isDone = true
                        val innerAction = object : AbstractGameAction() {
                            init {
                                // Max timeout of 5 seconds
                                duration = 5f
                            }

                            override fun update() {
                                tickDuration()
                            }
                        }

                        listeners.filterIsInstance<AnimationEventListener>()
                            .forEach { it.addOnFlashAction(innerAction) }

                        val actions = AbstractDungeon.actionManager.actions
                        for ((i, action) in actions.withIndex()) {
                            if (action::class.qualifiedName == info.onFlash.beforeAction) {
                                actions.add(i, innerAction)
                                return
                            }
                        }
                        addToBot(innerAction)
                    }
                }
                AbstractDungeon.actionManager.addToBottom(action)
            }
        }
    }

    private fun lastEmptyTrackIndex(anim: AnimationState): Int {
        if (anim.tracks.size == 0) {
            return 0
        }
        for (i in (anim.tracks.size - 1) downTo 0) {
            if (anim.tracks[i] != null) {
                return i+1
            }
        }
        return 0
    }
}
