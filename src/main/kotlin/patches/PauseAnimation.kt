package haberdashery.patches

import com.esotericsoftware.spine.AnimationState
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2
import com.megacrit.cardcrawl.characters.AbstractPlayer
import haberdashery.AdjustRelic
import javassist.expr.ExprEditor
import javassist.expr.MethodCall

@SpirePatch2(
    clz = AbstractPlayer::class,
    method = "renderPlayerImage"
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
}
