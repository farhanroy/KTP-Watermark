package com.example.ktpwatermark.utils

import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.graphics.Paint.DITHER_FLAG
import androidx.annotation.ColorInt
import android.graphics.Bitmap

import android.content.ContextWrapper
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.os.Environment
import java.lang.Exception
import android.media.MediaScannerConnection

import android.provider.MediaStore

import android.content.ContentValues

import android.content.ContentResolver

import android.os.Build

import androidx.annotation.NonNull





fun addWatermark(
    bitmap: Bitmap,
    watermarkText: String,
    options: WatermarkOptions = WatermarkOptions()
): Bitmap {
    val result = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(result)
    val paint = Paint(ANTI_ALIAS_FLAG or DITHER_FLAG)
    paint.textAlign = when (options.corner) {
        Corner.TOP_LEFT,
        Corner.BOTTOM_LEFT -> Paint.Align.LEFT
        Corner.TOP_RIGHT,
        Corner.BOTTOM_RIGHT -> Paint.Align.RIGHT
    }
    val textSize = result.width * options.textSizeToWidthRatio
    paint.textSize = textSize
    paint.color = options.textColor
    if (options.shadowColor != null) {
        paint.setShadowLayer(textSize / 2, 0f, 0f, options.shadowColor)
    }
    if (options.typeface != null) {
        paint.typeface = options.typeface
    }
    val padding = result.width * options.paddingToWidthRatio
    val coordinates =
        calculateCoordinates(watermarkText, paint, options, canvas.width, canvas.height, padding)
    canvas.drawText(watermarkText, coordinates.x, coordinates.y, paint)
    return result
}

private fun calculateCoordinates(
    watermarkText: String,
    paint: Paint,
    options: WatermarkOptions,
    width: Int,
    height: Int,
    padding: Float
): PointF {
    val x = when (options.corner) {
        Corner.TOP_LEFT,
        Corner.BOTTOM_LEFT -> {
            padding
        }
        Corner.TOP_RIGHT,
        Corner.BOTTOM_RIGHT -> {
            width - padding
        }
    }
    val y = when (options.corner) {
        Corner.BOTTOM_LEFT,
        Corner.BOTTOM_RIGHT -> {
            height - padding
        }
        Corner.TOP_LEFT,
        Corner.TOP_RIGHT -> {
            val bounds = Rect()
            paint.getTextBounds(watermarkText, 0, watermarkText.length, bounds)
            val textHeight = bounds.height()
            textHeight + padding

        }
    }
    return PointF(x, y)
}

enum class Corner {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
}

data class WatermarkOptions(
    val corner: Corner = Corner.BOTTOM_RIGHT,
    val textSizeToWidthRatio: Float = 0.04f,
    val paddingToWidthRatio: Float = 0.03f,
    @ColorInt val textColor: Int = Color.WHITE,
    @ColorInt val shadowColor: Int? = Color.BLACK,
    val typeface: Typeface? = null
)

fun watermarkOptions(
    cornerStr: String,
    textSizeToWidthRatio: Float = 0.09f,
    textColor: Int
): WatermarkOptions {
    var corner = Corner.BOTTOM_RIGHT
    when (cornerStr) {
        "Pojok Kiri Bawah" -> {
            corner = Corner.BOTTOM_LEFT
        }

        "Pojok Kanan Bawah" -> {
            corner = Corner.BOTTOM_RIGHT
        }

        "Pojok Kiri Atas" -> {
            corner = Corner.TOP_LEFT
        }

        "Pojok Kanan Atas" -> {
            corner = Corner.TOP_RIGHT
        }
    }

    return WatermarkOptions(
        corner = corner,
        textSizeToWidthRatio = textSizeToWidthRatio,
        textColor = textColor
    )
}

@Throws(IOException::class)
fun saveImage(
    context: Context,
    bitmap: Bitmap,

): Uri? {
    var fos: OutputStream? = null
    var imageFile: File? = null
    var imageUri: Uri? = null
    val folderName: String = "ktp watermark"
    val fileName: String = "as"
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            contentValues.put(
                MediaStore.MediaColumns.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + File.separator + folderName
            )
            imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (imageUri == null) throw IOException("Failed to create new MediaStore record.")
            fos = resolver.openOutputStream(imageUri)
        } else {
            val imagesDir = File(
                Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES
                ).toString() + File.separator + folderName
            )
            if (!imagesDir.exists()) imagesDir.mkdir()
            imageFile = File(imagesDir, "$fileName.png")
            fos = FileOutputStream(imageFile)
        }
        if (!bitmap.compress(
                Bitmap.CompressFormat.PNG,
                100,
                fos
            )
        ) throw IOException("Failed to save bitmap.")
        fos!!.flush()
    } finally {
        fos?.close()
    }
    if (imageFile != null) { //pre Q
        MediaScannerConnection.scanFile(context, arrayOf(imageFile.toString()), null, null)
        imageUri = Uri.fromFile(imageFile)
    }
    return imageUri
}