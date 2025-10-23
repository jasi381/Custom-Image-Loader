package com.jasmeet.customimageloader.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

class DiskCache(private val context: Context) {
    private val cacheDir = File(context.cacheDir, "image_cache").apply {
        if (!exists()) mkdirs()
    }

    private fun urlToFilename(url: String, isGif: Boolean = false): String {
        val md = MessageDigest.getInstance("MD5")
        val hash = md.digest(url.toByteArray())
        val ext = if (isGif) ".gif" else ".png"
        return hash.joinToString("") { "%02x".format(it) } + ext
    }

    suspend fun get(url: String): ImageData? = withContext(Dispatchers.IO) {
        try {
            // Try GIF first
            val gifFile = File(cacheDir, urlToFilename(url, true))
            if (gifFile.exists()) {
                val bytes = gifFile.readBytes()
                return@withContext ImageData.AnimatedGif(bytes, countGifFrames(bytes))
            }

            // Try static image
            val file = File(cacheDir, urlToFilename(url, false))
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                return@withContext if (bitmap != null) ImageData.StaticImage(bitmap) else null
            }

            null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun put(url: String, data: ImageData) = withContext(Dispatchers.IO) {
        try {
            when (data) {
                is ImageData.StaticImage -> {
                    val file = File(cacheDir, urlToFilename(url, false))
                    FileOutputStream(file).use { out ->
                        data.bitmap.compress(CompressFormat.PNG, 90, out)
                    }
                }
                is ImageData.AnimatedGif -> {
                    val file = File(cacheDir, urlToFilename(url, true))
                    file.writeBytes(data.bytes)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun countGifFrames(bytes: ByteArray): Int {
        var count = 0
        for (i in 0 until bytes.size - 1) {
            if (bytes[i] == 0x2C.toByte()) count++
        }
        return maxOf(count, 1)
    }

    fun clear() {
        cacheDir.listFiles()?.forEach { it.delete() }
    }
}