package com.jasmeet.customimageloader.utils

import kotlinx.coroutines.*
import java.net.URL
import android.graphics.Bitmap
import android.graphics.BitmapFactory


sealed class ImageData {
    data class StaticImage(val bitmap: Bitmap) : ImageData()
    data class AnimatedGif(val bytes: ByteArray, val frameCount: Int) : ImageData() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as AnimatedGif

            if (frameCount != other.frameCount) return false
            if (!bytes.contentEquals(other.bytes)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = frameCount
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

            // Check if it's a GIF
            if (isGif(bytes)) {
                val frameCount = getGifFrameCount(bytes)
                ImageData.AnimatedGif(bytes, frameCount)
            } else {
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bitmap != null) ImageData.StaticImage(bitmap) else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun isGif(bytes: ByteArray): Boolean {
        return bytes.size >= 6 &&
                bytes[0] == 'G'.code.toByte() &&
                bytes[1] == 'I'.code.toByte() &&
                bytes[2] == 'F'.code.toByte()
    }

    private fun getGifFrameCount(bytes: ByteArray): Int {
        // Simple frame counting - looks for Image Separator (0x2C)
        var count = 0
        for (i in 0 until bytes.size - 1) {
            if (bytes[i] == 0x2C.toByte()) count++
        }
        return maxOf(count, 1)
    }
}
