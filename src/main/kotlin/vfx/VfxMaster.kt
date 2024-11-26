package haberdashery.vfx

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import haberdashery.HaberdasheryMod

object VfxMaster {
    val atlas by lazy { TextureAtlas(Gdx.files.internal(HaberdasheryMod.assetPath("images/vfx/vfx.atlas"))) }
}
