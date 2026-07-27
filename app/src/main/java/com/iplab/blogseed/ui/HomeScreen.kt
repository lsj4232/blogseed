package com.iplab.blogseed.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.iplab.blogseed.MainViewModel
import com.iplab.blogseed.model.BlogDraft
import com.iplab.blogseed.model.Tone
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Gutter = 16.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    vm: MainViewModel,
    padding: PaddingValues,
    onOpenDraft: (String) -> Unit,
    onSettings: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0xFF123A29), SeedCanvas),
                    radius = 900f
                )
            )
            .padding(padding),
        contentPadding = PaddingValues(bottom = 40.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { Masthead(onSettings = onSettings) }

        item {
            DashboardCard {
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
            DashboardCard {
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
            DashboardCard {
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
            DashboardCard {
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
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = SeedMint,
                        contentColor = Color(0xFF002114)
                    )
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
                        Text("  초안 생성", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        if (vm.drafts.isNotEmpty()) {
            item {
                Column(Modifier.padding(horizontal = Gutter)) {
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

@Composable
private fun Masthead(onSettings: () -> Unit) {
    Column(Modifier.padding(start = Gutter, end = 8.dp, top = 22.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "BlogSeed",
                    style = MaterialTheme.typography.displaySmall,
                    color = Color.White
                )
                Text(
                    "아이디어를 발행 가능한 초안으로",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onSettings) {
                Icon(Icons.Filled.Settings, contentDescription = "설정")
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            "CREATE  •  EDIT  •  PUBLISH",
            style = MaterialTheme.typography.labelSmall,
            color = SeedMint
        )
    }
}

/** 구역 머리표. 자간을 벌린 산세리프라 세리프 본문과 층위가 갈린다. */
@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = SeedMint,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun DashboardCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Gutter),
        colors = CardDefaults.cardColors(containerColor = SeedPanel.copy(alpha = 0.94f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            content = content
        )
    }
}

@Composable
private fun DraftRow(draft: BlogDraft, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Gutter).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = SeedPanel),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
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
    }
}
