package com.evacipated.cardcrawl.mod.haberdashery.extensions

import com.esotericsoftware.spine.Skeleton
import com.evacipated.cardcrawl.mod.haberdashery.patches.PlayerFields
import com.evacipated.cardcrawl.mod.haberdashery.patches.SubSkeleton
import com.evacipated.cardcrawl.mod.haberdashery.util.SpireFieldDelegate
import com.megacrit.cardcrawl.characters.AbstractPlayer
import com.megacrit.cardcrawl.core.AbstractCreature

val AbstractPlayer.skeleton
    get() = this.getPrivate<Skeleton?>("skeleton", clazz = AbstractCreature::class.java)

val AbstractPlayer.subSkeletons
    by SpireFieldDelegate<AbstractPlayer, MutableMap<String, SubSkeleton>>(PlayerFields.subSkeletons)

val AbstractPlayer.chosenExclusions: MutableMap<String, String>
    by SpireFieldDelegate<AbstractPlayer, MutableMap<String, String>>(PlayerFields.chosenExclusions)
