package haberdashery

import basemod.DevConsole
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.esotericsoftware.spine.BonePickerSkeletonRendererDebug
import com.esotericsoftware.spine.Skeleton
import com.esotericsoftware.spine.Slot
import com.esotericsoftware.spine.attachments.Attachment
import com.esotericsoftware.spine.attachments.RegionAttachment
import com.megacrit.cardcrawl.core.AbstractCreature
import com.megacrit.cardcrawl.core.CardCrawlGame
import com.megacrit.cardcrawl.core.Settings
import com.megacrit.cardcrawl.dungeons.AbstractDungeon
import com.megacrit.cardcrawl.helpers.FontHelper
import com.megacrit.cardcrawl.helpers.RelicLibrary
import com.megacrit.cardcrawl.helpers.input.InputHelper
import haberdashery.database.AttachDatabase
import haberdashery.database.AttachInfo
import haberdashery.database.MySlotData
import haberdashery.extensions.flipY
import haberdashery.extensions.getPrivate
import haberdashery.extensions.scale
import kotlin.math.absoluteValue
import kotlin.math.max

object AdjustRelic {
    private val debugRenderer = ShapeRenderer()
    private val projection
        get() = Gdx.app.applicationListener.getPrivate<OrthographicCamera>("camera", clazz = CardCrawlGame::class.java).combined
    private val skeleton
        get() = AbstractDungeon.player?.getPrivate<Skeleton?>("skeleton", clazz = AbstractCreature::class.java)
    private val skeletonStart by lazy {
        Skeleton(skeleton).apply {
            setToSetupPose()
            updateWorldTransform()
        }
    }
    private val srd = BonePickerSkeletonRendererDebug().apply {
        setPremultipliedAlpha(true)
        setBoundingBoxes(false)
        setMeshHull(false)
        setMeshTriangles(false)
        setRegionAttachments(false)
        setScale(Settings.scale)
    }
    private val attachment: Attachment?
        get() {
            val relicSlotName = "${HaberdasheryMod.ID}:${relicId}"
            val slotIndex = skeleton?.findSlotIndex(relicSlotName) ?: return null
            return skeleton?.getAttachment(slotIndex, relicSlotName)
        }

    private var relicId: String? = null
        set(value) {
            field = value
            if (value != null) {
                info = AttachDatabase.getInfo(AbstractDungeon.player.chosenClass, value)
            }
        }
    private var info: AttachInfo? = null

    private var positioning: Vector2? = null
    private var rotating: Vector2? = null
    private var scaling: Float? = null

    var renderBones: Boolean = false

    fun addRelic(relicId: String) {
        this.relicId = relicId
        renderBones = true
    }

    fun setRelic(relicId: String?) {
        if (relicId == null) {
            this.relicId = relicId
            return
        }

        val player = AbstractDungeon.player ?: return

        val relicSlotName = "${HaberdasheryMod.ID}:${relicId}"
        val skeleton = skeleton ?: return
        if (skeleton.findSlotIndex(relicSlotName) < 0) {
            return
        }

        this.relicId = relicId
    }

    fun update() {
        val relicId = relicId
        if (DevConsole.visible || relicId == null) {
            return
        }

        if (renderBones) {
            if (InputHelper.justClickedLeft && srd.hoveredBone != null) {
                renderBones = false

                val relic = RelicLibrary.getRelic(relicId)?.makeCopy() ?: return
                AttachDatabase.relic(
                    AbstractDungeon.player.chosenClass,
                    relicId,
                    AttachInfo(srd.hoveredBone.data.name)
                )
                AbstractDungeon.getCurrRoom().spawnRelicAndObtain(
                    Settings.WIDTH / 2f, Settings.HEIGHT / 2f,
                    relic
                )

                setRelic(relicId)
            }
            return
        }

        val info = info ?: return

        if (Gdx.input.isKeyJustPressed(Input.Keys.F)) {
            AttachDatabase.save(AbstractDungeon.player.chosenClass)
        }

        // Reset changes
        if (InputHelper.justClickedRight) {
            info.clean()
            if (positioning != null) {
                attachmentPosition(info)
                positioning = null
            }
            if (rotating != null) {
                attachmentRotation(info)
                rotating = null
            }
            if (scaling != null) {
                attachmentScale(info)
                scaling = null
            }
        }

        // Draw order
        skeleton?.also { skeleton ->
            val relicSlotName = "${HaberdasheryMod.ID}:${relicId}"
            val drawOrder = skeleton.drawOrder
            val slot = skeleton.findSlot(relicSlotName)

            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
                    info.drawOrder(info.drawOrderSlotName!!, info.drawOrderZIndex - 1)
                    (slot.data as? MySlotData)?.let { it.zIndex = info.drawOrderZIndex }
                    moveToZIndex(drawOrder, slot, drawOrder.indexOfFirst { it.data.name == info.drawOrderSlotName }, info.drawOrderZIndex)
                } else if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
                    info.drawOrder(info.drawOrderSlotName!!, info.drawOrderZIndex + 1)
                    (slot.data as? MySlotData)?.let { it.zIndex = info.drawOrderZIndex }
                    moveToZIndex(drawOrder, slot, drawOrder.indexOfFirst { it.data.name == info.drawOrderSlotName }, info.drawOrderZIndex)
                }
            } else {
                if (Gdx.input.isKeyJustPressed(Input.Keys.K)) {
                    moveSlotName(drawOrder, slot, info, false)
                } else if (Gdx.input.isKeyJustPressed(Input.Keys.J)) {
                    moveSlotName(drawOrder, slot, info, true)
                }
            }
        }

        // Position
        if (Gdx.input.isKeyJustPressed(Input.Keys.T)) {
            positioning = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
        } else if (!Gdx.input.isKeyPressed(Input.Keys.T) && positioning != null) {
            positioning = null
            info.finalize()
        }

        // Rotation
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            rotating = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
                .sub(Settings.WIDTH / 2f, Settings.HEIGHT / 2f)
                .nor()
                .scl(200.scale())
        } else if (!Gdx.input.isKeyPressed(Input.Keys.R) && rotating != null) {
            rotating = null
            info.finalize()
        }

        // Scale
        if (Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            val mouse = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat()).sub(Settings.WIDTH / 2f, Settings.HEIGHT / 2f)
            scaling = if (mouse.x.absoluteValue > mouse.y.absoluteValue) {
                mouse.x.absoluteValue
            } else {
                mouse.y.absoluteValue
            }
        } else if (!Gdx.input.isKeyPressed(Input.Keys.S) && scaling != null) {
            scaling = null
            info.finalize()
        }

        // Flip
        if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
            info.flipHorizontal(!info.flipHorizontal)
            attachmentScale(info)
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.Y)) {
            info.flipVertical(!info.flipVertical)
            attachmentScale(info)
        }
    }

    fun render(sb: SpriteBatch) {
        if (renderBones) {
            sb.end()
            srd.shapeRenderer.projectionMatrix = projection
            srd.draw(skeleton)
            sb.begin()

            FontHelper.renderFontLeftTopAligned(
                sb,
                FontHelper.tipBodyFont,
                "[$relicId]\n" +
                        "Bone: " + if (srd.hoveredBone != null) {
                            srd.hoveredBone.data.name
                        } else {
                            "<Select a Bone>"
                        } + "\n",
                30f, Settings.HEIGHT - 300.scale(),
                Color.WHITE
            )

            return
        }

        val relicId = relicId
        val info = info
        if (relicId == null || info == null) {
            return
        }

        positionWidget(sb, info)
        rotationWidget(sb, info)
        scaleWidget(sb, info)

        FontHelper.renderFontLeftTopAligned(
            sb,
            FontHelper.tipBodyFont,
            "[$relicId]\n" +
                    "Bone: ${info.boneName}\n" +
                    "Draw Order: ${info.drawOrderSlotName} [${info.drawOrderZIndex}]\n" +
                    "Position: ${info.dirtyPosition.x}, ${info.dirtyPosition.y}\n" +
                    "Rotation: ${info.dirtyRotation}\n" +
                    "Scale: ${info.dirtyScaleX}, ${info.dirtyScaleY}\n",
            30f.scale(), Settings.HEIGHT - 300.scale(),
            Color.WHITE
        )

        val relicSlotName = "${HaberdasheryMod.ID}:${relicId}"
        val drawOrderMsg = StringBuilder("[Draw Order]\n")
        skeleton?.drawOrder?.let { drawOrder ->
            val bottom = 0
            val top = drawOrder.size-1
            var lastOrigSlot: String? = null
            for (i in bottom..top) {
                val data = drawOrder[i].data
                if (data is MySlotData) {
                    if (lastOrigSlot != info.drawOrderSlotName) {
                        continue
                    }
                    drawOrderMsg.append("    [").append(data.zIndex).append("] ")
                } else {
                    lastOrigSlot = data.name
                }
                if (data.name == relicSlotName) {
                    drawOrderMsg.append("[#${Settings.GOLD_COLOR}]").append(data.name).append("[]\n")
                } else {
                    drawOrderMsg.append(data.name).append('\n')
                }
            }
        }

        FontHelper.renderFontRightTopAligned(
            sb,
            FontHelper.tipBodyFont,
            drawOrderMsg.toString(),
            Settings.WIDTH - 30f.scale(), Settings.HEIGHT - 200.scale(),
            Color.WHITE
        )
    }

    private fun attachmentPosition(info: AttachInfo) {
        val attachment = attachment
        if (attachment is RegionAttachment) {
            skeletonStart.findBone(info.boneName)?.let { bone ->
                val pos = info.dirtyPosition.cpy().rotate(bone.worldRotationX)
                attachment.x = pos.x
                attachment.y = pos.y
                attachment.updateOffset()
            }
        }
    }

    private fun attachmentRotation(info: AttachInfo) {
        val attachment = attachment
        if (attachment is RegionAttachment) {
            attachment.rotation = info.dirtyRotation
            attachment.updateOffset()
        }
    }

    private fun attachmentScale(info: AttachInfo) {
        val attachment = attachment
        if (attachment is RegionAttachment) {
            attachment.scaleX = info.dirtyScaleX
            if (info.flipHorizontal) {
                attachment.scaleX *= -1
            }
            attachment.scaleY = info.dirtyScaleY
            if (info.flipVertical) {
                attachment.scaleY *= -1
            }
            attachment.updateOffset()
        }
    }

    private fun positionWidget(sb: SpriteBatch, info: AttachInfo) {
        val startPosition = positioning
        if (startPosition != null) {
            sb.end()

            val mouse = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
            val diff = mouse.cpy().sub(startPosition).scl(0.1f)
            skeletonStart.findBone(info.boneName)?.let { bone ->
                diff.rotate(bone.worldRotationX).scl(1f, -1f).rotate(-bone.worldRotationX)
            }

            info.relativePosition(diff.x , diff.y)

            if (info.dirtyPosition != info.position) {
                attachmentPosition(info)
            }

            Gdx.gl.glLineWidth(2f)
            debugRenderer.projectionMatrix = projection
            debugRenderer.begin(ShapeRenderer.ShapeType.Line)
            debugRenderer.color = Color.WHITE
            debugRenderer.line(startPosition.cpy().add(-20f, 0f).flipY(), startPosition.cpy().add(20f, 0f).flipY())
            debugRenderer.line(startPosition.cpy().add(0f, -20f).flipY(), startPosition.cpy().add(0f, 20f).flipY())
            debugRenderer.color = Color.RED
            debugRenderer.line(startPosition.cpy().flipY(), mouse.cpy().flipY())
            debugRenderer.line(mouse.cpy().add(-20f, 0f).flipY(), mouse.cpy().add(20f, 0f).flipY())
            debugRenderer.line(mouse.cpy().add(0f, -20f).flipY(), mouse.cpy().add(0f, 20f).flipY())
            debugRenderer.end()
            Gdx.gl.glLineWidth(1f)

            sb.begin()
        }
    }

    private fun rotationWidget(sb: SpriteBatch, info: AttachInfo) {
        val startRotation = rotating
        if (startRotation != null) {
            sb.end()

            val center = Vector2(Settings.WIDTH / 2f, Settings.HEIGHT / 2f)
            val mouse = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat()).sub(center)

            val angle = mouse.angle(startRotation)
            info.relativeRotation(angle)

            if (info.dirtyRotation != info.rotation) {
                attachmentRotation(info)
            }

            Gdx.gl.glLineWidth(2f)
            debugRenderer.projectionMatrix = projection
            debugRenderer.begin(ShapeRenderer.ShapeType.Line)
            debugRenderer.color = Color.RED
            debugRenderer.arc(center.x, center.y, 100.scale(), 360f - startRotation.angle(), angle, max(1, (angle.absoluteValue / 10f).toInt()))
            debugRenderer.color = Color.WHITE
            debugRenderer.line(center, startRotation.cpy().add(center).flipY())
            debugRenderer.color = Color.RED
            debugRenderer.line(center, mouse.cpy().add(center).flipY())
            debugRenderer.end()
            Gdx.gl.glLineWidth(1f)

            sb.begin()
        }
    }

    private fun scaleWidget(sb: SpriteBatch, info: AttachInfo) {
        val startScale = scaling
        if (startScale != null) {
            sb.end()

            val center = Vector2(Settings.WIDTH / 2f, Settings.HEIGHT / 2f)
            val mouse = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat()).sub(center)
            val longSide = if (mouse.x.absoluteValue > mouse.y.absoluteValue) {
                mouse.x.absoluteValue
            } else {
                mouse.y.absoluteValue
            }

            info.relativeScale(longSide / startScale)

            if (info.dirtyScaleX != info.scaleX) {
                attachmentScale(info)
            }

            Gdx.gl.glLineWidth(2f)
            debugRenderer.projectionMatrix = projection
            debugRenderer.begin(ShapeRenderer.ShapeType.Line)
            debugRenderer.color = Color.WHITE
            debugRenderer.rect(center.x - startScale, center.y - startScale, startScale * 2, startScale * 2)
            debugRenderer.color = Color.RED
            debugRenderer.rect(center.x - longSide, center.y - longSide, longSide * 2, longSide * 2)
            debugRenderer.end()
            Gdx.gl.glLineWidth(1f)

            sb.begin()
        }
    }

    private fun moveSlotName(drawOrder: Array<Slot>, slot: Slot, info: AttachInfo, up: Boolean) {
        val index = drawOrder.indexOf(slot, true)
        val range = if (up) {
            index+1 until drawOrder.size
        } else {
            index-1 downTo 0
        }
        var find = if (up) {
            1
        } else {
            2
        }

        var newParentSlotIndex: Int? = null
        for (i in range) {
            if (!drawOrder[i].data.name.startsWith(HaberdasheryMod.makeID(""))) {
                if (--find == 0) {
                    info.drawOrder(drawOrder[i].data.name, info.drawOrderZIndex)
                    newParentSlotIndex = i
                    break
                }
            }
        }

        if (newParentSlotIndex != null) {
            moveToZIndex(drawOrder, slot, newParentSlotIndex, info.drawOrderZIndex)
        }
    }

    private fun moveToZIndex(drawOrder: Array<Slot>, slot: Slot, parentSlotIndex: Int, zIndex: Int) {
        val origIndex = drawOrder.indexOf(slot, true)
        drawOrder.removeIndex(origIndex)

        var insertIndex: Int = parentSlotIndex + 1
        if (parentSlotIndex > origIndex) {
            --insertIndex
        }
        for (i in insertIndex until drawOrder.size) {
            val data = drawOrder[i].data
            if (data is MySlotData) {
                if (zIndex >= data.zIndex) {
                    insertIndex = i + 1
                }
            } else {
                break
            }
        }
        drawOrder.insert(insertIndex, slot)
    }
}
