# Custom Image Loader

[![](https://jitpack.io/v/jasi381/Custom-Image-Loader.svg)](https://jitpack.io/#jasi381/Custom-Image-Loader)
[![API](https://img.shields.io/badge/API-24%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

A powerful, efficient, and modern Android image loading library built with Jetpack Compose. Features multi-level caching, animated image support (GIF, WebP), image transformations, and more.

## Features

- **Async Image Loading** - Load images from URLs with automatic caching
- **Multi-Level Caching** - Memory cache (LRU) + Disk cache for optimal performance
- **Animated Images** - Full support for GIF and WebP animations
- **Image Transformations** - Rounded corners, circles with borders, blur effects
- **Compose-First** - Built specifically for Jetpack Compose
- **Smooth Animations** - Fade, Scale, and Slide-up animations
- **Prefetching** - Preload images for better UX
- **Memory Efficient** - Bitmap pooling and smart memory management
- **Error Handling** - Built-in loading, success, and error states

## Screenshots

<table>
  <tr>
    <td><img src="screenshots/example1.png" width="200"/></td>
    <td><img src="screenshots/example2.png" width="200"/></td>
    <td><img src="screenshots/example3.png" width="200"/></td>
  </tr>
</table>

## Installation

### Step 1: Add JitPack repository

Add the JitPack repository to your project's `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // Add this line
    }
}
```

### Step 2: Add dependency

Add the dependency to your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation("com.github.jasi381:Custom-Image-Loader:1.0.0")
}
```

## Usage

### Basic Usage

```kotlin
import com.jasmeet.imageloader.ui.theme.AsyncImage

@Composable
fun MyScreen() {
    AsyncImage(
        url = "https://example.com/image.jpg",
        contentDescription = "My image",
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}
```

### With Transformations

#### Rounded Corners

```kotlin
AsyncImage(
    url = "https://example.com/image.jpg",
    contentDescription = "Rounded image",
    modifier = Modifier
        .fillMaxWidth()
        .height(200.dp),
    transformation = ImageTransformation.RoundedCorners(cornerRadius = 32f)
)
```

#### Circle with Border

```kotlin
AsyncImage(
    url = "https://example.com/avatar.jpg",
    contentDescription = "Profile picture",
    modifier = Modifier.size(120.dp),
    transformation = ImageTransformation.Circle(
        borderWidth = 4f,
        borderColor = 0xFF6200EE.toInt()
    )
)
```

#### Blur Effect

```kotlin
AsyncImage(
    url = "https://example.com/background.jpg",
    contentDescription = "Blurred background",
    modifier = Modifier.fillMaxSize(),
    transformation = ImageTransformation.Blur(radius = 25f)
)
```

### With Animations

```kotlin
// Fade animation
AsyncImage(
    url = "https://example.com/image.jpg",
    contentDescription = "Fading image",
    modifier = Modifier.size(200.dp),
    animation = ImageAnimation.FADE
)

// Scale animation
AsyncImage(
    url = "https://example.com/image.jpg",
    contentDescription = "Scaling image",
    modifier = Modifier.size(200.dp),
    animation = ImageAnimation.SCALE
)

// Slide up animation
AsyncImage(
    url = "https://example.com/image.jpg",
    contentDescription = "Sliding image",
    modifier = Modifier.size(200.dp),
    animation = ImageAnimation.SLIDE_UP
)
```

### Loading Animated GIFs

```kotlin
AsyncImage(
    url = "https://media.giphy.com/media/3o7abKhOpu0NwenH3O/giphy.gif",
    contentDescription = "Animated GIF",
    modifier = Modifier
        .fillMaxWidth()
        .height(200.dp)
)
```

### Custom Placeholder and Error Images

```kotlin
AsyncImage(
    url = "https://example.com/image.jpg",
    contentDescription = "Image with placeholders",
    modifier = Modifier.size(200.dp),
    placeholderResId = R.drawable.placeholder,
    errorResId = R.drawable.error_image
)
```

### Using ImageLoader Directly

```kotlin
@Composable
fun MyScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val imageLoader = remember { ImageLoader(context) }

    // Prefetch images
    LaunchedEffect(Unit) {
        imageLoader.prefetch("https://example.com/image1.jpg", scope)
        imageLoader.prefetch("https://example.com/image2.jpg", scope)
    }

    // Use AsyncImage as normal
    AsyncImage(
        url = "https://example.com/image1.jpg",
        contentDescription = "Prefetched image",
        modifier = Modifier.fillMaxWidth()
    )
}
```

### Handling Image State

```kotlin
import com.jasmeet.imageloader.utils.ImageState

@Composable
fun MyScreen() {
    var imageState by remember { mutableStateOf<ImageState>(ImageState.Loading) }

    when (imageState) {
        is ImageState.Loading -> {
            CircularProgressIndicator()
        }
        is ImageState.Success -> {
            val bitmap = (imageState as ImageState.Success).bitmap
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Loaded image"
            )
        }
        is ImageState.Error -> {
            Text("Error loading image: ${(imageState as ImageState.Error).message}")
        }
    }
}
```

## Advanced Features

### Caching Strategy

The library uses a two-tier caching system:

1. **Memory Cache** - LRU-based in-memory cache (1/8 of max heap)
2. **Disk Cache** - Persistent storage for downloaded images

The caching is automatic and requires no configuration. Images are first checked in memory, then disk, and finally downloaded if not found.

### Image Transformations

Transformations are applied after the image is loaded but before it's displayed. Available transformations:

- `ImageTransformation.RoundedCorners(cornerRadius: Float)` - Applies rounded corners
- `ImageTransformation.Circle(borderWidth: Float, borderColor: Int)` - Makes the image circular with optional border
- `ImageTransformation.Blur(radius: Float)` - Applies Gaussian blur (requires RenderScript)

### Supported Image Formats

- **Static Images**: JPEG, PNG, WebP
- **Animated Images**: GIF, WebP (animated, API 28+)

The library automatically detects the image format and uses the appropriate decoder.

## Performance

- **Lazy Loading** - Images are loaded only when needed
- **Bitmap Pooling** - Reuses bitmap memory to reduce GC pressure
- **Background Threading** - All network and disk I/O happens off the main thread
- **Efficient Caching** - Minimizes redundant downloads and memory usage

## Requirements

- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 36
- **Jetpack Compose**: BOM 2024.09.00+
- **Kotlin**: 2.0.21+

## License

```
Copyright 2025 Jasmeet Singh

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Author

**Jasmeet Singh** - [@jasi381](https://github.com/jasi381)

## Changelog

### Version 1.0.0 (Initial Release)
- Async image loading with Jetpack Compose
- Multi-level caching (Memory + Disk)
- GIF and WebP animation support
- Image transformations (Rounded corners, Circle, Blur)
- Loading animations (Fade, Scale, Slide-up)
- Prefetching capability
- Error handling and state management

## Support

If you find this library useful, please give it a star ⭐ on GitHub!

For issues and feature requests, please use the [GitHub Issues](https://github.com/jasi381/Custom-Image-Loader/issues) page.
