package com.evacipated.cardcrawl.mod.haberdashery.patches

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2
import com.esotericsoftware.spine.Skeleton
import com.esotericsoftware.spine.attachments.RegionAttachment
import com.evacipated.cardcrawl.mod.haberdashery.HaberdasheryMod
import com.evacipated.cardcrawl.mod.haberdashery.database.MySlotData
import com.evacipated.cardcrawl.mod.haberdashery.spine.attachments.OffsetSkeletonAttachment
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2
import com.evacipated.cardcrawl.modthespire.lib.SpirePatches2
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch
import com.megacrit.cardcrawl.characters.AbstractPlayer
import com.megacrit.cardcrawl.characters.Watcher
import kotlin.reflect.KFunction
import kotlin.reflect.full.declaredMemberFunctions

@SpirePatches2(
    SpirePatch2(
        clz = AbstractPlayer::class,
        method = "renderPlayerImage",
        requiredModId = "LightsOut"
    ),
    SpirePatch2(
        clz = Watcher::class,
        method = "renderPlayerImage",
        requiredModId = "LightsOut"
    ),
)
object LightsOutSupport {
    @JvmStatic
    @SpirePostfixPatch
    fun addLights(__instance: AbstractPlayer, ___skeleton: Skeleton) {
        for (relic in __instance.relics) {
            val slot = ___skeleton.findSlot(HaberdasheryMod.makeID(relic.relicId)) ?: continue
            val data = slot.data as? MySlotData ?: continue
            if (!data.visible) continue
            val attachment = slot.attachment ?: continue

            try {
                if (!methodsCache.contains(relic::class.java)) {
                    methodsCache[relic::class.java] = Methods(
                        relic::class.declaredMemberFunctions.firstOrNull { it.name == "_lightsOutGetColor" } as KFunction<Array<Color>>?,
                        relic::class.declaredMemberFunctions.firstOrNull { it.name == "_lightsOutGetXYRI" } as KFunction<FloatArray>?,
                    )
                }
                val methods = methodsCache[relic::class.java]
                if (methods?.color == null || methods.xyri == null) continue
                val color: Array<Color> = methods.color.call(relic)
                val xyri: FloatArray = methods.xyri.call(relic)
                val pos = when (attachment) {
                    is RegionAttachment ->
                        Vector2(attachment.x, attachment.y)
                            .rotate(slot.bone.worldRotationX)
                            .add(___skeleton.x, ___skeleton.y)
                            .add(slot.bone.worldX, slot.bone.worldY)
                    is OffsetSkeletonAttachment -> {
                        val bone = attachment.skeleton.findBone("relic") ?: continue
                        val offset = slot.bone.localToWorld(attachment.position.cpy())
                        Vector2(bone.worldX, bone.worldY)
                            .add(___skeleton.x, ___skeleton.y)
                            .add(offset)
                    }
                    else -> continue
                }

                val lightData = classLightData.kotlin.constructors
                    .first { it.parameters.size == 5 }
                    .call(
                        pos.x,
                        pos.y,
                        xyri[2] * 0.25f,
                        xyri[3] * 1f,
                        color[0]
                    )

                lightsToRender.add(lightData)
            } catch (ignore: Exception) {
                continue
            }
        }
    }

    private val classLightData by lazy { Class.forName("LightsOut.util.LightData") }
    private val classShaderLogic by lazy { Class.forName("LightsOut.util.ShaderLogic") }
    private val fieldLightsToRender by lazy { classShaderLogic.getDeclaredField("lightsToRender") }
    private val lightsToRender: java.util.ArrayList<Any?>
        get() = fieldLightsToRender.get(null) as java.util.ArrayList<Any?>
    private val methodsCache = mutableMapOf<Class<*>, Methods>()

    private data class Methods(
        val color: KFunction<Array<Color>>?,
        val xyri: KFunction<FloatArray>?,
    )
}
