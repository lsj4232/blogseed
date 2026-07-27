package com.iplab.blogseed.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iplab.blogseed.MainViewModel
import com.iplab.blogseed.model.BlogDraft
import com.iplab.blogseed.model.Tone
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    vm: MainViewModel,
    padding: PaddingValues,
    onOpenDraft: (String) -> Unit,
    onSettings: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(padding),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TopAppBar(
                title = { Text("블로그 초안 생성기") },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "설정")
                    }
                }
            )
        }

        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("씨앗(seed)", style = MaterialTheme.typography.labelLarge)
                OutlinedTextField(
                    value = vm.seed,
                    onValueChange = { vm.seed = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("예: 제주 3박4일 뚜벅이 여행") },
                    singleLine = false,
                    minLines = 1
                )
            }
        }

        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("스토리보드", style = MaterialTheme.typography.labelLarge)
                Text(
                    "한 줄에 한 장면씩 적으면 순서대로 5개 소제목에 배분된다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = vm.storyboard,
                    onValueChange = { vm.storyboard = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 160.dp),
                    placeholder = { Text("도착 첫날 공항버스\n숙소 근처 시장 저녁\n둘째날 오름 등반\n비 오는 날 실내 카페\n마지막날 공항 가는 길") }
                )
            }
        }

        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("톤", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Tone.entries.take(2).forEach { t ->
                        FilterChip(
                            selected = vm.tone == t,
                            onClick = { vm.tone = t },
                            label = { Text(t.label) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Tone.entries.drop(2).forEach { t ->
                        FilterChip(
                            selected = vm.tone == t,
                            onClick = { vm.tone = t },
                            label = { Text(t.label) }
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "엔진: ${vm.provider.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onSettings) { Text("변경") }
            }
        }

        item {
            Button(
                onClick = { vm.generate { onOpenDraft(it) } },
                enabled = !vm.isGenerating,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            ) {
                if (vm.isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Text("  초안 쓰는 중")
                } else {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                    Text("  초안 생성 (소제목 5개)")
                }
            }
        }

        if (vm.drafts.isNotEmpty()) {
            item {
                Text(
                    "저장된 초안",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                )
            }
            items(vm.drafts, key = { it.id }) { draft ->
                DraftRow(
                    draft = draft,
                    onClick = { onOpenDraft(draft.id) },
                    onDelete = { vm.delete(draft.id) }
                )
            }
        }
    }
}

@Composable
private fun DraftRow(draft: BlogDraft, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(draft.displayTitle, fontWeight = FontWeight.SemiBold)
                val stamp = SimpleDateFormat("MM/dd HH:mm", Locale.KOREA).format(Date(draft.updatedAt))
                val imageState =
                    if (draft.imagesComplete) "이미지 완비" else "이미지 ${draft.missingImageCount}개 소제목 비어 있음"
                Text(
                    "$stamp | $imageState",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "삭제")
            }
        }
    }
}
