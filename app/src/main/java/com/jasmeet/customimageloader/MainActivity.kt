package com.jasmeet.customimageloader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.jasmeet.customimageloader.ui.theme.AsyncImage
import com.jasmeet.customimageloader.ui.theme.CustomImageLoaderTheme
import com.jasmeet.customimageloader.utils.ImageAnimation
import com.jasmeet.customimageloader.utils.ImageLoader
import com.jasmeet.customimageloader.utils.ImageTransformation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CustomImageLoaderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    ExampleScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}


@Composable
fun ExampleScreen(modifier: Modifier) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val imageLoader = remember { ImageLoader(context) }

    // Prefetch on screen load
    LaunchedEffect(Unit) {
        imageLoader.prefetch("https://picsum.photos/400/600", scope)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
        .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Animated GIF (no transformations for GIFs)
        AsyncImage(
            url = "https://media.giphy.com/media/3o7abKhOpu0NwenH3O/giphy.gif",
            contentDescription = "Animated GIF",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        // Fade animation with rounded corners
        AsyncImage(
            url = "https://picsum.photos/400/300",
            contentDescription = "Rounded image",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            transformation = ImageTransformation.RoundedCorners(32f),
            animation = ImageAnimation.FADE
        )

        // Scale animation with circle
        AsyncImage(
            url = "https://picsum.photos/400/400",
            contentDescription = "Circle image",
            modifier = Modifier.size(150.dp),
            transformation = ImageTransformation.Circle(
                borderWidth = 8f,
                borderColor = 0xFF6200EE.toInt()
            ),
            animation = ImageAnimation.SCALE
        )

        // Slide up animation
        AsyncImage(
            url = "https://picsum.photos/500/300",
            contentDescription = "Slide up",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            animation = ImageAnimation.SLIDE_UP
        )

        // Another GIF example
        AsyncImage(
            url = "https://media.giphy.com/media/26BROrSHlmyzzHf3i/giphy.gif",
            contentDescription = "Cat GIF",
            modifier = Modifier
                .size(150.dp)
        )
    }
}