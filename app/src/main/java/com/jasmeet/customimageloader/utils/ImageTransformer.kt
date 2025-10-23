package com.jasmeet.customimageloader.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import androidx.core.graphics.createBitmap

sealed class ImageTransformation {
    object None : ImageTransformation()
    data class RoundedCorners(val radius: Float) : ImageTransformation()
    data class Circle(val borderWidth: Float = 0f, val borderColor: Int = 0xFFFFFFFF.toInt()) : ImageTransformation()
    data class Blur(val radius: Float = 15f) : ImageTransformation()
}

object ImageTransformer {
    fun apply(data: ImageData, transformation: ImageTransformation, context: Context? = null): ImageData {
        // Animated images don't support transformations (would need frame-by-frame processing)
        if (data is ImageData.AnimatedImage) return data

        if (data !is ImageData.StaticImage) return data

        return when (transformation) {
            is ImageTransformation.None -> data
            is ImageTransformation.RoundedCorners -> {
                ImageData.StaticImage(applyRoundedCorners(data.bitmap, transformation.radius))
            }
            is ImageTransformation.Circle -> {
                ImageData.StaticImage(applyCircle(data.bitmap, transformation.borderWidth, transformation.borderColor))
            }
            is ImageTransformation.Blur -> {
                ImageData.StaticImage(applyBlur(data.bitmap, transformation.radius, context))
            }
        }
    }

    private fun applyRoundedCorners(bitmap: Bitmap, radius: Float): Bitmap {
        val output = createBitmap(bitmap.width, bitmap.height)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)

        canvas.drawRoundRect(rectF, radius, radius, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)

        return output
    }

    private fun applyCircle(bitmap: Bitmap, borderWidth: Float, borderColor: Int): Bitmap {
        val size = minOf(bitmap.width, bitmap.height)
        val output = createBitmap(size, size)
        val canvas = Canvas(output)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = Rect(0, 0, size, size)

        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)

        val left = (bitmap.width - size) / 2
        val top = (bitmap.height - size) / 2
        val srcRect = Rect(left, top, left + size, top + size)
        canvas.drawBitmap(bitmap, srcRect, rect, paint)

        if (borderWidth > 0) {
            paint.xfermode = null
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = borderWidth
            paint.color = borderColor
            canvas.drawCircle(size / 2f, size / 2f, size / 2f - borderWidth / 2, paint)
        }

        return output
    }

    private fun applyBlur(bitmap: Bitmap, radius: Float, context: Context?): Bitmap {
        if (context == null) return bitmap

        return try {
            val rs = RenderScript.create(context)
            val input = Allocation.createFromBitmap(rs, bitmap)
            val output = Allocation.createTyped(rs, input.type)
            val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))

            script.setRadius(radius.coerceIn(0f, 25f))
            script.setInput(input)
            script.forEach(output)

            val blurred = bitmap.config?.let { createBitmap(bitmap.width, bitmap.height, it) }
            output.copyTo(blurred)

            rs.destroy()
            blurred
        } catch (e: Exception) {
            bitmap
        }!!
    }
}

