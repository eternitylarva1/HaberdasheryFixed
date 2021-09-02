package haberdashery.database

import com.badlogic.gdx.math.Vector2
import com.google.gson.annotations.SerializedName
import kotlin.math.absoluteValue

class AttachInfo(
    val boneName: String
) {
    // for gson initializing
    private constructor() : this("") {}

    var large: Boolean = false
    var drawOrderSlotName: String? = null
        private set
    var drawOrderZIndex: Int = 0
        private set
    var hideSlotNames: Array<out String> = emptyArray()
        private set
    @Transient
    internal val hideSlotAttachmentMemory = mutableMapOf<String, String>()

    @Transient
    var scaleX: Float = 1f
        private set
    @Transient
    var scaleY: Float = 1f
        private set
    var flipHorizontal: Boolean = false
        private set
    var flipVertical: Boolean = false
        private set
    @Transient
    var rotation: Float = 0f
        private set
    @Transient
    var position: Vector2 = Vector2()
        private set


    @SerializedName("scaleX")
    internal var dirtyScaleX: Float = scaleX
        private set
    @SerializedName("scaleY")
    internal var dirtyScaleY: Float = scaleY
        private set
    @SerializedName("rotation")
    internal var dirtyRotation: Float = rotation
        private set(value) {
            field = value % 360f
        }
    @SerializedName("position")
    internal var dirtyPosition: Vector2 = Vector2()
        private set

    internal fun finalize() = apply {
        scaleX = dirtyScaleX
        if (flipHorizontal) {
            scaleX *= -1
        }
        scaleY = dirtyScaleY
        if (flipVertical) {
            scaleY *= -1
        }
        rotation = dirtyRotation
        position.set(dirtyPosition)
    }
    internal fun clean() = apply {
        dirtyScaleX = scaleX.absoluteValue
        dirtyScaleY = scaleY.absoluteValue
        dirtyRotation = rotation
        dirtyPosition.set(position)
    }

    fun hideSlots(vararg names: String) = apply { this.hideSlotNames = names }
    fun drawOrder(slotName: String, zIndex: Int = 0) = apply {
        this.drawOrderSlotName = slotName
        this.drawOrderZIndex = zIndex
    }
    fun scale(scale: Float) = apply { scaleX(scale).scaleY(scale) }
    internal fun relativeScale(scale: Float) = apply { scaleX(this.scaleX * scale).scaleY(this.scaleY * scale) }
    fun scaleX(scale: Float) = apply {
        this.dirtyScaleX = scale
        if (scale < 0) {
            this.dirtyScaleX *= -1
            flipHorizontal(true)
        }
    }
    fun scaleY(scale: Float) = apply {
        this.dirtyScaleY = scale
        if (scale < 0) {
            this.dirtyScaleY *= -1
            flipVertical(true)
        }
    }
    fun flipHorizontal(flip: Boolean) = apply { this.flipHorizontal = flip }
    fun flipVertical(flip: Boolean) = apply { this.flipVertical = flip }
    fun rotation(degrees: Float) = apply { this.dirtyRotation = degrees }
    internal fun relativeRotation(degrees: Float) = apply { this.dirtyRotation = this.rotation + degrees }
    fun position(x: Float, y: Float) = apply {
        this.dirtyPosition.set(x, y)
    }
    internal fun relativePosition(x: Float, y: Float) = apply {
        this.dirtyPosition.set(position.x + x, position.y + y)
    }
}
