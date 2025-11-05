package com.jasmeet.imageloader.utils

import android.graphics.Bitmap

sealed class ImageState {
    object Loading : ImageState()
    data class Success(val data: ImageData) : ImageState()
    data class Error(val message: String) : ImageState()
}