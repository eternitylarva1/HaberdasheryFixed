package haberdashery.spine.attachments

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.esotericsoftware.spine.attachments.RegionAttachment

class MaskedRegionAttachment(name: String) : RegionAttachment(name) {
    private var mask: TextureRegion? = null

    fun getMask(): TextureRegion {
        mask?.let { return it }
            ?: throw IllegalStateException("Mask has not been set: $this")
    }

    fun setMask(mask: TextureRegion) {
        this.mask = mask
    }
}
