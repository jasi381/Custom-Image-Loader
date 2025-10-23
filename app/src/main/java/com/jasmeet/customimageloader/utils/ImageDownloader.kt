package com.jasmeet.customimageloader.utils

import kotlinx.coroutines.*
import java.net.URL
import android.graphics.Bitmap
import android.graphics.BitmapFactory

class ImageDownloader {
    suspend fun download(url: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection()
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.getInputStream().use { input ->
                BitmapFactory.decodeStream(input)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}