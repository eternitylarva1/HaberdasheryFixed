package haberdashery.patches

import com.esotericsoftware.spine.AnimationState
import com.esotericsoftware.spine.Skeleton
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch
import com.megacrit.cardcrawl.characters.Watcher
import com.megacrit.cardcrawl.core.Settings
import haberdashery.extensions.inst
import haberdashery.extensions.subSkeletons
import haberdashery.spine.attachments.OffsetSkeletonAttachment
import javassist.CtBehavior
import javassist.CtMethod
import javassist.expr.ExprEditor
import javassist.expr.FieldAccess

object WatcherEyeFix {
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
            val attachment = OffsetSkeletonAttachment("eye_anim").apply {
                skeleton = ___eyeSkeleton
                rotation = 25f
            }
            val eyeSlot = ___skeleton.findSlot("eye_anchor")
            eyeSlot.data.attachmentName = attachment.name
            val skin = ___skeleton.data.defaultSkin
            skin.addAttachment(eyeSlot.data.index, attachment.name, attachment)

            __instance.subSkeletons["eye_anim"] = SubSkeleton(___eyeSkeleton, ___eyeState, {})
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
