package haberdashery.spine

import com.esotericsoftware.spine.AnimationState.AnimationStateListener
import com.esotericsoftware.spine.Event
import com.esotericsoftware.spine.Skeleton

class AnimationEventListener(
    private val parent: Skeleton,
    private val slotName: String,
) : AnimationStateListener {
    override fun event(trackIndex: Int, event: Event) {
        val slot = parent.findSlot(slotName) ?: return
        when (event.data.name) {
            "drawOrder" -> when (event.string) {
                BACK -> {
                    parent.drawOrder.removeValue(slot, true)
                    parent.drawOrder.insert(0, slot)
                }
                FRONT -> {
                    parent.drawOrder.removeValue(slot, true)
                    parent.drawOrder.add(slot)
                }
            }
        }
    }

    override fun complete(trackIndex: Int, loopCount: Int) {
    }

    override fun start(trackIndex: Int) {
    }

    override fun end(trackIndex: Int) {
    }

    companion object {
        private const val BACK = "<BACK>"
        private const val FRONT = "<FRONT>"
    }
}
