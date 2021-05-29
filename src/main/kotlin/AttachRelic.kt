package haberdashery

import com.badlogic.gdx.utils.Array
import com.esotericsoftware.spine.BlendMode
import com.esotericsoftware.spine.Bone
import com.esotericsoftware.spine.Skeleton
import com.esotericsoftware.spine.Slot
import com.esotericsoftware.spine.attachments.RegionAttachment
import com.megacrit.cardcrawl.core.AbstractCreature
import com.megacrit.cardcrawl.dungeons.AbstractDungeon
import com.megacrit.cardcrawl.helpers.ImageMaster
import com.megacrit.cardcrawl.relics.AbstractRelic
import haberdashery.database.AttachDatabase
import haberdashery.database.AttachInfo
import haberdashery.database.MySlotData
import haberdashery.extensions.asRegion
import haberdashery.extensions.getPrivate
import haberdashery.extensions.premultiplyAlpha

object AttachRelic {
    fun receive(relic: AbstractRelic) {
        val player = AbstractDungeon.player ?: return
        val info = AttachDatabase.getInfo(player.chosenClass, relic.relicId) ?: return

        val relicSlotName = "${HaberdasheryMod.ID}:${relic.relicId}"
        val skeleton = player.getPrivate<Skeleton?>("skeleton", clazz = AbstractCreature::class.java) ?: return
        if (skeleton.findSlotIndex(relicSlotName) >= 0) {
            return
        }

        val bone = skeleton.findBone(info.boneName) ?: return
        val slotClone = Slot(
            MySlotData(
                skeleton.slots.size,
                relicSlotName,
                bone.data,
                info.drawOrderZIndex
            ),
            bone
        )
        slotClone.data.blendMode = BlendMode.normal
        skeleton.slots.add(slotClone)

        val drawOrder = skeleton.drawOrder
        var insertIndex = startingDrawOrder(relic.relicId, info, drawOrder, bone) + 1
        for (i in insertIndex until drawOrder.size) {
            val slot = drawOrder[i]
            val data = slot.data
            if (data.name.startsWith("${HaberdasheryMod.ID}:") && data is MySlotData) {
                if (info.drawOrderZIndex >= data.zIndex) {
                    insertIndex = i + 1
                }
            } else {
                break
            }
        }
        drawOrder.insert(insertIndex, slotClone)
        skeleton.drawOrder = drawOrder

        val tex = ImageMaster.getRelicImg(relic.relicId)
            .premultiplyAlpha()
            .asRegion()
        val attachment = RegionAttachment(relicSlotName).apply {
            region = tex
            width = tex.regionWidth.toFloat()
            height = tex.regionHeight.toFloat()
            x = info.position.x
            y = info.position.y
            scaleX = info.scaleX
            scaleY = info.scaleY
            rotation = info.rotation
            updateOffset()
        }

        val skin = skeleton.data.defaultSkin
        skin.addAttachment(slotClone.data.index, attachment.name, attachment)

        skeleton.setAttachment(relicSlotName, attachment.name)
        for (slot in info.hideSlotNames) {
            skeleton.setAttachment(slot, null)
        }
    }

    private fun startingDrawOrder(relicId: String, info: AttachInfo, drawOrder: Array<Slot>, bone: Bone): Int {
        val ret = if (info.drawOrderSlotName != null) {
            drawOrder.indexOfFirst { it.data.name == info.drawOrderSlotName }
        } else {
            drawOrder.indexOfLast { it.bone == bone }
        }
        if (ret < 0) {
            TODO("Cannot determine draw order for relic: ${relicId}")
        }
        return ret
    }
}
