package haberdashery.extensions

import com.esotericsoftware.spine.BoneData
import haberdashery.patches.BoneNoFlip
import haberdashery.util.SpireFieldDelegate

var BoneData.inheritFlip
        by SpireFieldDelegate<BoneData, Boolean>(BoneNoFlip.Fields.inheritFlip)
