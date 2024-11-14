package haberdashery.ui.config

import basemod.ModLabel
import basemod.ModPanel
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.megacrit.cardcrawl.helpers.FontHelper
import java.util.function.Consumer

class ModCenteredLabel(
    labelText: String,
    xPos: Float,
    yPos: Float,
    color: Color,
    font: BitmapFont,
    p: ModPanel,
    updateFunc: Consumer<ModLabel>
) : ModLabel(labelText, xPos, yPos, color, font, p, updateFunc) {
    override fun render(sb: SpriteBatch) {
        FontHelper.renderFontCentered(sb, font, text, x, y, color)
    }
}
