package com.example.pawshield.data.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// Singleton object to provide a configured Retrofit instance
object RetrofitClient {

    // IMPORTANT: Replace with your actual API URL
    // For Android Emulator testing against localhost: use "http://10.0.2.2:5000/"
    // For physical device testing on same Wi-Fi: use "http://<your-computer-ip>:5000/"
    // For deployed API (e.g., Ngrok, Cloud Run): use the public URL
    private const val BASE_URL = "https://pawshield.ngrok.app/" // **MUST END WITH /**

    // Configure OkHttpClient with logging (useful for debugging)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        // Use Level.BODY for detailed logs during development, Level.NONE for production
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS) // Optional: Set timeouts
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Lazy initialization of the Retrofit instance
    val instance: PawShieldApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Attach the custom OkHttpClient
            .addConverterFactory(GsonConverterFactory.create()) // Use Gson for JSON parsing
            .build()

        retrofit.create(PawShieldApiService::class.java)
    }
}