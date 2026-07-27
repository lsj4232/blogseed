package com.iplab.blogseed.ai

import com.iplab.blogseed.model.GeneratedDraft
import com.iplab.blogseed.model.GeneratedSection
import com.iplab.blogseed.model.Tone

/**
 * 키 없이 동작하는 골격 생성기. 스토리보드를 비트 단위로 잘라 5개 소제목에 배분하고,
 * 각 소제목마다 문단 3개(요지, 근거, 정리) 자리를 만들어 준다. 문장 다듬기는 사용자 몫이다.
 */
object OfflineTemplate {

    private val roles = listOf(
        "이 대목의 핵심을 한 문장으로 정리한다.",
        "겪은 일이나 확인한 사실을 근거로 덧붙인다.",
        "독자가 바로 해볼 수 있는 행동으로 마무리한다."
    )

    fun build(seed: String, storyboard: String, tone: Tone): GeneratedDraft {
        val beats = splitBeats(storyboard)
        val buckets = List(DraftGenerator.SECTION_COUNT) { mutableListOf<String>() }
        beats.forEachIndexed { i, beat -> buckets[i % DraftGenerator.SECTION_COUNT] += beat }

        val topic = seed.ifBlank { "이 주제" }
        val sections = buckets.mapIndexed { index, beatsInBucket ->
            val head = beatsInBucket.firstOrNull()?.let { headingOf(it) } ?: "${topic} 이야기 ${index + 1}"
            val paragraphs = (0 until DraftGenerator.PARAGRAPH_COUNT).map { p ->
                val source = beatsInBucket.getOrNull(p)
                if (source != null) "$source\n(${roles[p]})" else "(${roles[p]})"
            }
            GeneratedSection(
                heading = head,
                paragraphs = paragraphs,
                imageHint = "$head 장면을 담은 사진 1장",
                caption = head.take(15)
            )
        }

        return GeneratedDraft(
            title = seed.ifBlank { "블로그 초안" },
            intro = "$topic 에 대해 정리한다. ${tone.label} 톤으로 다듬을 것. (도입 2문장을 여기에 쓴다.)",
            sections = sections,
            outro = "요약 한 문장과 다음 글 예고를 쓴다.",
            tags = listOf(topic.take(12), "블로그", "기록")
        )
    }

    private fun splitBeats(storyboard: String): List<String> =
        storyboard.split('\n', '。')
            .flatMap { line -> line.split(Regex("(?<=[.!?])\\s+")) }
            .map { it.trim().removePrefix("-").removePrefix("*").trim() }
            .filter { it.isNotEmpty() }

    private fun headingOf(beat: String): String {
        val clean = beat.trim().trimEnd('.', '!', '?')
        return if (clean.length <= 24) clean else clean.take(24) + "…"
    }
}
