package com.example.pawshield.data.database

import androidx.room.TypeConverter
import com.example.pawshield.data.model.TreatmentInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class Converters {

    private val gson = Gson()

    // Converter for Date <-> Long (Timestamp)
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    // Converter for TreatmentInfo? <-> String? (JSON)
    @TypeConverter
    fun fromTreatmentInfo(treatmentInfo: TreatmentInfo?): String? {
        return gson.toJson(treatmentInfo)
    }

    @TypeConverter
    fun toTreatmentInfo(treatmentJson: String?): TreatmentInfo? {
        if (treatmentJson == null) {
            return null
        }
        // Use TypeToken for generic types if TreatmentInfo contained generics,
        // but for a simple data class, fromJson should work directly.
        // If TreatmentInfo had List<SomethingComplex>, TypeToken would be needed.
        // val type = object : TypeToken<TreatmentInfo>() {}.type
        return try {
            gson.fromJson(treatmentJson, TreatmentInfo::class.java)
        } catch (e: Exception) {
            // Handle potential parsing errors, e.g., if JSON format changes
            null // Or log the error and return null/default
        }
    }
}