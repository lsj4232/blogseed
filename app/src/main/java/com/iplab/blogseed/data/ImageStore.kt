package com.iplab.blogseed.data

import android.content.Context
import android.net.Uri
import java.io.File
import java.util.UUID

/**
 * 갤러리/카메라에서 받은 이미지를 앱 내부 저장소로 복사한다. 사진 선택기가 준 URI 권한은
 * 프로세스 종료 후 사라지므로, 선택 즉시 복사해야 초안을 나중에 열어도 이미지가 남는다.
 */
class ImageStore(context: Context) {

    private val app = context.applicationContext
    private val dir = File(app.filesDir, "images").apply { mkdirs() }

    /** 카메라 촬영 결과를 받을 빈 파일. */
    fun newCameraFile(): File = File(dir, "cam_${UUID.randomUUID()}.jpg")

    /** 외부 URI 내용을 내부 파일로 복사하고 절대경로를 돌려준다. 실패 시 null. */
    fun importFromUri(uri: Uri): String? = runCatching {
        val target = File(dir, "img_${UUID.randomUUID()}.jpg")
        app.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input)
            target.outputStream().use { output -> input.copyTo(output) }
        }
        target.absolutePath
    }.getOrNull()

    fun delete(path: String) {
        runCatching { File(path).takeIf { it.parentFile == dir }?.delete() }
    }
}
