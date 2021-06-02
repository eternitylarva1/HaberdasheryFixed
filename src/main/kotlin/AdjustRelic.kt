package haberdashery

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.esotericsoftware.spine.Skeleton
import com.esotericsoftware.spine.attachments.RegionAttachment
import com.megacrit.cardcrawl.core.AbstractCreature
import com.megacrit.cardcrawl.core.CardCrawlGame
import com.megacrit.cardcrawl.core.Settings
import com.megacrit.cardcrawl.dungeons.AbstractDungeon
import com.megacrit.cardcrawl.helpers.FontHelper
import haberdashery.database.AttachDatabase
import haberdashery.database.AttachInfo
import haberdashery.extensions.getPrivate
import haberdashery.extensions.scale
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.roundToInt

object AdjustRelic {
    private val debugRenderer = ShapeRenderer()

    private var relicId: String? = null
        set(value) {
            field = value
            if (value != null) {
                info = AttachDatabase.getInfo(AbstractDungeon.player.chosenClass, value)
            }
        }
    private var info: AttachInfo? = null

    private var rotating: Vector2? = null

    fun setRelic(relicId: String) {
        val player = AbstractDungeon.player ?: return

        val relicSlotName = "${HaberdasheryMod.ID}:${relicId}"
        val skeleton = player.getPrivate<Skeleton?>("skeleton", clazz = AbstractCreature::class.java) ?: return
        if (skeleton.findSlotIndex(relicSlotName) < 0) {
            return
        }

        this.relicId = relicId
    }

    fun update() {
        val relicId = relicId
        val info = info
        if (relicId == null || info == null) {
            return
        }

        // Rotation
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            rotating = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
                .sub(Settings.WIDTH / 2f, Settings.HEIGHT / 2f)
                .nor()
                .scl(200.scale())
        } else if (!Gdx.input.isKeyPressed(Input.Keys.R)) {
            rotating = null
            info.finalize()
        }
    }

    fun render(sb: SpriteBatch) {
        val relicId = relicId
        val info = info
        if (relicId == null || info == null) {
            return
        }

        rotationWidget(sb, info)

        FontHelper.renderFontCenteredHeight(
            sb,
            FontHelper.tipBodyFont,
            "[$relicId]\n" +
                    "Bone: ${info.boneName}\n" +
                    "Position: ${info.positionData.x}°, ${info.positionData.y}\n" +
                    "Rotation: ${info.dirtyRotation}\n" +
                    "Scale: ${info.scaleX}, ${info.scaleY}\n",
            30f, Settings.HEIGHT / 2f,
            Color.WHITE
        )
    }

    private fun rotationWidget(sb: SpriteBatch, info: AttachInfo) {
        val startRotation = rotating
        if (startRotation != null) {
            sb.end()

            val projection = Gdx.app.applicationListener.getPrivate<OrthographicCamera>("camera", clazz = CardCrawlGame::class.java).combined
            val center = Vector2(Settings.WIDTH / 2f, Settings.HEIGHT / 2f)
            val mouse = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat()).sub(center)

            val angle = mouse.angle(startRotation)
            info.relativeRotation(angle)

            if (info.dirtyRotation != info.rotation) {
                val relicSlotName = "${HaberdasheryMod.ID}:${relicId}"
                val skeleton = AbstractDungeon.player.getPrivate<Skeleton?>("skeleton", clazz = AbstractCreature::class.java) ?: return
                val slotIndex = skeleton.findSlotIndex(relicSlotName)
                val attachment = skeleton.getAttachment(slotIndex, relicSlotName)
                if (attachment is RegionAttachment) {
                    attachment.rotation = info.dirtyRotation
                    attachment.updateOffset()
                }
            }

            Gdx.gl.glLineWidth(2f)
            debugRenderer.projectionMatrix = projection
            debugRenderer.begin(ShapeRenderer.ShapeType.Line)
            debugRenderer.color = Color.RED
            debugRenderer.arc(center.x, center.y, 100.scale(), 360f - startRotation.angle(), angle, max(1, (angle.absoluteValue / 10f).toInt()))
            debugRenderer.color = Color.WHITE
            debugRenderer.line(center, startRotation.cpy().add(center).apply { y = Settings.HEIGHT - y })
            debugRenderer.color = Color.RED
            debugRenderer.line(center, mouse.cpy().add(center).apply { y = Settings.HEIGHT - y })
            debugRenderer.end()
            Gdx.gl.glLineWidth(1f)

            sb.begin()
        }
    }
}
