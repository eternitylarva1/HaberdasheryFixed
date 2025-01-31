package haberdashery.patches

import basemod.ModBadge
import basemod.ModPanel
import com.evacipated.cardcrawl.modthespire.lib.ByRef
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2
import com.evacipated.cardcrawl.modthespire.lib.SpirePrefixPatch
import haberdashery.extensions.scale

@SpirePatch2(
    clz = ModBadge::class,
    method = "receiveRender"
)
object ModBadgePatch {
    internal lateinit var panel: ModPanel

    @JvmStatic
    @SpirePrefixPatch
    fun prefix(___modPanel: ModPanel, @ByRef ___x: FloatArray, @ByRef ___y: FloatArray) {
        if (___modPanel == panel) {
            ___x[0] += 34.5f.scale()
            ___y[0] += 9.scale()
        }
    }
}
