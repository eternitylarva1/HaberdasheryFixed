package haberdashery

import basemod.devcommands.ConsoleCommand
import haberdashery.database.AttachDatabase

class TestCommand : ConsoleCommand() {
    init {
        requiresPlayer = true
    }

    override fun execute(tokens: Array<out String>, depth: Int) {
        AttachDatabase.test()
    }
}
