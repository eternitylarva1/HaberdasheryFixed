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
            .relic(CultistMask.ID,
                AttachInfo("Head")
                    .hideSlots("helmet", "eye", "hair")
                    .drawOrder("eye")
                    .position(-77f, 11f)
                    .rotation(-61f)
                    .scale(0.95f)
            )
            .relic(SmilingMask.ID,
                AttachInfo("Head")
                    .drawOrder("eye", 1)
                    .position(20f, 16f)
                    .rotation(-90f)
                    .scale(0.6f)
            )
            .relic(GremlinHorn.ID,
                AttachInfo("Head")
                    .drawOrder("shadow")
                    .position(-13f, 25f)
                    .rotation(-80f)
                    .scale(0.7f)
            )
            .relic(PenNib.ID,
                AttachInfo("Arm_R_3")
                    .position(77.5f, 140f)
                    .rotation(-67f)
                    .scaleY(-1f)
            )
            .relic(Courier.ID,
                AttachInfo("Arm_R_1")
                    .position(-120f, 22f)
                    .rotation(14f)
                    .scaleX(0.6f)
                    .scaleY(-0.6f)
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
            .relic(TinyChest.ID,
                AttachInfo("root")
                    .drawOrder("boot_left")
                    .position(176f, 39f)
                    .rotation(5f)
                    .scaleX(-0.4f)
                    .scaleY(0.4f)
            )
            .relic(WarPaint.ID,
                AttachInfo("root")
                    .drawOrder("shadow", 1)
                    .position(164f, 110f)
                    .scale(0.7f)
            )
            .relic(Whetstone.ID,
                AttachInfo("root")
                    .drawOrder("shadow", 2)
                    .position(173f, 90f)
                    .scale(0.6f)
            )
            .relic(Lantern.ID,
                AttachInfo("Hips")
                    .drawOrder("pants")
                    .position(15f, 32f)
                    .rotation(40f)
                    .scaleX(-0.5f)
                    .scaleY(0.5f)
            )
            .relic(CentennialPuzzle.ID,
                AttachInfo("Neck_1")
                    .drawOrder("pauldron_right")
                    .position(-150f, 10f)
                    .rotation(-100f)
                    .scaleX(-0.4f)
                    .scaleY(0.4f)
            )
            .relic(Anchor.ID,
                AttachInfo("Leg_R_2")
                    .drawOrder("shirt", 1)
                    .position(135f, 6f)
                    .rotation(-70f)
                    .scale(0.6f)
            )
            .relic(HornCleat.ID,
                AttachInfo("Leg_R_")
                    .drawOrder("shirt", 0)
                    .position(-40f, 28f)
                    .rotation(180f)
                    .scale(0.55f)
            )
            .relic(CaptainsWheel.ID,
                AttachInfo("Hips")
                    .drawOrder("shirt", 0)
                    .position(0f, 3f)
                    .rotation(-35f)
                    .scaleX(-0.6f)
                    .scaleY(0.6f)
            )
            .relic(MealTicket.ID,
                AttachInfo("Hips")
                    .drawOrder("shadow", 5)
                    .position(140f, 35f)
                    .scaleX(-0.5f)
                    .scaleY(0.5f)
            )
    }
}
