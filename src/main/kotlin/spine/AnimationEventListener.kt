package haberdashery.spine

import com.esotericsoftware.spine.Animation.EventTimeline
import com.esotericsoftware.spine.AnimationState
import com.esotericsoftware.spine.AnimationState.AnimationStateListener
import com.esotericsoftware.spine.Event
import com.esotericsoftware.spine.Skeleton
import com.megacrit.cardcrawl.actions.AbstractGameAction
import com.megacrit.cardcrawl.core.CardCrawlGame
import haberdashery.Config
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class AnimationEventListener(
    private val anim: AnimationState,
    private val parent: Skeleton,
    private val slotName: String,
) : AnimationStateListener {
    private val actions = mutableListOf<AbstractGameAction>()

    override fun event(trackIndex: Int, event: Event) {
        val slot = parent.findSlot(slotName) ?: return
        when (event.data.name) {
            DRAW_ORDER -> when (event.string) {
                DRAW_ORDER_BACK -> {
                    parent.drawOrder.removeValue(slot, true)
                    parent.drawOrder.insert(0, slot)
                }
                DRAW_ORDER_FRONT -> {
                    parent.drawOrder.removeValue(slot, true)
                    parent.drawOrder.add(slot)
                }
            }
            PLAY_SFX -> {
                if (Config.playSfx) {
                    event.string?.let { key ->
                        CardCrawlGame.sound.playV(
                            key,
                            if (event.float == 0f) 1f else event.float
                        )
                    }
                }
            }
            ON_FLASH -> when (event.string) {
                ON_FLASH_CONTINUE -> {
                    continueActions()
                }
            }
            else -> logger.info("Unknown animation event(${event.data.name}) in subskeleton($slotName)")
        }
    }

    override fun complete(trackIndex: Int, loopCount: Int) {
    }

    override fun start(trackIndex: Int) {
    }

    override fun end(trackIndex: Int) {
        val hasContinueEvent = anim.tracks[trackIndex].animation.timelines
            .filterIsInstance<EventTimeline>()
            .flatMap { it.events.toList() }
            .any { it.data.name == ON_FLASH && it.string == ON_FLASH_CONTINUE }
        if (!hasContinueEvent) {
            continueActions()
        }
    }

    fun addOnFlashAction(action: AbstractGameAction) {
        actions.add(action)
    }

    private fun continueActions() {
        if (actions.size > 0) {
            actions.removeAt(0).isDone = true
        }
    }

    companion object {
        private val logger: Logger = LogManager.getLogger(AnimationEventListener::class.java)

        private const val DRAW_ORDER = "drawOrder"
        private const val DRAW_ORDER_BACK = "back"
        private const val DRAW_ORDER_FRONT = "front"

        private const val PLAY_SFX = "sfx"

        private const val ON_FLASH = "onFlash"
        private const val ON_FLASH_CONTINUE = "continue"
    }
}
