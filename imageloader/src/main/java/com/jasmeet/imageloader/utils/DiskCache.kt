package com.jasmeet.imageloader.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import com.jasmeet.imageloader.decoder.AnimatedImageDecoderFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

class DiskCache(private val context: Context) {
    private val cacheDir = File(context.cacheDir, "image_cache").apply {
        if (!exists()) mkdirs()
    }

    private fun urlToFilename(url: String, extension: String = ".png"): String {
        val md = MessageDigest.getInstance("MD5")
        val hash = md.digest(url.toByteArray())
        return hash.joinToString("") { "%02x".format(it) } + extension
    }

    suspend fun get(url: String): ImageData? = withContext(Dispatchers.IO) {
        try {
            // Try animated formats (GIF, WebP)
            for (ext in listOf(".gif", ".webp")) {
                val animatedFile = File(cacheDir, urlToFilename(url, ext))
                if (animatedFile.exists()) {
                    val bytes = animatedFile.readBytes()
                    val format = AnimatedImageDecoderFactory.detectFormat(bytes)

                    if (format != null) {
                        try {
                            val decoder = AnimatedImageDecoderFactory.createDecoder(bytes, format)
                            return@withContext ImageData.AnimatedImage(
                                bytes = bytes,
                                format = format,
                                frameCount = decoder.frameCount,
                                width = decoder.width,
                                height = decoder.height
                            ).also {
                                decoder.release()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            // Continue to try other formats or static image
                        }
                    }
                }
            }

            // Try static image
            val file = File(cacheDir, urlToFilename(url, ".png"))
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
                    val file = File(cacheDir, urlToFilename(url, ".png"))
                    FileOutputStream(file).use { out ->
                        data.bitmap.compress(CompressFormat.PNG, 90, out)
                    }
                }
                is ImageData.AnimatedImage -> {
                    val ext = when (data.format) {
                        AnimatedFormat.GIF -> ".gif"
                        AnimatedFormat.WEBP -> ".webp"
                    }
                    val file = File(cacheDir, urlToFilename(url, ext))
                    file.writeBytes(data.bytes)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clear() {
        cacheDir.listFiles()?.forEach { it.delete() }
    }
}