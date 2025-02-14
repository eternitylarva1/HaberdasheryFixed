package com.evacipated.cardcrawl.mod.haberdashery.util

import com.badlogic.gdx.utils.GdxRuntimeException

internal object FileReadUtil {
    fun readTuple(line: String): Tuple {
        val colon = line.indexOf(':')
        if (colon == -1) {
            throw GdxRuntimeException("Invalid line: $line")
        } else {
            val tuple = Tuple(line.substring(0, colon))
            var lastMatch = colon + 1
            var i = 0

            while (i < 3) {
                val comma = line.indexOf(',', lastMatch)
                if (comma == -1) {
                    break
                }

                tuple.add(line.substring(lastMatch, comma).trim())
                lastMatch = comma + 1
                i++
            }
            tuple.add(line.substring(lastMatch).trim())
            return tuple
        }
    }

    data class Tuple(val name: String) {
        val data = Array(4) { "" }
        var size = 0
            private set

        fun add(str: String) {
            data[size] = str
            size++
        }
    }
}