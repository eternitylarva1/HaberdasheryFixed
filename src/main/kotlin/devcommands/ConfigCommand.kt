package com.evacipated.cardcrawl.mod.haberdashery.devcommands

import basemod.DevConsole
import basemod.devcommands.ConsoleCommand
import com.evacipated.cardcrawl.mod.haberdashery.Config

class ConfigCommand : ConsoleCommand() {
    init {
        requiresPlayer = false
        minExtraTokens = 2
        maxExtraTokens = 2
        simpleCheck = true
    }

    override fun execute(tokens: Array<out String>, depth: Int) {
        if (tokens.size < depth+2) return

        val name = tokens[depth]
        val value = tokens[depth+1]
        val err = Config.setFromCommand(name, value)
        if (err != null) {
            DevConsole.log("Error: $err")
        } else {
            DevConsole.log("Set $name=$value")
        }
    }

    override fun extraOptions(tokens: Array<out String>, depth: Int): ArrayList<String> {
        val autocomplete = Config.autocompleteInfo()
        return when (tokens.size) {
            3 -> ArrayList(autocomplete.map { it.first })
            4 -> {
                val type = autocomplete.firstOrNull { it.first == tokens[2] }?.second
                when (type) {
                    Boolean::class -> arrayListOf("true", "false")
                    else -> arrayListOf()
                }
            }
            else -> arrayListOf()
        }
    }
}
