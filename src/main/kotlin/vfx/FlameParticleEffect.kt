package haberdashery.vfx

import com.badlogic.gdx.math.MathUtils
import com.megacrit.cardcrawl.vfx.scene.TorchParticleSEffect
import haberdashery.extensions.scale
import haberdashery.extensions.setPrivate

class FlameParticleEffect(x: Float, y: Float, behind: Boolean = false) : TorchParticleSEffect(setNotGreen(x), y) {
    init {
        val vY = MathUtils.random(7f, 40f).scale()
        setPrivate("vY", vY, clazz = TorchParticleSEffect::class.java)
        renderBehind = behind
    }

    companion object {
        private fun setNotGreen(x: Float): Float {
            renderGreen = false
            return x
        }
    }
}
