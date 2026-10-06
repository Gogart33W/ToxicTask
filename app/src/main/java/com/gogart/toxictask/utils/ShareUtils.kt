package com.gogart.toxictask.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ShareUtils {
    fun shareToTikTokOrSystem(context: Context, bitmap: Bitmap) {
        // 1. Save Bitmap to cache directory
        val cachePath = File(context.cacheDir, "shared")
        cachePath.mkdirs()
        val file = File(cachePath, "toxic_status_${System.currentTimeMillis()}.png")
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.close()

        // 2. Get secure content URI using FileProvider
        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        // 3. Create Intent for TikTok specifically
        val tiktokIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            setPackage("com.zhiliaoapp.musically")
        }

        // 4. Check if TikTok is installed
        val packageManager = context.packageManager
        if (tiktokIntent.resolveActivity(packageManager) != null) {
            context.startActivity(tiktokIntent)
        } else {
            // Fallback to standard share sheet if TikTok is missing
            val genericIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(genericIntent, "Share your shame"))
        }
    }
}