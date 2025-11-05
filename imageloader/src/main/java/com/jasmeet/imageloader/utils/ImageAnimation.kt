package com.jasmeet.imageloader.utils

enum class ImageAnimation(val durationMillis: Int) {
    NONE(0),
    FADE(300),
    SCALE(400),
    SLIDE_UP(350),
    ROTATE(500)
}