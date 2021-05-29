package haberdashery.database

import com.megacrit.cardcrawl.characters.AbstractPlayer
import com.megacrit.cardcrawl.relics.RunicDome

object Ironclad {
    fun initialize() {
        AttachDatabase
            .character(AbstractPlayer.PlayerClass.IRONCLAD)
            .relic(RunicDome.ID,
                AttachInfo("Head")
                    .hideSlots("helmet")
                    .drawOrder("eye")
                    .position(5f, 6f)
                    .rotation(-85f)
                    .scale(0.93f)
            )
    }
}
