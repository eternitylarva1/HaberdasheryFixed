package haberdashery.devcommands

import basemod.devcommands.ConsoleCommand

class HaberdasheryCommand : ConsoleCommand() {
    init {
        requiresPlayer = false
        followup["test"] = TestCommand::class.java
        followup["edit"] = EditCommand::class.java
        followup["add"] = AddCommand::class.java
        followup["saveall"] = SaveAllCommand::class.java
        followup["reload"] = ReloadCommand::class.java
        followup["debug"] = DebugCommand::class.java
    }

    override fun execute(tokens: Array<out String>, depth: Int) {}
}
