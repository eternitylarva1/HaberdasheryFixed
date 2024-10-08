package haberdashery.spine.attachments

import com.badlogic.gdx.math.Vector2
import com.esotericsoftware.spine.attachments.SkeletonAttachment

class OffsetSkeletonAttachment(name: String) : SkeletonAttachment(name) {
    var position: Vector2 = Vector2()
    var rotation: Float = 0f
    var scaleX: Float = 1f
    var scaleY: Float = 1f
}
