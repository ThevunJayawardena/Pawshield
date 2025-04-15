package com.example.pawshield.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.pawshield.data.database.Converters // Import your converters
import java.util.Date

@Entity(tableName = "diagnosis_history") // Define the table name
@TypeConverters(Converters::class)      // Tell Room to use your converters
data class DiagnosisHistoryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Auto-generated primary key

    val conditionName: String,

    // Store TreatmentInfo as a JSON String. Nullable if treatment might be absent.
    val treatmentJson: String?,

    val timestamp: Date,        // Room needs a TypeConverter for Date

    val imageUriString: String? // Store the Uri as a String (nullable if it might be missing)
) {
    // Optional: Add a convenience function to convert back to DiagnosisResult
    // Note: Gson instance would ideally be injected or passed, but this works for simplicity
    fun toDiagnosisResult(): DiagnosisResult {
        val gson = com.google.gson.Gson()
        val treatmentInfo: TreatmentInfo? = if (treatmentJson != null) {
            try {
                gson.fromJson(treatmentJson, TreatmentInfo::class.java)
            } catch (e: Exception) {
                // Handle potential JSON parsing error, maybe return null or default
                null
            }
        } else {
            null
        }

        return DiagnosisResult(
            conditionName = this.conditionName,
            treatment = treatmentInfo,
            timestamp = this.timestamp,
            imageUriString = this.imageUriString
        )
    }
}

// Static function to create an Entry from a Result (useful in ViewModel)
fun DiagnosisResult.toHistoryEntry(): DiagnosisHistoryEntry {
    val gson = com.google.gson.Gson()
    val jsonString = if (this.treatment != null) gson.toJson(this.treatment) else null
    return DiagnosisHistoryEntry(
        conditionName = this.conditionName,
        treatmentJson = jsonString,
        timestamp = this.timestamp,
        imageUriString = this.imageUriString
    )
}