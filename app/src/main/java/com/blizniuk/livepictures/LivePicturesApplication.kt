package com.blizniuk.livepictures

import android.app.Application
import android.graphics.Color
import com.blizniuk.livepictures.domain.graphics.entity.CircleShapeCmd
import com.blizniuk.livepictures.domain.graphics.entity.DrawCmd
import com.blizniuk.livepictures.domain.graphics.entity.ErasePathCmd
import com.blizniuk.livepictures.domain.graphics.entity.FreePathCmd
import com.blizniuk.livepictures.domain.graphics.entity.Point
import com.blizniuk.livepictures.domain.graphics.entity.RectShapeCmd
import dagger.hilt.android.HiltAndroidApp
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random

@HiltAndroidApp
class LivePicturesApplication : Application()


fun main() {
    val json = Json {
        classDiscriminator = "type"
        prettyPrint = true
    }

    val rnd = Random(seed = 123)
    val points = listOf(
        Point(1F, 0F),
        Point(2F, 3F),
        Point(4F, 5F),
    )

    val testEntities = buildList {
        this += FreePathCmd(
            points = points,
            color = Color.BLUE,
            thicknessLevel = 2F
        )

        this += ErasePathCmd(
            points = points,
            thicknessLevel = 6F
        )

        this += FreePathCmd(
            points = points,
            color = Color.BLUE,
            thicknessLevel = 1F
        )

        this += RectShapeCmd(
            topLeft = Point(1F, 4F),
            bottomRight = Point(2F, 3F),
            color = Color.RED,
            thicknessLevel = 2F
        )

        this += CircleShapeCmd(
            center = Point(1F, 4F),
            radius = 10F,
            color = Color.YELLOW,
            thicknessLevel = 2F
        )
    }

    val result = json.encodeToString(testEntities)
    val parsed = json.decodeFromString<List<DrawCmd>>(result)

    println(result)
    println("=========")
    println(parsed)
    println("=========")
    println(testEntities == parsed)
}

