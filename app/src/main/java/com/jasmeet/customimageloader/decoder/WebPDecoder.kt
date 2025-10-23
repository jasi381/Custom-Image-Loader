package com.jasmeet.customimageloader.decoder

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ImageDecoder
import android.graphics.drawable.AnimatedImageDrawable
import android.os.Build
import androidx.annotation.RequiresApi
import java.nio.ByteBuffer

/**
 * WebP animated decoder using Android's ImageDecoder (API 28+)
 * Hardware-accelerated and efficient
 */
@RequiresApi(Build.VERSION_CODES.P)
class WebPDecoder(private val bytes: ByteArray) : AnimatedImageDecoder {

    private val source: ImageDecoder.Source = ImageDecoder.createSource(ByteBuffer.wrap(bytes))
    private val drawable: AnimatedImageDrawable
    private var currentTime = 0L

    override val frameCount: Int
    override val duration: Long
    override val width: Int
    override val height: Int
    override val isLooping: Boolean = true

    init {
        drawable = ImageDecoder.decodeDrawable(source) as AnimatedImageDrawable
        drawable.repeatCount = AnimatedImageDrawable.REPEAT_INFINITE

        width = drawable.intrinsicWidth
        height = drawable.intrinsicHeight

        // Get total duration from drawable
        // AnimatedImageDrawable doesn't directly expose this, so we estimate
        duration = estimateDuration()

        // Estimate frame count (WebP typically 10-30 fps)
        frameCount = estimateFrameCount()
    }

    override fun decodeFrame(frameIndex: Int, outputBitmap: Bitmap): Long {
        // Calculate target time for this frame
        val targetTime = if (duration > 0) {
            (frameIndex * duration / frameCount.toFloat()).toLong()
        } else {
            frameIndex * 100L
        }

        // Start drawable if not started
        if (!drawable.isRunning) {
            drawable.start()
        }

        // Clear output bitmap
        outputBitmap.eraseColor(0)

        // Draw current frame
        val canvas = Canvas(outputBitmap)
        val scaleX = outputBitmap.width.toFloat() / width
        val scaleY = outputBitmap.height.toFloat() / height
        val scale = minOf(scaleX, scaleY)

        canvas.save()
        canvas.scale(scale, scale)
        drawable.setBounds(0, 0, width, height)
        drawable.draw(canvas)
        canvas.restore()

        currentTime = targetTime

        return getFrameDuration(frameIndex)
    }

    override fun getFrameDuration(frameIndex: Int): Long {
        // Return average frame duration
        return if (duration > 0 && frameCount > 0) {
            duration / frameCount
        } else {
            100L // Default 100ms
        }
    }

    override fun release() {
        drawable.stop()
    }

    private fun estimateDuration(): Long {
        // Try to parse WebP ANIM chunk for accurate duration
        // For now, use a heuristic based on file size
        // Typical animated WebP: 1-5 seconds
        return parseWebPDuration() ?: 2000L
    }

    private fun estimateFrameCount(): Int {
        // Parse WebP header to get frame count if possible
        return parseWebPFrameCount() ?: 20
    }

    /**
     * Parse WebP ANIM chunk to get duration
     */
    private fun parseWebPDuration(): Long? {
        try {
            // Look for ANIM chunk in WebP file
            var offset = 12 // Skip RIFF header
            while (offset < bytes.size - 8) {
                val chunkType = String(bytes.copyOfRange(offset, offset + 4))
                val chunkSize = readInt32LE(bytes, offset + 4)

                if (chunkType == "ANIM") {
                    // ANIM chunk found - structure is:
                    // Background color (4 bytes)
                    // Loop count (2 bytes)
                    // We don't have frame durations here directly
                    return null // Will need to sum frame durations
                }

                if (chunkType == "ANMF") {
                    // Animation frame - contains duration
                    // For simplicity, return null and use estimation
                    return null
                }

                offset += 8 + chunkSize + (chunkSize % 2) // Chunks are padded to even size
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Parse WebP to get frame count
     */
    private fun parseWebPFrameCount(): Int? {
        try {
            var count = 0
            var offset = 12 // Skip RIFF header

            while (offset < bytes.size - 8) {
                val chunkType = String(bytes.copyOfRange(offset, offset + 4))
                val chunkSize = readInt32LE(bytes, offset + 4)

                if (chunkType == "ANMF") {
                    count++
                }

                offset += 8 + chunkSize + (chunkSize % 2)
            }

            return if (count > 0) count else null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private fun readInt32LE(data: ByteArray, offset: Int): Int {
        return (data[offset].toInt() and 0xFF) or
                ((data[offset + 1].toInt() and 0xFF) shl 8) or
                ((data[offset + 2].toInt() and 0xFF) shl 16) or
                ((data[offset + 3].toInt() and 0xFF) shl 24)
    }
}

/**
 * Legacy WebP decoder for API < 28
 * Falls back to static image or throws exception
 */
class WebPDecoderLegacy(private val bytes: ByteArray) : AnimatedImageDecoder {

    override val frameCount: Int = 1
    override val duration: Long = 0
    override val width: Int = 0
    override val height: Int = 0
    override val isLooping: Boolean = false

    override fun decodeFrame(frameIndex: Int, outputBitmap: Bitmap): Long {
        throw UnsupportedOperationException("Animated WebP requires API 28+. Current API: ${Build.VERSION.SDK_INT}")
    }

    override fun getFrameDuration(frameIndex: Int): Long = 0

    override fun release() {}
}
