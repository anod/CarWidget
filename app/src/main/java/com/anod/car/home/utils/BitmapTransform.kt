package com.anod.car.home.utils

import com.anod.car.home.skin.icon.IconProcessor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PaintFlagsDrawFilter

class BitmapTransform(context: Context) {

    private var iconSize = UtilitiesBitmap.getSystemIconSize(context)
    var applyGrayFilter = false
    var tintColor: Int? = null
    var scaleSize = 1.0f
    var rotateDirection = RotateDirection.NONE
    var paddingBottom = 0
    var iconProcessor: IconProcessor? = null

    val cacheKey: String
        get() = applyGrayFilter.toString() + "," +
                tintColor.toString() + "," +
                scaleSize.toString() + "," +
                rotateDirection.name + "," +
                paddingBottom.toString() + "," +
                iconProcessorId

    private val iconProcessorId: String
        get() = iconProcessor?.id ?: "none"

    enum class RotateDirection {
        NONE, RIGHT, LEFT
    }

    fun transform(bitmap: Bitmap): Bitmap {
        var src = bitmap
        var sizeDiff = 0.0f

        iconProcessor?.let {
            src = it.process(src)
            sizeDiff = it.sizeDiff
        }

        val height = src.height
        val width = src.width

        val canvas = Canvas()
        canvas.drawFilter = PaintFlagsDrawFilter(Paint.DITHER_FLAG, Paint.FILTER_BITMAP_FLAG)

        val paint = Paint()
        paint.isFilterBitmap = true

        if (applyGrayFilter) {
            val cm = ColorMatrix()
            cm.setSaturation(0f) //gray scale

            tintColor?.let {
                applyTintColor(it, cm)
            }

            val filter = ColorMatrixColorFilter(cm)
            paint.colorFilter = filter
        }

        var degrees = 0
        if (rotateDirection != RotateDirection.NONE) {
            degrees = if (rotateDirection == RotateDirection.LEFT) 90 else 270
        }

        val iconSize = iconSize - sizeDiff
        val scaledSize = (iconSize * scaleSize).toInt()
        val ratioX = scaledSize / width.toFloat()
        val ratioY = scaledSize / height.toFloat()
        val scaledMiddleX = scaledSize / 2.0f
        val scaledMiddleY = scaledSize / 2.0f

        val matrix = Matrix()
        matrix.postScale(ratioX, ratioY, scaledMiddleX, scaledMiddleY)
        matrix.postRotate(degrees.toFloat(), scaledMiddleX, scaledMiddleX)

        val output = Bitmap.createBitmap(scaledSize, scaledSize + paddingBottom, Bitmap.Config.ARGB_8888)
        canvas.setBitmap(output)

        canvas.setMatrix(matrix)
        canvas.drawBitmap(src, scaledMiddleX - width / 2, scaledMiddleY - height / 2, paint)

        return output
    }

    private fun applyTintColor(color: Int, cm: ColorMatrix) {
        val r = Color.red(color) / 255.0f
        val g = Color.green(color) / 255.0f
        val b = Color.blue(color) / 255.0f

        val matrix = floatArrayOf(
            r, 0f, 0f, 0f, 0f, //red
            0f, g, 0f, 0f, 0f, //green
            0f, 0f, b, 0f, 0f, //blue
            0f, 0f, 0f, 1.0f, 0f //alpha
        )

        cm.postConcat(ColorMatrix(matrix))
    }
}