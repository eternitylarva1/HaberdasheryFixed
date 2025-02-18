package com.evacipated.cardcrawl.mod.haberdashery.ui

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.evacipated.cardcrawl.mod.haberdashery.extensions.scale
import com.megacrit.cardcrawl.core.Settings
import com.megacrit.cardcrawl.helpers.ImageMaster

class CustomCursor(
    private val tex: Texture,
    private val width: Float,
    private val height: Float,
    private val hotspot: Vector2,
) {
    constructor(tex: Texture, width: Float, height: Float, hotspotX: Float, hotspotY: Float)
        : this(tex, width, height, Vector2(hotspotX, hotspotY))

    fun render(sb: SpriteBatch, x: Int, y: Int) {
        render(sb, x.toFloat(), y.toFloat())
    }

    fun render(sb: SpriteBatch, x: Float, y: Float) {
        sb.color = SHADOW_COLOR
        sb.draw(
            tex,
            x - hotspot.x - SHADOW_OFFSET_X,
            y + hotspot.y - height - SHADOW_OFFSET_Y,
            width,
            height,
        )
        sb.color = Color.WHITE
        sb.draw(
            tex,
            x - hotspot.x,
            y + hotspot.y - height,
            width,
            height,
        )

        if (Settings.isDebug) {
            sb.color = Color.RED
            sb.draw(
                ImageMaster.WHITE_SQUARE_IMG,
                x - 1, y - 1, 2f, 2f
            )
        }
    }

    companion object {
        private val SHADOW_COLOR by lazy { Color(0f, 0f, 0f, 0.15f) }
        private val SHADOW_OFFSET_X by lazy { (-10).scale() }
        private val SHADOW_OFFSET_Y by lazy { 8.scale() }
    }
}
