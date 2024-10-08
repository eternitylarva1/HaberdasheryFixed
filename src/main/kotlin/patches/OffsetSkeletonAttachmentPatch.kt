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
        locator = Locator::class,
        localvars = [
            "attachment",
            "bone",
            "rootBone",
            "oldRotation"
        ]
    )
    fun offset(skeleton: Skeleton, attachment: Attachment, bone: Bone, rootBone: Bone, oldRotation: Float) {
        if (attachment !is OffsetSkeletonAttachment) return

        var offsetX = attachment.position.x
        var offsetY = attachment.position.y
        if (skeleton.flipX) {
            offsetX *= -1
        }
        if (skeleton.flipY) {
            offsetY *= -1
        }
        attachment.skeleton.setPosition(
            skeleton.x + bone.worldX + offsetX,
            skeleton.y + bone.worldY - offsetY,
        )
        var boneRotation = bone.worldRotationX
        if (skeleton.flipX xor skeleton.flipY) {
            boneRotation = 180f - boneRotation
        }
        rootBone.rotation = oldRotation + boneRotation + attachment.rotation
        rootBone.setScale(attachment.scaleX, attachment.scaleY)
        attachment.skeleton.setFlip(skeleton.flipX, skeleton.flipY)
    }

    private class Locator : SpireInsertLocator() {
        override fun Locate(ctBehavior: CtBehavior): IntArray {
            val finalMatcher = Matcher.MethodCallMatcher(Skeleton::class.java, "updateWorldTransform")
            return LineFinder.findInOrder(ctBehavior, finalMatcher)
        }
    }
}
