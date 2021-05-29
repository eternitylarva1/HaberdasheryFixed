package haberdashery.database

import com.badlogic.gdx.math.Vector2

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
    var rotation: Float = 0f
        private set
    var position: Vector2 = Vector2()
        private set

    fun hideSlots(vararg names: String) = apply { this.hideSlotNames = names }
    fun drawOrder(slotName: String, zIndex: Int = 0) = apply {
        this.drawOrderSlotName = slotName
        this.drawOrderZIndex = zIndex
    }
    fun scale(scale: Float) = apply { scaleX(scale).scaleY(scale) }
    fun scaleX(scale: Float) = apply { this.scaleX = scale }
    fun scaleY(scale: Float) = apply { this.scaleY = scale }
    fun rotation(degrees: Float) = apply { this.rotation = degrees }
    fun position(degrees: Float, distance: Float) = apply { this.position = Vector2(distance, 0f).rotate(degrees) }
}
