package com.anod.car.home.utils

import android.annotation.TargetApi
import android.content.Context
import android.graphics.*
import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build
import android.util.TypedValue
import info.anodsplace.framework.graphics.PathParser

@TargetApi(Build.VERSION_CODES.O)
class AdaptiveIcon(val drawable: AdaptiveIconDrawable, private val mask: Path, val context: Context) {

    constructor(drawable: AdaptiveIconDrawable, pathData: String, context: Context)
        : this(drawable, PathParser.createPathFromPathData(pathData), context)

    private val layerSize = Math.round(TypedValue.applyDimension(
    TypedValue.COMPLEX_UNIT_DIP, 108f * UtilitiesBitmap.maxScale, context.resources.displayMetrics))

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.DITHER_FLAG or
            Paint.FILTER_BITMAP_FLAG)

    @TargetApi(Build.VERSION_CODES.O)
    fun toBitmap(): Bitmap {
        val canvas = Canvas()
        val resultBitmap = Bitmap.createBitmap(layerSize, layerSize, Bitmap.Config.ARGB_8888)
        drawable.setBounds(0, 0, layerSize, layerSize)

        if (mask.isEmpty) {
            canvas.setBitmap(resultBitmap)
            drawable.draw(canvas)
            return resultBitmap
        }

        val maskMatrix = Matrix()
        maskMatrix.setScale(layerSize / MASK_SIZE, layerSize / MASK_SIZE)
        val cMask = Path()
        mask.transform(maskMatrix, cMask)

        val maskBitmap = Bitmap.createBitmap(layerSize, layerSize, Bitmap.Config.ALPHA_8)
        canvas.setBitmap(maskBitmap)
        paint.shader = null
        canvas.drawPath(cMask, paint)

        val layersBitmap = Bitmap.createBitmap(layerSize, layerSize, Bitmap.Config.ARGB_8888)
        canvas.setBitmap(layersBitmap)
        canvas.drawColor(Color.BLACK)

        val background = drawable.background
        background.draw(canvas)

        val foreground = drawable.foreground
        foreground.draw(canvas)

        canvas.setBitmap(resultBitmap)

        paint.shader = BitmapShader(layersBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        canvas.drawBitmap(maskBitmap, 0f, 0f, paint)

        return resultBitmap
    }

    companion object {
        const val MASK_SIZE = 100f
    }
}