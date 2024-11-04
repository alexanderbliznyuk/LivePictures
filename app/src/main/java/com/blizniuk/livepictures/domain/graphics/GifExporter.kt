package com.blizniuk.livepictures.domain.graphics

interface GifExporter {
    suspend fun export(width: Int, height: Int): String?
}