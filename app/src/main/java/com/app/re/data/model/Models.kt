package com.app.re.data.model

data class ParseRequest(
    val owner: String,
    val repo: String,
    val filePath: String
)

data class ParseResponse(
    val sha: String,
    val originalHtml: String,
    val resumeData: ResumeData
)

data class UpdateRequest(
    val owner: String,
    val repo: String,
    val filePath: String,
    val sha: String,
    val originalHtml: String,
    val resumeData: ResumeData
)

data class UpdateResponse(
    val message: String,
    val commitUrl: String,
    val newSha: String,
    val updatedHtml: String
)

data class ImageUploadResponse(
    val imageUrl: String
)

data class RepoStatsResponse(
    val stars: Int,
    val watchers: Int,
    val forks: Int
)

data class SkillGroup(
    val category: String?,
    val items: List<String>?
)

data class ResumeData(
    val name: String?,
    val tagline: String?,
    val about: String?,
    val profileImageUrl: String?,
    val skills: List<SkillGroup>?,
    val projects: List<Project>?,
    val experience: List<Experience>?,
    val education: List<Education>?,
    val contact: Contact?,
    val certifications: List<String>?,
    val achievements: List<String>?,
    val languages: List<String>?,
    val blogPosts: List<BlogPost>?,
    val hobbies: List<String>?,
    val resumePdfUrl: String?
)

data class Project(
    val title: String?,
    val description: String?,
    val techStack: List<String>?,
    val link: String?
)

data class Experience(
    val company: String?,
    val role: String?,
    val startDate: String?,
    val endDate: String?,
    val description: String?
)

data class Education(
    val institution: String?,
    val degree: String?,
    val field: String?,
    val startYear: String?,
    val endYear: String?,
    val grade: String?
)

data class Contact(
    val email: String?,
    val linkedin: String?,
    val github: String?,
    val website: String?
)

data class BlogPost(
    val title: String?,
    val link: String?,
    val date: String?
)
