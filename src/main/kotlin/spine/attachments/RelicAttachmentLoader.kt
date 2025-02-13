package com.evacipated.cardcrawl.mod.haberdashery.spine.attachments

import com.badlogic.gdx.graphics.Texture
import com.esotericsoftware.spine.Skin
import com.esotericsoftware.spine.attachments.*
import com.evacipated.cardcrawl.mod.haberdashery.extensions.asRegion

class RelicAttachmentLoader(
    private val tex: Texture
) : AttachmentLoader {
    override fun newRegionAttachment(skin: Skin, name: String, path: String): RegionAttachment? {
        return RegionAttachment(name).apply {
            region = tex.asRegion()
        }
    }

    override fun newMeshAttachment(skin: Skin, name: String, path: String): MeshAttachment? {
        return MeshAttachment(name).apply {
            region = tex.asRegion()
        }
    }

    override fun newBoundingBoxAttachment(skin: Skin, name: String): BoundingBoxAttachment? {
        return BoundingBoxAttachment(name)
    }

    override fun newPathAttachment(skin: Skin, name: String): PathAttachment? {
        return PathAttachment(name)
    }
}
