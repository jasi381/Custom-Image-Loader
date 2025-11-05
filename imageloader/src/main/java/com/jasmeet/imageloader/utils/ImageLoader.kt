package com.jasmeet.imageloader.utils

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
    ): ImageData? {
        val cacheKey = "$url-${transformation.hashCode()}"

        // 1. Check memory cache
        MemoryCache.get(cacheKey)?.let { return it }

        // 2. Check disk cache
        diskCache.get(url)?.let { data ->
            val transformed = ImageTransformer.apply(data, transformation, context)
            MemoryCache.put(cacheKey, transformed)
            return transformed
        }

        // 3. Download and transform
        downloader.download(url)?.let { data ->
            val transformed = ImageTransformer.apply(data, transformation, context)
            MemoryCache.put(cacheKey, transformed)
            diskCache.put(url, data) // Store original
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
