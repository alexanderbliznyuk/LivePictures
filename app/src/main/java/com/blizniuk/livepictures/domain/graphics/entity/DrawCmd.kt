package com.blizniuk.livepictures.domain.graphics.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface DrawCmd {
    //fun render(canvas: Canvas, renderContext: RenderContext)
}

@Serializable
@SerialName("free_path")
data class FreePathCmd(
    @SerialName("points") val points: List<Point>,
    @SerialName("color") val color: Int,
    @SerialName("thickness_level") val thicknessLevel: Float,
) : DrawCmd

@Serializable
@SerialName("erase_path")
data class ErasePathCmd(
    @SerialName("points") val points: List<Point>,
    @SerialName("thickness_level") val thicknessLevel: Float,
) : DrawCmd


@Serializable
sealed interface ShapeCmd : DrawCmd

@Serializable
@SerialName("circle_shape")
data class CircleShapeCmd(
    @SerialName("center") val center: Point,
    @SerialName("radius") val radius: Float,
    @SerialName("color") val color: Int,
    @SerialName("thickness_level") val thicknessLevel: Float,
) : ShapeCmd

@Serializable
@SerialName("rect_shape")
data class RectShapeCmd(
    @SerialName("top_left") val topLeft: Point,
    @SerialName("bottom_right") val bottomRight: Point,
    @SerialName("color") val color: Int,
    @SerialName("thickness_level") val thicknessLevel: Float,
) : ShapeCmd








