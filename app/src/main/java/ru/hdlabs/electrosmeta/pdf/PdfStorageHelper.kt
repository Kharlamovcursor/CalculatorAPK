package ru.hdlabs.electrosmeta.pdf

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfStorageHelper {
    private const val SUBDIR = "HDLabs"

    fun buildDisplayName(estimateTitle: String): String {
        val base = estimateTitle.trim().take(48).ifBlank { "smeta" }
            .replace(Regex("[\\\\/:*?\"<>|]"), "_")
        val stamp = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.US).format(Date())
        return "${base}_$stamp.pdf"
    }

    /**
     * Копирует PDF в общую папку «Загрузки/HDLabs».
     * @return человекочитаемый путь для показа пользователю
     */
    fun saveToDownloads(context: Context, source: File, displayName: String): Result<String> {
        if (!source.exists()) {
            return Result.failure(IllegalStateException("Файл PDF не найден"))
        }
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOWNLOADS}/$SUBDIR")
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: error("Не удалось создать файл в Загрузках")
                resolver.openOutputStream(uri)?.use { out ->
                    source.inputStream().use { it.copyTo(out) }
                } ?: error("Не удалось записать PDF")
                values.clear()
                values.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
                "Загрузки/$SUBDIR/$displayName"
            } else {
                @Suppress("DEPRECATION")
                val dir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                    SUBDIR,
                )
                if (!dir.exists() && !dir.mkdirs()) {
                    error("Не удалось создать папку Загрузки/$SUBDIR")
                }
                val dest = File(dir, displayName)
                source.inputStream().use { input ->
                    dest.outputStream().use { output -> input.copyTo(output) }
                }
                "Загрузки/$SUBDIR/$displayName"
            }
        }
    }
}
