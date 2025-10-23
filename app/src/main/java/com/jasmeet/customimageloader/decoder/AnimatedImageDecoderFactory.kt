package com.jasmeet.customimageloader.decoder

import android.os.Build
import com.jasmeet.customimageloader.utils.AnimatedFormat

/**
 * Factory for creating appropriate decoder based on image format
 */
object AnimatedImageDecoderFactory {

    fun createDecoder(bytes: ByteArray, format: AnimatedFormat): AnimatedImageDecoder {
        return when (format) {
            AnimatedFormat.GIF -> GifDecoder(bytes)
            AnimatedFormat.WEBP -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    WebPDecoder(bytes)
                } else {
                    WebPDecoderLegacy(bytes)
                }
            }
        }
    }

    /**
     * Detect animated format from byte signature
     */
    fun detectFormat(bytes: ByteArray): AnimatedFormat? {
        if (bytes.size < 12) return null

        // GIF signature: "GIF87a" or "GIF89a"
        if (bytes[0] == 'G'.code.toByte() &&
            bytes[1] == 'I'.code.toByte() &&
            bytes[2] == 'F'.code.toByte()) {
            return AnimatedFormat.GIF
        }

        // WebP signature: "RIFF" + "WEBP" + "VP8X" (extended, includes animation)
        if (bytes[0] == 'R'.code.toByte() &&
            bytes[1] == 'I'.code.toByte() &&
            bytes[2] == 'F'.code.toByte() &&
            bytes[3] == 'F'.code.toByte() &&
            bytes[8] == 'W'.code.toByte() &&
            bytes[9] == 'E'.code.toByte() &&
            bytes[10] == 'B'.code.toByte() &&
            bytes[11] == 'P'.code.toByte()) {

            // Check for animation flag in VP8X chunk
            if (bytes.size >= 21) {
                val flags = bytes[20].toInt()
                val hasAnimation = (flags and 0x02) != 0
                if (hasAnimation) return AnimatedFormat.WEBP
            }
        }

        return null
    }
}
