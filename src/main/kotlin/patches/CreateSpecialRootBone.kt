package haberdashery.patches

import com.esotericsoftware.spine.Bone
import com.esotericsoftware.spine.BoneData
import com.esotericsoftware.spine.Skeleton
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch
import com.megacrit.cardcrawl.core.AbstractCreature
import haberdashery.extensions.inheritFlip

@SpirePatch2(
    clz = AbstractCreature::class,
    method = "loadAnimation"
)
object CreateSpecialRootBone {
    const val SPECIAL_ROOT_BONE_NAME = "haberdashery_root"

    @JvmStatic
    @SpirePostfixPatch
    fun test(___skeleton: Skeleton) {
        val boneData = BoneData(
            ___skeleton.data.bones.size,
            SPECIAL_ROOT_BONE_NAME,
            null
        ).apply {
            length = 30f
            setScale(1f, 1f)
            rotation = 0f
            setPosition(0f, 0f)
            inheritScale = false
            inheritRotation = false
            inheritFlip = false
        }
        ___skeleton.data.bones.add(boneData)

        val bone = Bone(boneData, ___skeleton, null)
        ___skeleton.bones.add(bone)

        ___skeleton.updateCache()
    }
}
