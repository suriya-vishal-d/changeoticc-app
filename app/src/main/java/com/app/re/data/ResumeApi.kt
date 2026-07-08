package com.app.re.data

import com.app.re.data.model.ImageUploadResponse
import com.app.re.data.model.ParseRequest
import com.app.re.data.model.ParseResponse
import com.app.re.data.model.UpdateRequest
import com.app.re.data.model.UpdateResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ResumeApi {

    @POST("resume/parse")
    suspend fun parseResume(@Body request: ParseRequest): ParseResponse

    @POST("resume/update")
    suspend fun updateResume(@Body request: UpdateRequest): UpdateResponse

    @Multipart
    @POST("resume/upload-image")
    suspend fun uploadProfileImage(
        @Part image: MultipartBody.Part,
        @Part("repo") repo: RequestBody,
        @Part("branch") branch: RequestBody?
    ): ImageUploadResponse

    @retrofit2.http.GET("resume/stats")
    suspend fun getRepoStats(@retrofit2.http.Query("repo") repo: String): com.app.re.data.model.RepoStatsResponse
}
