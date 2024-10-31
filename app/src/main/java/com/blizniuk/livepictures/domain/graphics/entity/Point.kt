package com.blizniuk.livepictures.domain.graphics.entity

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.math.roundToInt

@Serializable
data class Point(
    @SerialName("x")
    @Serializable(with = FastFloatSerializer::class)
    val x: Float,

    @SerialName("y")
    @Serializable(with = FastFloatSerializer::class)
    val y: Float,
)


class FastFloatSerializer : KSerializer<Float> {
    override val descriptor = PrimitiveSerialDescriptor("fast_float", PrimitiveKind.FLOAT)

    override fun deserialize(decoder: Decoder): Float {
        return decoder.decodeInt() / 10F
    }

    override fun serialize(encoder: Encoder, value: Float) {
        encoder.encodeInt((value * 10).roundToInt())
    }
}