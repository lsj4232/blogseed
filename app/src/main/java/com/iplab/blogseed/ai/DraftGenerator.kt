package com.iplab.blogseed.ai

import com.iplab.blogseed.model.GeneratedDraft
import com.iplab.blogseed.model.GeneratedSection
import com.iplab.blogseed.model.Provider
import com.iplab.blogseed.model.Tone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class GenerationException(message: String) : Exception(message)

object DraftGenerator {

    const val SECTION_COUNT = 5
    const val PARAGRAPH_COUNT = 3

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val jsonMedia = "application/json; charset=utf-8".toMediaType()

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    suspend fun generate(
        provider: Provider,
        apiKey: String,
        model: String,
        seed: String,
        storyboard: String,
        tone: Tone
    ): GeneratedDraft = withContext(Dispatchers.IO) {
        if (provider == Provider.OFFLINE) return@withContext OfflineTemplate.build(seed, storyboard, tone)
        if (apiKey.isBlank()) throw GenerationException("${provider.label} API 키가 설정되어 있지 않다. 설정 화면에서 키를 넣거나 오프라인 템플릿을 선택하라.")

        val userMsg = Prompt.user(seed, storyboard, tone)
        val raw = when (provider) {
            Provider.GEMINI -> callGemini(apiKey, model, userMsg)
            Provider.OPENAI -> callOpenAi(apiKey, model, userMsg)
            Provider.ANTHROPIC -> callAnthropic(apiKey, model, userMsg)
            Provider.OFFLINE -> ""
        }
        normalize(parse(raw))
    }

    // ---------- 제공사별 호출 ----------

    private fun callGemini(apiKey: String, model: String, userMsg: String): String {
        val body = buildJsonObject {
            putJsonObject("systemInstruction") {
                putJsonArray("parts") { add(buildJsonObject { put("text", Prompt.SYSTEM) }) }
            }
            putJsonArray("contents") {
                add(buildJsonObject {
                    put("role", "user")
                    putJsonArray("parts") { add(buildJsonObject { put("text", userMsg) }) }
                })
            }
            putJsonObject("generationConfig") {
                put("temperature", 0.8)
                put("responseMimeType", "application/json")
            }
        }
        val req = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent")
            .header("x-goog-api-key", apiKey)
            .post(body.toString().toRequestBody(jsonMedia))
            .build()
        val root = execute(req, "Gemini")
        return root["candidates"]?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("content")?.jsonObject
            ?.get("parts")?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("text")?.jsonPrimitive?.content
            ?: throw GenerationException("Gemini 응답에 본문이 없다. 모델명($model)을 확인하라.")
    }

    private fun callOpenAi(apiKey: String, model: String, userMsg: String): String {
        val body = buildJsonObject {
            put("model", model)
            put("temperature", 0.8)
            putJsonObject("response_format") { put("type", "json_object") }
            putJsonArray("messages") {
                add(buildJsonObject { put("role", "system"); put("content", Prompt.SYSTEM) })
                add(buildJsonObject { put("role", "user"); put("content", userMsg) })
            }
        }
        val req = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .post(body.toString().toRequestBody(jsonMedia))
            .build()
        val root = execute(req, "OpenAI")
        return root["choices"]?.jsonArray?.firstOrNull()
            ?.jsonObject?.get("message")?.jsonObject
            ?.get("content")?.jsonPrimitive?.content
            ?: throw GenerationException("OpenAI 응답에 본문이 없다. 모델명($model)을 확인하라.")
    }

    private fun callAnthropic(apiKey: String, model: String, userMsg: String): String {
        val body = buildJsonObject {
            put("model", model)
            put("max_tokens", 4096)
            put("temperature", 0.8)
            put("system", Prompt.SYSTEM)
            putJsonArray("messages") {
                add(buildJsonObject { put("role", "user"); put("content", userMsg) })
            }
        }
        val req = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .post(body.toString().toRequestBody(jsonMedia))
            .build()
        val root = execute(req, "Anthropic")
        return root["content"]?.jsonArray
            ?.firstOrNull { it.jsonObject["type"]?.jsonPrimitive?.content == "text" }
            ?.jsonObject?.get("text")?.jsonPrimitive?.content
            ?: throw GenerationException("Anthropic 응답에 본문이 없다. 모델명($model)을 확인하라.")
    }

    private fun execute(request: Request, label: String): JsonObject {
        val response = try {
            client.newCall(request).execute()
        } catch (e: Exception) {
            throw GenerationException("$label 연결 실패: ${e.message ?: "네트워크 오류"}")
        }
        response.use {
            val text = it.body?.string().orEmpty()
            if (!it.isSuccessful) {
                throw GenerationException("$label 오류 ${it.code}: ${errorMessage(text)}")
            }
            return runCatching { json.parseToJsonElement(text).jsonObject }
                .getOrElse { throw GenerationException("$label 응답을 해석하지 못했다.") }
        }
    }

    private fun errorMessage(body: String): String {
        val fromJson = runCatching {
            val obj = json.parseToJsonElement(body).jsonObject
            val err = obj["error"]
            when (err) {
                is JsonObject -> err["message"]?.jsonPrimitive?.content
                is JsonPrimitive -> err.content
                else -> obj["message"]?.jsonPrimitive?.content
            }
        }.getOrNull()
        return fromJson ?: body.take(200).ifBlank { "본문 없음" }
    }

    // ---------- 파싱과 정규화 ----------

    /** 모델이 코드펜스나 잡담을 붙여도 JSON 객체만 뽑아낸다. */
    private fun parse(raw: String): GeneratedDraft {
        val start = raw.indexOf('{')
        val end = raw.lastIndexOf('}')
        if (start < 0 || end <= start) throw GenerationException("모델이 JSON을 반환하지 않았다. 다시 생성해 보라.")
        val slice = raw.substring(start, end + 1)
        return runCatching { json.decodeFromString<GeneratedDraft>(slice) }
            .getOrElse { throw GenerationException("초안 JSON 형식이 어긋났다. 다시 생성해 보라.") }
    }

    /** 소제목 5개, 문단 3개를 강제한다. 모자라면 빈칸으로 채워 사용자가 메꾸게 한다. */
    fun normalize(draft: GeneratedDraft): GeneratedDraft {
        val sections = draft.sections.take(SECTION_COUNT).toMutableList()
        while (sections.size < SECTION_COUNT) {
            sections += GeneratedSection(heading = "소제목 ${sections.size + 1}")
        }
        val fixed = sections.mapIndexed { index, s ->
            val paragraphs = s.paragraphs.map { it.trim() }.filter { it.isNotEmpty() }.toMutableList()
            if (paragraphs.size > PARAGRAPH_COUNT) {
                val tail = paragraphs.subList(PARAGRAPH_COUNT - 1, paragraphs.size).joinToString(" ")
                while (paragraphs.size >= PARAGRAPH_COUNT) paragraphs.removeAt(paragraphs.size - 1)
                paragraphs += tail
            }
            while (paragraphs.size < PARAGRAPH_COUNT) paragraphs += ""
            s.copy(
                heading = s.heading.ifBlank { "소제목 ${index + 1}" },
                paragraphs = paragraphs
            )
        }
        return draft.copy(sections = fixed)
    }
}
