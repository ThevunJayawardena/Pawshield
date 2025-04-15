package com.example.pawshield.data.repository

import android.content.Context
import com.example.pawshield.data.database.AppDatabase
import com.example.pawshield.data.model.DiagnosisHistoryEntry
import kotlinx.coroutines.flow.Flow

// Repository to abstract data sources (currently only Room DB)
class HistoryRepository(context: Context) {

    private val historyDao = AppDatabase.getDatabase(context).historyDao()

    val allHistoryEntries: Flow<List<DiagnosisHistoryEntry>> = historyDao.getAllHistoryEntries()

    suspend fun insert(entry: DiagnosisHistoryEntry) {
        historyDao.insertHistoryEntry(entry)
    }

    suspend fun deleteById(id: Int) {
        historyDao.deleteHistoryEntryById(id)
    }

    suspend fun deleteAll() {
        historyDao.clearAllHistory()
    }
}