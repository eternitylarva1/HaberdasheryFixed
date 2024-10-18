package haberdashery.patches

import com.evacipated.cardcrawl.modthespire.lib.SpireInstrumentPatch
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2
import com.megacrit.cardcrawl.characters.AbstractPlayer
import haberdashery.AttachRelic
import haberdashery.utils.ObservableArrayList
import javassist.expr.ExprEditor
import javassist.expr.FieldAccess

@SpirePatch2(
    clz = AbstractPlayer::class,
    method = SpirePatch.CONSTRUCTOR
)
object ObservableRelicsList {
    @JvmStatic
    @SpireInstrumentPatch
    fun replaceRelicsList() = object : ExprEditor() {
        override fun edit(f: FieldAccess) {
            if (f.isWriter && f.className == AbstractPlayer::class.qualifiedName && f.fieldName == "relics") {
                f.replace(
                    "${ObservableArrayList::class.qualifiedName} list = new ${ObservableArrayList::class.qualifiedName}();" +
                            "list.addListener(${AttachRelic::class.qualifiedName}.INSTANCE);" +
                            "\$proceed(list);"
                )
            }
        }
    }
}
