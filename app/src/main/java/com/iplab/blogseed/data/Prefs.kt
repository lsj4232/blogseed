package com.iplab.blogseed.data

import android.content.Context
import com.iplab.blogseed.model.Provider

/**
 * API 키는 앱 전용(private) SharedPreferences에만 저장된다. 루팅되지 않은 기기에서는
 * 다른 앱이 읽을 수 없다. 서버로 전송하지 않으며 선택한 제공사 엔드포인트로만 나간다.
 */
class Prefs(context: Context) {

    private val sp = context.applicationContext
        .getSharedPreferences("blogseed_prefs", Context.MODE_PRIVATE)

    var provider: Provider
        get() = runCatching { Provider.valueOf(sp.getString(KEY_PROVIDER, null) ?: "") }
            .getOrDefault(Provider.GEMINI)
        set(value) = sp.edit().putString(KEY_PROVIDER, value.name).apply()

    fun apiKey(p: Provider): String = sp.getString("api_key_${p.name}", "") ?: ""

    fun setApiKey(p: Provider, key: String) {
        sp.edit().putString("api_key_${p.name}", key.trim()).apply()
    }

    fun model(p: Provider): String =
        sp.getString("model_${p.name}", null)?.takeIf { it.isNotBlank() } ?: p.defaultModel

    fun setModel(p: Provider, model: String) {
        sp.edit().putString("model_${p.name}", model.trim()).apply()
    }

    private companion object {
        const val KEY_PROVIDER = "provider"
    }
}
