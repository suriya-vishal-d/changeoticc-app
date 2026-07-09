package com.app.re.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore

import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton

import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme

import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.app.re.data.model.Contact
import com.app.re.data.model.Education
import com.app.re.data.model.Experience
import com.app.re.data.model.Project
import com.app.re.data.model.ResumeData
import com.app.re.ui.viewmodel.EditScreenState
import com.app.re.ui.viewmodel.EditViewModel
import com.app.re.ui.viewmodel.PhotoUploadState
import com.app.re.ui.viewmodel.PublishState
import kotlinx.coroutines.launch

private val tabs = listOf("About", "Skills", "Projects", "Experience", "Education", "Contact")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EditViewModel = viewModel()
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val resumeData by viewModel.resumeData.collectAsStateWithLifecycle()
    val originalResumeData by viewModel.originalResumeData.collectAsStateWithLifecycle()
    val publishState by viewModel.publishState.collectAsStateWithLifecycle()
    val validationError by viewModel.validationError.collectAsStateWithLifecycle()
    val photoUploadState by viewModel.photoUploadState.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    val showPublishSuccessDialog = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    if (showPublishSuccessDialog.value) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { },
            title = { Text("Published!") },
            text = { Text("It may take a few minutes for the portfolio to update on GitHub Pages.") },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    showPublishSuccessDialog.value = false
                    viewModel.dismissPublishResult()
                    onNavigateBack()
                }) {
                    Text("OK")
                }
            }
        )
    }

    // Handle publish result
    LaunchedEffect(publishState) {
        if (publishState is PublishState.Success) {
            showPublishSuccessDialog.value = true
        }
        if (publishState is PublishState.Error) {
            scope.launch {
                snackbarHostState.showSnackbar((publishState as PublishState.Error).message)
            }
            viewModel.dismissPublishResult()
        }
    }

    // Show photo upload result as snackbar
    LaunchedEffect(photoUploadState) {
        when (val state = photoUploadState) {
            is PhotoUploadState.Success -> {
                scope.launch {
                    snackbarHostState.showSnackbar("✅ Photo updated — tap Save & Publish to go live")
                }
                viewModel.dismissPhotoUploadResult()
            }
            is PhotoUploadState.Error -> {
                scope.launch {
                    snackbarHostState.showSnackbar(state.message)
                }
                viewModel.dismissPhotoUploadResult()
            }
            else -> Unit
        }
    }

    // Show validation error as snackbar
    LaunchedEffect(validationError) {
        validationError?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            viewModel.clearValidationError()
        }
    }

    // Intercept back press — warn if unsaved changes
    BackHandler {
        if (viewModel.hasUnsavedChanges) {
            showDiscardDialog = true
        } else {
            onNavigateBack()
        }
    }

    // Discard changes dialog
    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Unsaved Changes") },
            text = { Text("You have unsaved changes. Discard and go back?") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardDialog = false
                    onNavigateBack()
                }) { Text("Discard", color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Keep Editing")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "Edit Portfolio",
                            style = androidx.compose.material3.MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold, fontSize = 20.sp
                            ),
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (viewModel.hasUnsavedChanges) showDiscardDialog = true
                            else onNavigateBack()
                        }) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.retryLoad() }) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface)
                )
                // Linear progress while publishing
                if (publishState is PublishState.Publishing) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                        trackColor = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant
                    )
                }
                // Tab row
                if (screenState is EditScreenState.Ready) {
                    ScrollableTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary,
                        edgePadding = 16.dp
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold
                                        else FontWeight.Normal,
                                        fontSize = 14.sp
                                    )
                                }
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (screenState is EditScreenState.Ready) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(androidx.compose.material3.MaterialTheme.colorScheme.surface)
                        .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 40.dp)
                ) {
                    Button(
                        onClick = { viewModel.saveAndPublish() },
                        enabled = publishState !is PublishState.Publishing,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                            contentColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                            disabledContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha=0.5f),
                            disabledContentColor = androidx.compose.material3.MaterialTheme.colorScheme.surface
                        )
                    ) {
                        if (publishState is PublishState.Publishing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save & Publish",
                                style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (screenState) {
                is EditScreenState.Loading -> LoadingContent()
                is EditScreenState.Error -> ErrorContent(
                    message = (screenState as EditScreenState.Error).message,
                    onRetry = { viewModel.retryLoad() }
                )
                is EditScreenState.Ready -> TabContent(
                    selectedTab = selectedTab,
                    resumeData = resumeData,
                    originalResumeData = originalResumeData,
                    viewModel = viewModel,
                    photoUploadState = photoUploadState
                )
            }
        }
    }
}

// ── Loading / Error states ────────────────────────────────────────────────────

@Composable
private fun LoadingContent() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            CircularProgressIndicator(color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface)
            Text("Loading portfolio…", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(message, color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
            Button(onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary)) {
                Text("Try Again")
            }
        }
    }
}

// ── Tab content dispatcher ────────────────────────────────────────────────────

@Composable
private fun TabContent(
    selectedTab: Int,
    resumeData: ResumeData,
    originalResumeData: ResumeData,
    viewModel: EditViewModel,
    photoUploadState: PhotoUploadState
) {
    when (selectedTab) {
        0 -> AboutTab(resumeData, originalResumeData, viewModel, photoUploadState)
        1 -> SkillsTab(resumeData, originalResumeData, viewModel)
        2 -> ProjectsTab(resumeData, originalResumeData, viewModel)
        3 -> ExperienceTab(resumeData, originalResumeData, viewModel)
        4 -> EducationTab(resumeData, originalResumeData, viewModel)
        5 -> ContactTab(resumeData, originalResumeData, viewModel)
    }
}

// ── Shared tab scaffold ───────────────────────────────────────────────────────

@Composable
private fun TabScaffold(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) { content() }
}

// ── Shared field label ────────────────────────────────────────────────────────

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        style = androidx.compose.material3.MaterialTheme.typography.labelLarge.copy(
            fontWeight = FontWeight.SemiBold, fontSize = 13.sp
        ),
        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
    )
}

// ── Shared OutlinedTextField style ────────────────────────────────────────────

private val fieldColors: @Composable () -> androidx.compose.material3.TextFieldColors
    @Composable get() = {
        OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface
        )
    }

@Composable
private fun EditField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        FieldLabel(label)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.6f)) },
            singleLine = singleLine,
            minLines = minLines,
            shape = RoundedCornerShape(10.dp),
            colors = fieldColors(),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ABOUT TAB
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AboutTab(resumeData: ResumeData, originalResumeData: ResumeData, viewModel: EditViewModel, photoUploadState: PhotoUploadState) {
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.uploadProfileImage(it, context) }
    }

    TabScaffold {
        // ─ Profile photo section ──────────────────────────────────────────────────────
        // Always show the photo picker: templates that use CSS initials (e.g. "AV") still
        // have an avatar field in their JS data — they just default to initials when null.
        // The user should always be able to upload a photo to replace those initials.
        ProfilePhotoSection(
            imageUrl = resumeData.profileImageUrl,
            isUploading = photoUploadState is PhotoUploadState.Uploading,
            onPickImage = { imagePickerLauncher.launch("image/*") }
        )

        if (!originalResumeData.name.isNullOrBlank()) {
            EditField(
                value = resumeData.name.orEmpty(),
                onValueChange = viewModel::updateName,
                label = "Name *",
                placeholder = "Your full name"
            )
        }
        if (!originalResumeData.tagline.isNullOrBlank()) {
            EditField(
                value = resumeData.tagline.orEmpty(),
                onValueChange = viewModel::updateTagline,
                label = "Tagline",
                placeholder = "e.g. Full-stack Developer & Open Source Enthusiast"
            )
        }
        if (!originalResumeData.about.isNullOrBlank()) {
            EditField(
                value = resumeData.about.orEmpty(),
                onValueChange = viewModel::updateAbout,
                label = "About",
                placeholder = "Tell visitors about yourself…",
                singleLine = false,
                minLines = 4
            )
        }
        if (!originalResumeData.resumePdfUrl.isNullOrBlank()) {
            EditField(
                value = resumeData.resumePdfUrl.orEmpty(),
                onValueChange = viewModel::updateResumePdfUrl,
                label = "Resume PDF URL",
                placeholder = "https://example.com/resume.pdf"
            )
        }
    }
}

/**
 * Circular profile photo with an edit/camera badge at the bottom-right.
 * Shows a placeholder avatar when no URL is set.
 * Overlays a spinner while an upload is in progress.
 */
@Composable
private fun ProfilePhotoSection(
    imageUrl: String?,
    isUploading: Boolean,
    onPickImage: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FieldLabel("Profile Photo")

        Box(
            modifier = Modifier
                .size(100.dp)
                .clickable(enabled = !isUploading, onClick = onPickImage),
            contentAlignment = Alignment.Center
        ) {
            // Circle photo or placeholder
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)
                    .border(2.dp, androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    val context = LocalContext.current
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUrl)
                            .crossfade(true)
                            .memoryCachePolicy(coil.request.CachePolicy.DISABLED)
                            .diskCachePolicy(coil.request.CachePolicy.DISABLED)
                            .build(),
                        contentDescription = "Profile photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                    )
                } else {
                    // Placeholder: person icon with a + overlay
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "No photo",
                        modifier = Modifier.size(52.dp),
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Upload spinner overlay
            if (isUploading) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                        strokeWidth = 3.dp
                    )
                }
            }

            // Camera badge at bottom-right — hidden while uploading
            if (!isUploading) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Change photo",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // Hint text
        Text(
            text = if (isUploading) "Uploading…" else "Tap to change photo",
            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SKILLS TAB
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SkillsTab(resumeData: ResumeData, originalResumeData: ResumeData, viewModel: EditViewModel) {
    val skills = resumeData.skills.orEmpty()

    TabScaffold {
        if (skills.isEmpty()) {
            Text(
                text = "No skills found",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            skills.forEachIndexed { groupIndex, group ->
                // ── Category header — editable ──────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    FieldLabel("Category")
                    OutlinedTextField(
                        value = group.category ?: "General",
                        onValueChange = { viewModel.updateSkillCategory(groupIndex, it) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = fieldColors(),
                        placeholder = { Text("Category name", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) }
                    )
                }

                // ── Skill items — each individually editable ─────────────────
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        group.items.orEmpty().forEachIndexed { itemIndex, skill ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = skill,
                                    onValueChange = { viewModel.updateSkillItem(groupIndex, itemIndex, it) },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    colors = fieldColors(),
                                    placeholder = { Text("Skill name", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) }
                                )
                                IconButton(
                                    onClick = { viewModel.removeSkill(group.category ?: "General", skill) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove $skill",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ─────────────────────────────────────────────────────────────────────────────
// PROJECTS TAB
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ProjectsTab(resumeData: ResumeData, originalResumeData: ResumeData, viewModel: EditViewModel) {
    val projects = resumeData.projects.orEmpty()
    val originalProjects = originalResumeData.projects.orEmpty()
    TabScaffold {
        projects.forEachIndexed { index, project ->
            val orig = originalProjects.getOrNull(index) ?: project
            ProjectCard(
                index = index,
                project = project,
                originalProject = orig,
                onUpdate = { viewModel.updateProject(index, it) },
                onDelete = { viewModel.removeProject(index) }
            )
        }
    }
}

@Composable
private fun ProjectCard(
    index: Int,
    project: Project,
    originalProject: Project,
    onUpdate: (Project) -> Unit,
    onDelete: () -> Unit
) {
    var expanded by rememberSaveable(index) { mutableStateOf(true) }
    val title = project.title?.takeIf { it.isNotBlank() } ?: "Project ${index + 1}"

    ExpandableCard(
        title = title,
        expanded = expanded,
        onToggle = { expanded = !expanded },
        onDelete = onDelete
    ) {
        if (!originalProject.title.isNullOrBlank()) {
            EditField(project.title.orEmpty(), { onUpdate(project.copy(title = it)) },
                "Title", "Project title")
        }
        if (!originalProject.description.isNullOrBlank()) {
            EditField(project.description.orEmpty(), { onUpdate(project.copy(description = it)) },
                "Description", "What did you build?", singleLine = false, minLines = 3)
        }
        if (!originalProject.techStack.isNullOrEmpty()) {
            EditField(
                value = project.techStack.orEmpty().joinToString(", "),
                onValueChange = { raw ->
                    val stack = raw.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    onUpdate(project.copy(techStack = stack))
                },
                label = "Tech Stack",
                placeholder = "Kotlin, Jetpack Compose, Retrofit"
            )
        }
        if (!originalProject.link.isNullOrBlank()) {
            EditField(project.link.orEmpty(), { onUpdate(project.copy(link = it)) },
                "Link", "https://github.com/…")
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// EXPERIENCE TAB
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ExperienceTab(resumeData: ResumeData, originalResumeData: ResumeData, viewModel: EditViewModel) {
    val experience = resumeData.experience.orEmpty()
    val originalExperience = originalResumeData.experience.orEmpty()
    TabScaffold {
        experience.forEachIndexed { index, exp ->
            val orig = originalExperience.getOrNull(index) ?: exp
            ExperienceCard(
                index = index,
                exp = exp,
                originalExp = orig,
                onUpdate = { viewModel.updateExperience(index, it) },
                onDelete = { viewModel.removeExperience(index) }
            )
        }
    }
}

@Composable
private fun ExperienceCard(
    index: Int,
    exp: Experience,
    originalExp: Experience,
    onUpdate: (Experience) -> Unit,
    onDelete: () -> Unit
) {
    var expanded by rememberSaveable(index) { mutableStateOf(true) }
    val title = exp.role?.takeIf { it.isNotBlank() } ?: "Experience ${index + 1}"

    ExpandableCard(
        title = title,
        subtitle = exp.company,
        expanded = expanded,
        onToggle = { expanded = !expanded },
        onDelete = onDelete
    ) {
        if (!originalExp.company.isNullOrBlank()) {
            EditField(exp.company.orEmpty(), { onUpdate(exp.copy(company = it)) },
                "Company", "Company name")
        }
        if (!originalExp.role.isNullOrBlank()) {
            EditField(exp.role.orEmpty(), { onUpdate(exp.copy(role = it)) },
                "Role", "Your job title")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (!originalExp.startDate.isNullOrBlank()) {
                Column(modifier = Modifier.weight(1f)) {
                    EditField(exp.startDate.orEmpty(), { onUpdate(exp.copy(startDate = it)) },
                        "Start Date", "Jan 2022")
                }
            }
            if (!originalExp.endDate.isNullOrBlank()) {
                Column(modifier = Modifier.weight(1f)) {
                    EditField(exp.endDate.orEmpty(), { onUpdate(exp.copy(endDate = it)) },
                        "End Date", "Present")
                }
            }
        }
        // Experience doesn't actually have a description field in the Java model, 
        // but it's here in the UI. If it existed in the original data, we would show it:
        // if (!originalExp.description.isNullOrBlank()) { ... }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// EDUCATION TAB
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun EducationTab(resumeData: ResumeData, originalResumeData: ResumeData, viewModel: EditViewModel) {
    val education = resumeData.education.orEmpty()
    val originalEducation = originalResumeData.education.orEmpty()
    TabScaffold {
        education.forEachIndexed { index, edu ->
            val orig = originalEducation.getOrNull(index) ?: edu
            EducationCard(
                index = index,
                edu = edu,
                originalEdu = orig,
                onUpdate = { viewModel.updateEducation(index, it) },
                onDelete = { viewModel.removeEducation(index) }
            )
        }
    }
}

@Composable
private fun EducationCard(
    index: Int,
    edu: Education,
    originalEdu: Education,
    onUpdate: (Education) -> Unit,
    onDelete: () -> Unit
) {
    var expanded by rememberSaveable(index) { mutableStateOf(true) }
    val title = edu.institution?.takeIf { it.isNotBlank() } ?: "Education ${index + 1}"

    ExpandableCard(
        title = title,
        subtitle = edu.degree,
        expanded = expanded,
        onToggle = { expanded = !expanded },
        onDelete = onDelete
    ) {
        if (!originalEdu.institution.isNullOrBlank()) {
            EditField(edu.institution.orEmpty(), { onUpdate(edu.copy(institution = it)) },
                "Institution", "University / College name")
        }
        if (!originalEdu.degree.isNullOrBlank()) {
            EditField(edu.degree.orEmpty(), { onUpdate(edu.copy(degree = it)) },
                "Degree", "B.Tech, M.Sc, etc.")
        }
        if (!originalEdu.field.isNullOrBlank()) {
            EditField(edu.field.orEmpty(), { onUpdate(edu.copy(field = it)) },
                "Field of Study", "Computer Science")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (!originalEdu.startYear.isNullOrBlank()) {
                Column(modifier = Modifier.weight(1f)) {
                    EditField(edu.startYear.orEmpty(), { onUpdate(edu.copy(startYear = it)) },
                        "Start Year", "2020")
                }
            }
            if (!originalEdu.endYear.isNullOrBlank()) {
                Column(modifier = Modifier.weight(1f)) {
                    EditField(edu.endYear.orEmpty(), { onUpdate(edu.copy(endYear = it)) },
                        "End Year", "2024")
                }
            }
        }
        if (!originalEdu.grade.isNullOrBlank()) {
            EditField(edu.grade.orEmpty(), { onUpdate(edu.copy(grade = it)) },
                "Grade / GPA", "8.5 CGPA")
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CONTACT TAB
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ContactTab(resumeData: ResumeData, originalResumeData: ResumeData, viewModel: EditViewModel) {
    val contact = resumeData.contact ?: Contact(null, null, null, null)
    val originalContact = originalResumeData.contact ?: Contact(null, null, null, null)
    TabScaffold {
        if (!originalContact.email.isNullOrBlank()) {
            EditField(
                value = contact.email.orEmpty(),
                onValueChange = { viewModel.updateContact(contact.copy(email = it)) },
                label = "Email",
                placeholder = "you@example.com",
                keyboardType = KeyboardType.Email
            )
        }
        if (!originalContact.linkedin.isNullOrBlank()) {
            EditField(
                value = contact.linkedin.orEmpty(),
                onValueChange = { viewModel.updateContact(contact.copy(linkedin = it)) },
                label = "LinkedIn URL",
                placeholder = "https://linkedin.com/in/yourname"
            )
        }
        if (!originalContact.github.isNullOrBlank()) {
            EditField(
                value = contact.github.orEmpty(),
                onValueChange = { viewModel.updateContact(contact.copy(github = it)) },
                label = "GitHub URL",
                placeholder = "https://github.com/yourname"
            )
        }
        if (!originalContact.website.isNullOrBlank()) {
            EditField(
                value = contact.website.orEmpty(),
                onValueChange = { viewModel.updateContact(contact.copy(website = it)) },
                label = "Website",
                placeholder = "https://yourname.dev"
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SHARED COMPOSABLES
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun ExpandableCard(
    title: String,
    subtitle: String? = null,
    expanded: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = androidx.compose.material3.MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(onClick = onToggle, modifier = Modifier.size(32.dp)) {
                    Icon(
                        if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (expanded) {
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    content()
                }
            }
        }
    }
}

// AddItemButton removed as this is strictly an editor app now.

// ─────────────────────────────────────────────────────────────────────────────
// END OF FILE
// ─────────────────────────────────────────────────────────────────────────────

