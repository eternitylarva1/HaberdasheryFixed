package haberdashery.patches

import com.badlogic.gdx.Gdx
import com.esotericsoftware.spine.AnimationState
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2
import com.evacipated.cardcrawl.modthespire.lib.SpirePatches2
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch
import com.megacrit.cardcrawl.characters.AbstractPlayer
import com.megacrit.cardcrawl.characters.Watcher
import haberdashery.AdjustRelic
import javassist.expr.ExprEditor
import javassist.expr.MethodCall

@SpirePatches2(
    SpirePatch2(
        clz = AbstractPlayer::class,
        method = "renderPlayerImage"
    ),
    SpirePatch2(
        clz = Watcher::class,
        method = "renderPlayerImage"
    ),
)
object PauseAnimation {
    @JvmStatic
    @SpireInstrumentPatch
    fun pause(): ExprEditor =
        object : ExprEditor() {
            override fun edit(m: MethodCall) {
                if (m.className == AnimationState::class.qualifiedName && m.methodName == "update") {
                    m.replace(
                        "if (!${AdjustRelic::class.qualifiedName}.pauseAnimation()) {" +
                                "\$_ = \$proceed(\$\$);" +
                                "}"
                    )
                }
            }
        }

    @JvmStatic
    @SpirePrefixPatch
    fun animateAttachments(__instance: AbstractPlayer) {
        if (AdjustRelic.pauseAnimation()) return

        __instance.subSkeletons.forEach {
            it.anim.update(Gdx.graphics.deltaTime)
            it.anim.apply(it.skeleton)
            it.skeleton.updateWorldTransform()
            it.skeleton.color = __instance.tint.color
        }
    }
}
