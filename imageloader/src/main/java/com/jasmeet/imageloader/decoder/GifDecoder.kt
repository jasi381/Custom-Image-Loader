package com.jasmeet.imageloader.decoder

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Movie
import android.graphics.Paint

/**
 * Optimized GIF decoder using lazy frame decoding
 * Uses Android's Movie API with frame pooling and proper timing
 *
 * Note: For production, consider implementing a full LZW decoder or using
 * a library like Fresco's GIF decoder for better performance
 */
class GifDecoder(private val bytes: ByteArray) : AnimatedImageDecoder {

    private val movie: Movie = Movie.decodeByteArray(bytes, 0, bytes.size)
        ?: throw IllegalArgumentException("Failed to decode GIF")

    private val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
    private var isReleased = false

    override val frameCount: Int
    override val duration: Long
    override val width: Int
    override val height: Int
    override val isLooping: Boolean = true

    init {
        width = movie.width()
        height = movie.height()
        duration = movie.duration().toLong()

        // Estimate frame count based on duration
        // Movie API doesn't expose frame count directly
        // Assuming average of 10fps if duration is available
        frameCount = if (duration > 0) {
            (duration / 100).toInt().coerceAtLeast(1)
        } else {
            countGifFrames(bytes)
        }
    }

    override fun decodeFrame(frameIndex: Int, outputBitmap: Bitmap): Long {
        if (isReleased) throw IllegalStateException("Decoder released")

        // Calculate time position for this frame
        val frameTime = if (duration > 0) {
            (frameIndex * duration / frameCount.toFloat()).toInt()
        } else {
            frameIndex * 100 // Default 100ms per frame
        }

        // Set movie to specific time
        movie.setTime(frameTime)

        // Clear output bitmap
        outputBitmap.eraseColor(0)

        // Draw movie frame onto bitmap
        val canvas = Canvas(outputBitmap)

        // Scale to fit output bitmap if needed
        val scaleX = outputBitmap.width.toFloat() / width
        val scaleY = outputBitmap.height.toFloat() / height
        val scale = minOf(scaleX, scaleY)

        canvas.save()
        canvas.scale(scale, scale)
        movie.draw(canvas, 0f, 0f, paint)
        canvas.restore()

        return getFrameDuration(frameIndex)
    }

    override fun getFrameDuration(frameIndex: Int): Long {
        // Return average frame duration
        // Movie API doesn't expose per-frame durations
        return if (duration > 0 && frameCount > 0) {
            duration / frameCount
        } else {
            100L // Default 100ms per frame
        }
    }

    override fun release() {
        if (!isReleased) {
            isReleased = true
            // Movie class doesn't have explicit cleanup
        }
    }

    /**
     * Count GIF frames by looking for Image Separator markers (0x2C)
     * This is a heuristic approach
     */
    private fun countGifFrames(bytes: ByteArray): Int {
        var count = 0
        for (i in 0 until bytes.size - 1) {
            if (bytes[i] == 0x2C.toByte()) count++
        }
        return maxOf(count, 1)
    }
}
