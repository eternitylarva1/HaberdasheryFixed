package haberdashery.patches

import com.evacipated.cardcrawl.modthespire.lib.SpirePatch2
import com.evacipated.cardcrawl.modthespire.lib.SpireRawPatch
import com.megacrit.cardcrawl.characters.AbstractPlayer
import javassist.CodeConverter
import javassist.CtBehavior
import javassist.CtClass
import javassist.bytecode.*
import javassist.convert.Transformer

// Moves the bytecode for rendering the player's hp bar and orbs to the end of the render method
// so that the UI renders on top of the player model
@SpirePatch2(
    clz = AbstractPlayer::class,
    method = "render"
)
object RenderUnderHPBar {
    @JvmStatic
    @SpireRawPatch
    fun moveBytecode(ctBehavior: CtBehavior) {
        val code = ctBehavior.methodInfo.codeAttribute
        val lna = code.getAttribute(LineNumberAttribute.tag) as? LineNumberAttribute
        if (lna != null) {
            // Range of line numbers corresponding to the bytecode we want to move
            val start = lna.toStartPc(2109)
            val end = lna.toStartPc(2120)
            var ret = -1

            val codeConverter = object : CodeConverter() {
                init {
                    transformers = object : Transformer(transformers) {
                        lateinit var storage: ByteArray

                        override fun initialize(cp: ConstPool?, attr: CodeAttribute) {
                            super.initialize(cp, attr)
                            storage = attr.code.copyOfRange(start, end)
                        }

                        override fun transform(clazz: CtClass?, pos: Int, iterator: CodeIterator, cp: ConstPool?): Int {
                            // If bytecode is in our desired range, NOP it
                            if (pos in start until end) {
                                for (i in pos until iterator.lookAhead()) {
                                    iterator.writeByte(Opcode.NOP, i)
                                }
                            }
                            // Insert the saved bytecode at the end of the method
                            if (!iterator.hasNext() && iterator.byteAt(pos) == Opcode.RETURN) {
                                ret = iterator.insertAt(pos, storage)
                            }
                            return pos
                        }
                    }
                }
            }
            ctBehavior.instrument(codeConverter)

            // Alter the line number table to match the new bytecode positions
            // We do this so any patches looking for these line numbers still work correctly
            val info = lna.get()
            for (i in 0 until lna.tableLength()) {
                val pc = lna.startPc(i)
                if (pc == ret) {
                    javassist.bytecode.ByteArray.write16bit(ret + (end - start), info, i * 4 + 2)
                } else if (pc in start until end) {
                    javassist.bytecode.ByteArray.write16bit(ret + (pc - start), info, i * 4 + 2)
                }
            }
        }
    }
}
