package com.blizniuk.livepictures.domain.graphics.entity.cmd

import com.blizniuk.livepictures.domain.graphics.entity.Point
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed class DrawCmdData {
    open fun toDrawCmd(): DrawCmd {
        TODO()
    }
}






@Serializable
sealed class ShapeCmdData : DrawCmdData()

@Serializable
@SerialName("circle_shape")
data class CircleShapeCmdData(
    @SerialName("center") val center: Point,
    @SerialName("radius") val radius: Float,
    @SerialName("color") val color: Int,
    @SerialName("thickness_level") val thicknessLevel: Float,
) : ShapeCmdData()

@Serializable
@SerialName("rect_shape")
data class RectShapeCmdData(
    @SerialName("top_left") val topLeft: Point,
    @SerialName("bottom_right") val bottomRight: Point,
    @SerialName("color") val color: Int,
    @SerialName("thickness_level") val thicknessLevel: Float,
) : ShapeCmdData()








