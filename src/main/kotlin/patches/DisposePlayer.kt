package haberdashery.patches

import com.badlogic.gdx.graphics.glutils.PixmapTextureData
import com.esotericsoftware.spine.Skeleton
import com.esotericsoftware.spine.attachments.RegionAttachment
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2
import com.megacrit.cardcrawl.characters.AbstractPlayer
import com.megacrit.cardcrawl.core.AbstractCreature
import haberdashery.HaberdasheryMod
import haberdashery.database.MySlotData
import haberdashery.extensions.getPrivate

@SpirePatch2(
    clz = AbstractPlayer::class,
    method = "dispose"
)
object DisposePlayer {
    @JvmStatic
    fun Postfix(__instance: AbstractPlayer) {
        val skeleton = __instance.getPrivate<Skeleton?>("skeleton", clazz = AbstractCreature::class.java) ?: return

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
