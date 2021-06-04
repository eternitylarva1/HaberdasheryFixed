package haberdashery.database

import com.megacrit.cardcrawl.characters.AbstractPlayer
import com.megacrit.cardcrawl.relics.CultistMask
import com.megacrit.cardcrawl.relics.RunicDome

object Watcher {
    fun initialize() {
        AttachDatabase
            .character(AbstractPlayer.PlayerClass.WATCHER)
            .relic(RunicDome.ID,
                AttachInfo("Head")
                    .drawOrder("ear")
                    .positionVector(-20f, 10f)
                    .rotation(-100f)
                    .scale(0.8f)
            )
            .relic(CultistMask.ID,
                AttachInfo("Head")
                    .hideSlots("ear", "hair_accessory copy", "hair_fg", "face")
                    .drawOrder("ear")
                    .positionVector(-90f, 10f)
                    .rotation(-78f)
                    .scale(0.9f)
            )
    }
}
