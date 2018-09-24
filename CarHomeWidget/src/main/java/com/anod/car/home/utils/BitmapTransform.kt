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

    private var mIconSize = -1
    private var mApplyGrayFilter = false
    private var mTintColor: Int? = null
    private var mScaleSize = 1.0f
    private var mRotateDirection = RotateDirection.NONE
    private var mPaddingBottom = 0
    private var mIconProcessor: IconProcessor? = null

    val cacheKey: String
        get() = mApplyGrayFilter.toString() + "," +
                mTintColor.toString() + "," +
                mScaleSize.toString() + "," +
                mRotateDirection.name + "," +
                mPaddingBottom.toString() + "," +
                iconProcessorId

    val iconProcessorId: String
        get() = if (mIconProcessor != null) {
            mIconProcessor!!.id()
        } else "none"

    enum class RotateDirection {
        NONE, RIGHT, LEFT
    }

    init {
        mIconSize = UtilitiesBitmap.getSystemIconSize(context)
    }

    fun setApplyGrayFilter(applyGrayFilter: Boolean): BitmapTransform {
        mApplyGrayFilter = applyGrayFilter
        return this
    }

    fun setTintColor(tintColor: Int?): BitmapTransform {
        mTintColor = tintColor
        return this
    }

    fun setScaleSize(scaleSize: Float): BitmapTransform {
        mScaleSize = scaleSize
        return this
    }

    fun setRotateDirection(dir: RotateDirection): BitmapTransform {
        mRotateDirection = dir
        return this
    }

    fun setPaddingBottom(paddingBottom: Int): BitmapTransform {
        mPaddingBottom = paddingBottom
        return this
    }


    fun setIconProcessor(ip: IconProcessor): BitmapTransform {
        mIconProcessor = ip
        return this
    }

    fun transform(src: Bitmap): Bitmap {
        var src = src
        var sizeDiff = 0.0f
        if (mIconProcessor != null) {
            src = mIconProcessor!!.process(src)
            sizeDiff = mIconProcessor!!.sizeDiff
        }

        val height = src.height
        val width = src.width

        val canvas = Canvas()
        canvas.drawFilter = PaintFlagsDrawFilter(Paint.DITHER_FLAG, Paint.FILTER_BITMAP_FLAG)

        val paint = Paint()
        paint.isFilterBitmap = true

        if (mApplyGrayFilter) {
            val cm = ColorMatrix()
            cm.setSaturation(0f) //gray scale

            if (mTintColor != null) {
                applyTintColor(cm)
            }

            val filter = ColorMatrixColorFilter(cm)
            paint.colorFilter = filter
        }

        val matrix = Matrix()

        var degrees = 0
        if (mRotateDirection != RotateDirection.NONE) {
            degrees = if (mRotateDirection == RotateDirection.LEFT) 90 else 270
        }

        val iconSize = mIconSize - sizeDiff
        val scaledSize = (iconSize * mScaleSize).toInt()
        val ratioX = scaledSize / width.toFloat()
        val ratioY = scaledSize / height.toFloat()
        val scaledMiddleX = scaledSize / 2.0f
        val scaledMiddleY = scaledSize / 2.0f

        matrix.postScale(ratioX, ratioY, scaledMiddleX, scaledMiddleY)
        matrix.postRotate(degrees.toFloat(), scaledMiddleX, scaledMiddleX)

        val output = Bitmap
                .createBitmap(scaledSize, scaledSize + mPaddingBottom, Bitmap.Config.ARGB_8888)
        canvas.setBitmap(output)

        //paint.setColor(android.graphics.Color.RED);
        //paint.setStyle(Paint.Style.FILL);
        //canvas.drawRect(0, 0, output.getWidth(), output.getHeight(), paint);

        canvas.matrix = matrix
        canvas.drawBitmap(src, scaledMiddleX - width / 2, scaledMiddleY - height / 2, paint)

        return output
    }

    private fun applyTintColor(cm: ColorMatrix) {
        val r = Color.red(mTintColor!!) / 255.0f
        val g = Color.green(mTintColor!!) / 255.0f
        val b = Color.blue(mTintColor!!) / 255.0f

        val matrix = floatArrayOf(r, 0f, 0f, 0f, 0f, //red
                0f, g, 0f, 0f, 0f, //green
                0f, 0f, b, 0f, 0f, //blue
                0f, 0f, 0f, 1.0f, 0f //alpha
        )

        cm.postConcat(ColorMatrix(matrix))
    }


}
