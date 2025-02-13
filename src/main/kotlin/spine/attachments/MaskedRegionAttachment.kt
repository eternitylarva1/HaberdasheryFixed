package com.evacipated.cardcrawl.mod.haberdashery.spine.attachments

import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Affine2
import com.badlogic.gdx.math.Vector2
import com.esotericsoftware.spine.attachments.RegionAttachment

class MaskedRegionAttachment(name: String) : RegionAttachment(name) {
    private var mask: TextureRegion? = null
    var shearFactor: Vector2 = Vector2()

    private val affine = Affine2()
    private val tmpVector = Vector2()

    fun hasMask(): Boolean {
        return mask != null
    }

    fun getMask(): TextureRegion {
        mask?.let { return it }
            ?: throw IllegalStateException("Mask has not been set: $this")
    }

    fun setMask(mask: TextureRegion) {
        this.mask = mask
    }

    override fun updateOffset() {
        val width = width
        val height = height
        val localX2 = width / 2f
        val localY2 = height / 2f
        val localX = -localX2
        val localY = -localY2

        affine.idt()
        affine.rotate(rotation)
        affine.shear(shearFactor.x, shearFactor.y)
        affine.scale(scaleX, scaleY)
        affine.preTranslate(x, y)

        val offset = offset
        tmpVector.set(localX, localY)
        affine.applyTo(tmpVector)
        offset[0] = tmpVector.x
        offset[1] = tmpVector.y
        tmpVector.set(localX, localY2)
        affine.applyTo(tmpVector)
        offset[2] = tmpVector.x
        offset[3] = tmpVector.y
        tmpVector.set(localX2, localY2)
        affine.applyTo(tmpVector)
        offset[4] = tmpVector.x
        offset[5] = tmpVector.y
        tmpVector.set(localX2, localY)
        affine.applyTo(tmpVector)
        offset[6] = tmpVector.x
        offset[7] = tmpVector.y
    }
}
