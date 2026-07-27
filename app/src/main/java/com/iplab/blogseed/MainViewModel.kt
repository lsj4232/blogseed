package com.iplab.blogseed

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.iplab.blogseed.ai.DraftGenerator
import com.iplab.blogseed.ai.GenerationException
import com.iplab.blogseed.data.DraftStore
import com.iplab.blogseed.data.ImageStore
import com.iplab.blogseed.data.Prefs
import com.iplab.blogseed.model.BlogDraft
import com.iplab.blogseed.model.Provider
import com.iplab.blogseed.model.Section
import com.iplab.blogseed.model.Tone
import kotlinx.coroutines.launch
import java.util.UUID

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val store = DraftStore(app)
    val images = ImageStore(app)
    val prefs = Prefs(app)

    var seed by mutableStateOf("")
    var storyboard by mutableStateOf("")
    var tone by mutableStateOf(Tone.FRIENDLY)

    var drafts by mutableStateOf<List<BlogDraft>>(emptyList())
        private set
    var current by mutableStateOf<BlogDraft?>(null)
        private set
    var isGenerating by mutableStateOf(false)
        private set
    var message by mutableStateOf<String?>(null)

    // 설정 화면 표시용 상태
    var provider by mutableStateOf(prefs.provider)
        private set

    init {
        drafts = store.load()
    }

    fun selectProvider(p: Provider) {
        provider = p
        prefs.provider = p
    }

    fun apiKey(p: Provider) = prefs.apiKey(p)
    fun setApiKey(p: Provider, key: String) = prefs.setApiKey(p, key)
    fun model(p: Provider) = prefs.model(p)
    fun setModel(p: Provider, m: String) = prefs.setModel(p, m)

    fun clearMessage() {
        message = null
    }

    /** 초안 생성. 성공하면 새 초안 id를 onDone으로 넘긴다. */
    fun generate(onDone: (String) -> Unit) {
        if (isGenerating) return
        if (seed.isBlank() && storyboard.isBlank()) {
            message = "씨앗 주제나 스토리보드 중 하나는 채워야 한다."
            return
        }
        isGenerating = true
        viewModelScope.launch {
            try {
                val p = provider
                val generated = DraftGenerator.generate(
                    provider = p,
                    apiKey = prefs.apiKey(p),
                    model = prefs.model(p),
                    seed = seed,
                    storyboard = storyboard,
                    tone = tone
                )
                val draft = BlogDraft(
                    id = UUID.randomUUID().toString(),
                    seed = seed,
                    storyboard = storyboard,
                    tone = tone.label,
                    title = generated.title,
                    intro = generated.intro,
                    sections = generated.sections.map {
                        Section(
                            heading = it.heading,
                            paragraphs = it.paragraphs,
                            imageHint = it.imageHint,
                            caption = it.caption
                        )
                    },
                    outro = generated.outro,
                    tags = generated.tags,
                    updatedAt = System.currentTimeMillis(),
                    generatedBy = if (p == Provider.OFFLINE) "오프라인 템플릿" else "${p.label} / ${prefs.model(p)}"
                )
                current = draft
                persist(draft)
                onDone(draft.id)
            } catch (e: GenerationException) {
                message = e.message
            } catch (e: Exception) {
                message = "생성 실패: ${e.message ?: e::class.java.simpleName}"
            } finally {
                isGenerating = false
            }
        }
    }

    fun open(id: String) {
        current = drafts.firstOrNull { it.id == id } ?: current
    }

    fun delete(id: String) {
        drafts.firstOrNull { it.id == id }?.sections?.flatMap { it.images }?.forEach { images.delete(it) }
        drafts = drafts.filterNot { it.id == id }
        store.save(drafts)
        if (current?.id == id) current = null
    }

    // ---------- 편집 ----------

    fun updateTitle(value: String) = mutate { it.copy(title = value) }
    fun updateIntro(value: String) = mutate { it.copy(intro = value) }
    fun updateOutro(value: String) = mutate { it.copy(outro = value) }

    fun updateHeading(sectionIndex: Int, value: String) =
        mutateSection(sectionIndex) { it.copy(heading = value) }

    fun updateParagraph(sectionIndex: Int, paragraphIndex: Int, value: String) =
        mutateSection(sectionIndex) { section ->
            val list = section.paragraphs.toMutableList()
            while (list.size <= paragraphIndex) list += ""
            list[paragraphIndex] = value
            section.copy(paragraphs = list)
        }

    fun updateCaption(sectionIndex: Int, value: String) =
        mutateSection(sectionIndex) { it.copy(caption = value) }

    fun addImageFromUri(sectionIndex: Int, uri: Uri) {
        val path = images.importFromUri(uri)
        if (path == null) {
            message = "이미지를 불러오지 못했다."
            return
        }
        addImagePath(sectionIndex, path)
    }

    fun addImagePath(sectionIndex: Int, path: String) =
        mutateSection(sectionIndex) { it.copy(images = it.images + path) }

    fun removeImage(sectionIndex: Int, path: String) {
        images.delete(path)
        mutateSection(sectionIndex) { it.copy(images = it.images - path) }
    }

    private fun mutateSection(index: Int, block: (Section) -> Section) = mutate { draft ->
        val list = draft.sections.toMutableList()
        if (index !in list.indices) return@mutate draft
        list[index] = block(list[index])
        draft.copy(sections = list)
    }

    private fun mutate(block: (BlogDraft) -> BlogDraft) {
        val draft = current ?: return
        val updated = block(draft).copy(updatedAt = System.currentTimeMillis())
        current = updated
        persist(updated)
    }

    private fun persist(draft: BlogDraft) {
        drafts = (listOf(draft) + drafts.filterNot { it.id == draft.id })
            .sortedByDescending { it.updatedAt }
        store.save(drafts)
    }
}
