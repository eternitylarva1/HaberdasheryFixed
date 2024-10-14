package haberdashery.devcommands

import basemod.devcommands.ConsoleCommand
import haberdashery.AdjustRelic

class DebugCommand : ConsoleCommand() {
    init {
        requiresPlayer = true
    }

    override fun execute(tokens: Array<out String>, depth: Int) {
        AdjustRelic.DEBUG_RENDER_SKELETON = !AdjustRelic.DEBUG_RENDER_SKELETON
    }
}