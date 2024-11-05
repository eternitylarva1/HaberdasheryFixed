package haberdashery.extensions

import com.esotericsoftware.spine.Skeleton
import com.megacrit.cardcrawl.characters.AbstractPlayer
import com.megacrit.cardcrawl.core.AbstractCreature
import haberdashery.patches.PlayerFields
import haberdashery.patches.SubSkeleton
import haberdashery.util.SpireFieldDelegate

val AbstractPlayer.skeleton
    get() = this.getPrivate<Skeleton?>("skeleton", clazz = AbstractCreature::class.java)

val AbstractPlayer.subSkeletons
    by SpireFieldDelegate<AbstractPlayer, MutableMap<String, SubSkeleton>>(PlayerFields.subSkeletons)

val AbstractPlayer.chosenExclusions: MutableMap<String, String>
    by SpireFieldDelegate<AbstractPlayer, MutableMap<String, String>>(PlayerFields.chosenExclusions)
