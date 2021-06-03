package haberdashery.database

import com.badlogic.gdx.math.Vector2
import kotlin.math.absoluteValue

class AttachInfo(
    val boneName: String
) {
    var drawOrderSlotName: String? = null
        private set
    var drawOrderZIndex: Int = 0
        private set
    var hideSlotNames: Array<out String> = emptyArray()
        private set

    var scaleX: Float = 1f
        private set
    var scaleY: Float = 1f
        private set
    var flipHorizontal: Boolean = false
        private set
    var flipVertical: Boolean = false
        private set
    var rotation: Float = 0f
        private set
    var position: Vector2 = Vector2()
        private set
    var positionData: Vector2 = Vector2()
        private set


    internal var dirtyScaleX: Float = scaleX
        private set
    internal var dirtyScaleY: Float = scaleY
        private set
    internal var dirtyRotation: Float = rotation
        private set(value) {
            field = value % 360f
        }
    internal var dirtyPosition: Vector2 = Vector2()
        private set
    internal var dirtyPositionData: Vector2 = Vector2()
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
        positionData.set(dirtyPositionData)
    }
    internal fun clean() = apply {
        dirtyScaleX = scaleX.absoluteValue
        dirtyScaleY = scaleY.absoluteValue
        dirtyRotation = rotation
        dirtyPosition.set(position)
        dirtyPositionData.set(positionData)
    }

    fun hideSlots(vararg names: String) = apply { this.hideSlotNames = names }
    fun drawOrder(slotName: String, zIndex: Int = 0) = apply {
        this.drawOrderSlotName = slotName
        this.drawOrderZIndex = zIndex
    }
    fun scale(scale: Float) = apply { scaleX(scale).scaleY(scale) }
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
    fun position(degrees: Float, distance: Float) = apply {
        this.dirtyPositionData.set(degrees, distance)
        this.dirtyPosition.set(distance, 0f).rotate(degrees)
    }
    internal fun relativePosition(x: Float, y: Float) = apply {
        this.dirtyPosition.set(position.x + x, position.y + y)
        this.dirtyPositionData.set(dirtyPosition.angle(), dirtyPosition.len())
    }
}
