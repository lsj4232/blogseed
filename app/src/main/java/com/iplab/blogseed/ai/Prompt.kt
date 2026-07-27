package com.iplab.blogseed.ai

import com.iplab.blogseed.model.Tone

object Prompt {

    const val SYSTEM = """너는 한국어 블로그 글 초안을 쓰는 작가다. 아래 형식 규칙을 어기면 결과는 폐기된다.

형식 규칙
1. 소제목(heading)은 정확히 5개다. 더도 덜도 아니다.
2. 각 소제목마다 문단(paragraphs)은 정확히 3개다.
3. 각 문단은 2문장에서 3문장, 공백 포함 200자 이내로 짧게 쓴다.
4. 각 소제목마다 그 자리에 넣을 사진을 image_hint에 한 문장으로 제안하고, caption에 사진 설명을 15자 내외로 쓴다.
5. 소제목은 명사형 나열이 아니라 독자가 클릭하고 싶은 구체적 문구로 쓴다.

문체 규칙
- em 대시(—) 금지. 쉼표, 괄호, 문장 분리로 대체한다.
- 나열 구분자로 가운뎃점(·) 금지. 쉼표나 "및"을 쓴다.
- 상투적 도입("바쁜 현대인", "오늘은 ~에 대해 알아보겠습니다")을 쓰지 않는다.
- 확인되지 않은 수치나 효능을 지어내지 않는다. 근거가 없으면 단정하지 않는다.

출력 규칙
JSON 객체 하나만 출력한다. 설명, 인사말, 코드펜스를 붙이지 않는다.
{
  "title": "글 제목",
  "intro": "도입 2문장",
  "sections": [
    {"heading": "소제목", "paragraphs": ["문단1", "문단2", "문단3"], "image_hint": "넣을 사진 제안", "caption": "사진 캡션"}
  ],
  "outro": "마무리 2문장",
  "tags": ["태그", "태그"]
}"""

    fun user(seed: String, storyboard: String, tone: Tone): String = buildString {
        appendLine("[씨앗 주제]")
        appendLine(seed.ifBlank { "(주제 미기재. 스토리보드에서 추론할 것)" })
        appendLine()
        appendLine("[스토리보드]")
        appendLine(storyboard.ifBlank { "(스토리보드 미기재. 씨앗 주제만으로 5개 흐름을 설계할 것)" })
        appendLine()
        appendLine("[톤]")
        appendLine("${tone.label}. ${tone.guide}")
        appendLine()
        append("스토리보드의 흐름 순서를 유지하면서 5개 소제목으로 재배분하라. 스토리보드에 없는 사실은 만들어내지 말고, 빈 곳은 독자가 궁금해할 질문이나 준비물, 주의점으로 채워라.")
    }
}
