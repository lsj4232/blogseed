package com.iplab.blogseed.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iplab.blogseed.MainViewModel
import com.iplab.blogseed.model.BlogDraft
import com.iplab.blogseed.model.Tone
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/*
 * 에디토리얼 레이아웃 규칙(Theme.kt의 컨셉을 화면에 적용한 것)
 *   - 상단은 앱바가 아니라 지면의 제호(masthead). 굵은 세리프 제목 아래 가로줄 하나.
 *   - 각 입력 구역은 카드로 띄우지 않고, 자간을 벌린 작은 머리표 + 가는 구분선으로만 나눈다.
 *     종이 지면처럼 배경이 끊기지 않게 하려는 것이다.
 *   - 좌우 여백은 20dp로 통일해 글줄의 시작선이 화면 전체에서 하나로 맞는다.
 */
private val Gutter = 20.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    vm: MainViewModel,
    padding: PaddingValues,
    onOpenDraft: (String) -> Unit,
    onSettings: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(padding),
        contentPadding = PaddingValues(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { Masthead(onSettings = onSettings) }

        item {
            Column(Modifier.padding(horizontal = Gutter)) {
                SectionLabel("씨앗")
                OutlinedTextField(
                    value = vm.seed,
                    onValueChange = { vm.seed = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("예: 제주 3박4일 뚜벅이 여행") },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    singleLine = false,
                    minLines = 1
                )
            }
        }

        item {
            Column(Modifier.padding(horizontal = Gutter)) {
                SectionLabel("스토리보드")
                Text(
                    "한 줄에 한 장면씩 적으면 순서대로 5개 소제목에 배분된다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = vm.storyboard,
                    onValueChange = { vm.storyboard = it },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    placeholder = { Text("도착 첫날 공항버스\n숙소 근처 시장 저녁\n둘째날 오름 등반\n비 오는 날 실내 카페\n마지막날 공항 가는 길") }
                )
            }
        }

        item {
            Column(Modifier.padding(horizontal = Gutter)) {
                SectionLabel("톤")
                // 톤은 개수가 바뀔 수 있으므로 2개씩 끊지 않고 흐르게 둔다
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Tone.entries.forEach { t ->
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
            Column(Modifier.padding(horizontal = Gutter)) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "엔진 ${vm.provider.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = onSettings) { Text("변경") }
                }
                Button(
                    onClick = { vm.generate { onOpenDraft(it) } },
                    enabled = !vm.isGenerating,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
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
        }

        if (vm.drafts.isNotEmpty()) {
            item {
                Column(Modifier.padding(horizontal = Gutter)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(12.dp))
                    SectionLabel("보관함")
                }
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

/** 지면 제호. 앱 이름을 굵은 세리프로 크게 놓고 아래에 굵은 줄 하나로 지면을 연다. */
@Composable
private fun Masthead(onSettings: () -> Unit) {
    Column(Modifier.padding(start = Gutter, end = 8.dp, top = 16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "블로그 초안",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "씨앗과 장면을 넣으면 5×3 구조로 자란다",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onSettings) {
                Icon(Icons.Filled.Settings, contentDescription = "설정")
            }
        }
        Spacer(Modifier.height(12.dp))
        HorizontalDivider(
            modifier = Modifier.padding(end = 12.dp),
            thickness = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/** 구역 머리표. 자간을 벌린 산세리프라 세리프 본문과 층위가 갈린다. */
@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun DraftRow(draft: BlogDraft, onClick: () -> Unit, onDelete: () -> Unit) {
    Column(Modifier.clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Gutter, end = 8.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    draft.displayTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                val stamp = SimpleDateFormat("MM/dd HH:mm", Locale.KOREA).format(Date(draft.updatedAt))
                val imageState =
                    if (draft.imagesComplete) "이미지 완비" else "이미지 ${draft.missingImageCount}개 소제목 비어 있음"
                Text(
                    "$stamp   $imageState",
                    style = MaterialTheme.typography.bodySmall,
                    // 이미지가 비면 경고색으로 바꿔, 목록만 훑어도 미완성 초안이 드러나게 한다
                    color = if (draft.imagesComplete) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.error
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "삭제",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(start = Gutter, end = Gutter),
            color = MaterialTheme.colorScheme.outlineVariant
        )
    }
}
