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

    private fun urlToFilename(url: String): String {
        val md = MessageDigest.getInstance("MD5")
        val hash = md.digest(url.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    suspend fun get(url: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val file = File(cacheDir, urlToFilename(url))
            if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun put(url: String, bitmap: Bitmap) = withContext(Dispatchers.IO) {
        try {
            val file = File(cacheDir, urlToFilename(url))
            FileOutputStream(file).use { out ->
                bitmap.compress(CompressFormat.PNG, 90, out)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun clear() {
        cacheDir.listFiles()?.forEach { it.delete() }
    }
}