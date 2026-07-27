package com.iplab.blogseed.export

import android.content.ContentValues
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.iplab.blogseed.model.BlogDraft
import java.io.File

/**
 * 초안 이미지를 Pictures/BlogSeed 에 순서대로 저장한다. 파일명 앞 두 자리는 네이버용 텍스트의
 * [사진 01] 마커 번호와 일치하므로, 네이버 앱 사진 첨부 화면에서 순서대로 고르면 된다.
 */
object GalleryExporter {

    const val ALBUM = "BlogSeed"

    /** API 28 이하에서만 저장소 권한이 필요하다. */
    fun needsLegacyPermission(): Boolean = Build.VERSION.SDK_INT <= Build.VERSION_CODES.P

    /** 저장에 성공한 장수를 돌려준다. */
    fun saveAll(context: Context, draft: BlogDraft): Int {
        var index = 0
        var saved = 0
        draft.sections.forEach { section ->
            section.images.forEach { path ->
                index++
                val source = File(path)
                if (!source.exists()) return@forEach
                val name = fileName(index, section.heading)
                val ok = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    saveViaMediaStore(context, source, name)
                } else {
                    saveViaLegacyFile(context, source, name)
                }
                if (ok) saved++
            }
        }
        return saved
    }

    private fun saveViaMediaStore(context: Context, source: File, name: String): Boolean =
        runCatching {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, name)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/$ALBUM")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: return false
            resolver.openOutputStream(uri)?.use { out -> source.inputStream().use { it.copyTo(out) } }
                ?: return false
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            true
        }.getOrDefault(false)

    private fun saveViaLegacyFile(context: Context, source: File, name: String): Boolean =
        runCatching {
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                ALBUM
            ).apply { mkdirs() }
            val target = File(dir, name)
            source.inputStream().use { input -> target.outputStream().use { input.copyTo(it) } }
            MediaScannerConnection.scanFile(context, arrayOf(target.absolutePath), arrayOf("image/jpeg"), null)
            true
        }.getOrDefault(false)

    fun fileName(index: Int, heading: String): String {
        val clean = heading.trim()
            .replace(Regex("[\\\\/:*?\"<>|]"), "")
            .replace(Regex("\\s+"), "_")
            .take(16)
            .ifBlank { "section" }
        return "%02d_%s.jpg".format(index, clean)
    }
}
