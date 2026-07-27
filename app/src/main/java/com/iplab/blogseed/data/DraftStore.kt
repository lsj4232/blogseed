package com.iplab.blogseed.data

import android.content.Context
import com.iplab.blogseed.model.BlogDraft
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

/** 초안 목록을 filesDir/drafts.json 한 파일에 보관한다. */
class DraftStore(context: Context) {

    private val file = File(context.applicationContext.filesDir, "drafts.json")
    private val json = Json { ignoreUnknownKeys = true; prettyPrint = true }

    private val serializer = ListSerializer(BlogDraft.serializer())

    fun load(): List<BlogDraft> {
        if (!file.exists()) return emptyList()
        return runCatching { json.decodeFromString(serializer, file.readText()) }
            .getOrDefault(emptyList())
            .sortedByDescending { it.updatedAt }
    }

    fun save(drafts: List<BlogDraft>) {
        runCatching { file.writeText(json.encodeToString(serializer, drafts)) }
    }
}
