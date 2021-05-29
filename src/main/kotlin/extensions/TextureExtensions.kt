package haberdashery.extensions

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion

fun Texture.asAtlasRegion(): TextureAtlas.AtlasRegion =
    TextureAtlas.AtlasRegion(this, 0, 0, this.width, this.height)

fun Texture.asRegion(): TextureRegion =
    TextureRegion(this)

fun Texture.premultiplyAlpha(): Texture {
    textureData.prepare()
    val pixmap = textureData.consumePixmap()
    val saveBlending = Pixmap.getBlending()
    Pixmap.setBlending(Pixmap.Blending.None)
    val color = Color()
    for (y in 0 until pixmap.height) {
        for (x in 0 until pixmap.width) {
            Color.rgba8888ToColor(color, pixmap.getPixel(x, y))
            color.premultiplyAlpha()
            pixmap.drawPixel(x, y, Color.rgba8888(color))
        }
    }
    val newTexture = Texture(pixmap).apply {
        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    }
    pixmap.dispose()
    Pixmap.setBlending(saveBlending)
    return newTexture
}
