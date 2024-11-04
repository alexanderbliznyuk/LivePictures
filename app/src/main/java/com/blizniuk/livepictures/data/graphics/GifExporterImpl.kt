package com.blizniuk.livepictures.data.graphics

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Rect
import android.os.Environment
import com.blizniuk.livepictures.R
import com.blizniuk.livepictures.domain.graphics.GifExporter
import com.blizniuk.livepictures.domain.graphics.entity.RenderContext
import com.blizniuk.livepictures.util.gif.AnimatedGifEncoder
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date

class GifExporterImpl(
    private val framesRepositoryImpl: FramesRepositoryImpl,
    private val appContext: Context,
) : GifExporter {

    override suspend fun export(
        width: Int,
        height: Int,
    ): String? {
        val background = BitmapFactory.decodeResource(appContext.resources, R.drawable.canvas)
        val targetRect = Rect(0, 0, width, height)
        val renderContext = RenderContext(appContext)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val outputFile = getOutputFile()
        val fos = BufferedOutputStream(FileOutputStream(outputFile))

        val frameDuration = framesRepositoryImpl.getAppSettings().defaultFrameDurationMs
        val cursor = framesRepositoryImpl.getFramesCursor()
        try {
            val encoder = AnimatedGifEncoder()
            encoder.start(fos)
            encoder.setRepeat(0)
            encoder.setSize(width, height)
            encoder.setDelay(frameDuration.toInt())

            val idIndex = cursor.getColumnIndex(FrameDb.ColumnIdKey)
            val indexIndex = cursor.getColumnIndex(FrameDb.FrameIndexdKey)
            val dataIndex = cursor.getColumnIndex(FrameDb.SerializedDataKey)

            while (cursor.moveToNext()) {
                canvas.drawBitmap(background, null, targetRect, null)

                val id = cursor.getLong(idIndex)
                val index = cursor.getLong(indexIndex)
                val data = cursor.getString(dataIndex)
                val frame = framesRepositoryImpl.mapRawData(id, index, data)
                canvas.saveLayer(0F, 0F, width.toFloat(), height.toFloat(), null)
                frame.render(canvas, renderContext)
                canvas.restore()

                encoder.addFrame(bitmap)
            }
            encoder.finish()
            return outputFile.absolutePath
        } finally {
            bitmap.recycle()
            try {
                cursor.close()
                fos.close()
            } catch (e: Exception) {
            }
        }
    }

    private fun getOutputFile(): File {
        val dir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
            "LivePictures"
        )
        dir.mkdirs()
        val format = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Date())
        return File(dir, "$format.gif")
    }
}