package com.jasmeet.imageloader.ui.theme

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import com.jasmeet.imageloader.utils.ImageAnimation

@Composable
internal fun AnimatedImage(
    bitmap: Bitmap,
    contentDescription: String?,
    contentScale: ContentScale,
    animation: ImageAnimation
) {
    val visible = remember { mutableStateOf(false) }

    LaunchedEffect(bitmap) {
        visible.value = true
    }

    when (animation) {
        ImageAnimation.NONE -> {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = contentScale
            )
        }
        ImageAnimation.FADE -> {
            AnimatedVisibility(
                visible = visible.value,
                enter = fadeIn(animationSpec = tween(animation.durationMillis))
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale
                )
            }
        }
        ImageAnimation.SCALE -> {
            val scale by animateFloatAsState(
                targetValue = if (visible.value) 1f else 0.8f,
                animationSpec = tween(animation.durationMillis),
                label = "scale"
            )
            val alpha by animateFloatAsState(
                targetValue = if (visible.value) 1f else 0f,
                animationSpec = tween(animation.durationMillis),
                label = "alpha"
            )

            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scale)
                    .graphicsLayer { this.alpha = alpha },
                contentScale = contentScale
            )
        }
        ImageAnimation.SLIDE_UP -> {
            AnimatedVisibility(
                visible = visible.value,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(animation.durationMillis)
                ) + fadeIn(animationSpec = tween(animation.durationMillis))
            ) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = contentDescription,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = contentScale
                )
            }
        }
        ImageAnimation.ROTATE -> {
            val rotation by animateFloatAsState(
                targetValue = if (visible.value) 0f else -90f,
                animationSpec = tween(animation.durationMillis),
                label = "rotation"
            )
            val alpha by animateFloatAsState(
                targetValue = if (visible.value) 1f else 0f,
                animationSpec = tween(animation.durationMillis),
                label = "alpha"
            )

            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(rotation)
                    .graphicsLayer { this.alpha = alpha },
                contentScale = contentScale
            )
        }
    }
}