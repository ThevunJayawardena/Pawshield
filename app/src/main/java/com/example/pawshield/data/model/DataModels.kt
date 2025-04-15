package com.example.pawshield.data.model

import android.net.Uri
import com.google.gson.annotations.SerializedName
import java.util.Date

// Data class representing the core prediction from the ML model
data class SkinCondition(
    val name: String,
    val confidence: Float // Keep confidence here as it comes from the API
)

// Data class for treatment information retrieved from JSON/API
data class TreatmentInfo(
    val description: String,
    val symptoms: List<String>,
    val treatments: List<String>,
    @SerializedName("veterinary_visit")
    val veterinaryVisit: String
)

// Data class representing the full JSON response from your Flask /predict endpoint
data class DiagnosisApiResponse(
    val animal_type: String,
    val diagnosis: String,
    val confidence_percent: Float,
    // val probabilities: List<Float>, // Optional
    val treatment_info: TreatmentInfo?
)


data class DiagnosisResult(
    val conditionName: String, // Store only the name needed for display/lookup
    val treatment: TreatmentInfo?,
    val timestamp: Date = Date(), // Automatically capture when the result is created
    val imageUriString: String? = null // Store the URI string for display in history details
)