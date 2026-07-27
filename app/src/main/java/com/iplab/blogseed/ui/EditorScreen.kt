package com.iplab.blogseed.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.iplab.blogseed.MainViewModel
import com.iplab.blogseed.export.Exporters
import com.iplab.blogseed.export.GalleryExporter
import com.iplab.blogseed.model.Section
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(vm: MainViewModel, padding: PaddingValues, onBack: () -> Unit) {
    val draft = vm.current
    val context = LocalContext.current
    var targetSection by remember { mutableIntStateOf(0) }
    var cameraFile by remember { mutableStateOf<File?>(null) }
    var menuOpen by remember { mutableStateOf(false) }

    val pickImage = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri -> uri?.let { vm.addImageFromUri(targetSection, it) } }

    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val file = cameraFile
        if (success && file != null && file.exists()) vm.addImagePath(targetSection, file.absolutePath)
    }

    val saveToGallery: () -> Unit = {
        val d = vm.current
        if (d == null) {
            vm.message = "저장할 초안이 없다."
        } else {
            val saved = GalleryExporter.saveAll(context, d)
            vm.message = if (saved > 0) {
                "사진 ${saved}장을 갤러리 ${GalleryExporter.ALBUM} 앨범에 저장했다. 번호 순서대로 첨부하면 된다."
            } else {
                "저장할 이미지가 없거나 저장에 실패했다."
            }
        }
    }

    val requestStorage = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) saveToGallery() else vm.message = "저장소 권한이 없으면 갤러리에 넣을 수 없다."
    }

    if (draft == null) {
        Column(Modifier.padding(padding).padding(24.dp)) {
            Text("열린 초안이 없다.")
            FilledTonalButton(onClick = onBack) { Text("돌아가기") }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxWidth().padding(padding),
        contentPadding = PaddingValues(bottom = 48.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            TopAppBar(
                title = { Text("초안 편집") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        Exporters.copyToClipboard(context, Exporters.toMarkdown(draft))
                        vm.message = "마크다운을 클립보드에 복사했다."
                    }) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = "복사")
                    }
                    Box {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Filled.Share, contentDescription = "내보내기")
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            DropdownMenuItem(
                                text = { Text("네이버용 텍스트 복사") },
                                onClick = {
                                    menuOpen = false
                                    Exporters.copyToClipboard(context, Exporters.toNaverText(draft), "네이버 초안")
                                    vm.message = "네이버용 텍스트를 복사했다. [사진 01] 자리에 사진을 넣으면 된다."
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("사진 갤러리에 저장") },
                                onClick = {
                                    menuOpen = false
                                    if (GalleryExporter.needsLegacyPermission()) {
                                        requestStorage.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    } else {
                                        saveToGallery()
                                    }
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("마크다운 파일(.md)") },
                                onClick = {
                                    menuOpen = false
                                    runCatching {
                                        val f = Exporters.writeMarkdownFile(context, draft)
                                        Exporters.share(context, f, "text/markdown", draft.displayTitle)
                                    }.onFailure { vm.message = "내보내기 실패: ${it.message}" }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("마크다운 + 이미지(.zip)") },
                                onClick = {
                                    menuOpen = false
                                    runCatching {
                                        val f = Exporters.writeMarkdownZip(context, draft)
                                        Exporters.share(context, f, "application/zip", draft.displayTitle)
                                    }.onFailure { vm.message = "내보내기 실패: ${it.message}" }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("HTML 파일(이미지 내장)") },
                                onClick = {
                                    menuOpen = false
                                    runCatching {
                                        val f = Exporters.writeHtmlFile(context, draft)
                                        Exporters.share(context, f, "text/html", draft.displayTitle)
                                    }.onFailure { vm.message = "내보내기 실패: ${it.message}" }
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("HTML 소스 복사") },
                                onClick = {
                                    menuOpen = false
                                    Exporters.copyToClipboard(context, Exporters.toHtml(draft), "HTML")
                                    vm.message = "HTML 소스를 클립보드에 복사했다."
                                }
                            )
                        }
                    }
                }
            )
        }

        if (!draft.imagesComplete) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        "소제목 ${draft.missingImageCount}개에 이미지가 없다. 소제목마다 최소 1장이 필요하다.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = draft.title,
                    onValueChange = vm::updateTitle,
                    label = { Text("제목") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = draft.intro,
                    onValueChange = vm::updateIntro,
                    label = { Text("도입") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 96.dp)
                )
                Text(
                    "생성 엔진: ${draft.generatedBy}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        itemsIndexedSections(draft.sections) { index, section ->
            SectionCard(
                index = index,
                section = section,
                onHeading = { vm.updateHeading(index, it) },
                onParagraph = { p, v -> vm.updateParagraph(index, p, v) },
                onCaption = { vm.updateCaption(index, it) },
                onPickGallery = {
                    targetSection = index
                    pickImage.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onCamera = {
                    targetSection = index
                    val file = vm.images.newCameraFile()
                    cameraFile = file
                    val uri = FileProvider.getUriForFile(
                        context, "${context.packageName}.fileprovider", file
                    )
                    takePicture.launch(uri)
                },
                onRemoveImage = { vm.removeImage(index, it) }
            )
        }

        item {
            Column(Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = draft.outro,
                    onValueChange = vm::updateOutro,
                    label = { Text("마무리") },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 96.dp)
                )
                if (draft.tags.isNotEmpty()) {
                    Text(
                        draft.tags.joinToString(" ") { "#$it" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.itemsIndexedSections(
    sections: List<Section>,
    content: @Composable (Int, Section) -> Unit
) {
    sections.forEachIndexed { index, section ->
        item(key = "section_$index") { content(index, section) }
    }
}

@Composable
private fun SectionCard(
    index: Int,
    section: Section,
    onHeading: (String) -> Unit,
    onParagraph: (Int, String) -> Unit,
    onCaption: (String) -> Unit,
    onPickGallery: () -> Unit,
    onCamera: () -> Unit,
    onRemoveImage: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("소제목 ${index + 1}", style = MaterialTheme.typography.labelLarge)
            OutlinedTextField(
                value = section.heading,
                onValueChange = onHeading,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("제목") }
            )
            section.paragraphs.forEachIndexed { p, text ->
                OutlinedTextField(
                    value = text,
                    onValueChange = { onParagraph(p, it) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 88.dp),
                    label = { Text("문단 ${p + 1}") }
                )
            }

            if (section.imageHint.isNotBlank()) {
                Text(
                    "사진 제안: ${section.imageHint}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (section.images.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    section.images.forEach { path ->
                        item(key = path) {
                            Box {
                                AsyncImage(
                                    model = File(path),
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )
                                IconButton(
                                    onClick = { onRemoveImage(path) },
                                    modifier = Modifier.align(Alignment.TopEnd).size(28.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "이미지 삭제",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
                OutlinedTextField(
                    value = section.caption,
                    onValueChange = onCaption,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("사진 캡션") }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "이미지 없음. 소제목마다 최소 1장.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(onClick = onPickGallery) {
                    Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                    Text("  갤러리")
                }
                FilledTonalButton(onClick = onCamera) {
                    Icon(Icons.Filled.PhotoCamera, contentDescription = null)
                    Text("  카메라")
                }
            }
        }
    }
}
