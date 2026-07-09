package com.app.re.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.re.data.ResumeRepository
import com.app.re.data.model.Contact
import com.app.re.data.model.Education
import com.app.re.data.model.Experience
import com.app.re.data.model.Project
import com.app.re.data.model.ResumeData
import com.app.re.util.AppCache
import com.app.re.util.SecurePrefsManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.app.re.data.model.SkillGroup
import com.app.re.data.model.BlogPost
class EditViewModel(
    private val repository: ResumeRepository = ResumeRepository()
) : ViewModel() {

    // ── Screen-level state ────────────────────────────────────────────────────

    private val _screenState = MutableStateFlow<EditScreenState>(EditScreenState.Loading)
    val screenState: StateFlow<EditScreenState> = _screenState.asStateFlow()

    // ── Editable resume fields ────────────────────────────────────────────────

    private val _resumeData = MutableStateFlow(emptyResumeData())
    val resumeData: StateFlow<ResumeData> = _resumeData.asStateFlow()

    // Track unsaved changes
    private val _originalResumeData = MutableStateFlow(emptyResumeData())
    val originalResumeData: StateFlow<ResumeData> = _originalResumeData.asStateFlow()

    val hasUnsavedChanges: Boolean
        get() = _resumeData.value != _originalResumeData.value

    // SHA & original HTML — needed by updateResume; sourced from the cached parse response
    private var cachedSha: String = ""
    private var cachedOriginalHtml: String = ""

    // ── Publish state ─────────────────────────────────────────────────────────

    private val _publishState = MutableStateFlow<PublishState>(PublishState.Idle)
    val publishState: StateFlow<PublishState> = _publishState.asStateFlow()

    // ── Photo upload state ────────────────────────────────────────────────────

    private val _photoUploadState = MutableStateFlow<PhotoUploadState>(PhotoUploadState.Idle)
    val photoUploadState: StateFlow<PhotoUploadState> = _photoUploadState.asStateFlow()

    // ── Validation errors ─────────────────────────────────────────────────────

    private val _validationError = MutableStateFlow<String?>(null)
    val validationError: StateFlow<String?> = _validationError.asStateFlow()

    // ─────────────────────────────────────────────────────────────────────────

    init {
        loadData()
    }

    private fun loadData() {
        val cached = AppCache.parseResponse
        if (cached != null) {
            // Use cached data — never re-parse
            cachedSha = cached.sha
            cachedOriginalHtml = cached.originalHtml
            val data = cached.resumeData
            _originalResumeData.value = data
            _resumeData.value = data
            _screenState.value = EditScreenState.Ready
        } else {
            // Fallback: re-fetch from network (app restart case, cache was cleared)
            val owner = SecurePrefsManager.getUsername()
            val repo = SecurePrefsManager.getRepoName()?.trimEnd('/', '.')
            val filePath = SecurePrefsManager.getFilePath()?.trimStart('/')
            val branch = SecurePrefsManager.getBranchName()
            if (owner == null || repo == null || filePath == null) {
                _screenState.value = EditScreenState.Error("Session data missing. Please log in again.")
                return
            }
            _screenState.value = EditScreenState.Loading
            viewModelScope.launch {
                try {
                    val response = repository.parseResume(owner, repo, filePath, branch)
                    AppCache.parseResponse = response
                    cachedSha = response.sha
                    cachedOriginalHtml = response.originalHtml
                    val data = response.resumeData
                    _originalResumeData.value = data
                    _resumeData.value = data
                    _screenState.value = EditScreenState.Ready
                } catch (e: Exception) {
                    _screenState.value = EditScreenState.Error(
                        e.message ?: "Failed to load portfolio. Please try again."
                    )
                }
            }
        }
    }

    fun retryLoad() {
        AppCache.clear()
        loadData()
    }

    // ─── About Tab ────────────────────────────────────────────────────────────

    fun updateName(value: String) {
        _resumeData.value = _resumeData.value.copy(name = value)
    }

    fun updateTagline(value: String) {
        _resumeData.value = _resumeData.value.copy(tagline = value)
    }

    fun updateAbout(value: String) {
        _resumeData.value = _resumeData.value.copy(about = value)
    }

    fun updateProfileImageUrl(value: String) {
        _resumeData.value = _resumeData.value.copy(profileImageUrl = value)
    }

    fun updateResumePdfUrl(value: String) {
        _resumeData.value = _resumeData.value.copy(resumePdfUrl = value)
    }

    /**
     * Picks a photo from [imageUri], compresses it, uploads it to the GitHub repo,
     * and updates [resumeData].profileImageUrl with the returned GitHub Pages URL.
     * The URL is committed to HTML the next time the user taps "Save & Publish".
     */
    fun uploadProfileImage(imageUri: Uri, context: Context) {
        val repo = SecurePrefsManager.getRepoName()?.trimEnd('/', '.') ?: run {
            _photoUploadState.value = PhotoUploadState.Error("Repository not set. Please log in again.")
            return
        }
        val branch = SecurePrefsManager.getBranchName()
        _photoUploadState.value = PhotoUploadState.Uploading
        viewModelScope.launch {
            try {
                val imageUrl = repository.uploadProfileImage(repo, imageUri, context, branch)
                updateProfileImageUrl(imageUrl)
                _photoUploadState.value = PhotoUploadState.Success(imageUrl)
            } catch (e: Exception) {
                val message = when {
                    e.message?.contains("401") == true || e.message?.contains("403") == true ->
                        "GitHub token expired. Please log out and sign in again."
                    e.message?.contains("Unable to resolve host") == true ||
                    e.message?.contains("Failed to connect") == true ->
                        "No internet connection."
                    else -> e.message ?: "Failed to upload photo. Please try again."
                }
                _photoUploadState.value = PhotoUploadState.Error(message)
            }
        }
    }

    fun dismissPhotoUploadResult() {
        _photoUploadState.value = PhotoUploadState.Idle
    }

    // ─── Skills Tab ───────────────────────────────────────────────────────────

    fun addSkill(category: String, skill: String) {
        val trimmed = skill.trim()
        val catTrimmed = if (category.isBlank()) "General" else category.trim()
        if (trimmed.isBlank()) return
        
        val current = _resumeData.value.skills.orEmpty().toMutableList()
        val groupIndex = current.indexOfFirst { it.category.equals(catTrimmed, ignoreCase = true) }
        
        if (groupIndex != -1) {
            val group = current[groupIndex]
            val items = group.items.orEmpty()
            if (!items.any { it.equals(trimmed, ignoreCase = true) }) {
                current[groupIndex] = group.copy(items = items + trimmed)
            }
        } else {
            current.add(SkillGroup(category = catTrimmed, items = listOf(trimmed)))
        }
        
        _resumeData.value = _resumeData.value.copy(skills = current)
    }

    fun removeSkill(category: String, skill: String) {
        val current = _resumeData.value.skills.orEmpty().toMutableList()
        val groupIndex = current.indexOfFirst { it.category == category }
        if (groupIndex != -1) {
            val group = current[groupIndex]
            val updatedItems = group.items.orEmpty().filter { it != skill }
            if (updatedItems.isEmpty()) {
                current.removeAt(groupIndex)
            } else {
                current[groupIndex] = group.copy(items = updatedItems)
            }
            _resumeData.value = _resumeData.value.copy(skills = current)
        }
    }

    fun updateSkillCategory(groupIndex: Int, newCategory: String) {
        val current = _resumeData.value.skills.orEmpty().toMutableList()
        if (groupIndex in current.indices) {
            current[groupIndex] = current[groupIndex].copy(category = newCategory)
            _resumeData.value = _resumeData.value.copy(skills = current)
        }
    }

    fun updateSkillItem(groupIndex: Int, itemIndex: Int, newItem: String) {
        val current = _resumeData.value.skills.orEmpty().toMutableList()
        if (groupIndex in current.indices) {
            val items = current[groupIndex].items.orEmpty().toMutableList()
            if (itemIndex in items.indices) {
                items[itemIndex] = newItem
                current[groupIndex] = current[groupIndex].copy(items = items)
                _resumeData.value = _resumeData.value.copy(skills = current)
            }
        }
    }

    // ─── Projects Tab ─────────────────────────────────────────────────────────

    fun addProject() {
        val current = _resumeData.value.projects.orEmpty()
        _resumeData.value = _resumeData.value.copy(
            projects = current + Project(title = "", description = "", techStack = emptyList(), link = "")
        )
    }

    fun updateProject(index: Int, project: Project) {
        val updated = _resumeData.value.projects.orEmpty().toMutableList()
        if (index in updated.indices) {
            updated[index] = project
            _resumeData.value = _resumeData.value.copy(projects = updated)
        }
    }

    fun removeProject(index: Int) {
        val updated = _resumeData.value.projects.orEmpty().toMutableList()
        if (index in updated.indices) {
            updated.removeAt(index)
            _resumeData.value = _resumeData.value.copy(projects = updated)
        }
    }

    // ─── Experience Tab ───────────────────────────────────────────────────────

    fun addExperience() {
        val current = _resumeData.value.experience.orEmpty()
        _resumeData.value = _resumeData.value.copy(
            experience = current + Experience(
                company = "", role = "", startDate = "", endDate = "", description = ""
            )
        )
    }

    fun updateExperience(index: Int, exp: Experience) {
        val updated = _resumeData.value.experience.orEmpty().toMutableList()
        if (index in updated.indices) {
            updated[index] = exp
            _resumeData.value = _resumeData.value.copy(experience = updated)
        }
    }

    fun removeExperience(index: Int) {
        val updated = _resumeData.value.experience.orEmpty().toMutableList()
        if (index in updated.indices) {
            updated.removeAt(index)
            _resumeData.value = _resumeData.value.copy(experience = updated)
        }
    }

    // ─── Education Tab ────────────────────────────────────────────────────────

    fun addEducation() {
        val current = _resumeData.value.education.orEmpty()
        _resumeData.value = _resumeData.value.copy(
            education = current + Education(
                institution = "", degree = "", field = "", startYear = "", endYear = "", grade = ""
            )
        )
    }

    fun updateEducation(index: Int, edu: Education) {
        val updated = _resumeData.value.education.orEmpty().toMutableList()
        if (index in updated.indices) {
            updated[index] = edu
            _resumeData.value = _resumeData.value.copy(education = updated)
        }
    }

    fun removeEducation(index: Int) {
        val updated = _resumeData.value.education.orEmpty().toMutableList()
        if (index in updated.indices) {
            updated.removeAt(index)
            _resumeData.value = _resumeData.value.copy(education = updated)
        }
    }

    // ─── Certifications Tab ───────────────────────────────────────────────────

    fun addCertification(cert: String) {
        val trimmed = cert.trim()
        if (trimmed.isBlank()) return
        val current = _resumeData.value.certifications.orEmpty()
        if (!current.contains(trimmed)) {
            _resumeData.value = _resumeData.value.copy(certifications = current + trimmed)
        }
    }

    fun removeCertification(cert: String) {
        val current = _resumeData.value.certifications.orEmpty()
        _resumeData.value = _resumeData.value.copy(
            certifications = current.filter { it != cert }
        )
    }

    // ─── Achievements Tab ─────────────────────────────────────────────────────

    fun addAchievement(achievement: String) {
        val trimmed = achievement.trim()
        if (trimmed.isBlank()) return
        val current = _resumeData.value.achievements.orEmpty()
        if (!current.contains(trimmed)) {
            _resumeData.value = _resumeData.value.copy(achievements = current + trimmed)
        }
    }

    fun removeAchievement(achievement: String) {
        val current = _resumeData.value.achievements.orEmpty()
        _resumeData.value = _resumeData.value.copy(
            achievements = current.filter { it != achievement }
        )
    }

    // ─── Languages Tab ────────────────────────────────────────────────────────

    fun addLanguage(language: String) {
        val trimmed = language.trim()
        if (trimmed.isBlank()) return
        val current = _resumeData.value.languages.orEmpty()
        if (!current.contains(trimmed)) {
            _resumeData.value = _resumeData.value.copy(languages = current + trimmed)
        }
    }

    fun removeLanguage(language: String) {
        val current = _resumeData.value.languages.orEmpty()
        _resumeData.value = _resumeData.value.copy(
            languages = current.filter { it != language }
        )
    }

    // ─── Hobbies Tab ──────────────────────────────────────────────────────────

    fun addHobby(hobby: String) {
        val trimmed = hobby.trim()
        if (trimmed.isBlank()) return
        val current = _resumeData.value.hobbies.orEmpty()
        if (!current.contains(trimmed)) {
            _resumeData.value = _resumeData.value.copy(hobbies = current + trimmed)
        }
    }

    fun removeHobby(hobby: String) {
        val current = _resumeData.value.hobbies.orEmpty()
        _resumeData.value = _resumeData.value.copy(
            hobbies = current.filter { it != hobby }
        )
    }

    // ─── Blog Posts Tab ───────────────────────────────────────────────────────

    fun addBlogPost() {
        val current = _resumeData.value.blogPosts.orEmpty()
        _resumeData.value = _resumeData.value.copy(
            blogPosts = current + BlogPost(title = "", link = "", date = "")
        )
    }

    fun updateBlogPost(index: Int, post: BlogPost) {
        val updated = _resumeData.value.blogPosts.orEmpty().toMutableList()
        if (index in updated.indices) {
            updated[index] = post
            _resumeData.value = _resumeData.value.copy(blogPosts = updated)
        }
    }

    fun removeBlogPost(index: Int) {
        val updated = _resumeData.value.blogPosts.orEmpty().toMutableList()
        if (index in updated.indices) {
            updated.removeAt(index)
            _resumeData.value = _resumeData.value.copy(blogPosts = updated)
        }
    }

    // ─── Contact Tab ──────────────────────────────────────────────────────────

    fun updateContact(contact: Contact) {
        _resumeData.value = _resumeData.value.copy(contact = contact)
    }

    // ─── Save & Publish ───────────────────────────────────────────────────────

    fun saveAndPublish() {
        val data = _resumeData.value

        // Validate
        val nameError = validateName(data.name)
        if (nameError != null) { _validationError.value = nameError; return }

        val owner = SecurePrefsManager.getUsername() ?: run {
            _validationError.value = "Session expired. Please log in again."
            return
        }
        val repo = SecurePrefsManager.getRepoName()?.trimEnd('/', '.') ?: run {
            _validationError.value = "Repository not set."
            return
        }
        val filePath = SecurePrefsManager.getFilePath()?.trimStart('/') ?: run {
            _validationError.value = "File path not set."
            return
        }

        _publishState.value = PublishState.Publishing

        viewModelScope.launch {
            try {
                // If a photo upload is still running, wait for it to finish before
                // committing the HTML. This prevents concurrent GitHub API writes
                // which cause a 502/409 conflict error.
                if (_photoUploadState.value is PhotoUploadState.Uploading) {
                    _photoUploadState.first { it !is PhotoUploadState.Uploading }
                }

                val branch = SecurePrefsManager.getBranchName()
                val response = repository.updateResume(
                    owner = owner,
                    repo = repo,
                    filePath = filePath,
                    branch = branch,
                    sha = cachedSha,
                    originalHtml = cachedOriginalHtml,
                    resumeData = _resumeData.value  // re-read after possible URL update from upload
                )
                
                // Update local cache so future edits (and re-opening the screen) use the new data!
                cachedSha = response.newSha
                cachedOriginalHtml = response.updatedHtml
                AppCache.parseResponse = com.app.re.data.model.ParseResponse(
                    sha = response.newSha,
                    originalHtml = response.updatedHtml,
                    resumeData = _resumeData.value
                )

                SecurePrefsManager.saveLastUpdated(System.currentTimeMillis())
                SecurePrefsManager.setPortfolioUpdateAcknowledged(false)
                // Update the baseline so hasUnsavedChanges resets
                _originalResumeData.value = _resumeData.value
                _publishState.value = PublishState.Success(
                    commitUrl = response.commitUrl,
                    portfolioUrl = buildPortfolioUrl(owner, repo)
                )
            } catch (e: Exception) {
                val message = when {
                    e.message?.contains("401") == true || e.message?.contains("403") == true ->
                        "GitHub token expired. Please log out and sign in again."
                    e.message?.contains("409") == true ->
                        "Conflict detected. Your file was changed elsewhere. Please restart the app."
                    e.message?.contains("Unable to resolve host") == true ||
                    e.message?.contains("Failed to connect") == true ->
                        "No internet connection."
                    else -> e.message ?: "Publish failed. Please try again."
                }
                _publishState.value = PublishState.Error(message)
            }
        }
    }

    fun dismissPublishResult() {
        _publishState.value = PublishState.Idle
    }

    fun clearValidationError() {
        _validationError.value = null
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private fun validateName(name: String?): String? {
        if (name.isNullOrBlank()) return "Name cannot be empty."
        return null
    }

    private fun validateEmail(email: String?): String? {
        if (email.isNullOrBlank()) return null // optional
        val emailRegex = Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")
        return if (!emailRegex.matches(email)) "Email address is not valid." else null
    }

    private fun validateUrl(label: String, url: String?): String? {
        if (url.isNullOrBlank()) return null // optional
        return if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "$label URL must start with http:// or https://"
        } else null
    }

    private fun buildPortfolioUrl(username: String, repo: String): String {
        val u = username.lowercase()
        val r = repo.trimEnd('/', '.')
        return if (r.lowercase() == "$u.github.io") "https://$u.github.io" else "https://$u.github.io/$r"
    }

    private fun emptyResumeData() = ResumeData(
        name = null, tagline = null, about = null, profileImageUrl = null,
        skills = null, projects = null, experience = null, education = null,
        contact = null, certifications = null, achievements = null,
        languages = null, blogPosts = null, hobbies = null, resumePdfUrl = null
    )
}

// ── Sealed states ─────────────────────────────────────────────────────────────

sealed class EditScreenState {
    data object Loading : EditScreenState()
    data object Ready : EditScreenState()
    data class Error(val message: String) : EditScreenState()
}

sealed class PublishState {
    data object Idle : PublishState()
    data object Publishing : PublishState()
    data class Success(val commitUrl: String, val portfolioUrl: String) : PublishState()
    data class Error(val message: String) : PublishState()
}

sealed class PhotoUploadState {
    data object Idle : PhotoUploadState()
    data object Uploading : PhotoUploadState()
    data class Success(val imageUrl: String) : PhotoUploadState()
    data class Error(val message: String) : PhotoUploadState()
}
