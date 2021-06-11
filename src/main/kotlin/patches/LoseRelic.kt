package haberdashery.patches

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2
import com.megacrit.cardcrawl.characters.AbstractPlayer
import haberdashery.AttachRelic

@SpirePatch2(
    clz = AbstractPlayer::class,
    method = "loseRelic"
)
object LoseRelic {
    @JvmStatic
    fun Postfix(__result: Boolean, targetID: String) {
        if (__result) {
            AttachRelic.lose(targetID)
        }
    }
}
