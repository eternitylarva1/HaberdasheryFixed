package haberdashery.patches

import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.esotericsoftware.spine.Bone
import com.esotericsoftware.spine.Skeleton
import com.esotericsoftware.spine.SkeletonMeshRenderer
import com.esotericsoftware.spine.attachments.Attachment
import com.evacipated.cardcrawl.modthespire.lib.*
import haberdashery.spine.attachments.OffsetSkeletonAttachment
import javassist.CtBehavior

@SpirePatch2(
    clz = SkeletonMeshRenderer::class,
    method = "draw",
    paramtypez = [
        PolygonSpriteBatch::class,
        Skeleton::class,
    ]
)
object OffsetSkeletonAttachmentPatch {
    @JvmStatic
    @SpireInsertPatch(
        locator = LocatorStart::class,
        localvars = [
            "attachment",
            "bone",
            "rootBone",
            "oldRotation"
        ]
    )
    fun offset(skeleton: Skeleton, attachment: Attachment, bone: Bone, rootBone: Bone, oldRotation: Float) {
        if (attachment !is OffsetSkeletonAttachment) return

        attachment.apply(skeleton, bone, oldRotation)
    }

    @JvmStatic
    @SpireInsertPatch(
        locator = LocatorEnd::class,
        localvars = [
            "attachment",
            "bone",
            "rootBone",
            "oldRotation"
        ]
    )
    fun reset(skeleton: Skeleton, attachment: Attachment, bone: Bone, rootBone: Bone, oldRotation: Float) {
        if (attachment !is OffsetSkeletonAttachment) return

        attachment.undoBoneTransforms()
    }

    private class LocatorStart : SpireInsertLocator() {
        override fun Locate(ctBehavior: CtBehavior): IntArray {
            val finalMatcher = Matcher.MethodCallMatcher(Skeleton::class.java, "updateWorldTransform")
            return LineFinder.findInOrder(ctBehavior, finalMatcher)
        }
    }

    private class LocatorEnd : SpireInsertLocator() {
        override fun Locate(ctBehavior: CtBehavior): IntArray {
            val finalMatcher = Matcher.MethodCallMatcher(Bone::class.java, "setScaleX")
            return LineFinder.findInOrder(ctBehavior, finalMatcher)
        }
    }
}
