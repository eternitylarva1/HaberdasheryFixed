package haberdashery.util

import com.badlogic.gdx.graphics.g2d.TextureAtlas
import haberdashery.HaberdasheryMod

object Assets {
    val vfx by lazy { AssetLoader.get<TextureAtlas>(VFX_PATH)!! }

    internal fun preload() {
        AssetLoader.preload<TextureAtlas>(VFX_PATH)
    }

    private val VFX_PATH = HaberdasheryMod.assetPath("images/vfx/vfx.atlas")
}
