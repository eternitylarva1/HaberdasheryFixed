package haberdashery

import com.esotericsoftware.spine.BlendMode
import com.esotericsoftware.spine.Skeleton
import com.esotericsoftware.spine.Slot
import com.esotericsoftware.spine.SlotData
import com.esotericsoftware.spine.attachments.RegionAttachment
import com.megacrit.cardcrawl.core.AbstractCreature
import com.megacrit.cardcrawl.dungeons.AbstractDungeon
import com.megacrit.cardcrawl.helpers.ImageMaster
import com.megacrit.cardcrawl.relics.AbstractRelic
import haberdashery.database.AttachDatabase
import haberdashery.extensions.asRegion
import haberdashery.extensions.getPrivate
import haberdashery.extensions.premultiplyAlpha

object AttachRelic {
    fun receive(relic: AbstractRelic) {
        val player = AbstractDungeon.player ?: return
        val info = AttachDatabase.getInfo(player.chosenClass, relic.relicId) ?: return

        val skeleton = player.getPrivate<Skeleton?>("skeleton", clazz = AbstractCreature::class.java) ?: return
        if (skeleton.findSlotIndex(relic.relicId) >= 0) {
            return
        }

        val bone = skeleton.findBone(info.boneName) ?: return
        val slotClone = Slot(
            SlotData(
                skeleton.slots.size,
                relic.relicId,
                bone.data
            ),
            bone
        )
        slotClone.data.blendMode = BlendMode.normal
        skeleton.slots.add(slotClone)

        val drawOrder = skeleton.drawOrder
        val drawOrderSlot = info.drawOrderSlotName ?: drawOrder.lastOrNull { it.bone == bone }?.data?.name ?: TODO("ASDF")
        val insertIndex = drawOrder.indexOf(skeleton.findSlot(drawOrderSlot), true) + 1
        drawOrder.insert(insertIndex, slotClone)
        skeleton.drawOrder = drawOrder

        val tex = ImageMaster.getRelicImg(relic.relicId)
            .premultiplyAlpha()
            .asRegion()
        val attachment = RegionAttachment(relic.relicId).apply {
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

        skeleton.setAttachment(relic.relicId, attachment.name)
        for (slot in info.hideSlotNames) {
            skeleton.setAttachment(slot, null)
        }
    }
}
