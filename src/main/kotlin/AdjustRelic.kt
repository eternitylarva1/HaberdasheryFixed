package haberdashery

import basemod.DevConsole
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Affine2
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.ScreenUtils
import com.esotericsoftware.spine.BonePickerSkeletonRendererDebug
import com.esotericsoftware.spine.Skeleton
import com.esotericsoftware.spine.SkeletonRendererDebug
import com.esotericsoftware.spine.Slot
import com.esotericsoftware.spine.attachments.Attachment
import com.esotericsoftware.spine.attachments.RegionAttachment
import com.megacrit.cardcrawl.core.CardCrawlGame
import com.megacrit.cardcrawl.core.Settings
import com.megacrit.cardcrawl.dungeons.AbstractDungeon
import com.megacrit.cardcrawl.helpers.FontHelper
import com.megacrit.cardcrawl.helpers.ImageMaster
import com.megacrit.cardcrawl.helpers.RelicLibrary
import com.megacrit.cardcrawl.helpers.input.InputHelper
import haberdashery.database.AttachDatabase
import haberdashery.database.AttachInfo
import haberdashery.database.MySlotData
import haberdashery.extensions.*
import haberdashery.patches.StopOtherKeyboardShortcuts
import haberdashery.spine.attachments.MaskedRegionAttachment
import haberdashery.spine.attachments.OffsetSkeletonAttachment
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.max

object AdjustRelic {
    private val debugRenderer = ShapeRenderer()
    private val projection
        get() = Gdx.app.applicationListener.getPrivate<OrthographicCamera>("camera", clazz = CardCrawlGame::class.java).combined
    private val skeleton
        get() = AbstractDungeon.player?.skeleton
    private val skeletonStart
        get() = Skeleton(skeleton).apply {
            setToSetupPose()
            updateWorldTransform()
        }
    private val bonePicker = BonePickerSkeletonRendererDebug().apply {
        setPremultipliedAlpha(true)
        setBoundingBoxes(false)
        setMeshHull(false)
        setMeshTriangles(false)
        setRegionAttachments(false)
        setPaths(false)
        setScale(Settings.scale)
    }
    private val srd = SkeletonRendererDebug().apply {
        setPremultipliedAlpha(true)
        setBoundingBoxes(true)
        setMeshHull(true)
        setMeshTriangles(false)
        setRegionAttachments(false)
        setPaths(false)
        setScale(Settings.scale)
    }
    private val maskVisualizerShader =
        ShaderProgram(
            Gdx.files.internal(HaberdasheryMod.assetPath("shaders/mask.vert")),
            Gdx.files.internal(HaberdasheryMod.assetPath("shaders/maskVisualizer.frag"))
        ).apply {
            if (!isCompiled) {
                throw RuntimeException(log)
            }
        }
    private val attachment: Attachment?
        get() {
            if (relicId == null) return null
            val relicSlotName = HaberdasheryMod.makeID(relicId!!)
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
    private var shearing: Vector2? = null
    private var shearAxis: Axis? = null

    private var mode: EditMode = EditMode.Main
    private var pauseAnimation: Boolean = false

    private var saveTimer = 0f

    // Mask editing settings
    private const val DEBUG_TEST_FBO = false
    private var justStartedMaskMode: Boolean = false
    private val dirtyMaskFbo: FrameBuffer = FrameBuffer(Pixmap.Format.Alpha, Gdx.graphics.width, Gdx.graphics.height, false, false)
    private var dirtyMaskIsDirty: Boolean = false
    private val currFillPos: Vector2 = Vector2()
    private var lastFillPos: Vector2? = null
    private var brushSize: Int = 20
    private var zoomLevel: Float = 1f
    private var viewMask: Boolean = false

    internal var DEBUG_RENDER_SKELETON = false

    @Suppress("unused")
    @JvmStatic
    fun pauseAnimation(): Boolean {
        return mode == EditMode.PickingBone && pauseAnimation
    }

    fun addRelic(relicId: String) {
        if (AttachDatabase.getInfo(AbstractDungeon.player.chosenClass, relicId) != null) {
            return
        }
        this.relicId = relicId
        mode = EditMode.PickingBone
    }

    fun setRelic(relicId: String?) {
        if (relicId == null) {
            this.relicId = relicId
            return
        }

        val player = AbstractDungeon.player ?: return

        val relicSlotName = HaberdasheryMod.makeID(relicId)
        val skeleton = skeleton ?: return
        if (skeleton.findSlotIndex(relicSlotName) < 0) {
            return
        }

        this.relicId = relicId
    }

    fun reload() {
        setRelic(relicId)
    }

    fun update() {
        StopOtherKeyboardShortcuts.clear()

        if (AbstractDungeon.player == null) {
            setRelic(null)
        }

        if (saveTimer > 0f) {
            saveTimer -= Gdx.graphics.rawDeltaTime
        }

        val relicId = relicId
        if (DevConsole.visible || relicId == null) {
            return
        }

        if (mode == EditMode.PickingBone) {
            if (InputHelper.justClickedLeft && bonePicker.hoveredBone != null) {
                mode = EditMode.Main

                val relic = RelicLibrary.getRelic(relicId)?.makeCopy() ?: return
                AttachDatabase.relic(
                    AbstractDungeon.player.chosenClass,
                    relicId,
                    AttachInfo(bonePicker.hoveredBone.data.name)
                )
                AbstractDungeon.getCurrRoom().spawnRelicAndObtain(
                    Settings.WIDTH / 2f, Settings.HEIGHT / 2f,
                    relic
                )

                setRelic(relicId)
            }
            if (isKeyJustPressed(Input.Keys.P)) {
                pauseAnimation = !pauseAnimation
            }
            return
        } else if (mode == EditMode.EditingMask) {
            val attachment = attachment
            if (attachment !is MaskedRegionAttachment) {
                mode = EditMode.Main
                return
            }

            // Exit mask mode
            if (isKeyJustPressed(Input.Keys.M)) {
                mode = EditMode.Main
                // Save mask to attachment
                Pixmap.setBlending(Pixmap.Blending.None)
                dirtyMaskFbo.scope {
                    val p = ScreenUtils.getFrameBufferPixmap(
                        Settings.WIDTH / 2 - attachment.getMask().regionWidth / 2,
                        Settings.HEIGHT / 2 - attachment.getMask().regionHeight / 2,
                        attachment.getMask().regionWidth,
                        attachment.getMask().regionHeight,
                    )
                    attachment.getMask().texture.textureData.consumePixmap().drawPixmap(p, 0, 0)
                }
                Pixmap.setBlending(Pixmap.Blending.SourceOver)
                info?.let { info ->
                    if (info.maskChanged && info.mask == null) {
                        info.mask(AttachDatabase.makeRelicMaskFilename(relicId))
                    }
                    if (info.maskChanged) {
                        info.maskRequiresSave()
                    }
                }
                return
            }

            // Reset mask from attachment
            if (isKeyJustPressed(Input.Keys.R)) {
                attachment.getMask().texture.draw(attachment.getMask().texture.textureData.consumePixmap(), 0, 0)
                info?.maskChanged(false)
                justStartedMaskMode = true
                dirtyMaskIsDirty = true
            }

            if (Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT)) {
                // Zoom
                if (InputHelper.scrolledUp) {
                    zoomLevel += 0.1f
                }
                if (InputHelper.scrolledDown) {
                    zoomLevel = (zoomLevel - 0.1f).coerceAtLeast(1f)
                }
            } else {
                // Brush size
                if (InputHelper.scrolledUp) {
                    ++brushSize
                }
                if (InputHelper.scrolledDown) {
                    brushSize = (brushSize - 1).coerceAtLeast(1)
                }
            }

            // Toggle mask view
            if (isKeyJustPressed(Input.Keys.N)) {
                viewMask = !viewMask
            }

            // Draw on mask
            if (InputHelper.isMouseDown || InputHelper.isMouseDown_R) {
                dirtyMaskFbo.scope {
                    debugRenderer.projectionMatrix = projection
                    debugRenderer.begin(ShapeRenderer.ShapeType.Filled)
                    if (InputHelper.isMouseDown) {
                        debugRenderer.setColor(0f, 0f, 0f, 0f)
                    } else {
                        debugRenderer.setColor(1f, 1f, 1f, 1f)
                    }

                    currFillPos.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
                    currFillPos.sub(Settings.WIDTH / 2f, Settings.HEIGHT / 2f)
                    currFillPos.scl(1f / zoomLevel)
                    currFillPos.add(Settings.WIDTH / 2f, Settings.HEIGHT / 2f)
                    debugRenderer.circle(
                        currFillPos.x,
                        currFillPos.y,
                        brushSize / 2f
                    )
                    // Fill pixels between current and last mouse position
                    lastFillPos?.let { last ->
                        if (!last.epsilonEquals(currFillPos, 0.01f)) {
                            debugRenderer.rectLine(last, currFillPos, brushSize.toFloat())
                        }
                    }
                    if (lastFillPos == null) {
                        lastFillPos = Vector2(currFillPos)
                    } else {
                        lastFillPos!!.set(currFillPos)
                    }
                    debugRenderer.end()
                }
                info?.maskChanged(true)
                dirtyMaskIsDirty = true
            } else {
                lastFillPos = null
            }

            return
        }

        val info = info ?: return

        // Save
        if (isKeyJustPressed(Input.Keys.F)) {
            AttachDatabase.save(AttachDatabase.Enums.COMMON, skeleton!!)
            AttachDatabase.save(AbstractDungeon.player.chosenClass, skeleton!!)
            saveTimer = 2f
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
            if (shearing != null) {
                attachmentShear(info)
                shearing = null
                shearAxis = null
            }
        }

        // Draw order
        skeleton?.also { skeleton ->
            val relicSlotName = HaberdasheryMod.makeID(relicId)
            val drawOrder = skeleton.drawOrder
            val slot = skeleton.findSlot(relicSlotName)

            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
                if (isKeyJustPressed(Input.Keys.K)) {
                    info.drawOrder(info.drawOrder.slotName!!, info.drawOrder.zIndex - 1)
                    (slot.data as? MySlotData)?.let { it.zIndex = info.drawOrder.zIndex }
                    moveToZIndex(drawOrder, slot, drawOrder.indexOfFirst { it.data.name == info.drawOrder.slotName }, info.drawOrder.zIndex)
                } else if (isKeyJustPressed(Input.Keys.J)) {
                    info.drawOrder(info.drawOrder.slotName!!, info.drawOrder.zIndex + 1)
                    (slot.data as? MySlotData)?.let { it.zIndex = info.drawOrder.zIndex }
                    moveToZIndex(drawOrder, slot, drawOrder.indexOfFirst { it.data.name == info.drawOrder.slotName }, info.drawOrder.zIndex)
                }
            } else {
                if (isKeyJustPressed(Input.Keys.K)) {
                    moveSlotName(drawOrder, slot, info, false)
                } else if (isKeyJustPressed(Input.Keys.J)) {
                    moveSlotName(drawOrder, slot, info, true)
                }
            }
        }

        // Position
        if (isKeyJustPressed(Input.Keys.T)) {
            positioning = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
        } else if (!Gdx.input.isKeyPressed(Input.Keys.T) && positioning != null) {
            positioning = null
            info.finalize()
        }

        // Rotation
        if (isKeyJustPressed(Input.Keys.R)) {
            rotating = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
                .sub(Settings.WIDTH / 2f, Settings.HEIGHT / 2f)
                .nor()
                .scl(200.scale())
        } else if (!Gdx.input.isKeyPressed(Input.Keys.R) && rotating != null) {
            rotating = null
            info.finalize()
        }

        // Scale
        if (isKeyJustPressed(Input.Keys.S)) {
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

        // Shear
        if (isKeyJustPressed(Input.Keys.C) && shearing == null) {
            shearing = Vector2(Gdx.input.x.toFloat(), Settings.HEIGHT - Gdx.input.y.toFloat())
            shearAxis = Axis.X
        } else if (isKeyJustPressed(Input.Keys.V) && shearing == null) {
            shearing = Vector2(Gdx.input.x.toFloat(), Settings.HEIGHT - Gdx.input.y.toFloat())
            shearAxis = Axis.Y
        } else if (shearing != null
            && (shearAxis == Axis.X && !Gdx.input.isKeyPressed(Input.Keys.C))
            || (shearAxis == Axis.Y && !Gdx.input.isKeyPressed(Input.Keys.V))
            ) {
            shearing = null
            shearAxis = null
            info.finalize()
        }

        // Flip
        if (isKeyJustPressed(Input.Keys.X)) {
            info.flipHorizontal(!info.flipHorizontal)
            attachmentScale(info)
        }
        if (isKeyJustPressed(Input.Keys.Y)) {
            info.flipVertical(!info.flipVertical)
            attachmentScale(info)
        }

        // Large
        if (isKeyJustPressed(Input.Keys.Q)) {
            info.large(!info.large)
            largeRelicSwap(relicId, info)
        }

        // Mask
        if (isKeyJustPressed(Input.Keys.M)) {
            mode = when (mode) {
                EditMode.Main -> EditMode.EditingMask.also {
                    viewMask = false
                    justStartedMaskMode = true
                    //
                    (attachment as? MaskedRegionAttachment)?.let { attachment ->
                        if (!attachment.hasMask()) {
                            // Initialize empty mask if no mask
                            attachment.setMask(
                                Texture(
                                    Pixmap(
                                        attachment.region.regionWidth,
                                        attachment.region.regionHeight,
                                        Pixmap.Format.Alpha
                                    ).apply {
                                        setColor(1f, 1f, 1f, 1f)
                                        fill()
                                    }
                                ).asRegion()
                            )
                        }
                    }
                }
                EditMode.EditingMask -> EditMode.Main
                else -> mode
            }
        }
    }

    fun render(sb: SpriteBatch) {
        if (mode == EditMode.PickingBone) {
            if (skeleton == null) {
                mode = EditMode.Main
                return
            }

            renderBoneSelection(sb)
            return
        }
        renderSkeletonDebug(sb)

        val relicId = relicId
        val info = info
        if (relicId == null || info == null) {
            return
        }

        renderModeInfo(sb)

        if (mode == EditMode.EditingMask) {
            renderMaskEditing(sb, info)
            return
        }

        positionWidget(sb, info)
        rotationWidget(sb, info)
        scaleWidget(sb, info)
        shearWidget(sb, info)

        renderAttachInfo(sb, info)
        renderDrawOrder(sb, info)
    }

    private fun attachmentPosition(info: AttachInfo) {
        val attachment = attachment
        val bone = skeletonStart.findBone(info.boneName) ?: return
        if (attachment is RegionAttachment) {
            val pos = info.dirtyPosition.cpy().rotate(bone.worldRotationX)
            attachment.x = pos.x.renderScale()
            attachment.y = pos.y.renderScale()
            attachment.updateOffset()
        } else if (attachment is OffsetSkeletonAttachment) {
            val pos = info.dirtyPosition.cpy()
                .scl(Settings.renderScale, -Settings.renderScale)
                .add(bone.worldX, bone.worldY)
            bone.worldToLocal(pos)
            attachment.position.set(pos)
        }
    }

    private fun attachmentRotation(info: AttachInfo) {
        val attachment = attachment
        if (attachment is RegionAttachment) {
            attachment.rotation = info.dirtyRotation
            attachment.updateOffset()
        } else if (attachment is OffsetSkeletonAttachment) {
            attachment.rotation = info.dirtyRotation
        }
    }

    private fun attachmentScale(info: AttachInfo) {
        val attachment = attachment
        if (attachment is RegionAttachment) {
            attachment.scaleX = info.dirtyScale.x.renderScale()
            if (info.flipHorizontal) {
                attachment.scaleX *= -1
            }
            attachment.scaleY = info.dirtyScale.y.renderScale()
            if (info.flipVertical) {
                attachment.scaleY *= -1
            }
            attachment.updateOffset()
        } else if (attachment is OffsetSkeletonAttachment) {
            attachment.scaleX = info.dirtyScale.x.renderScale()
            if (info.flipHorizontal) {
                attachment.scaleX *= -1
            }
            attachment.scaleY = info.dirtyScale.y.renderScale()
            if (info.flipVertical) {
                attachment.scaleY *= -1
            }
        }
    }

    private fun attachmentShear(info: AttachInfo) {
        val attachment = attachment
        if (attachment is MaskedRegionAttachment) {
            attachment.shearFactor.set(info.dirtyShearFactor)
            attachment.updateOffset() // TODO not necessary?
        }
    }

    private fun renderModeInfo(sb: SpriteBatch) {
        FontHelper.renderFontLeftTopAligned(
            sb,
            FontHelper.tipBodyFont,
            "[Mode:$mode]",
            30f.scale(), Settings.HEIGHT - 270.scale(),
            Color.WHITE
        )
    }

    private fun renderBoneSelection(sb: SpriteBatch) {
        sb.end()
        bonePicker.shapeRenderer.projectionMatrix = projection
        bonePicker.draw(skeleton)
        sb.begin()

        renderModeInfo(sb)
        FontHelper.renderFontLeftTopAligned(
            sb,
            FontHelper.tipBodyFont,
            "[$relicId]\n" +
                    "Bone: " + if (bonePicker.hoveredBone != null) {
                bonePicker.hoveredBone.data.name
            } else {
                "<Select a Bone>"
            } + "\n",
            30f.scale(), Settings.HEIGHT - 300.scale(),
            Color.WHITE
        )
    }

    private fun renderSkeletonDebug(sb: SpriteBatch) {
        if (!DEBUG_RENDER_SKELETON) return
        val skeleton = skeleton ?: return

        sb.end()
        srd.shapeRenderer.projectionMatrix = projection
        srd.setMeshHull(false)
        srd.setRegionAttachments(true)
        srd.setPaths(false)
        srd.draw(skeleton)
        srd.setMeshHull(true)
        srd.setRegionAttachments(true)
        srd.setPaths(true)
        for (slot in skeleton.drawOrder) {
            val attachment = slot.attachment
            if (attachment is OffsetSkeletonAttachment) {
                attachment.apply(skeleton, slot.bone)
                srd.draw(attachment.skeleton)
                attachment.reset()
            }
        }
        sb.begin()
    }

    private fun renderMaskEditing(sb: SpriteBatch, info: AttachInfo) {
        val attachment = attachment
        if (attachment !is MaskedRegionAttachment) return

        val x = Settings.WIDTH / 2f - attachment.region.regionWidth / 2f
        val y = Settings.HEIGHT / 2f - attachment.region.regionHeight / 2f

        if (justStartedMaskMode) {
            justStartedMaskMode = false
            sb.flush()
            // Clear fbo
            dirtyMaskFbo.clear(1f, 1f, 1f, 1f)
            // Copy existing mask into fbo
            dirtyMaskFbo.scope {
                sb.disableBlending()
                attachment.getMask().apply {
                    flip(false, true)
                    sb.draw(
                        this,
                        x,
                        y,
                    )
                    flip(false, true)
                }
                sb.enableBlending()
            }
        }

        val origShader = sb.shader
        if (DEBUG_TEST_FBO) {
            sb.shader = maskVisualizerShader
            maskVisualizerShader.bind("u_mask", 1, dirtyMaskFbo.colorBufferTexture)
            maskVisualizerShader.setUniformi("u_viewMask", 1)
            sb.draw(dirtyMaskFbo.colorBufferTexture, 0f, 0f)
            sb.shader = origShader
        }

        if (dirtyMaskIsDirty) {
            dirtyMaskIsDirty = false
            dirtyMaskFbo.scope {
                val p = ScreenUtils.getFrameBufferPixmap(
                    x.toInt(),
                    y.toInt(),
                    attachment.getMask().regionWidth,
                    attachment.getMask().regionHeight,
                )
                attachment.getMask().texture.draw(p, 0, 0)
            }
        }

        // Render mask image
        sb.shader = maskVisualizerShader
        maskVisualizerShader.bind("u_mask", 1, attachment.getMask().texture)
        maskVisualizerShader.setUniformi("u_viewMask", if (viewMask) 1 else 0)
        sb.color = Color.WHITE
        sb.draw(
            attachment.region,
            x,
            y,
            attachment.region.regionWidth / 2f,
            attachment.region.regionHeight / 2f,
            attachment.region.regionWidth.toFloat(),
            attachment.region.regionHeight.toFloat(),
            zoomLevel,
            zoomLevel,
            0f
        )
        sb.shader = origShader

        // Info
        FontHelper.renderFontLeftTopAligned(
            sb,
            FontHelper.tipBodyFont,
            "View Mask: $viewMask\n" +
                    "Brush Size: $brushSize\n" +
                    "Zoom: ${"%.0f".format(Locale.ROOT, zoomLevel * 100)}%",
            30.scale(),
            Settings.HEIGHT - 300.scale(),
            Color.WHITE
        )

        // Cursor
        sb.end()

        val mouse = Vector2(InputHelper.mX.toFloat(), InputHelper.mY.toFloat())

        debugRenderer.projectionMatrix = projection
        debugRenderer.begin(ShapeRenderer.ShapeType.Line)
        debugRenderer.color = Color.LIGHT_GRAY
        debugRenderer.circle(mouse.x, mouse.y, brushSize.toFloat() / 2f * zoomLevel + 0.5f)
        debugRenderer.end()

        sb.begin()
    }

    private fun renderAttachInfo(sb: SpriteBatch, info: AttachInfo) {
        FontHelper.renderFontLeftTopAligned(
            sb,
            FontHelper.tipBodyFont,
            "[$relicId]\n" +
                    "Bone: ${info.boneName}\n" +
                    "Draw Order: ${info.drawOrder}\n" +
                    "Position: ${info.dirtyPosition.x}, ${info.dirtyPosition.y}\n" +
                    "Rotation: ${info.dirtyRotation}\n" +
                    "Scale: ${info.dirtyScale.x}, ${info.dirtyScale.y}\n" +
                    "Shear: ${info.dirtyShear.x}, ${info.dirtyShear.y}\n" +
                    "Large: ${info.large}\n" +
                    "Mask: ${info.mask}" + if (info.maskRequiresSave) "*" else "" + "\n" +
                    if (saveTimer > 0) {
                        "\nSaved!"
                    } else { "" }
            ,
            30f.scale(), Settings.HEIGHT - 300.scale(),
            Color.WHITE
        )
    }

    private fun renderDrawOrder(sb: SpriteBatch, info: AttachInfo) {
        val relicSlotName = HaberdasheryMod.makeID(relicId!!)
        val drawOrderMsg = StringBuilder("[Draw Order]\n")
        skeleton?.drawOrder?.let { drawOrder ->
            val bottom = 0
            val top = drawOrder.size-1
            var lastOrigSlot: String? = null
            for (i in bottom..top) {
                val data = drawOrder[i].data
                if (data is MySlotData) {
                    if (lastOrigSlot != info.drawOrder.slotName) {
                        continue
                    }
                    drawOrderMsg.append("    [").append(data.zIndex).append("] ")
                } else {
                    lastOrigSlot = data.name
                }
                if (data.name == relicSlotName) {
                    drawOrderMsg.append("[#${Settings.GOLD_COLOR}]").append(data.name.removeIDPrefix()).append("[]\n")
                } else {
                    drawOrderMsg.append(data.name.removeIDPrefix()).append('\n')
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

    private fun String.removeIDPrefix(): String {
        return this.removePrefix("${HaberdasheryMod.ID}:")
    }

    private fun positionWidget(sb: SpriteBatch, info: AttachInfo) {
        val startPosition = positioning
        if (startPosition != null) {
            sb.end()

            val mouse = Vector2(Gdx.input.x.toFloat(), Gdx.input.y.toFloat())
            val diff = mouse.cpy().sub(startPosition).scl(0.1f)
            if (attachment is RegionAttachment) {
                skeletonStart.findBone(info.boneName)?.let { bone ->
                    diff.rotate(bone.worldRotationX).scl(1f, -1f).rotate(-bone.worldRotationX)
                }
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

            if (info.dirtyScale != info.scale) {
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

    private fun shearWidget(sb: SpriteBatch, info: AttachInfo) {
        val startPosition = shearing
        val axis = shearAxis
        if (startPosition != null && axis != null) {
            sb.end()

            val affine = Affine2()
            affine.shear(info.shearFactor)
            val test = Vector2().apply {
                when (axis) {
                    Axis.X -> y = -1f
                    Axis.Y -> x = -1f
                }
            }
            affine.applyTo(test)
            test.setLength(200f)

            val center = startPosition.cpy().add(test)
            val mouse = Vector2(Gdx.input.x.toFloat(), Settings.HEIGHT - Gdx.input.y.toFloat()).sub(center)

            val axisVec = Vector2().apply {
                when (axis) {
                    Axis.X -> y = 1f
                    Axis.Y -> x = 1f
                }
            }
            val angle = mouse.angle(axisVec) * if (axis == Axis.X) -1 else 1
            info.shear(axis, angle)

            if (info.dirtyShear != info.shear) {
                attachmentShear(info)
            }

            affine.idt()
            val dirtyAffine = Affine2()
            affine.translate(center)
            dirtyAffine.set(affine)
            affine.shear(info.shearFactor)
            dirtyAffine.shear(info.dirtyShearFactor)

            val a = Vector2(-100f, -100f)
            val b = Vector2(-100f, +100f)
            val c = Vector2(+100f, +100f)
            val d = Vector2(+100f, -100f)
            val da = a.cpy()
            val db = b.cpy()
            val dc = c.cpy()
            val dd = d.cpy()
            affine.applyTo(a)
            affine.applyTo(b)
            affine.applyTo(c)
            affine.applyTo(d)
            dirtyAffine.applyTo(da)
            dirtyAffine.applyTo(db)
            dirtyAffine.applyTo(dc)
            dirtyAffine.applyTo(dd)

            dirtyAffine.setToShearing(info.dirtyShearFactor)
            val x1 = Vector2(0f, 1f)
            val x2 = Vector2(0f, -1f)
            val y1 = Vector2(1f, 0f)
            val y2 = Vector2(-1f, 0f)
            dirtyAffine.applyTo(x1)
            dirtyAffine.applyTo(x2)
            dirtyAffine.applyTo(y1)
            dirtyAffine.applyTo(y2)
            x1.setLength(200f)
            x2.setLength(200f)
            y1.setLength(200f)
            y2.setLength(200f)
            dirtyAffine.setToTranslation(center)
            dirtyAffine.applyTo(x1)
            dirtyAffine.applyTo(x2)
            dirtyAffine.applyTo(y1)
            dirtyAffine.applyTo(y2)

            Gdx.gl.glLineWidth(2f)
            debugRenderer.projectionMatrix = projection
            debugRenderer.begin(ShapeRenderer.ShapeType.Line)
            debugRenderer.color = Color.WHITE
            debugRenderer.polygon(floatArrayOf(
                a.x, a.y,
                b.x, b.y,
                c.x, c.y,
                d.x, d.y,
            ))
            debugRenderer.color = Color.RED
            debugRenderer.polygon(floatArrayOf(
                da.x, da.y,
                db.x, db.y,
                dc.x, dc.y,
                dd.x, dd.y,
            ))
            debugRenderer.color = if (axis == Axis.X) Color.GREEN else Color.WHITE
            debugRenderer.line(x1, x2)
            debugRenderer.color = if (axis == Axis.Y) Color.GREEN else Color.WHITE
            debugRenderer.line(y1, y2)
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
                    info.drawOrder(drawOrder[i].data.name, info.drawOrder.zIndex)
                    newParentSlotIndex = i
                    break
                }
            }
        }

        if (newParentSlotIndex != null) {
            moveToZIndex(drawOrder, slot, newParentSlotIndex, info.drawOrder.zIndex)
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

    private fun largeRelicSwap(relicId: String, info: AttachInfo) {
        val relic = RelicLibrary.getRelic(relicId)
        val attachment = attachment
        if (attachment is RegionAttachment) {
            val newTexture = if (info.large) {
                val largeTex = ImageMaster.loadImage("images/largeRelics/${relic.imgUrl}")
                    ?.asPremultiplyAlpha(true)
                    ?: run {
                        info.large(false)
                        return
                    }
                largeTex
            } else {
                ImageMaster.getRelicImg(relic.relicId).asPremultiplyAlpha()
            }.asRegion()
            attachment.region.texture.dispose()
            attachment.region = newTexture
            attachment.width = newTexture.regionWidth.toFloat()
            attachment.height = newTexture.regionHeight.toFloat()
            attachment.updateOffset()
        }
    }

    private fun isKeyJustPressed(key: Int): Boolean {
        return Gdx.input.isKeyJustPressed(key).also {
            if (it) {
                StopOtherKeyboardShortcuts.stopForOneFrame(key)
            }
        }
    }

    private enum class EditMode {
        Main,
        PickingBone,
        EditingMask,
    }

    internal enum class Axis {
        X,
        Y,
    }
}
