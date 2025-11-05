# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep library classes
-keep class com.jasmeet.imageloader.** { *; }

# Keep public API
-keepclassmembers class com.jasmeet.imageloader.** {
    public *;
}

# Keep Compose components
-dontwarn androidx.compose.**
