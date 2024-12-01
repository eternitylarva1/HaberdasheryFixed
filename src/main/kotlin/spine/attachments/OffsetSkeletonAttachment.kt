package haberdashery.spine.attachments

import com.badlogic.gdx.math.Vector2
import com.esotericsoftware.spine.Bone
import com.esotericsoftware.spine.Skeleton
import com.esotericsoftware.spine.attachments.SkeletonAttachment
import haberdashery.database.AttachInfo

class OffsetSkeletonAttachment(name: String) : SkeletonAttachment(name) {
    var position: Vector2 = Vector2()
    var rotation: Float = 0f
    var scaleX: Float = 1f
    var scaleY: Float = 1f

    private var oldRotation: Float = 0f
    private var oldScaleX: Float = 1f
    private var oldScaleY: Float = 1f

    var boneTransforms: List<AttachInfo.BoneTransform>? = null

    fun apply(parentSkeleton: Skeleton, parentBone: Bone, oldRotation: Float = skeleton.rootBone.rotation) {
        this.oldRotation = oldRotation
        oldScaleX = skeleton.rootBone.scaleX
        oldScaleY = skeleton.rootBone.scaleY

        val offset = parentBone.localToWorld(position.cpy())
        skeleton.setPosition(
            parentSkeleton.x + offset.x,
            parentSkeleton.y + offset.y,
        )
        var boneRotation = parentBone.worldRotationX
        if (parentSkeleton.flipX xor parentSkeleton.flipY) {
            boneRotation = 180f - boneRotation
        }
        val rootBone = skeleton.rootBone
        rootBone.rotation = oldRotation + boneRotation + rotation
        rootBone.setScale(scaleX, scaleY)
        skeleton.setFlip(parentSkeleton.flipX, parentSkeleton.flipY)

        boneTransforms?.forEach { transform ->
            val bone = skeleton.findBone(transform.name) ?: return@forEach
            transform.rotation?.let { bone.rotation += it }
            transform.scaleX?.let { bone.scaleX *= it }
            transform.scaleY?.let { bone.scaleY *= it }
        }
    }

    fun reset() {
        undoBoneTransforms()

        skeleton.setPosition(0f, 0f)
        skeleton.rootBone.setScale(oldScaleX, oldScaleY)
        skeleton.rootBone.rotation = oldRotation
    }

    fun undoBoneTransforms() {
        boneTransforms?.forEach { transform ->
            val bone = skeleton.findBone(transform.name) ?: return@forEach
            transform.rotation?.let { bone.rotation -= it }
            transform.scaleX?.let { bone.scaleX /= it }
            transform.scaleY?.let { bone.scaleY /= it }
        }
    }
}
