package haberdashery.database

import com.megacrit.cardcrawl.characters.AbstractPlayer
import com.megacrit.cardcrawl.relics.RunicDome

object Silent {
    fun initialize() {
        AttachDatabase
            .character(AbstractPlayer.PlayerClass.THE_SILENT)
            .relic(
                RunicDome.ID,
                AttachInfo("Skull")
                    .hideSlots("skull")
                    .position(-110f, 8f)
                    .rotation(-55f)
            )
    }
}
