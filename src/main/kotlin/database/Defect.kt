package haberdashery.database

import com.megacrit.cardcrawl.characters.AbstractPlayer
import com.megacrit.cardcrawl.relics.RunicDome

object Defect {
    fun initialize() {
        AttachDatabase
            .character(AbstractPlayer.PlayerClass.DEFECT)
            .relic(
                RunicDome.ID,
                AttachInfo("Neck_3")
                    .drawOrder("Eye_up")
                    .position(18f, 18f)
                    .rotation(-50f)
            )
    }
}
