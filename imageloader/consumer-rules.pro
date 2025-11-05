# Consumer ProGuard rules for Custom Image Loader

# Keep public API classes and methods
-keep public class com.jasmeet.imageloader.** { public *; }

# Keep data classes used in the API
-keep class com.jasmeet.imageloader.utils.** { *; }
-keep class com.jasmeet.imageloader.decoder.** { *; }

# Keep Compose UI components
-keep class com.jasmeet.imageloader.ui.** { *; }
