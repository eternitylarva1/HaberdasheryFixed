package haberdashery.devcommands

import basemod.devcommands.ConsoleCommand
import haberdashery.database.AttachDatabase
import java.util.ArrayList

class TestCommand : ConsoleCommand() {
    init {
        requiresPlayer = true
        minExtraTokens = 0
        maxExtraTokens = 1
    }

    override fun execute(tokens: Array<out String>, depth: Int) {
        val type = if (tokens.size < 3) {
            "all"
        } else {
            tokens[depth]
        }
        AttachDatabase.test(type)
    }

    override fun extraOptions(tokens: Array<out String>, depth: Int): ArrayList<String> {
        return arrayListOf("all", "character", "shared")
    }
}