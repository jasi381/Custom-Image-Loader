package com.jasmeet.customimageloader.utils

import android.graphics.Bitmap

sealed class ImageState {
    object Loading : ImageState()
    data class Success(val bitmap: Bitmap) : ImageState()
    data class Error(val message: String) : ImageState()
}