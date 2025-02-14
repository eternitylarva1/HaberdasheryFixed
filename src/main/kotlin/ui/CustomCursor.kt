package com.evacipated.cardcrawl.mod.haberdashery.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.GdxRuntimeException
import com.badlogic.gdx.utils.StreamUtils
import com.evacipated.cardcrawl.mod.haberdashery.extensions.scale
import com.evacipated.cardcrawl.mod.haberdashery.util.FileReadUtil
import com.megacrit.cardcrawl.core.Settings
import com.megacrit.cardcrawl.helpers.ImageMaster
import java.io.BufferedReader
import java.io.InputStreamReader

class CustomCursor(cursorFile: FileHandle, imagesDir: FileHandle) {
    constructor(cursorFile: FileHandle) : this(cursorFile, cursorFile.parent())
    constructor(internalCursorFile: String) : this(Gdx.files.internal(internalCursorFile))

    private var tex: Texture? = null
    private var width = 0f
    private var height = 0f
    private val hotspot = Vector2()

    init {
        load(cursorFile, imagesDir)
    }

    private fun load(cursorFile: FileHandle, imagesDir: FileHandle) {
        val reader = BufferedReader(InputStreamReader(cursorFile.read()), 64)

        try {
            while (true) {
                val line = reader.readLine() ?: break

                if (line.trim().isEmpty()) {
                    continue
                }

                if (tex == null) {
                    val imageFile = imagesDir.child(line)
                    tex = Texture(imageFile).apply {
                        setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
                        this@CustomCursor.width = width.toFloat()
                        this@CustomCursor.height = height.toFloat()
                    }
                } else {
                    with(FileReadUtil.readTuple(line)) {
                        when (name) {
                            "size" -> if (size == 2) {
                                width = data[0].toInt().toFloat()
                                height = data[1].toInt().toFloat()
                            }
                            "hotspot" -> if (size == 2) {
                                hotspot.x = data[0].toInt().toFloat()
                                hotspot.y = data[1].toInt().toFloat()
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            throw GdxRuntimeException("Error reading cursor file: $cursorFile", e)
        } finally {
            StreamUtils.closeQuietly(reader)
        }
    }

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
