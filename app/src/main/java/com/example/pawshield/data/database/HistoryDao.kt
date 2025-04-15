package com.example.pawshield.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.pawshield.data.model.DiagnosisHistoryEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    // Insert a new history entry, replacing duplicates based on primary key (id)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistoryEntry(entry: DiagnosisHistoryEntry) : Long // Return Long (new rowId or updated rowId)

    // Get all history entries as a Flow, ordered by newest first
    @Query("SELECT * FROM diagnosis_history ORDER BY timestamp DESC")
    fun getAllHistoryEntries(): Flow<List<DiagnosisHistoryEntry>> // Flow ensures it emits an initial empty list if DB is empty

    // Get a single entry by ID (useful if you need to re-fetch details)
    @Query("SELECT * FROM diagnosis_history WHERE id = :entryId")
    fun getHistoryEntryById(entryId: Int): Flow<DiagnosisHistoryEntry?> // Flow for observing changes

    // Delete a specific entry by ID
    @Query("DELETE FROM diagnosis_history WHERE id = :entryId")
    suspend fun deleteHistoryEntryById(entryId: Int): Int // Return number of rows deleted

    // Clear all history
    @Query("DELETE FROM diagnosis_history")
    suspend fun clearAllHistory(): Int // Return number of rows deleted
}