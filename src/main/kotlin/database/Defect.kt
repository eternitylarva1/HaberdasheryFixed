package haberdashery.database

import com.megacrit.cardcrawl.characters.AbstractPlayer
import com.megacrit.cardcrawl.relics.CultistMask
import com.megacrit.cardcrawl.relics.RunicDome

object Defect {
    fun initialize() {
        AttachDatabase
            .character(AbstractPlayer.PlayerClass.DEFECT)
            .relic(RunicDome.ID,
                AttachInfo("Neck_3")
                    .drawOrder("Eye_up")
                    .position(18f, 18f)
                    .rotation(-50f)
            )
            .relic(CultistMask.ID,
                AttachInfo("Neck_3")
                    .hideSlots("head", "Eye_up", "Eye_down")
                    .drawOrder("Eye_up")
                    .position(-33f, 18f)
                    .rotation(-15f)
            )
    }
}
