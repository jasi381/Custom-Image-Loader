package com.jasmeet.customimageloader.utils

import kotlinx.coroutines.*
import java.net.URL
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.jasmeet.customimageloader.decoder.AnimatedImageDecoderFactory


enum class AnimatedFormat {
    GIF,
    WEBP
}

sealed class ImageData {
    data class StaticImage(val bitmap: Bitmap) : ImageData()

    /**
     * Animated image with lazy frame decoding
     * Stores raw bytes + metadata, decodes frames on-demand
     */
    data class AnimatedImage(
        val bytes: ByteArray,
        val format: AnimatedFormat,
        val frameCount: Int,
        val width: Int,
        val height: Int
    ) : ImageData() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as AnimatedImage
            if (frameCount != other.frameCount) return false
            if (width != other.width) return false
            if (height != other.height) return false
            if (format != other.format) return false
            if (!bytes.contentEquals(other.bytes)) return false
            return true
        }

        override fun hashCode(): Int {
            var result = frameCount
            result = 31 * result + width
            result = 31 * result + height
            result = 31 * result + format.hashCode()
            result = 31 * result + bytes.contentHashCode()
            return result
        }
    }
}

class ImageDownloader {
    suspend fun download(url: String): ImageData? = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection()
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val bytes = connection.getInputStream().use { it.readBytes() }

            // Detect animated format (GIF or WebP)
            val animatedFormat = AnimatedImageDecoderFactory.detectFormat(bytes)

            if (animatedFormat != null) {
                // It's an animated image (GIF or WebP)
                try {
                    val decoder = AnimatedImageDecoderFactory.createDecoder(bytes, animatedFormat)

                    return@withContext ImageData.AnimatedImage(
                        bytes = bytes,
                        format = animatedFormat,
                        frameCount = decoder.frameCount,
                        width = decoder.width,
                        height = decoder.height
                    ).also {
                        // Release temporary decoder used for metadata extraction
                        decoder.release()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Fall back to static image if animated decoding fails
                    val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    return@withContext if (bitmap != null) ImageData.StaticImage(bitmap) else null
                }
            } else {
                // Static image
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bitmap != null) ImageData.StaticImage(bitmap) else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
