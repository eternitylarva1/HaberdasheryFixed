package com.evacipated.cardcrawl.mod.haberdashery.patches

import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.JsonValue
import com.esotericsoftware.spine.*
import com.evacipated.cardcrawl.modthespire.lib.*
import javassist.CtBehavior

object ApplyPathConstraintsBeforeIKConstraints {
    @SpirePatch2(
        clz = Skeleton::class,
        method = "updateCache"
    )
    object AlterConstraintOrder {
        @JvmStatic
        @SpirePostfixPatch
        fun before(__instance: Skeleton, ___updateCache: Array<Updatable>) {
            if (!SkeletonDataFields.pathBeforeIk[__instance.data]) return

            val pathIdx = ___updateCache.indexOfFirst { it is PathConstraint }
            val ikIdx = ___updateCache.indexOfFirst { it is IkConstraint }
            if (pathIdx >= 0 && ikIdx >= 0) {
                val tmp = mutableListOf<Updatable>()
                for (i in ___updateCache.size - 1 downTo pathIdx) {
                    tmp.add(___updateCache.removeIndex(i))
                }
                for (upd in tmp) {
                    ___updateCache.insert(ikIdx, upd)
                }
            }
        }
    }

    @SpirePatch2(
        clz = SkeletonJson::class,
        method = "readSkeletonData"
    )
    private object ReadSkeletonData {
        @JvmStatic
        @SpireInsertPatch(
            locator = Locator::class,
            localvars = [
                "skeletonData",
                "skeletonMap"
            ]
        )
        private fun readCustomData(skeletonData: SkeletonData, skeletonMap: JsonValue) {
            SkeletonDataFields.pathBeforeIk[skeletonData] = skeletonMap.getBoolean("pathBeforeIk", false)
        }

        private class Locator : SpireInsertLocator() {
            override fun Locate(ctBehavior: CtBehavior): IntArray {
                val finalMatcher = Matcher.FieldAccessMatcher(SkeletonData::class.java, "hash")
                return LineFinder.findInOrder(ctBehavior, finalMatcher)
            }
        }
    }

    @SpirePatch2(
        clz = SkeletonData::class,
        method = SpirePatch.CLASS
    )
    private object SkeletonDataFields {
        @JvmField
        val pathBeforeIk: SpireField<Boolean> = SpireField { false }
    }
}
