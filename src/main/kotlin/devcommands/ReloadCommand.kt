package haberdashery.devcommands

import basemod.ReflectionHacks
import basemod.devcommands.ConsoleCommand
import com.badlogic.gdx.utils.ObjectMap
import com.esotericsoftware.spine.Skeleton
import com.esotericsoftware.spine.Skin
import com.esotericsoftware.spine.attachments.Attachment
import com.megacrit.cardcrawl.characters.AbstractPlayer
import com.megacrit.cardcrawl.core.AbstractCreature
import com.megacrit.cardcrawl.dungeons.AbstractDungeon
import haberdashery.AdjustRelic
import haberdashery.AttachRelic
import haberdashery.HaberdasheryMod
import haberdashery.database.AttachDatabase
import haberdashery.database.MySlotData
import haberdashery.extensions.getPrivate
import haberdashery.extensions.setPrivate
import haberdashery.patches.subSkeletons

class ReloadCommand : ConsoleCommand() {
    override fun execute(tokens: Array<out String>, depth: Int) {
        AttachDatabase.load()

        val player = AbstractDungeon.player ?: return
        val skeleton = player.skeleton ?: return

        skeleton.slots.removeAll { slot -> slot.data.name.startsWith(HaberdasheryMod.makeID("")) && slot.data is MySlotData }
        skeleton.drawOrder.removeAll { slot -> slot.data.name.startsWith(HaberdasheryMod.makeID("")) && slot.data is MySlotData }
        val attachments = ReflectionHacks.getPrivate<ObjectMap<Any, Attachment>>(skeleton.data.defaultSkin, Skin::class.java, "attachments")
        attachments.removeAll { it.value.name.startsWith(HaberdasheryMod.makeID("")) }
        player.subSkeletons.forEach { it.toDispose.dispose() }
        player.subSkeletons.clear()
        AttachRelic.onChange()
        player.relics.forEach { relic ->
            AttachRelic.receive(relic)
        }
        AdjustRelic.reload()
    }

    private var AbstractPlayer.skeleton
        get() = this.getPrivate<Skeleton?>("skeleton", clazz = AbstractCreature::class.java)
        set(value) = this.setPrivate("skeleton", clazz = AbstractCreature::class.java, value = value)
}
