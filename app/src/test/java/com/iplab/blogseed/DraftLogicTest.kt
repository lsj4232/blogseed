package com.iplab.blogseed

import com.iplab.blogseed.ai.DraftGenerator
import com.iplab.blogseed.ai.OfflineTemplate
import com.iplab.blogseed.export.Exporters
import com.iplab.blogseed.export.GalleryExporter
import com.iplab.blogseed.model.BlogDraft
import com.iplab.blogseed.model.GeneratedDraft
import com.iplab.blogseed.model.GeneratedSection
import com.iplab.blogseed.model.Section
import com.iplab.blogseed.model.Tone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DraftLogicTest {

    @Test
    fun `오프라인 템플릿은 소제목 5개와 문단 3개를 만든다`() {
        val draft = OfflineTemplate.build(
            seed = "제주 뚜벅이 여행",
            storyboard = "공항버스 도착\n시장 저녁\n오름 등반\n비 오는 날 카페\n공항 가는 길",
            tone = Tone.FRIENDLY
        )
        assertEquals(5, draft.sections.size)
        draft.sections.forEach { assertEquals(3, it.paragraphs.size) }
    }

    @Test
    fun `문단이 넘치면 3개로 접고 모자라면 빈칸으로 채운다`() {
        val over = GeneratedDraft(
            sections = List(7) { GeneratedSection(heading = "h$it", paragraphs = List(5) { p -> "p$p" }) }
        )
        val fixed = DraftGenerator.normalize(over)
        assertEquals(5, fixed.sections.size)
        fixed.sections.forEach { assertEquals(3, it.paragraphs.size) }
        assertTrue(fixed.sections.first().paragraphs[2].contains("p4"))

        val under = GeneratedDraft(sections = listOf(GeneratedSection(heading = "하나", paragraphs = listOf("한 문단"))))
        val padded = DraftGenerator.normalize(under)
        assertEquals(5, padded.sections.size)
        assertEquals(3, padded.sections[0].paragraphs.size)
        assertEquals("", padded.sections[0].paragraphs[2])
    }

    @Test
    fun `마크다운은 소제목마다 이미지 링크를 붙인다`() {
        val draft = BlogDraft(
            id = "t",
            title = "테스트 글",
            intro = "도입",
            sections = listOf(
                Section(
                    heading = "첫 소제목",
                    paragraphs = listOf("가", "나", "다"),
                    caption = "캡션",
                    images = listOf("/data/user/0/app/files/images/img_1.jpg")
                )
            ),
            outro = "마무리",
            tags = listOf("제주", "여행")
        )
        val md = Exporters.toMarkdown(draft)
        assertTrue(md.startsWith("# 테스트 글"))
        assertTrue(md.contains("## 첫 소제목"))
        assertTrue(md.contains("![캡션](images/img_1.jpg)"))
        assertTrue(md.contains("#제주 #여행"))
    }

    @Test
    fun `네이버용 텍스트는 마크다운 기호 없이 사진 마커를 번호대로 넣는다`() {
        val draft = BlogDraft(
            id = "t",
            title = "제주 여행",
            intro = "도입",
            sections = listOf(
                Section(heading = "첫째", paragraphs = listOf("가", "나", "다"), caption = "공항", images = listOf("/x/a.jpg")),
                Section(heading = "둘째", paragraphs = listOf("라", "마", "바"), images = listOf("/x/b.jpg", "/x/c.jpg")),
                Section(heading = "셋째", paragraphs = listOf("사", "아", "자"))
            )
        )
        val text = Exporters.toNaverText(draft)
        assertTrue(!text.contains("#"))
        assertTrue(!text.contains("!["))
        assertTrue(text.contains("[사진 01]"))
        assertTrue(text.contains("[사진 02]"))
        assertTrue(text.contains("[사진 03]"))
        assertTrue(text.contains("[사진 없음]"))
        assertTrue(text.indexOf("첫째") < text.indexOf("[사진 01]"))
    }

    @Test
    fun `갤러리 파일명 번호는 사진 마커 번호와 맞물린다`() {
        assertEquals("01_첫째_소제목.jpg", GalleryExporter.fileName(1, "첫째 소제목"))
        assertEquals("12_section.jpg", GalleryExporter.fileName(12, "   "))
        assertTrue(!GalleryExporter.fileName(3, "제목/에:금지*문자").contains("/"))
    }

    @Test
    fun `이미지 없는 소제목 수를 센다`() {
        val draft = BlogDraft(
            id = "t",
            sections = listOf(
                Section(heading = "a", images = listOf("/x/1.jpg")),
                Section(heading = "b"),
                Section(heading = "c")
            )
        )
        assertEquals(2, draft.missingImageCount)
        assertTrue(!draft.imagesComplete)
    }
}
