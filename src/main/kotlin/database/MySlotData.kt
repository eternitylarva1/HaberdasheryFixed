package haberdashery.database

import com.esotericsoftware.spine.BoneData
import com.esotericsoftware.spine.SlotData

class MySlotData(
    index: Int,
    name: String,
    boneData: BoneData,
    var zIndex: Int
) : SlotData(index, name, boneData) {
}
