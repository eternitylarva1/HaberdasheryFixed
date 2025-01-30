package haberdashery.ui

import basemod.BaseMod
import basemod.TopPanelItem
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.megacrit.cardcrawl.core.Settings
import com.megacrit.cardcrawl.dungeons.AbstractDungeon
import com.megacrit.cardcrawl.helpers.MathHelper
import com.megacrit.cardcrawl.helpers.TipHelper
import com.megacrit.cardcrawl.rooms.AbstractRoom
import haberdashery.HaberdasheryMod
import haberdashery.extensions.scale
import haberdashery.util.Assets
import haberdashery.util.L10nStrings

class CustomizeAttachmentsTopPanelItem : TopPanelItem(Assets.topPanelImg, ID) {
    private var targetAngle = angle

    override fun onClick() {
        if (CustomizeAttachmentsScreen.isOpen()) {
            AbstractDungeon.closeCurrentScreen()
        } else {
            BaseMod.openCustomScreen(CustomizeAttachmentsScreen.Enum.CUSTOMIZE_ATTACHMENTS)
        }
    }

    override fun update() {
        isClickable = AbstractDungeon.currMapNode?.getRoom()?.phase == AbstractRoom.RoomPhase.COMBAT
        super.update()
        angle = MathHelper.angleLerpSnap(angle, targetAngle)
    }

    override fun onHover() {
        if (isClickable) {
            targetAngle = 15f
            tint.a = 0.25f
        }
    }

    override fun onUnhover() {
        targetAngle = 0f
        tint.a = 0f
    }

    override fun render(sb: SpriteBatch) {
        super.render(sb)

        if (hitbox.hovered) {
            TipHelper.renderGenericTip(
                x - hb_w,
                TIP_Y_POS,
                strings["header"],
                strings["body"]
            )
        }
    }

    companion object {
        val ID = HaberdasheryMod.makeID("CustomizeAttachmentsTopPanelItem")
        private val TIP_Y_POS = Settings.HEIGHT - 120.scale()
        private val strings by lazy { L10nStrings(ID) }
    }
}
