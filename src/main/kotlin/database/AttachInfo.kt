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


    var dirtyScaleX: Float = scaleX
        private set
    var dirtyScaleY: Float = scaleY
        private set
    var dirtyRotation: Float = rotation
        private set(value) {
            field = value % 360f
        }

    fun finalize() = apply {
        scaleX = dirtyScaleX
        if (flipHorizontal) {
            scaleX *= -1
        }
        scaleY = dirtyScaleY
        if (flipVertical) {
            scaleY *= -1
        }
        rotation = dirtyRotation
    }
    fun clean() = apply {
        dirtyScaleX = scaleX.absoluteValue
        dirtyScaleY = scaleY.absoluteValue
        dirtyRotation = rotation
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
    fun relativeRotation(degrees: Float) = apply { this.dirtyRotation = this.rotation + degrees }
    fun position(degrees: Float, distance: Float) = apply {
        this.positionData.set(degrees, distance)
        this.position.set(distance, 0f).rotate(degrees)
    }
}
