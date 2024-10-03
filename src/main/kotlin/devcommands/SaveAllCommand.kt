package haberdashery.devcommands

import basemod.devcommands.ConsoleCommand
import haberdashery.database.AttachDatabase

class SaveAllCommand : ConsoleCommand() {
    override fun execute(tokens: Array<out String>, depth: Int) {
        AttachDatabase.saveAll()
    }
}