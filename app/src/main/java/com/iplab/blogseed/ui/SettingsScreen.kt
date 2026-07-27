package com.iplab.blogseed.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.iplab.blogseed.MainViewModel
import com.iplab.blogseed.model.Provider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: MainViewModel, padding: PaddingValues, onBack: () -> Unit) {
    val selected = vm.provider
    var keyInput by remember(selected) { mutableStateOf(vm.apiKey(selected)) }
    var modelInput by remember(selected) { mutableStateOf(vm.model(selected)) }
    var showKey by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(padding),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            TopAppBar(
                title = { Text("설정") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                }
            )
        }

        item {
            Text("생성 엔진", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 16.dp))
        }

        for (p in Provider.entries) {
            item(key = p.name) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(selected = selected == p, onClick = { vm.selectProvider(p) })
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = selected == p, onClick = { vm.selectProvider(p) })
                    Column(Modifier.padding(start = 4.dp)) {
                        Text(p.label)
                        Text(
                            p.keyHelp,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (selected != Provider.OFFLINE) {
            item {
                Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Column(
                        Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = keyInput,
                            onValueChange = {
                                keyInput = it
                                vm.setApiKey(selected, it)
                            },
                            label = { Text("${selected.label} API 키") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            visualTransformation = if (showKey) VisualTransformation.None
                            else PasswordVisualTransformation()
                        )
                        TextButton(onClick = { showKey = !showKey }) {
                            Text(if (showKey) "키 가리기" else "키 보기")
                        }
                        OutlinedTextField(
                            value = modelInput,
                            onValueChange = {
                                modelInput = it
                                vm.setModel(selected, it)
                            },
                            label = { Text("모델명") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        Text(
                            "키는 이 기기의 앱 전용 저장소에만 남는다. 다른 서버로 보내지 않으며 선택한 제공사 주소로만 요청이 나간다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Text(
                "ChatGPT Plus 구독은 API 사용권을 포함하지 않는다. OpenAI를 쓰려면 platform.openai.com 에서 별도 크레딧이 필요하다. 결제 없이 쓰려면 Gemini 무료 키 또는 오프라인 템플릿을 선택하라.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
