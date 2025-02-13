package com.evacipated.cardcrawl.mod.haberdashery.devcommands

import basemod.devcommands.ConsoleCommand
import com.evacipated.cardcrawl.mod.haberdashery.database.AttachDatabase

class SaveAllCommand : ConsoleCommand() {
    override fun execute(tokens: Array<out String>, depth: Int) {
        AttachDatabase.saveAll()
    }
}