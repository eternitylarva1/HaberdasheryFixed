package haberdashery.patches

import com.badlogic.gdx.graphics.Color
import com.esotericsoftware.spine.SkeletonRendererDebug
import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2
import haberdashery.extensions.inst
import javassist.expr.ExprEditor
import javassist.expr.FieldAccess

@SpirePatch2(
    clz = SkeletonRendererDebug::class,
    method = "draw"
)
object SkeletonDebugColors {
    @JvmStatic
    @SpireInstrumentPatch
    fun instrument() = object : ExprEditor() {
        private var foundMeshHull = false
        override fun edit(f: FieldAccess) {
            if (f.isReader && f.className == SkeletonRendererDebug::class.qualifiedName) {
                if (f.fieldName == "drawMeshHull") {
                    foundMeshHull = true
                } else if (foundMeshHull && f.fieldName == "attachmentLineColor") {
                    f.replace("\$_ = ${Color::SKY.inst};")
                }
            }
        }
    }
}
