package haberdashery.vfx

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.megacrit.cardcrawl.helpers.ImageMaster
import com.megacrit.cardcrawl.vfx.AbstractGameEffect
import haberdashery.extensions.scale

class SmokePuffEffect(
    private var x: Float,
    private var y: Float,
) : AbstractGameEffect() {
    private val startAlpha: Float
    private val aV: Float
    private val vY: Float
    private val targetScale: Float
    private val img: AtlasRegion

    init {
        if (MathUtils.randomBoolean()) {
            img = ImageMaster.EXHAUST_L
            targetScale = MathUtils.random(0.2f, 0.3f)
        } else {
            img = ImageMaster.EXHAUST_S
            targetScale = MathUtils.random(0.2f, 0.5f)
        }
        startAlpha = MathUtils.random(0.55f, 0.7f)
        color = Color(1f, 1f, 1f, startAlpha)
        duration = MathUtils.random(2f, 2.5f)
        startingDuration = duration
        scale = 0.01f
        rotation = MathUtils.random(360f)
        aV = MathUtils.random(50f, 150f) * if (MathUtils.randomBoolean()) 1 else -1
        vY = MathUtils.random(0.5f.scale(), 1.scale())
    }

    override fun update() {
        duration -= Gdx.graphics.deltaTime
        if (duration < 0) {
            isDone = true
        }
        y += vY
        rotation += aV * Gdx.graphics.deltaTime
        scale = Interpolation.exp10Out.apply(0.01f, targetScale, 1f - duration / startingDuration)
        if (duration < 0.33f) {
            color.a = startAlpha * duration * 3f
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
}
