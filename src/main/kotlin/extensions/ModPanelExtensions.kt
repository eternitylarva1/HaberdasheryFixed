package haberdashery.extensions

import basemod.ModLabel
import basemod.ModLabeledToggleButton
import basemod.ModPanel
import basemod.ModToggleButton
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.megacrit.cardcrawl.core.CardCrawlGame
import com.megacrit.cardcrawl.core.Settings
import com.megacrit.cardcrawl.helpers.FontHelper
import com.megacrit.cardcrawl.helpers.Hitbox
import haberdashery.ui.config.ModCenteredLabel
import kotlin.math.max
import kotlin.reflect.*
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible
import kotlin.reflect.jvm.jvmErasure

fun panel(block: PanelConfig.() -> Unit): ModPanel {
    val config = PanelConfig(ModPanel())
    block.invoke(config)
    return config.panel
}

class PanelConfig(val panel: ModPanel) {
    private var strings: Map<String, String> = emptyMap()
    var x = 380f
    var y = Settings.OPTION_Y / Settings.yScale + 242f
    var spacing = 0f

    fun loadStrings(id: String) {
        val uiStrings = CardCrawlGame.languagePack.getUIString(id)
        strings = uiStrings?.TEXT_DICT ?: emptyMap()
    }

    fun <T : Any> fromConfig(config: T) {
        config::class.declaredMemberProperties.forEach { prop ->
            prop.isAccessible = true
            if (prop.visibility == KVisibility.PUBLIC) {
                when (prop.returnType.jvmErasure) {
                    Boolean::class -> toggle {
                        configOption = (prop as KMutableProperty1<T, Boolean>).bind(config)
                    }
                }
            }
        }
    }

    fun indent(size: Float = 16f, block: PanelConfig.() -> Unit) {
        x += size
        block.invoke(this)
        x -= size
    }

    fun title(block: LabelConfig.() -> Unit = {}) {
        val saveX = x
        val saveY = y
        label {
            font = FontHelper.buttonLabelFont
            color = Settings.CREAM_COLOR
            x = Settings.WIDTH / Settings.xScale / 2f
            y = Settings.OPTION_Y / Settings.yScale + 321f
            center = true
            block.invoke(this)
        }
        x = saveX
        y = saveY
    }

    fun h1(block: LabelConfig.() -> Unit = {}) = label {
        font = FontHelper.panelNameFont
        color = Settings.GOLD_COLOR
        block.invoke(this)
    }

    fun h2(block: LabelConfig.() -> Unit = {}) = label {
        font = FontHelper.charDescFont
        block.invoke(this)
    }

    fun label(block: LabelConfig.() -> Unit = {}) {
        LabelConfig().let {
            block.invoke(it)
            val labelCtor = if (it.center) ::ModCenteredLabel else ::ModLabel
            labelCtor(
                it.getText(strings),
                x,
                y,
                it.color,
                it.font,
                panel,
                it.onUpdate,
            ).apply { panel.addUIElement(this) }
            y -= it.font.lineHeight + spacing
        }
    }

    fun toggle(block: ToggleConfig.() -> Unit = {}) {
        ToggleConfig().let {
            block.invoke(it)
            val toggle = ModLabeledToggleButton(
                it.getText(strings),
                it.tooltip ?: strings["${it.textID}_tooltip"],
                x,
                y,
                it.color,
                it.font,
                it.enabled,
                panel,
                it.onUpdate,
            ) { button ->
                button.enabled = it.onToggle.invoke(button.enabled)
                it.configOption?.set(button.enabled)
            }.apply {
                panel.addUIElement(this)
            }
            val hb = toggle.toggle.getPrivate<Hitbox>("hb", ModToggleButton::class.java)
            y -= max(hb.height, it.font.lineHeight) + spacing
        }
    }
}

open class LabelConfig {
    var textID: String? = null
    var text: String? = null
    var color: Color = Settings.CREAM_COLOR
    var font: BitmapFont = FontHelper.tipBodyFont
    var onUpdate: (ModLabel) -> Unit = {}
    var center: Boolean = false

    fun getText(strings: Map<String, String>): String =
        text ?: strings[textID] ?: textID?.let { "[MISSING:$it]" } ?: "[MISSING]"
}

open class ToggleConfig : LabelConfig() {
    var tooltip: String? = null
    var enabled: Boolean = false
    var onToggle: (Boolean) -> Boolean = { it }
        private set

    var configOption: KMutableProperty0<Boolean>? = null
        set(value) {
            textID = value?.name
            enabled = value?.get() ?: enabled
            field = value
        }

    fun onToggle(block: (Boolean) -> Boolean) {
        onToggle = block
    }
}

private fun <T, V> KMutableProperty1<T, V>.bind(receiver: T): KMutableProperty0<V> {
    return object : KMutableProperty0<V>, KMutableProperty<V> by this {
        override val getter: KProperty0.Getter<V> =
            object : KProperty0.Getter<V>, KProperty.Getter<V> by this@bind.getter,
                    () -> V by { this@bind.getter(receiver) } {}

        override val setter: KMutableProperty0.Setter<V> =
            object : KMutableProperty0.Setter<V>, KMutableProperty.Setter<V> by this@bind.setter,
                    (V) -> Unit by { value -> this@bind.setter(receiver, value) } {}

        override fun get(): V = this@bind.get(receiver)
        override fun set(value: V) = this@bind.set(receiver, value)
        override fun getDelegate(): Any? = this@bind.getDelegate(receiver)
        override fun invoke(): V = this@bind.invoke(receiver)
    }
}
