package haberdashery.patches

import com.badlogic.gdx.graphics.glutils.PixmapTextureData
import com.esotericsoftware.spine.attachments.RegionAttachment
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2
import com.megacrit.cardcrawl.characters.AbstractPlayer
import haberdashery.HaberdasheryMod
import haberdashery.database.MySlotData
import haberdashery.extensions.skeleton
import haberdashery.extensions.subSkeletons

@SpirePatch2(
    clz = AbstractPlayer::class,
    method = "dispose"
)
object DisposePlayer {
    @JvmStatic
    fun Postfix(__instance: AbstractPlayer) {
        val skeleton = __instance.skeleton ?: return

        skeleton.slots.forEach { slot ->
            if (slot.data.name.startsWith(HaberdasheryMod.makeID("")) && slot.data is MySlotData) {
                val attachment = slot.attachment
                if (attachment is RegionAttachment) {
                    val texData = attachment.region.texture.textureData
                    if (texData is PixmapTextureData) {
                        attachment.region.texture.dispose()
                    }
                }
            }
        }

        __instance.subSkeletons.forEach { it.toDispose.dispose() }
    }
}
