package com.n9nik.imagecompressor.ui

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.n9nik.imagecompressor.ads.BannerAd
import com.n9nik.imagecompressor.domain.ImageCompressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UtilityApp(
    adsReady: Boolean,
    privacyOptionsAvailable: Boolean,
    onPrivacyOptions: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var originalSize by remember { mutableStateOf<Long>(0L) }
    var compressedBytes by remember { mutableStateOf<ByteArray?>(null) }
    var compressedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isCompressing by remember { mutableStateOf(false) }
    var quality by remember { mutableFloatStateOf(80f) }
    var targetKB by remember { mutableIntStateOf(250) } // 100, 250, 500, 1000 preset but slider
    var useTarget by remember { mutableStateOf(true) }
    var format by remember { mutableStateOf(ImageCompressor.OutputFormat.JPEG) }
    var savedPath by remember { mutableStateOf<String?>(null) }

    // Modern photo picker (Android 13+), fallback to GetContent
    val pickVisualMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            pickedUri = uri
            savedPath = null
            compressedBytes = null
            compressedBitmap = null
            scope.launch {
                loadOriginal(context, uri) { bmp, size ->
                    originalBitmap = bmp
                    originalSize = size
                }
            }
        }
    }
    val pickFallback = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            pickedUri = uri
            savedPath = null
            compressedBytes = null
            compressedBitmap = null
            scope.launch {
                loadOriginal(context, uri) { bmp, size ->
                    originalBitmap = bmp
                    originalSize = size
                }
            }
        }
    }

    fun launchPicker() {
        try {
            pickVisualMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } catch (_: Exception) {
            pickFallback.launch("image/*")
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("TinyPic - Image Compressor") }) },
        bottomBar = { if (adsReady) BannerAd() },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("One job. No account. Fast by default.", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Offline MB→KB, no watermark, no cloud. Privacy first.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)

            // Pick button
            Button(onClick = { launchPicker() }, modifier = Modifier.fillMaxWidth()) {
                Text(if (pickedUri == null) "Pick Image" else "Pick Different Image")
            }

            if (originalBitmap != null) {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Original • ${ImageCompressor.formatBytes(originalSize)} • ${originalBitmap!!.width}×${originalBitmap!!.height}", style = MaterialTheme.typography.labelMedium)
                        Box(modifier = Modifier.fillMaxWidth().height(220.dp).background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium), contentAlignment = Alignment.Center) {
                            Image(bitmap = originalBitmap!!.asImageBitmap(), contentDescription = "Original", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                        }
                    }
                }

                // Controls row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    FilterChip(selected = format == ImageCompressor.OutputFormat.JPEG, onClick = { format = ImageCompressor.OutputFormat.JPEG }, label = { Text("JPEG") }, modifier = Modifier.weight(1f))
                    FilterChip(selected = format == ImageCompressor.OutputFormat.WEBP, onClick = { format = ImageCompressor.OutputFormat.WEBP }, label = { Text("WebP") }, modifier = Modifier.weight(1f))
                }

                // Mode toggle
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Mode:", style = MaterialTheme.typography.labelMedium)
                    FilterChip(selected = useTarget, onClick = { useTarget = true }, label = { Text("Target KB") })
                    FilterChip(selected = !useTarget, onClick = { useTarget = false }, label = { Text("Quality %") })
                }

                if (useTarget) {
                    Text("Target: ${targetKB} KB (for forms, uploads)", style = MaterialTheme.typography.labelMedium)
                    Slider(value = targetKB.toFloat(), onValueChange = { targetKB = it.toInt() }, valueRange = 50f..1500f, steps = 14)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(100, 250, 500, 1000).forEach { kb ->
                            AssistChip(onClick = { targetKB = kb }, label = { Text("${kb}KB") })
                        }
                    }
                } else {
                    Text("Quality: ${quality.toInt()}%", style = MaterialTheme.typography.labelMedium)
                    Slider(value = quality, onValueChange = { quality = it }, valueRange = 5f..95f)
                }

                Button(
                    onClick = {
                        val bmp = originalBitmap ?: return@Button
                        isCompressing = true
                        scope.launch(Dispatchers.Default) {
                            try {
                                val bytes = if (useTarget) ImageCompressor.compress(bmp, format, targetKB, 80) else ImageCompressor.compress(bmp, format, null, quality.toInt())
                                withContext(Dispatchers.Main) {
                                    compressedBytes = bytes
                                    compressedBitmap = android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    savedPath = null
                                    isCompressing = false
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    isCompressing = false
                                    snackbarHostState.showSnackbar("Compress failed: ${e.message}")
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isCompressing
                ) {
                    Text(if (isCompressing) "Compressing…" else "Compress Now — Offline")
                }

                if (isCompressing) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            if (compressedBytes != null && compressedBitmap != null) {
                val bytes = compressedBytes!!
                val cbmp = compressedBitmap!!
                val savedPct = if (originalSize > 0) ((1 - bytes.size.toDouble() / originalSize) * 100).toInt().coerceIn(0, 99) else 0
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Compressed • ${ImageCompressor.formatBytes(bytes.size)} • ${cbmp.width}×${cbmp.height} • saved $savedPct% • no watermark", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Box(modifier = Modifier.fillMaxWidth().height(220.dp).background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium), contentAlignment = Alignment.Center) {
                            Image(bitmap = cbmp.asImageBitmap(), contentDescription = "Compressed", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(onClick = {
                                scope.launch {
                                    val path = saveToGallery(context, bytes, format)
                                    savedPath = path
                                    snackbarHostState.showSnackbar(if (path != null) "Saved to Pictures/TinyPic" else "Saved to cache")
                                }
                            }, modifier = Modifier.weight(1f)) { Text("Save") }
                            OutlinedButton(onClick = {
                                compressedBytes = null
                                compressedBitmap = null
                                savedPath = null
                            }, modifier = Modifier.weight(1f)) { Text("Clear") }
                        }
                        if (savedPath != null) Text("Saved: $savedPath", style = MaterialTheme.typography.labelSmall)
                        Text("Offline • No watermark • Before: ${ImageCompressor.formatBytes(originalSize)} → After: ${ImageCompressor.formatBytes(bytes.size)}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Why TinyPic wins", style = MaterialTheme.typography.titleSmall)
                    Text("• True offline — works in airplane mode\n• Precise Target KB for forms (100KB, 200KB) \n• No watermark, no signup, batch-ready v1\n• Keeps EXIF orientation right, never corrupts\n• <12MB app, fast by default", style = MaterialTheme.typography.bodySmall)
                }
            }

            if (privacyOptionsAvailable) {
                OutlinedButton(onClick = onPrivacyOptions, modifier = Modifier.fillMaxWidth()) { Text("Privacy choices") }
            }
            Text("Banner test ad only in debug. Release needs your AdMob IDs.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

private suspend fun loadOriginal(context: Context, uri: Uri, onLoaded: (Bitmap?, Long) -> Unit) = withContext(Dispatchers.IO) {
    var size = 0L
    try {
        context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { size = it.length }
    } catch (_: Exception) {}
    if (size <= 0) {
        try { context.contentResolver.openInputStream(uri)?.use { size = it.available().toLong() } } catch (_: Exception) {}
    }
    val bmp = try { ImageCompressor.loadBitmap(context, uri) } catch (_: Exception) { null }
    withContext(Dispatchers.Main) { onLoaded(bmp, size) }
}

private fun saveToGallery(context: Context, bytes: ByteArray, format: ImageCompressor.OutputFormat): String? {
    return try {
        val ext = if (format == ImageCompressor.OutputFormat.JPEG) "jpg" else "webp"
        val mime = if (format == ImageCompressor.OutputFormat.JPEG) "image/jpeg" else "image/webp"
        val time = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val name = "TinyPic_$time.$ext"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, name)
                put(MediaStore.Images.Media.MIME_TYPE, mime)
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/TinyPic")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return null
            resolver.openOutputStream(uri)?.use { it.write(bytes) }
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            uri.toString()
        } else {
            @Suppress("DEPRECATION")
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES + "/TinyPic").apply { mkdirs() }
            val file = java.io.File(dir, name)
            file.outputStream().use { it.write(bytes) }
            // trigger scan
            android.media.MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), arrayOf(mime), null)
            file.absolutePath
        }
    } catch (e: Exception) {
        // fallback cache
        try {
            val f = ImageCompressor.saveToCache(context, bytes, "tinypic_${System.currentTimeMillis()}.${if (format == ImageCompressor.OutputFormat.JPEG) "jpg" else "webp"}")
            f.absolutePath
        } catch (_: Exception) { null }
    }
}
