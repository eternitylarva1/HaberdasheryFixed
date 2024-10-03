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
import haberdashery.extensions.scale
import haberdashery.spine.attachments.MaskedRegionAttachment

object AttachRelic {
    fun receive(relic: AbstractRelic) {
        val player = AbstractDungeon.player ?: return
        val info = AttachDatabase.getInfo(player.chosenClass, relic.relicId) ?: return

        val relicSlotName = HaberdasheryMod.makeID(relic.relicId)
        val skeleton = player.getPrivate<Skeleton?>("skeleton", clazz = AbstractCreature::class.java) ?: return
        if (skeleton.findSlotIndex(relicSlotName) >= 0) {
            val slot = skeleton.findSlot(relicSlotName)
            if (slot.attachment == null) {
                skeleton.setAttachment(relicSlotName, relicSlotName)
                (slot.data as MySlotData).visible = true
            }
            hideSlots(skeleton, info.hideSlotNames)
            checkSlotUpdates(skeleton)
            return
        }

        val bone = skeleton.findBone(info.boneName) ?: return
        val slotClone = Slot(
            MySlotData(
                skeleton.slots.size,
                relicSlotName,
                bone.data,
                info.drawOrderZIndex,
                info.hideSlotNames,
                info.requiredSlotNames,
            ),
            bone
        )
        slotClone.data.blendMode = BlendMode.normal
        skeleton.slots.add(slotClone)

        val drawOrder = skeleton.drawOrder
        var insertIndex = startingDrawOrder(relic.relicId, info, drawOrder, bone)
        if (info.drawOrderSlotName == null) {
            info.drawOrder(drawOrder[insertIndex].data.name, info.drawOrderZIndex)
        }
        ++insertIndex
        for (i in insertIndex until drawOrder.size) {
            val slot = drawOrder[i]
            val data = slot.data
            if (data.name.startsWith(HaberdasheryMod.makeID("")) && data is MySlotData) {
                if (info.drawOrderZIndex >= data.zIndex) {
                    insertIndex = i + 1
                }
            } else {
                break
            }
        }
        drawOrder.insert(insertIndex, slotClone)
        skeleton.drawOrder = drawOrder

        val attachment = makeAttachment(relicSlotName, relic, skeleton, info)
        val skin = skeleton.data.defaultSkin
        skin.addAttachment(slotClone.data.index, attachment.name, attachment)

        skeleton.setAttachment(relicSlotName, attachment.name)
        hideSlots(skeleton, info.hideSlotNames)
        checkSlotUpdates(skeleton)
    }

    fun lose(relicId: String) {
        val player = AbstractDungeon.player ?: return
        val info = AttachDatabase.getInfo(player.chosenClass, relicId) ?: return

        val relicSlotName = HaberdasheryMod.makeID(relicId)
        val skeleton = player.getPrivate<Skeleton?>("skeleton", clazz = AbstractCreature::class.java) ?: return
        val slotIndex = skeleton.findSlotIndex(relicSlotName)
        if (slotIndex < 0) {
            return
        }

        skeleton.setAttachment(relicSlotName, null)
        (skeleton.findSlot(relicSlotName).data as MySlotData).visible = false

        val hidden = skeleton.slots
            .asSequence()
            .filter { it.attachment != null }
            .map { it.data }
            .filterIsInstance<MySlotData>()
            .flatMap { it.hideSlotNames.toList() }
            .toList()
        for (slotName in info.hideSlotNames) {
            if (slotName in hidden) continue
            val slot = skeleton.findSlot(slotName)
            skeleton.setAttachment(slotName, slot.data.attachmentName)
        }
        checkSlotUpdates(skeleton)
    }

    private fun makeAttachment(relicSlotName: String, relic: AbstractRelic, skeleton: Skeleton, info: AttachInfo): RegionAttachment {
        val skeletonStart = Skeleton(skeleton).apply {
            setToSetupPose()
            updateWorldTransform()
        }

        val tex = (if (info.large) {
            ImageMaster.loadImage("images/largeRelics/${relic.imgUrl}")
                ?.premultiplyAlpha(true)
        } else {
            null
        } ?: ImageMaster.getRelicImg(relic.relicId).premultiplyAlpha()
                ).asRegion()
        val maskRegion = AttachDatabase.getMaskImg(relic, info)

        return MaskedRegionAttachment(relicSlotName).apply {
            if (maskRegion != null) {
                setMask(maskRegion)
            } else {
                info.mask(null)
            }
            region = tex
            width = tex.regionWidth.toFloat()
            height = tex.regionHeight.toFloat()
            skeletonStart.findBone(info.boneName)?.let { bone ->
                val pos = info.position.cpy().rotate(bone.worldRotationX)
                x = pos.x.scale()
                y = pos.y.scale()
            }
            scaleX = info.scaleX.scale()
            scaleY = info.scaleY.scale()
            rotation = info.rotation
            updateOffset()
        }
    }

    private fun startingDrawOrder(relicId: String, info: AttachInfo, drawOrder: Array<Slot>, bone: Bone): Int {
        val ret = if (info.drawOrderSlotName != null) {
            drawOrder.indexOfFirst { it.data.name == info.drawOrderSlotName }
        } else {
            drawOrder.indexOfLast { it.bone == bone && it.data !is MySlotData }
        }
        if (ret < 0) {
            return 0
        }
        return ret
    }

    private fun hideSlots(skeleton: Skeleton, slotNames: kotlin.Array<out String>) {
        for (slotName in slotNames) {
            skeleton.setAttachment(slotName, null)
        }
    }

    private fun checkSlotUpdates(skeleton: Skeleton) {
        for (slot in skeleton.slots) {
            val data = slot.data
            if (data !is MySlotData) continue

            var hide = false
            for (slotName in data.requiredSlotNames) {
                if (skeleton.findSlot(slotName)?.attachment == null) {
                    hide = true
                    break
                }
            }
            if (hide) {
                skeleton.setAttachment(data.name, null)
            } else if (data.visible) {
                skeleton.setAttachment(data.name, data.name)
            }
        }
    }
}
