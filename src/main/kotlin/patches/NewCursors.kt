package haberdashery.patches

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.evacipated.cardcrawl.modthespire.lib.*
import com.megacrit.cardcrawl.core.CardCrawlGame
import com.megacrit.cardcrawl.core.GameCursor
import com.megacrit.cardcrawl.core.GameCursor.CursorType
import com.megacrit.cardcrawl.helpers.ImageMaster
import com.megacrit.cardcrawl.helpers.input.InputHelper
import com.megacrit.cardcrawl.relics.AbstractRelic
import haberdashery.HaberdasheryMod
import haberdashery.extensions.scale
import javassist.CtBehavior
import javassist.expr.ExprEditor
import javassist.expr.MethodCall

object NewCursors {
    @SpireEnum @JvmStatic lateinit var HAND: CursorType
    @SpireEnum @JvmStatic lateinit var GRAB: CursorType

    @SpirePatch2(
        clz = GameCursor::class,
        method = SpirePatch.CLASS
    )
    object Fields {
        @JvmField val imgHand = SpireField { ImageMaster.loadImage(HaberdasheryMod.assetPath("images/cursorHand.png")) }
        @JvmField val imgGrab = SpireField { ImageMaster.loadImage(HaberdasheryMod.assetPath("images/cursorGrab.png")) }
    }

    @SpirePatch2(
        clz = GameCursor::class,
        method = "render"
    )
    object Render {
        private val SHADOW_OFFSET_X by lazy { (-10).scale() }
        private val SHADOW_OFFSET_Y by lazy { 8.scale() }

        @JvmStatic
        @SpireInsertPatch(
            locator = Locator::class
        )
        fun render(__instance: GameCursor, ___type: CursorType, sb: SpriteBatch, ___SHADOW_COLOR: Color) {
            val img = when (___type) {
                HAND -> Fields.imgHand.get(__instance)
                GRAB -> Fields.imgGrab.get(__instance)
                else -> return
            }
            sb.color = ___SHADOW_COLOR
            sb.draw(
                img,
                InputHelper.mX - 32f - SHADOW_OFFSET_X,
                InputHelper.mY - 32f - SHADOW_OFFSET_Y,
                img.width.scale()*0.8f,
                img.height.scale()*0.8f,
            )
            sb.color = Color.WHITE
            sb.draw(
                img,
                InputHelper.mX - 32f,
                InputHelper.mY - 32f,
                img.width.scale()*0.8f,
                img.height.scale()*0.8f,
            )
        }

        private class Locator : SpireInsertLocator() {
            override fun Locate(ctBehavior: CtBehavior): IntArray {
                val finalMatcher = Matcher.FieldAccessMatcher(GameCursor::class.java, "type")
                return LineFinder.findInOrder(ctBehavior, finalMatcher)
            }
        }
    }

    @SpirePatch2(
        clz = AbstractRelic::class,
        method = "update"
    )
    object Test {
        @JvmStatic
        @SpireInstrumentPatch
        fun instrument() = object : ExprEditor() {
            override fun edit(m: MethodCall) {
                if (m.className == GameCursor::class.qualifiedName && m.methodName == "changeType") {
                    m.replace(
                        "\$1 = ${NewCursors::class.qualifiedName}.HAND;" +
                                "\$_ = \$proceed(\$\$);"
                    )
                }
            }
        }

        @JvmStatic
        @SpirePostfixPatch
        fun postfix() {
            if (DragRelicToAdjustExcludes.grabbedRelic != null) {
                CardCrawlGame.cursor.changeType(GRAB)
            } else if (DragRelicToAdjustExcludes.droppedTimer > 0f) {
                CardCrawlGame.cursor.changeType(HAND)
            }
        }
    }
}
