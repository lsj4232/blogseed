package com.iplab.blogseed.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 소제목 1개 = 문단 3개 + 이미지 1개 이상. */
@Serializable
data class Section(
    val heading: String = "",
    val paragraphs: List<String> = emptyList(),
    /** 어떤 이미지를 넣으면 좋을지에 대한 힌트(모델이 제안). */
    @SerialName("image_hint") val imageHint: String = "",
    /** 이미지 캡션. */
    val caption: String = "",
    /** 앱 내부 저장소의 이미지 파일 절대경로 목록. */
    val images: List<String> = emptyList()
)

@Serializable
data class BlogDraft(
    val id: String,
    val seed: String = "",
    val storyboard: String = "",
    val tone: String = "",
    val title: String = "",
    val intro: String = "",
    val sections: List<Section> = emptyList(),
    val outro: String = "",
    val tags: List<String> = emptyList(),
    val updatedAt: Long = 0L,
    /** 초안을 만든 방식(모델명 또는 "오프라인 템플릿"). */
    val generatedBy: String = ""
) {
    val displayTitle: String
        get() = title.ifBlank { seed.ifBlank { "제목 없는 초안" } }

    /** 소제목 5개 전부가 이미지 1개 이상을 갖췄는지. */
    val imagesComplete: Boolean
        get() = sections.isNotEmpty() && sections.all { it.images.isNotEmpty() }

    val missingImageCount: Int
        get() = sections.count { it.images.isEmpty() }
}

/** 모델이 반환하는 JSON 스키마(이미지 경로 없음). */
@Serializable
data class GeneratedDraft(
    val title: String = "",
    val intro: String = "",
    val sections: List<GeneratedSection> = emptyList(),
    val outro: String = "",
    val tags: List<String> = emptyList()
)

@Serializable
data class GeneratedSection(
    val heading: String = "",
    val paragraphs: List<String> = emptyList(),
    @SerialName("image_hint") val imageHint: String = "",
    val caption: String = ""
)

enum class Provider(val label: String, val defaultModel: String, val keyHelp: String) {
    GEMINI(
        "Google Gemini",
        "gemini-3.5-flash",
        "aistudio.google.com/apikey 에서 무료 키 발급, 카드 등록 불필요. 무료 티어 기준 분당 10회, 하루 1500회. " +
            "한도에 걸리면(429) 모델명을 gemini-3.1-flash-lite 로 바꾸면 분당 15회까지 쓴다. " +
            "무료 티어 입력은 구글 모델 학습에 쓰일 수 있으므로 미공개 원고는 유료 티어를 쓰라."
    ),
    OPENAI(
        "OpenAI",
        "gpt-4o-mini",
        "platform.openai.com/api-keys. ChatGPT Plus 구독과 별도로 크레딧 충전이 필요하다."
    ),
    ANTHROPIC(
        "Anthropic Claude",
        "claude-sonnet-5",
        "console.anthropic.com 에서 키 발급."
    ),
    OFFLINE(
        "오프라인 템플릿 (키 불필요)",
        "-",
        "네트워크 없이 스토리보드를 5개 소제목 골격으로 배분한다. 문장 다듬기는 직접 해야 한다."
    );
}

enum class Tone(val label: String, val guide: String) {
    FRIENDLY("친근한 정보글", "독자에게 말 걸듯 편안한 존댓말. 전문용어는 한 줄로 풀어 설명한다."),
    PROFESSIONAL("전문가 리뷰", "담백한 서술체. 근거와 수치를 앞세우고 과장 표현을 쓰지 않는다."),
    STORY("경험 기록", "1인칭 경험담. 시간 흐름과 장면 묘사를 살린다."),
    MARKETING("제품/서비스 소개", "이점 중심. 다만 과장 광고 표현과 단정적 효능 주장은 피한다.")
}
