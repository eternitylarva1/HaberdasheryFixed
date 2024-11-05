package haberdashery.patches

import com.badlogic.gdx.utils.Disposable
import com.esotericsoftware.spine.AnimationState
import com.esotericsoftware.spine.Skeleton
import com.evacipated.cardcrawl.modthespire.lib.SpireField
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2
import com.megacrit.cardcrawl.characters.AbstractPlayer

@SpirePatch2(
    clz = AbstractPlayer::class,
    method = SpirePatch.CLASS,
)
object PlayerFields {
    @JvmField
    val subSkeletons: SpireField<MutableMap<String, SubSkeleton>> = SpireField { mutableMapOf() }
    @JvmField
    val chosenExclusions: SpireField<MutableMap<String, String>> = SpireField { mutableMapOf() }
}

data class SubSkeleton(
    val skeleton: Skeleton,
    val anim: AnimationState,
    val toDispose: Disposable,
)
