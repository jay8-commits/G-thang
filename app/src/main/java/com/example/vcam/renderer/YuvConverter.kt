package com.example.vcam.renderer

import android.graphics.Bitmap
import android.graphics.Color
import java.nio.ByteBuffer

object YuvConverter {

    /**
     * Converts an ARGB_8888 Bitmap into an NV21 byte array (YUV420SP).
     * Used by Camera1 PreviewCallback and buffer callbacks.
     */
    fun bitmapToNv21(bitmap: Bitmap, width: Int, height: Int): ByteArray {
        val argb = IntArray(width * height)
        val scaledBitmap = if (bitmap.width != width || bitmap.height != height) {
            Bitmap.createScaledBitmap(bitmap, width, height, true)
        } else {
            bitmap
        }
        scaledBitmap.getPixels(argb, 0, width, 0, 0, width, height)

        val yuv = ByteArray(width * height * 3 / 2)
        encodeYUV420SP(yuv, argb, width, height)
        return yuv
    }

    /**
     * Encodes ARGB int array into NV21 (YUV420 semi-planar with interleaved VU).
     */
    private fun encodeYUV420SP(yuv420sp: ByteArray, argb: IntArray, width: Int, height: Int) {
        val frameSize = width * height
        var yIndex = 0
        var uvIndex = frameSize

        var r: Int
        var g: Int
        var b: Int
        var y: Int
        var u: Int
        var v: Int
        var index = 0

        for (j in 0 until height) {
            for (i in 0 until width) {
                val pixel = argb[index++]
                r = (pixel and 0xff0000) shr 16
                g = (pixel and 0xff00) shr 8
                b = (pixel and 0xff)

                // ITU-R BT.601 conversion formula
                y = ((66 * r + 129 * g + 25 * b + 128) shr 8) + 16
                u = ((-38 * r - 74 * g + 112 * b + 128) shr 8) + 128
                v = ((112 * r - 94 * g - 18 * b + 128) shr 8) + 128

                yuv420sp[yIndex++] = (if (y < 0) 0 else if (y > 255) 255 else y).toByte()

                if (j % 2 == 0 && index % 2 == 0 && uvIndex < yuv420sp.size - 1) {
                    yuv420sp[uvIndex++] = (if (v < 0) 0 else if (v > 255) 255 else v).toByte()
                    yuv420sp[uvIndex++] = (if (u < 0) 0 else if (u > 255) 255 else u).toByte()
                }
            }
        }
    }
}
