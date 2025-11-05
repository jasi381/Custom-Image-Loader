package com.jasmeet.imageloader.ui.theme

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jasmeet.imageloader.decoder.AnimatedImageDecoderFactory
import com.jasmeet.imageloader.utils.ImageAnimation
import com.jasmeet.imageloader.utils.ImageData
import com.jasmeet.imageloader.utils.ImageLoader
import com.jasmeet.imageloader.utils.ImageState
import com.jasmeet.imageloader.utils.ImageTransformation

@Composable
fun AsyncImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    transformation: ImageTransformation = ImageTransformation.None,
    animation: ImageAnimation = ImageAnimation.FADE,
    placeholder: @Composable () -> Unit = {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    },
    error: @Composable (String) -> Unit = { message ->
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Error: $message")
        }
    }
) {
    val context = LocalContext.current
    var imageState by remember { mutableStateOf<ImageState>(ImageState.Loading) }
    val imageLoader = remember { ImageLoader(context) }

    LaunchedEffect(url) {
        imageState = ImageState.Loading
        val data = imageLoader.load(url, transformation)
        imageState = if (data != null) {
            ImageState.Success(data)
        } else {
            ImageState.Error("Failed to load image")
        }
    }

    Box(modifier = modifier) {
        when (val state = imageState) {
            is ImageState.Loading -> placeholder()
            is ImageState.Success -> {
                when (val data = state.data) {
                    is ImageData.StaticImage -> {
                        AnimatedImage(
                            bitmap = data.bitmap,
                            contentDescription = contentDescription,
                            contentScale = contentScale,
                            animation = animation
                        )
                    }
                    is ImageData.AnimatedImage -> {
                        // Create decoder and render using new AnimatedImageRenderer
                        val decoder = remember(data) {
                            AnimatedImageDecoderFactory.createDecoder(data.bytes, data.format)
                        }

                        AnimatedImageRenderer(
                            decoder = decoder,
                            contentDescription = contentDescription,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = contentScale
                        )
                    }
                }
            }
            is ImageState.Error -> error(state.message)
        }
    }
}
