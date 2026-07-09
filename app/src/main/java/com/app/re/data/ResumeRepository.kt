package com.app.re.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.app.re.data.model.ParseRequest
import com.app.re.data.model.ParseResponse
import com.app.re.data.model.ResumeData
import com.app.re.data.model.UpdateRequest
import com.app.re.data.model.UpdateResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import kotlin.math.min

class ResumeRepository(private val api: ResumeApi = RetrofitClient.api) {

    /**
     * Parses the user's portfolio HTML from GitHub via the backend.
     * Returns [ParseResponse] containing sha, originalHtml, and parsed [ResumeData].
     */
    suspend fun parseResume(
        owner: String,
        repo: String,
        filePath: String,
        branch: String?
    ): ParseResponse {
        return api.parseResume(
            ParseRequest(
                owner = owner,
                repo = repo,
                filePath = filePath,
                branch = branch
            )
        )
    }

    /**
     * Sends updated [ResumeData] back to the backend, which commits the changes to GitHub.
     * Returns [UpdateResponse] with a commit URL on success.
     */
    suspend fun updateResume(
        owner: String,
        repo: String,
        filePath: String,
        branch: String?,
        sha: String,
        originalHtml: String,
        resumeData: ResumeData
    ): UpdateResponse {
        return api.updateResume(
            UpdateRequest(
                owner = owner,
                repo = repo,
                filePath = filePath,
                branch = branch,
                sha = sha,
                originalHtml = originalHtml,
                resumeData = resumeData
            )
        )
    }

    /**
     * Uploads a profile photo to the GitHub repo via the backend.
     *
     * Steps:
     *  1. Read the image from [imageUri] using ContentResolver
     *  2. Scale down to at most 800×800 px (preserving aspect ratio)
     *  3. Compress to JPEG at 80% quality to minimise upload size
     *  4. POST the bytes as a multipart request to /resume/upload-image
     *
     * @return the public GitHub Pages URL of the uploaded image
     */
    suspend fun uploadProfileImage(repo: String, imageUri: Uri, context: Context, originalHtml: String): String {
        // 1. Decode bitmap from URI
        val originalBitmap = context.contentResolver.openInputStream(imageUri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        } ?: throw IllegalStateException("Could not open image from URI")

        // 2. Scale to max 800×800 while preserving aspect ratio
        val maxDimension = 800
        val scaledBitmap: Bitmap = if (originalBitmap.width > maxDimension || originalBitmap.height > maxDimension) {
            val scale = min(
                maxDimension.toFloat() / originalBitmap.width,
                maxDimension.toFloat() / originalBitmap.height
            )
            val newWidth  = (originalBitmap.width  * scale).toInt()
            val newHeight = (originalBitmap.height * scale).toInt()
            Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
        } else {
            originalBitmap
        }

        // 3. Compress to JPEG at 80% quality
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        val imageBytes = outputStream.toByteArray()

        // Recycle bitmaps if they differ (avoid memory leaks)
        if (scaledBitmap !== originalBitmap) {
            originalBitmap.recycle()
        }

        // 4. Build multipart parts and upload
        val requestBody = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", "profile.jpg", requestBody)
        val repoPart  = repo.toRequestBody("text/plain".toMediaTypeOrNull())
        val htmlPart = originalHtml.toRequestBody("text/plain".toMediaTypeOrNull())

        val response = api.uploadProfileImage(imagePart, repoPart, htmlPart)
        return response.imageUrl
    }
    /**
     * Deletes the profile image file from GitHub via the backend.
     * @param repo      the GitHub repo name
     * @param imagePath the repo-relative path of the image, e.g. "assets/img/profile.jpg"
     */
    suspend fun deleteProfileImage(repo: String, imagePath: String) {
        api.deleteProfileImage(repo = repo, imagePath = imagePath)
    }

    suspend fun getRepoStats(repo: String): com.app.re.data.model.RepoStatsResponse {
        return api.getRepoStats(repo)
    }
}

