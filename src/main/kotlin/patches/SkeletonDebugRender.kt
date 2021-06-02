package haberdashery.patches

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch
import com.esotericsoftware.spine.Skeleton
import com.esotericsoftware.spine.SkeletonRendererDebug
import com.evacipated.cardcrawl.modthespire.lib.*
import com.megacrit.cardcrawl.characters.AbstractPlayer
import com.megacrit.cardcrawl.core.AbstractCreature
import com.megacrit.cardcrawl.core.CardCrawlGame
import com.megacrit.cardcrawl.core.Settings
import haberdashery.AdjustRelic
import haberdashery.extensions.getPrivate
import javassist.CtBehavior

@SpirePatch2(
    clz = AbstractPlayer::class,
    method = "renderPlayerImage"
)
object SkeletonDebugRender {
    private val srd = SkeletonRendererDebug().apply {
        setPremultipliedAlpha(true)
        setBoundingBoxes(false)
        setMeshHull(false)
        setMeshTriangles(false)
        setRegionAttachments(false)
        setScale(Settings.scale)
    }

    @JvmStatic
    @SpireInsertPatch(
        locator = Locator::class
    )
    fun Insert(__instance: AbstractPlayer) {
        if (AdjustRelic.active) {
            val projection = Gdx.app.applicationListener.getPrivate<OrthographicCamera>("camera", clazz = CardCrawlGame::class.java).combined
            srd.shapeRenderer.projectionMatrix = projection
            __instance.getPrivate<Skeleton?>("skeleton", clazz = AbstractCreature::class.java)?.let {
                srd.draw(it)
            }
        }
    }

    private class Locator : SpireInsertLocator() {
        override fun Locate(ctBehavior: CtBehavior): IntArray {
            val finalMatcher = Matcher.MethodCallMatcher(PolygonSpriteBatch::class.java, "end")
            return LineFinder.findInOrder(ctBehavior, finalMatcher)
        }
    }
}
