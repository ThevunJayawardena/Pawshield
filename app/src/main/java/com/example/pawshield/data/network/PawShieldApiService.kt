package com.example.pawshield.data.network

import com.example.pawshield.data.model.DiagnosisApiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

// Defines the API endpoints for Retrofit
interface PawShieldApiService {

    @Multipart
    @POST("predict") // The endpoint path on your Flask server
    suspend fun predictSkinCondition(
        @Part image: MultipartBody.Part,
        @Part("animal_type") animalType: RequestBody // Name must match the form field name expected by Flask
    ): Response<DiagnosisApiResponse> // Using Response<> allows checking HTTP status codes
}