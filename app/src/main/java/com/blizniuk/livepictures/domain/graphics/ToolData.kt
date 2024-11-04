package com.blizniuk.livepictures.domain.graphics

sealed class ToolData {
    data class Pencil(
        val thicknessLevel: Float,
        val color: Int,
    ) : ToolData()

    data class Erase(
        val thicknessLevel: Float,
    ) : ToolData()

    data class Square(
        val thicknessLevel: Float,
        val color: Int,
        val filled: Boolean,
    ) : ToolData()

    data class Circle(
        val thicknessLevel: Float,
        val color: Int,
        val filled: Boolean,
    ) : ToolData()

    data class Triangle(
        val thicknessLevel: Float,
        val color: Int,
        val filled: Boolean,
    ) : ToolData()


    companion object {
        const val MinThickness = 2F
        const val MaxThickness = 20F
    }
}

enum class ToolId {
    Pencil, Erase, ShapeSquare, ShapeTriangle, ShapeCircle, ShapeSquareFilled, ShapeTriangleFilled, ShapeCircleFilled
}