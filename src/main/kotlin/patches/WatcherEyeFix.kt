package com.evacipated.cardcrawl.mod.haberdashery.patches

import com.esotericsoftware.spine.AnimationState
import com.esotericsoftware.spine.Skeleton
import com.evacipated.cardcrawl.mod.haberdashery.extensions.inst
import com.evacipated.cardcrawl.mod.haberdashery.extensions.subSkeletons
import com.evacipated.cardcrawl.mod.haberdashery.spine.attachments.OffsetSkeletonAttachment
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch
import com.megacrit.cardcrawl.characters.Watcher
import com.megacrit.cardcrawl.core.Settings
import javassist.CtBehavior
import javassist.CtMethod
import javassist.expr.ExprEditor
import javassist.expr.FieldAccess

object WatcherEyeFix {
    internal const val WATCHER_EYE_SKEL_KEY = "eye_anim"

    @SpirePatch2(
        clz = Watcher::class,
        method = "loadEyeAnimation"
    )
    object AttachEye {
        @JvmStatic
        @SpirePostfixPatch
        fun attach(
            __instance: Watcher,
            ___skeleton: Skeleton,
            ___eyeSkeleton: Skeleton,
            ___eyeState: AnimationState
        ) {
            val attachment = OffsetSkeletonAttachment(WATCHER_EYE_SKEL_KEY).apply {
                skeleton = ___eyeSkeleton
                rotation = 25f
            }
            val eyeSlot = ___skeleton.findSlot("eye_anchor")
            eyeSlot.data.attachmentName = attachment.name
            val skin = ___skeleton.data.defaultSkin
            skin.addAttachment(eyeSlot.data.index, attachment.name, attachment)

            __instance.subSkeletons[WATCHER_EYE_SKEL_KEY] = SubSkeleton(___eyeSkeleton, ___eyeState, {})
        }

        @JvmStatic
        @SpireInstrumentPatch
        fun fixScale() = object : ExprEditor() {
            override fun edit(f: FieldAccess) {
                if (f.isReader && f.className == Settings::class.qualifiedName && f.fieldName == "scale") {
                    f.replace("\$_ = ${Settings::renderScale.inst};")
                }
            }
        }
    }

    @SpirePatch2(
        clz = Watcher::class,
        method = "renderPlayerImage"
    )
    object RemoveEyeRender {
        @JvmStatic
        @SpireRawPatch
        fun remove(ctBehavior: CtBehavior) {
            ctBehavior.declaringClass.removeMethod(ctBehavior as CtMethod)
        }
    }
}
