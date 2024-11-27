package haberdashery.vfx

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import com.megacrit.cardcrawl.vfx.AbstractGameEffect
import haberdashery.extensions.getPrivate
import haberdashery.extensions.scale
import haberdashery.util.Assets

class BagOfMarblesEffect(
    private val x: Float,
    private val y: Float,
) : AbstractGameEffect() {
    private val marbles = mutableListOf<MarbleEffect>()

    init {
        duration = 0.4f
        startingDuration = duration
        renderBehind = true
    }
    override fun update() {
        if (marbles.isEmpty()) {
            repeat(120) {
                marbles.add(MarbleEffect(x, y))
            }
        }

        var allDone = true
        marbles.forEach {
            if (!it.isDone) {
                it.update()
                if (!it.isDone) {
                    allDone = false
                }
            }
        }

        if (allDone) {
            marbles.forEach {
                it.getPrivate<Color>("color", clazz = AbstractGameEffect::class.java).a = duration / startingDuration
            }
            duration -= Gdx.graphics.deltaTime
            if (duration <= 0) {
                isDone = true
            }
        }
    }

    override fun render(sb: SpriteBatch) {
        marbles.forEach {
            it.render(sb)
        }
    }
    override fun dispose() {
        marbles.forEach(MarbleEffect::dispose)
    }

    private class MarbleEffect(
        private var x: Float,
        private var y: Float,
    ) : AbstractGameEffect() {
        private val img: AtlasRegion = Assets.vfx.findRegions("marble").random()
        private val v: Vector2 = Vector2(1f, 0f)
        private var speed: Float = MathUtils.randomTriangular(MIN_FORCE, MAX_FORCE, MODE_FORCE).scale()

        init {
            color = Color.WHITE.cpy()
            v.rotate(MathUtils.random(-2f, 6f))
        }

        override fun update() {
            val c = MathUtils.PI * img.packedWidth
            val n = speed / c
            rotation -= n * 360f

            x += v.x * speed
            y += v.y * speed
            speed -= FRICTION.scale()

            if (speed <= 0f) {
                isDone = true
            }
        }

        override fun render(sb: SpriteBatch) {
            sb.color = color
            sb.draw(
                img,
                x - img.packedWidth / 2f,
                y - img.packedHeight / 2f,
                img.packedWidth / 2f,
                img.packedHeight / 2f,
                img.packedWidth.toFloat(),
                img.packedHeight.toFloat(),
                scale,
                scale,
                rotation,
            )
        }

        override fun dispose() {}

        companion object {
            private const val FRICTION = 0.05f
            private const val MIN_FORCE = 1f
            private const val MAX_FORCE = 10f
            private const val MODE_FORCE = 9f
        }
    }
}
