package haberdashery.database

import com.megacrit.cardcrawl.characters.AbstractPlayer
import com.megacrit.cardcrawl.relics.CultistMask
import com.megacrit.cardcrawl.relics.RunicDome

object Silent {
    fun initialize() {
        AttachDatabase
            .character(AbstractPlayer.PlayerClass.THE_SILENT)
            .relic(RunicDome.ID,
                AttachInfo("Skull")
                    .hideSlots("skull")
                    .positionVector(-110f, 8f)
                    .rotation(-55f)
            )
            .relic(CultistMask.ID,
                AttachInfo("Skull")
                    .hideSlots("skull", "hair", "hair2")
                    .positionVector(-100f, 18f)
                    .rotation(-40f)
            )
    }
}
