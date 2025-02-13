package com.evacipated.cardcrawl.mod.haberdashery.extensions

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.PixmapTextureData
import com.badlogic.gdx.graphics.glutils.ShaderProgram

fun Texture.asAtlasRegion(): TextureAtlas.AtlasRegion =
    TextureAtlas.AtlasRegion(this, 0, 0, this.width, this.height)

fun Texture.asRegion(): TextureRegion =
    TextureRegion(this)

fun Pixmap.premultiplyAlpha() {
    val saveBlending = Pixmap.getBlending()
    Pixmap.setBlending(Pixmap.Blending.None)
    val color = Color()
    for (y in 0 until height) {
        for (x in 0 until width) {
            Color.rgba8888ToColor(color, getPixel(x, y))
            color.premultiplyAlpha()
            drawPixel(x, y, Color.rgba8888(color))
        }
    }
    Pixmap.setBlending(saveBlending)
}

fun Texture.premultiplyAlpha() {
    textureData.prepare()
    val pixmap = textureData.consumePixmap()
    pixmap.premultiplyAlpha()
    load(PixmapTextureData(pixmap, null, false, false, true))
}

fun Texture.asPremultiplyAlpha(disposeOriginal: Boolean = false): Texture {
    textureData.prepare()
    val pixmap = textureData.consumePixmap()
    pixmap.premultiplyAlpha()
    val newTexture = Texture(pixmap).apply {
        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
    }
    if (disposeOriginal) {
        dispose()
    }
    if (textureData.disposePixmap()) {
        pixmap.dispose()
    }
    return newTexture
}

fun TextureAtlas.premultiplyAlpha() {
    textures.forEach(Texture::premultiplyAlpha)
}

fun ShaderProgram.bind(name: String, unit: Int, texture: Texture) {
    texture.bind(unit)
    setUniformi(name, unit)
    Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0)
}
