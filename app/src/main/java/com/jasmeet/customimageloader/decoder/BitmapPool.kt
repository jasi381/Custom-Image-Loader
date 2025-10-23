package com.jasmeet.customimageloader.decoder

import android.graphics.Bitmap
import java.util.concurrent.ConcurrentHashMap

/**
 * Thread-safe bitmap pool for recycling frame buffers
 * Reduces GC pressure by reusing bitmap allocations
 */
object BitmapPool {
    private val pool = ConcurrentHashMap<String, ArrayDeque<Bitmap>>()
    private const val MAX_POOL_SIZE = 6 // Keep 6 bitmaps per size

    /**
     * Get a bitmap from pool or create new one
     * @param width Desired width
     * @param height Desired height
     * @param config Bitmap configuration
     */
    fun get(width: Int, height: Int, config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap {
        val key = getKey(width, height, config)
        val queue = pool[key]

        // Try to reuse from pool
        queue?.let {
            synchronized(it) {
                it.removeFirstOrNull()?.let { bitmap ->
                    if (!bitmap.isRecycled && bitmap.isMutable) {
                        bitmap.eraseColor(0) // Clear previous content
                        return bitmap
                    }
                }
            }
        }

        // Create new bitmap if pool is empty or bitmap was recycled
        return Bitmap.createBitmap(width, height, config)
    }

    /**
     * Return bitmap to pool for reuse
     */
    fun recycle(bitmap: Bitmap) {
        if (bitmap.isRecycled || !bitmap.isMutable) return

        val config = bitmap.config ?: return // Can't pool bitmap without config
        val key = getKey(bitmap.width, bitmap.height, config)
        val queue = pool.getOrPut(key) { ArrayDeque(MAX_POOL_SIZE) }

        synchronized(queue) {
            if (queue.size < MAX_POOL_SIZE) {
                queue.addLast(bitmap)
            } else {
                // Pool is full, recycle the bitmap
                bitmap.recycle()
            }
        }
    }

    /**
     * Clear all pooled bitmaps
     */
    fun clear() {
        pool.values.forEach { queue ->
            synchronized(queue) {
                queue.forEach { it.recycle() }
                queue.clear()
            }
        }
        pool.clear()
    }

    private fun getKey(width: Int, height: Int, config: Bitmap.Config): String {
        return "$width-$height-${config.name}"
    }
}
