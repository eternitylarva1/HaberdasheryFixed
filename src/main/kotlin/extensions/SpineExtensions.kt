package com.evacipated.cardcrawl.mod.haberdashery.extensions

import com.esotericsoftware.spine.BoneData
import com.evacipated.cardcrawl.mod.haberdashery.patches.BoneNoFlip
import com.evacipated.cardcrawl.mod.haberdashery.util.SpireFieldDelegate

var BoneData.inheritFlip
        by SpireFieldDelegate<BoneData, Boolean>(BoneNoFlip.Fields.inheritFlip)
