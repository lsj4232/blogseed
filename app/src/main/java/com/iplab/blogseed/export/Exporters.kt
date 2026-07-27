package com.iplab.blogseed.export

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.util.Base64
import androidx.core.content.FileProvider
import com.iplab.blogseed.model.BlogDraft
import java.io.File
import java.io.FileInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object Exporters {

    // ---------- 텍스트 변환 ----------

    fun toMarkdown(draft: BlogDraft, imageDirName: String = "images"): String = buildString {
        appendLine("# ${draft.displayTitle}")
        appendLine()
        if (draft.intro.isNotBlank()) {
            appendLine(draft.intro.trim())
            appendLine()
        }
        draft.sections.forEach { section ->
            appendLine("## ${section.heading}")
            appendLine()
            section.paragraphs.filter { it.isNotBlank() }.forEach {
                appendLine(it.trim())
                appendLine()
            }
            section.images.forEach { path ->
                val name = File(path).name
                val alt = section.caption.ifBlank { section.heading }
                appendLine("![$alt]($imageDirName/$name)")
                appendLine()
            }
            if (section.caption.isNotBlank() && section.images.isNotEmpty()) {
                appendLine("*${section.caption}*")
                appendLine()
            }
        }
        if (draft.outro.isNotBlank()) {
            appendLine(draft.outro.trim())
            appendLine()
        }
        if (draft.tags.isNotEmpty()) {
            appendLine(draft.tags.joinToString(" ") { "#" + it.replace(" ", "") })
        }
    }

    /**
     * 네이버 블로그(스마트에디터 ONE)용 순수 텍스트. HTML 편집 모드가 없으므로 마크다운 기호를
     * 전부 걷어내고, 사진 들어갈 자리에 [사진 01] 마커만 남긴다. 마커 번호는 갤러리에 저장되는
     * 파일명 앞 두 자리와 같으므로 순서대로 첨부하면 된다.
     */
    fun toNaverText(draft: BlogDraft): String = buildString {
        var imageIndex = 0
        appendLine(draft.displayTitle)
        appendLine()
        if (draft.intro.isNotBlank()) {
            appendLine(draft.intro.trim())
            appendLine()
        }
        draft.sections.forEach { section ->
            appendLine(section.heading.trim())
            appendLine()
            section.images.forEach { _ ->
                imageIndex++
                appendLine("[사진 %02d]".format(imageIndex))
                if (section.caption.isNotBlank()) appendLine(section.caption.trim())
                appendLine()
            }
            if (section.images.isEmpty()) {
                appendLine("[사진 없음]")
                appendLine()
            }
            section.paragraphs.filter { it.isNotBlank() }.forEach {
                appendLine(it.trim())
                appendLine()
            }
        }
        if (draft.outro.isNotBlank()) {
            appendLine(draft.outro.trim())
            appendLine()
        }
        if (draft.tags.isNotEmpty()) {
            appendLine(draft.tags.joinToString(" ") { "#" + it.replace(" ", "") })
        }
    }

    /** 이미지를 base64로 심은 단일 HTML. 티스토리, 워드프레스 HTML 모드용. 네이버는 지원하지 않는다. */
    fun toHtml(draft: BlogDraft): String = buildString {
        appendLine("<!doctype html>")
        appendLine("<html lang=\"ko\"><head><meta charset=\"utf-8\">")
        appendLine("<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">")
        appendLine("<title>${esc(draft.displayTitle)}</title>")
        appendLine("<style>")
        appendLine("body{max-width:720px;margin:0 auto;padding:24px;font-family:-apple-system,'Noto Sans KR',sans-serif;line-height:1.75;color:#1c1c1e}")
        appendLine("h1{font-size:1.8rem;line-height:1.35}h2{font-size:1.25rem;margin-top:2.2rem}")
        appendLine("img{max-width:100%;height:auto;border-radius:12px;display:block;margin:1rem auto}")
        appendLine("figcaption{text-align:center;color:#6b6b70;font-size:.9rem}")
        appendLine(".tags{margin-top:2rem;color:#3a6ea5}")
        appendLine("</style></head><body>")
        appendLine("<h1>${esc(draft.displayTitle)}</h1>")
        if (draft.intro.isNotBlank()) appendLine("<p>${esc(draft.intro)}</p>")
        draft.sections.forEach { section ->
            appendLine("<h2>${esc(section.heading)}</h2>")
            section.paragraphs.filter { it.isNotBlank() }.forEach {
                appendLine("<p>${esc(it)}</p>")
            }
            section.images.forEach { path ->
                val data = base64Of(path) ?: return@forEach
                appendLine("<figure><img src=\"data:image/jpeg;base64,$data\" alt=\"${esc(section.caption.ifBlank { section.heading })}\">")
                if (section.caption.isNotBlank()) appendLine("<figcaption>${esc(section.caption)}</figcaption>")
                appendLine("</figure>")
            }
        }
        if (draft.outro.isNotBlank()) appendLine("<p>${esc(draft.outro)}</p>")
        if (draft.tags.isNotEmpty()) {
            appendLine("<p class=\"tags\">" + draft.tags.joinToString(" ") { "#" + esc(it.replace(" ", "")) } + "</p>")
        }
        appendLine("</body></html>")
    }

    // ---------- 파일 산출 ----------

    fun copyToClipboard(context: Context, text: String, label: String = "블로그 초안") {
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    /** 마크다운 + 이미지 폴더를 zip 한 개로 묶는다. */
    fun writeMarkdownZip(context: Context, draft: BlogDraft): File {
        val dir = exportDir(context)
        val zipFile = File(dir, "${slug(draft.displayTitle)}.zip")
        ZipOutputStream(zipFile.outputStream().buffered()).use { zip ->
            zip.putNextEntry(ZipEntry("${slug(draft.displayTitle)}.md"))
            zip.write(toMarkdown(draft).toByteArray(Charsets.UTF_8))
            zip.closeEntry()
            draft.sections.flatMap { it.images }.distinct().forEach { path ->
                val file = File(path)
                if (!file.exists()) return@forEach
                zip.putNextEntry(ZipEntry("images/${file.name}"))
                FileInputStream(file).use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
        return zipFile
    }

    fun writeMarkdownFile(context: Context, draft: BlogDraft): File {
        val file = File(exportDir(context), "${slug(draft.displayTitle)}.md")
        file.writeText(toMarkdown(draft), Charsets.UTF_8)
        return file
    }

    fun writeHtmlFile(context: Context, draft: BlogDraft): File {
        val file = File(exportDir(context), "${slug(draft.displayTitle)}.html")
        file.writeText(toHtml(draft), Charsets.UTF_8)
        return file
    }

    fun share(context: Context, file: File, mime: String, title: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    // ---------- 보조 ----------

    private fun exportDir(context: Context): File =
        File(context.cacheDir, "exports").apply { mkdirs() }

    private fun base64Of(path: String): String? = runCatching {
        Base64.encodeToString(File(path).readBytes(), Base64.NO_WRAP)
    }.getOrNull()

    private fun esc(s: String): String = s
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")

    private fun slug(title: String): String {
        val cleaned = title.trim().replace(Regex("[\\\\/:*?\"<>|]"), "").replace(Regex("\\s+"), "_")
        return cleaned.take(40).ifBlank { "blog_draft" }
    }
}
