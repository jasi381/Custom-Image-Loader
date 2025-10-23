package com.jasmeet.customimageloader.utils

import android.graphics.Bitmap
import android.util.LruCache

object MemoryCache {
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = maxMemory / 8

    private val cache = object : LruCache<String, ImageData>(cacheSize) {
        override fun sizeOf(key: String, data: ImageData): Int {
            return when (data) {
                is ImageData.StaticImage -> data.bitmap.byteCount / 1024
                is ImageData.AnimatedGif -> data.bytes.size / 1024
            }
        }
    }

    fun get(key: String): ImageData? = cache.get(key)
    fun put(key: String, data: ImageData) {
        if (get(key) == null) cache.put(key, data)
    }
    fun clear() = cache.evictAll()
}
