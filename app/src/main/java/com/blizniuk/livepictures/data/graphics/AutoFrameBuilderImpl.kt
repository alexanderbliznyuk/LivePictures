package com.blizniuk.livepictures.data.graphics

import android.graphics.Color
import android.graphics.PointF
import androidx.core.graphics.ColorUtils
import com.blizniuk.livepictures.domain.graphics.AutoFrameBuilder
import com.blizniuk.livepictures.domain.graphics.entity.Point
import com.blizniuk.livepictures.domain.graphics.entity.cmd.CircleShapeCmdData
import com.blizniuk.livepictures.domain.graphics.entity.cmd.DrawCmdData
import com.blizniuk.livepictures.domain.graphics.entity.cmd.SquareShapeCmdData
import com.blizniuk.livepictures.domain.graphics.entity.cmd.TriangleShapeCmdData
import com.blizniuk.livepictures.util.GeomUtils
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min

class AutoFrameBuilderImpl(
    private val framesRepository: FramesRepositoryImpl,
    private val canvasWidth: Float,
    private val canvasHeight: Float,
    private val count: Int,
) : AutoFrameBuilder {
    private val canvasCenter = Point(canvasWidth / 2, canvasHeight / 2)

    override suspend fun build() {
        var index = 0
        var lastIndex = framesRepository.getLastIndex()
        val lastFrame = framesRepository.getLastFrame()


        val frames = mutableListOf<FrameDb>()
        val cmds = mutableListOf<DrawCmdData>()

        var time = 0
        while (index < count) {
            cmds.clear()
            cmds += sky(getSkyColor(time))
            cmds += grass

            if (time in 5..19) {
                cmds.addAll(stars)
            }

            cmds += houseRoof
            cmds += houseBody
            cmds.addAll(houseWindow)

            cmds += if (time !in 6..18) {
                sun(time)
            } else {
                moon(time)
            }

            time++
            time %= 24

            frames += FrameDb(
                id = 0,
                index = ++lastIndex,
                data = framesRepository.serializeFrameData(FrameData(cmds))
            )

            if (frames.size == 10) {
                framesRepository.batchInsert(frames)
                frames.clear()
            }
            index++
        }

        if (frames.isNotEmpty()) {
            framesRepository.batchInsert(frames)
            frames.clear()
        }


        //If first frame is empty delete it.
        if (lastFrame.index == 1L && lastFrame.drawCmds.isEmpty()) {
            framesRepository.deleteFrame(lastFrame)
        }
    }


    private fun sky(color: Int): SquareShapeCmdData {
        return SquareShapeCmdData(
            center = canvasCenter,
            halfSize = max(canvasCenter.x, canvasCenter.y),
            color = color,
            thicknessLevel = 1F,
            filled = true,
            scale = 1F,
            rotation = 0F
        )
    }

    private val grassHeight: Float
        get() = canvasHeight / 10

    private val grass: SquareShapeCmdData by lazy {
        val halfSquareSize = max(grassHeight, canvasWidth)

        val cx = canvasCenter.x
        val cy = canvasHeight - grassHeight + halfSquareSize

        SquareShapeCmdData(
            center = Point(cx, cy),
            halfSize = halfSquareSize,
            color = GrassColor,
            thicknessLevel = 1F,
            filled = true,
            scale = 1F,
            rotation = 0F
        )
    }

    private val houseHalfSize = canvasWidth / 6
    private val HouseCX: Point
    init {
        val cx = 2 * houseHalfSize
        val cy = canvasHeight - grassHeight - houseHalfSize
        HouseCX = Point(cx, cy)
    }

    private val houseBody: SquareShapeCmdData by lazy{
        SquareShapeCmdData(
            center = HouseCX,
            halfSize = houseHalfSize,
            color = HouseBodyColor,
            thicknessLevel = 1F,
            filled = true,
            scale = 1F,
            rotation = 0F
        )
    }

    private val windowHalfSize = houseHalfSize / 2
    private val houseWindow: List<SquareShapeCmdData> by lazy {
        val smallFrameSize = windowHalfSize / 2
        listOf(
            SquareShapeCmdData(
                center = HouseCX,
                halfSize = windowHalfSize,
                color = HouseWindowColor,
                thicknessLevel = 1F,
                filled = true,
                scale = 1F,
                rotation = 0F
            ),

            SquareShapeCmdData(
                center = HouseCX,
                halfSize = windowHalfSize,
                color = HouseWindowFrameColor,
                thicknessLevel = 1F,
                filled = false,
                scale = 1F,
                rotation = 0F
            ),
            SquareShapeCmdData(
                center = Point(HouseCX.x - smallFrameSize, HouseCX.y + smallFrameSize),
                halfSize = smallFrameSize,
                color = HouseWindowFrameColor,
                thicknessLevel = 1F,
                filled = false,
                scale = 1F,
                rotation = 0F
            ),
            SquareShapeCmdData(
                center = Point(HouseCX.x + smallFrameSize, HouseCX.y + smallFrameSize),
                halfSize = smallFrameSize,
                color = HouseWindowFrameColor,
                thicknessLevel = 1F,
                filled = false,
                scale = 1F,
                rotation = 0F
            ),
        )
    }

    private val houseRoof: TriangleShapeCmdData by lazy {
        val roofSize = houseHalfSize * 1.3F
        val roofCenter = Point(
            HouseCX.x,
            HouseCX.y - houseHalfSize - roofSize * cos(Math.toRadians(60.0).toFloat())
        )
        TriangleShapeCmdData(
            center = roofCenter,
            radius = roofSize,
            color = HouseRoofColor,
            thicknessLevel = 1F,
            filled = true,
            scale = 1F,
            rotation = 0F
        )
    }

    private val pointF = PointF()
    private fun sun(time: Int): CircleShapeCmdData {
        val radius = min(canvasWidth, canvasHeight) / 12
        val dist = max(canvasCenter.x + radius, canvasHeight / 8 * 3)
        val cy = canvasCenter.y - dist
        val cx = canvasCenter.x

        val deg = 360F / 24 * time
        GeomUtils.rotatePoint(
            x = cx,
            y = cy,
            angleDegrees = deg,
            pivotX = canvasCenter.x,
            pivotY = canvasCenter.y,
            dest = pointF
        )

        return CircleShapeCmdData(
            center = Point(pointF.x, pointF.y),
            radius = radius,
            color = SunColor,
            thicknessLevel = 1F,
            filled = true,
            scale = 1F,
        )
    }

    private fun moon(t: Int): CircleShapeCmdData {
        val time = (t + 12) % 24
        val radius = min(canvasWidth, canvasHeight) / 12
        val dist = max(canvasCenter.x + radius, canvasHeight / 8 * 3)
        val cy = canvasCenter.y - dist
        val cx = canvasCenter.x

        val deg = 360F / 24 * time
        GeomUtils.rotatePoint(
            x = cx,
            y = cy,
            angleDegrees = deg,
            pivotX = canvasCenter.x,
            pivotY = canvasCenter.y,
            dest = pointF
        )

        return CircleShapeCmdData(
            center = Point(pointF.x, pointF.y),
            radius = radius,
            color = MoonColor,
            thicknessLevel = 1F,
            filled = true,
            scale = 1F,
        )
    }

    private val stars: List<CircleShapeCmdData> by lazy{
        val baselineY = canvasHeight / 8

        val stepX = canvasWidth / 18
        val stepY = canvasHeight / 32
        var index = 0
        listOf(
            Point(x = ++index * stepX, y = baselineY - stepY * 3),
            Point(x = ++index * stepX, y = baselineY + stepY * 1),
            Point(x = ++index * stepX, y = baselineY + stepY * 8),
            Point(x = ++index * stepX, y = baselineY - stepY * 1),
            Point(x = ++index * stepX, y = baselineY + stepY * 5),
            Point(x = ++index * stepX, y = baselineY + stepY * 10),
            Point(x = ++index * stepX, y = baselineY + stepY * 2),
            Point(x = ++index * stepX, y = baselineY - stepY * 1),
            Point(x = ++index * stepX, y = baselineY + stepY * 5),
            Point(x = ++index * stepX, y = baselineY + stepY * 9),
            Point(x = ++index * stepX, y = baselineY - stepY * 2),
            Point(x = ++index * stepX, y = baselineY + stepY * 4),
            Point(x = ++index * stepX, y = baselineY + stepY * 1),
            Point(x = ++index * stepX, y = baselineY + stepY * 7),
            Point(x = ++index * stepX, y = baselineY - stepY * 3),
            Point(x = ++index * stepX, y = baselineY - stepY * 0),
        ).map {
            CircleShapeCmdData(
                center = it,
                radius = 3F,
                color = StarsColor,
                thicknessLevel = 1F,
                filled = true,
                scale = 1F,
            )
        }
    }


    private fun getSkyColor(time: Int): Int {
        return when (time) {
            in 7..17 -> NightSkyColor
            in 0..1,
            23 -> DaySkyColor

            in 2..6 -> {
                val part = (time - 2) / 4F
                ColorUtils.blendARGB(DaySkyColor, NightSkyColor, part)
            }

            in 18..22 -> {
                val part = (time - 18) / 4F
                ColorUtils.blendARGB(NightSkyColor, DaySkyColor, part)
            }

            else -> DaySkyColor
        }
    }


    companion object {
        private const val GrassColor = 0xFF46B94D.toInt()
        private const val DaySkyColor = 0xFF00A2E8.toInt()
        private const val NightSkyColor = 0xFF00046B.toInt()
        private const val SunColor = 0xFFFFF200.toInt()
        private const val HouseBodyColor = 0xFFA15A1C.toInt()
        private const val HouseRoofColor = 0xFF7F7F7F.toInt()
        private const val HouseWindowColor = Color.WHITE
        private const val HouseWindowFrameColor = Color.BLACK
        private const val StarsColor = Color.WHITE
        private const val MoonColor = 0xFFF0F0F0.toInt()
    }
}