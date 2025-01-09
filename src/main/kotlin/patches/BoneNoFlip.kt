package haberdashery.patches

import com.esotericsoftware.spine.Bone
import com.esotericsoftware.spine.BoneData
import com.esotericsoftware.spine.Skeleton
import com.evacipated.cardcrawl.modthespire.lib.*
import haberdashery.extensions.inheritFlip

object BoneNoFlip {
    @SpirePatch2(
        clz = BoneData::class,
        method = SpirePatch.CLASS
    )
    object Fields {
        @JvmField
        val inheritFlip: SpireField<Boolean> = SpireField { true }
    }

    @SpirePatch2(
        clz = Bone::class,
        method = "updateWorldTransform",
        paramtypez = [
            Float::class,
            Float::class,
            Float::class,
            Float::class,
            Float::class,
            Float::class,
            Float::class,
        ]
    )
    object Patch {
        @JvmStatic
        @SpirePostfixPatch
        fun maybeUnFlip(__instance: Bone, ___skeleton: Skeleton,
                        @ByRef ___a: FloatArray, @ByRef ___b: FloatArray,
                        @ByRef ___c: FloatArray, @ByRef ___d: FloatArray,
                        @ByRef ___worldX: FloatArray, @ByRef ___worldY: FloatArray
        ) {
            if (!__instance.data.inheritFlip) {
                if (___skeleton.flipX) {
                    ___a[0] = -___a[0]
                    ___b[0] = -___b[0]
                    if (__instance.parent == null) {
                        ___worldX[0] = -___worldX[0]
                    }
                }

                if (___skeleton.flipY) {
                    ___c[0] = -___c[0]
                    ___d[0] = -___d[0]
                    if (__instance.parent == null) {
                        ___worldY[0] = -___worldY[0]
                    }
                }
            }
        }
    }
}
