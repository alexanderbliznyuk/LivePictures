package com.blizniuk.livepictures.domain.graphics

sealed class ToolData {
    data class Pencil(
        val thicknessLevel: Float,
        val color: Int,
    ) : ToolData()

    data class Erase(
        val thicknessLevel: Float,
    ): ToolData()
}

enum class ToolId {
    Pencil, Erase, ShapePicker, ColorPicker
}