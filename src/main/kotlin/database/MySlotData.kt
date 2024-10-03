package haberdashery.database

import com.esotericsoftware.spine.BoneData
import com.esotericsoftware.spine.SlotData

class MySlotData(
    index: Int,
    name: String,
    boneData: BoneData,
    var zIndex: Int,
    val hideSlotNames: Array<out String>,
    val requiredSlotNames: Array<out String>,
) : SlotData(index, name, boneData) {
    var visible = true
}
