package haberdashery.database

import com.megacrit.cardcrawl.characters.AbstractPlayer
import com.megacrit.cardcrawl.relics.RunicDome

object Watcher {
    fun initialize() {
        AttachDatabase
            .character(AbstractPlayer.PlayerClass.WATCHER)
            .relic(
                RunicDome.ID,
                AttachInfo("Head")
                    .drawOrder("ear")
                    .position(-20f, 10f)
                    .rotation(-100f)
                    .scale(0.8f)
            )
    }
}
