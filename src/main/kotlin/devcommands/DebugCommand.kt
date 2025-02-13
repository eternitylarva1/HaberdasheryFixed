package com.evacipated.cardcrawl.mod.haberdashery.devcommands

import basemod.devcommands.ConsoleCommand
import com.evacipated.cardcrawl.mod.haberdashery.AdjustRelic

class DebugCommand : ConsoleCommand() {
    override fun execute(tokens: Array<out String>, depth: Int) {
        AdjustRelic.DEBUG_RENDER_SKELETON = !AdjustRelic.DEBUG_RENDER_SKELETON
    }
}