package haberdashery.devcommands

import basemod.devcommands.ConsoleCommand

class HaberdasheryCommand : ConsoleCommand() {
    init {
        requiresPlayer = true
        followup["test"] = TestCommand::class.java
        followup["adjust"] = AdjustCommand::class.java
    }

    override fun execute(tokens: Array<out String>, depth: Int) {}
}
