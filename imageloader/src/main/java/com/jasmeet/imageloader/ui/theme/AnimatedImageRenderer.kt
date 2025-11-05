package com.jasmeet.imageloader.ui.theme

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import com.jasmeet.imageloader.decoder.AnimatedImageDecoder
import com.jasmeet.imageloader.decoder.BitmapPool
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Composable for rendering animated images with frame pooling
 * Animations run continuously regardless of scroll or UI state
 */
@Composable
fun AnimatedImageRenderer(
    decoder: AnimatedImageDecoder,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    var currentFrameIndex by remember { mutableIntStateOf(0) }
    var currentFrameBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Cleanup when composable is disposed
    DisposableEffect(decoder) {
        onDispose {
            // Return frame bitmap to pool
            currentFrameBitmap?.let { BitmapPool.recycle(it) }
            decoder.release()
        }
    }

    // Animation loop with frame-aware timing - always running
    LaunchedEffect(decoder) {
        // Get pooled bitmap for rendering
        val frameBitmap = BitmapPool.get(decoder.width, decoder.height)
        currentFrameBitmap = frameBitmap

        while (this.isActive) {
            try {
                // Decode current frame into pooled bitmap
                val frameDuration = decoder.decodeFrame(currentFrameIndex, frameBitmap)

                // Trigger recomposition to show new frame
                currentFrameIndex = (currentFrameIndex + 1) % decoder.frameCount

                // Delay based on actual frame duration from metadata
                delay(frameDuration.coerceAtLeast(16L)) // Min 16ms (60fps cap)
            } catch (e: Exception) {
                e.printStackTrace()
                break
            }
        }
    }

    // Render current frame
    // CRITICAL: Canvas must observe currentFrameIndex to trigger recomposition on frame changes
    Canvas(modifier = modifier.fillMaxSize()) {
        currentFrameBitmap?.let { bitmap ->
            // Force Canvas to observe currentFrameIndex - this triggers recomposition
            // when the frame changes, causing the Canvas to redraw with the updated bitmap
            @Suppress("UNUSED_VARIABLE")
            val observeFrameIndex = currentFrameIndex

            drawIntoCanvas { canvas ->
                val scaleX = size.width / decoder.width
                val scaleY = size.height / decoder.height

                val scale = when (contentScale) {
                    ContentScale.Crop -> maxOf(scaleX, scaleY)
                    ContentScale.Fit -> minOf(scaleX, scaleY)
                    ContentScale.FillWidth -> scaleX
                    ContentScale.FillHeight -> scaleY
                    ContentScale.Inside -> if (decoder.width <= size.width && decoder.height <= size.height) {
                        1f
                    } else {
                        minOf(scaleX, scaleY)
                    }
                    else -> minOf(scaleX, scaleY)
                }

                // Center the image
                val scaledWidth = decoder.width * scale
                val scaledHeight = decoder.height * scale
                val offsetX = (size.width - scaledWidth) / 2
                val offsetY = (size.height - scaledHeight) / 2

                canvas.nativeCanvas.save()
                canvas.nativeCanvas.translate(offsetX, offsetY)
                canvas.nativeCanvas.scale(scale, scale)
                canvas.nativeCanvas.drawBitmap(bitmap, 0f, 0f, null)
                canvas.nativeCanvas.restore()
            }
        }
    }
}
