package haberdashery.database

import com.megacrit.cardcrawl.characters.AbstractPlayer
import com.megacrit.cardcrawl.relics.*

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
            .relic(PenNib.ID,
                AttachInfo("Arm_R_3")
                    .position(77.5f, 140f)
                    .rotation(-67f)
                    .scaleY(-1f)
            )
            .relic(BronzeScales.ID,
                AttachInfo("Arm_L_1")
                    .position(125f, 13f)
                    .rotation(15f)
                    .scale(0.5f)
            )
            .relic(BagOfMarbles.ID,
                AttachInfo("root")
                    .drawOrder("shadow")
                    .position(0f, 140f)
                    .scaleX(-0.7f)
                    .scaleY(0.7f)
            )
            .relic(HappyFlower.ID,
                AttachInfo("root")
                    .drawOrder("shadow", 1)
                    .position(-90f, 5f)
                    .scale(0.8f)
            )
            .relic(AncientTeaSet.ID,
                AttachInfo("root")
                    .drawOrder("shadow", -1)
                    .position(135f, 40f)
                    .scale(0.9f)
            )
    }
}
