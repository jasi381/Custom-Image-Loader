package com.jasmeet.customimageloader.utils

import android.content.Context
import android.graphics.Bitmap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ImageLoader(private val context: Context) {
    private val downloader = ImageDownloader()
    private val diskCache = DiskCache(context)
    private val prefetchJobs = mutableMapOf<String, Job>()

    suspend fun load(
        url: String,
        transformation: ImageTransformation = ImageTransformation.None
    ): Bitmap? {
        val cacheKey = "$url-${transformation.hashCode()}"

        // 1. Check memory cache
        MemoryCache.get(cacheKey)?.let { return it }

        // 2. Check disk cache
        diskCache.get(cacheKey)?.let { bitmap ->
            MemoryCache.put(cacheKey, bitmap)
            return bitmap
        }

        // 3. Download and transform
        downloader.download(url)?.let { bitmap ->
            val transformed = ImageTransformer.apply(bitmap, transformation, context)
            transformed?.let { MemoryCache.put(cacheKey, it) }
            transformed?.let { diskCache.put(cacheKey, it) }
            return transformed
        }

        return null
    }

    fun prefetch(url: String, scope: CoroutineScope) {
        if (prefetchJobs.containsKey(url)) return

        prefetchJobs[url] = scope.launch {
            load(url)
        }
    }

    fun cancelPrefetch(url: String) {
        prefetchJobs[url]?.cancel()
        prefetchJobs.remove(url)
    }
}