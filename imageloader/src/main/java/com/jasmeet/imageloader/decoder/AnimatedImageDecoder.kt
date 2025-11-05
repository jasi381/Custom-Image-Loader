package com.jasmeet.imageloader.decoder

import android.graphics.Bitmap

/**
 * Interface for decoding animated images (GIF, WebP, APNG, etc.)
 * Implements lazy frame decoding and memory-efficient rendering
 */
interface AnimatedImageDecoder {
    /** Total number of frames in the animation */
    val frameCount: Int

    /** Total duration of animation in milliseconds */
    val duration: Long

    /** Width of the animation */
    val width: Int

    /** Height of the animation */
    val height: Int

    /** Whether animation should loop */
    val isLooping: Boolean

    /**
     * Decode a specific frame into the provided bitmap
     * @param frameIndex Zero-based frame index
     * @param outputBitmap Pre-allocated bitmap to render into (pooled)
     * @return Duration this frame should be displayed in ms
     */
    fun decodeFrame(frameIndex: Int, outputBitmap: Bitmap): Long

    /**
     * Get frame duration without decoding
     * @param frameIndex Zero-based frame index
     * @return Duration in milliseconds
     */
    fun getFrameDuration(frameIndex: Int): Long

    /**
     * Release all resources
     */
    fun release()
}