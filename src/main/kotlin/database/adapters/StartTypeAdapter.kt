package com.evacipated.cardcrawl.mod.haberdashery.database.adapters

import com.evacipated.cardcrawl.mod.haberdashery.database.AttachInfo
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class StartTypeAdapter : TypeAdapter<AttachInfo.StartType>() {
    override fun read(input: JsonReader): AttachInfo.StartType? {
        if (input.peek() == JsonToken.NULL) {
            input.nextNull()
            return AttachInfo.StartType.DEFAULT
        } else {
            return AttachInfo.StartType.valueOf(input.nextString())
        }
    }

    override fun write(output: JsonWriter, value: AttachInfo.StartType?) {
        output.value(
            when (value) {
                null, AttachInfo.StartType.DEFAULT -> null
                else -> value.name
            }
        )
    }
}
