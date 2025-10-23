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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
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
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CustomImageLoaderTheme {
        Greeting("Android")
    }
}

@Composable
fun ExampleScreen(modifier: Modifier) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val imageLoader = remember { ImageLoader(context) }

    // Prefetch on screen load
    LaunchedEffect(Unit) {
        imageLoader.prefetch("https://images.unsplash.com/photo-1760681554364-50e8cf5efdb5?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxmZWF0dXJlZC1waG90b3MtZmVlZHwxMnx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=60&w=900", scope)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Fade animation with rounded corners
        AsyncImage(
            url = "https://images.unsplash.com/photo-1760681554364-50e8cf5efdb5?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxmZWF0dXJlZC1waG90b3MtZmVlZHwxMnx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=60&w=900",
            contentDescription = "Rounded image",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            transformation = ImageTransformation.RoundedCorners(32f),
            animation = ImageAnimation.FADE
        )

        // Scale animation with circle
        AsyncImage(
            url = "https://images.unsplash.com/photo-1760681554364-50e8cf5efdb5?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxmZWF0dXJlZC1waG90b3MtZmVlZHwxMnx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=60&w=900",
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
            url = "https://images.unsplash.com/photo-1760681554364-50e8cf5efdb5?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxmZWF0dXJlZC1waG90b3MtZmVlZHwxMnx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=60&w=900",
            contentDescription = "Slide up",
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            animation = ImageAnimation.SLIDE_UP
        )

        // Rotate animation with blur
        AsyncImage(
            url = "https://images.unsplash.com/photo-1760681554364-50e8cf5efdb5?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxmZWF0dXJlZC1waG90b3MtZmVlZHwxMnx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=60&w=900",
            contentDescription = "Blurred image",
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            transformation = ImageTransformation.Blur(20f),
            animation = ImageAnimation.ROTATE
        )
    }
}