package com.evacipated.cardcrawl.mod.haberdashery.patches

import com.badlogic.gdx.math.MathUtils
import com.esotericsoftware.spine.Skeleton
import com.evacipated.cardcrawl.mod.haberdashery.Config
import com.evacipated.cardcrawl.mod.haberdashery.HaberdasheryMod
import com.evacipated.cardcrawl.mod.haberdashery.database.AttachDatabase
import com.evacipated.cardcrawl.mod.haberdashery.database.AttachInfo
import com.evacipated.cardcrawl.mod.haberdashery.database.MySlotData
import com.evacipated.cardcrawl.mod.haberdashery.extensions.subSkeletons
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch
import com.megacrit.cardcrawl.characters.Watcher

@SpirePatch2(
    clz = Watcher::class,
    method = "onStanceChange"
)
object OnStanceChangeTrigger {
    @JvmStatic
    @SpirePostfixPatch
    fun onStanceChange(__instance: Watcher, newStance: String, ___skeleton: Skeleton) {
        if (!Config.animatedRelics) return

        ___skeleton.slots
            .filter { it.data is MySlotData }
            .forEach { slot ->
                val relicId = slot.data.name.removePrefix(HaberdasheryMod.makeID(""))
                val info = AttachDatabase.getInfo(__instance.chosenClass, relicId)
                if (info?.skeletonInfo?.onStance != null) {
                    val subSkeleton = __instance.subSkeletons[relicId] ?: return@forEach
                    val animationInfo = info.skeletonInfo.onStance[newStance]

                    if (animationInfo != null) {
                        subSkeleton.skeleton.setToSetupPose()
                        val e = subSkeleton.anim.setAnimation(0, animationInfo.name, true)
                        animationInfo.speed?.let { e.timeScale = it }
                        if (animationInfo.startTime == AttachInfo.StartType.RANDOM) {
                            e.time = e.endTime * MathUtils.random()
                        }
                    } else if (info.skeletonInfo.animations.isNotEmpty()) {
                        subSkeleton.skeleton.setToSetupPose()
                        val e = subSkeleton.anim.setAnimation(0, info.skeletonInfo.animations[0].name, true)
                        info.skeletonInfo.animations[0].speed?.let { e.timeScale = it }
                        if (info.skeletonInfo.animations[0].startTime == AttachInfo.StartType.RANDOM) {
                            e.time = e.endTime * MathUtils.random()
                        }
                    } else {
                        subSkeleton.anim.clearTracks()
                        subSkeleton.skeleton.setToSetupPose()
                    }
                }
            }
    }
}
