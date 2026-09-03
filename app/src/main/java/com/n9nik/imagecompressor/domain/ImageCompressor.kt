package com.n9nik.imagecompressor.domain

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import kotlin.math.roundToInt

/**
 * Offline MB→KB compressor.
 * No cloud, no watermark, no account.
 * Core promise: take an image Uri, produce compressed bytes for JPEG/WEBP at target quality/size.
 */
object ImageCompressor {

    data class CompressResult(
        val bytes: ByteArray,
        val width: Int,
        val height: Int,
        val format: String, // "jpeg" or "webp"
        val qualityUsed: Int,
        val originalBytes: Long,
        val compressedBytes: Long
    ) {
        val savedPercent: Int get() = if (originalBytes <= 0) 0 else ((1 - compressedBytes.toDouble() / originalBytes) * 100).roundToInt().coerceIn(0, 99)
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as CompressResult
            if (!bytes.contentEquals(other.bytes)) return false
            return compressedBytes == other.compressedBytes
        }
        override fun hashCode(): Int {
            var result = bytes.contentHashCode()
            result = 31 * result + compressedBytes.hashCode()
            return result
        }
    }

    enum class OutputFormat { JPEG, WEBP }

    /**
     * Load bitmap safely with sampling to avoid OOM on 20+ MP images.
     * maxDimension: we cap longer side to 4096 by default for preview/compress, preserves aspect.
     */
    fun loadBitmap(context: Context, uri: Uri, maxDimension: Int = 4096): Bitmap? {
        // First decode bounds
        val optsBounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, optsBounds) }
        if (optsBounds.outWidth <= 0 || optsBounds.outHeight <= 0) return null

        var sample = 1
        val longest = maxOf(optsBounds.outWidth, optsBounds.outHeight)
        while (longest / sample > maxDimension) sample *= 2

        val opts = BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val bmp = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) } ?: return null
        return applyExifOrientation(context, uri, bmp)
    }

    fun applyExifOrientation(context: Context, uri: Uri, bitmap: Bitmap): Bitmap {
        return try {
            context.contentResolver.openInputStream(uri)?.use { ins ->
                val exif = ExifInterface(ins)
                val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                val matrix = Matrix()
                when (orientation) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                    ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                    ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                    ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.preScale(-1f, 1f)
                    ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.preScale(1f, -1f)
                    ExifInterface.ORIENTATION_TRANSPOSE -> { matrix.postRotate(90f); matrix.preScale(-1f,1f) }
                    ExifInterface.ORIENTATION_TRANSVERSE -> { matrix.postRotate(270f); matrix.preScale(-1f,1f) }
                }
                if (!matrix.isIdentity) {
                    Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).also {
                        if (it != bitmap) bitmap.recycle()
                    }
                } else bitmap
            } ?: bitmap
        } catch (_: Exception) { bitmap }
    }

    /**
     * Compress to hit targetKB if provided, else use fixed quality.
     * Uses binary-ish search on quality 95→5 to fit target bytes, never upscales.
     */
    fun compress(
        bitmap: Bitmap,
        format: OutputFormat = OutputFormat.JPEG,
        targetKB: Int? = null,
        quality: Int = 80
    ): ByteArray {
        if (targetKB == null) {
            return compressAtQuality(bitmap, format, quality.coerceIn(5, 95))
        }
        val targetBytes = targetKB * 1024L
        // If even at 95 quality it's already under target, return that (don't inflate)
        var low = 5
        var high = 95
        var best: ByteArray = compressAtQuality(bitmap, format, high)
        if (best.size <= targetBytes) return best
        // If even at low quality still over, downscale 15% and retry (one level) to guarantee MB→KB
        if (best.size > targetBytes) {
            // try binary search
            var candidate = best
            var bestFit: ByteArray? = null
            while (low <= high) {
                val mid = (low + high) / 2
                val bytes = compressAtQuality(bitmap, format, mid)
                if (bytes.size <= targetBytes) {
                    bestFit = bytes
                    low = mid + 1 // try higher quality still under target
                } else {
                    high = mid - 1
                }
                candidate = bytes
            }
            if (bestFit != null) return bestFit
            // Still over target at quality 5 → need resize. Scale factor sqrt(target/actual) for area.
            val ratio = kotlin.math.sqrt(targetBytes.toDouble() / candidate.size.toDouble()).coerceIn(0.2, 0.9)
            val scaled = Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).roundToInt().coerceAtLeast(64), (bitmap.height * ratio).roundToInt().coerceAtLeast(64), true)
            val scaledBytes = compressAtQuality(scaled, format, 75)
            if (scaled != bitmap) scaled.recycle()
            return if (scaledBytes.size <= targetBytes) scaledBytes else compressAtQuality(scaled, format, 40) // last ditch
        }
        return best
    }

    private fun compressAtQuality(bitmap: Bitmap, format: OutputFormat, quality: Int): ByteArray {
        val out = ByteArrayOutputStream()
        val compressFormat = when (format) {
            OutputFormat.JPEG -> Bitmap.CompressFormat.JPEG
            OutputFormat.WEBP -> if (android.os.Build.VERSION.SDK_INT >= 30) Bitmap.CompressFormat.WEBP_LOSSY else Bitmap.CompressFormat.WEBP
        }
        bitmap.compress(compressFormat, quality, out)
        return out.toByteArray()
    }

    fun saveToCache(context: Context, bytes: ByteArray, filename: String): File {
        val dir = File(context.cacheDir, "tinypic").apply { mkdirs() }
        val file = File(dir, filename)
        FileOutputStream(file).use { it.write(bytes) }
        return file
    }

    fun formatBytes(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${(bytes / 1024f).let { String.format("%.1f KB", it) }}"
            else -> "${bytes / (1024f * 1024f).let { String.format("%.2f MB", it) }}"
        }
    }

    fun formatBytes(bytes: Int): String = formatBytes(bytes.toLong())
}
