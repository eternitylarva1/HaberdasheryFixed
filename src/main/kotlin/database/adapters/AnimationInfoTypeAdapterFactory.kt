package com.evacipated.cardcrawl.mod.haberdashery.database.adapters

import com.evacipated.cardcrawl.mod.haberdashery.database.AttachInfo
import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class AnimationInfoTypeAdapterFactory : TypeAdapterFactory {
    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        if (type.rawType != AttachInfo.AnimationInfo::class.java) {
            return null
        }

        val delegate = gson.getDelegateAdapter(this, type)

        return object : TypeAdapter<T>() {
            override fun write(output: JsonWriter, value: T?) {
                if (value is AttachInfo.AnimationInfo) {
                    if (value.speed == null && value.startTime == AttachInfo.StartType.DEFAULT) {
                        output.value(value.name)
                    } else {
                        delegate.write(output, value)
                    }
                } else {
                    output.nullValue()
                }
            }

            override fun read(input: JsonReader): T? {
                when (input.peek()) {
                    JsonToken.NULL -> {
                        input.nextNull()
                        return null
                    }
                    JsonToken.STRING -> {
                        return AttachInfo.AnimationInfo(input.nextString()) as T
                    }
                    else -> {
                        return delegate.read(input)
                    }
                }
            }
        }
    }
}
