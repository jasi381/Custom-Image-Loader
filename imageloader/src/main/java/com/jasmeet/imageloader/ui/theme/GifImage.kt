package com.jasmeet.imageloader.ui.theme

import android.graphics.Movie
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun GifImage(
    gifBytes: ByteArray,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val movie = remember(gifBytes) {
        Movie.decodeByteArray(gifBytes, 0, gifBytes.size)
    }

    var currentTime by remember { mutableLongStateOf(0L) }

    LaunchedEffect(gifBytes) {
        val startTime = System.currentTimeMillis()
        while (isActive) {
            currentTime = System.currentTimeMillis() - startTime
            delay(16) // ~60fps
        }
    }

    Canvas(modifier = modifier) {
        if (movie != null) {
            val duration = if (movie.duration() > 0) movie.duration() else 1000
            val relTime = (currentTime % duration).toInt()
            movie.setTime(relTime)

            drawIntoCanvas { canvas ->
                val scaleX = size.width / movie.width()
                val scaleY = size.height / movie.height()

                val scale = when (contentScale) {
                    ContentScale.Crop -> maxOf(scaleX, scaleY)
                    ContentScale.Fit -> minOf(scaleX, scaleY)
                    ContentScale.FillWidth -> scaleX
                    ContentScale.FillHeight -> scaleY
                    else -> minOf(scaleX, scaleY)
                }

                canvas.nativeCanvas.save()
                canvas.nativeCanvas.scale(scale, scale)
                movie.draw(canvas.nativeCanvas, 0f, 0f)
                canvas.nativeCanvas.restore()
            }
        }
    }
}
