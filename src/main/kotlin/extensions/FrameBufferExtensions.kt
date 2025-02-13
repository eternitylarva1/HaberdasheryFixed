package com.evacipated.cardcrawl.mod.haberdashery.extensions

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.glutils.FrameBuffer

inline fun FrameBuffer.scope(block: () -> Unit): FrameBuffer {
    begin()
    block.invoke()
    end()
    return this
}

fun FrameBuffer.clear(r: Float, g: Float, b: Float, a: Float): FrameBuffer {
    return scope {
        Gdx.gl.glClearColor(r, g, b, a)
        var bufferBit = GL20.GL_COLOR_BUFFER_BIT
        if (depthBufferHandle != 0) {
            bufferBit = bufferBit or GL20.GL_DEPTH_BUFFER_BIT
        }
        if (stencilBufferHandle != 0) {
            bufferBit = bufferBit or GL20.GL_STENCIL_BUFFER_BIT
        }
        Gdx.gl.glClear(bufferBit)
    }
}
