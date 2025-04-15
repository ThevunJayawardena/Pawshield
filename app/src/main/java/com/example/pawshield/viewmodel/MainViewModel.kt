package com.example.pawshield.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pawshield.data.model.DiagnosisApiResponse
import com.example.pawshield.data.model.DiagnosisResult
import com.example.pawshield.data.model.SkinCondition
import com.example.pawshield.data.network.RetrofitClient
import com.example.pawshield.ui.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.*

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.instance

    // Navigation state
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Animal selection
    private val _selectedAnimalType = MutableStateFlow("dog")
    val selectedAnimalType: StateFlow<String> = _selectedAnimalType.asStateFlow()

    // Diagnosis state
    private val _capturedImageUri = MutableStateFlow<Uri?>(null)
    val capturedImageUri: StateFlow<Uri?> = _capturedImageUri.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    // Holds the *current* result before saving
    private val _currentDiagnosisResult = MutableStateFlow<DiagnosisResult?>(null)
    val currentDiagnosisResult: StateFlow<DiagnosisResult?> = _currentDiagnosisResult.asStateFlow()

    // History - Use a MutableList inside StateFlow for easier updates
    private val _diagnosisHistory = MutableStateFlow<List<DiagnosisResult>>(emptyList())
    val diagnosisHistory: StateFlow<List<DiagnosisResult>> = _diagnosisHistory.asStateFlow()

    // For displaying a selected history item
    private val _selectedHistoryItem = MutableStateFlow<DiagnosisResult?>(null)
    val selectedHistoryItem: StateFlow<DiagnosisResult?> = _selectedHistoryItem.asStateFlow()

    // Error Handling State
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Feedback Message State (e.g., for "Saved!")
    private val _feedbackMessage = MutableStateFlow<String?>(null)
    val feedbackMessage: StateFlow<String?> = _feedbackMessage.asStateFlow()

    fun setAnimalType(type: String) {
        _selectedAnimalType.value = type
        Log.d("MainViewModel", "Animal type set to: $type")
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
        // Clear specific states when navigating away or to certain screens
        if (screen == Screen.Home || screen == Screen.Diagnosis) {
            _capturedImageUri.value = null
            _currentDiagnosisResult.value = null // Clear current result
        }
        if (screen != Screen.HistoryDetail) {
            _selectedHistoryItem.value = null // Clear selected history item unless navigating to detail
        }
        _errorMessage.value = null // Clear errors on any navigation
        _feedbackMessage.value = null // Clear feedback messages
    }

    fun setCapturedImageUri(uri: Uri?) {
        _capturedImageUri.value = uri
        _currentDiagnosisResult.value = null // Clear previous result when new image is set
        _errorMessage.value = null
    }

    fun analyzeImage(imageUri: Uri) {
        if (_isAnalyzing.value) return

        viewModelScope.launch {
            _isAnalyzing.value = true
            _errorMessage.value = null
            _currentDiagnosisResult.value = null // Clear previous result before analyzing
            val context = getApplication<Application>().applicationContext

            try {
                context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
                    val imageBytes = inputStream.readBytes()
                    val mimeType = context.contentResolver.getType(imageUri) ?: "image/jpeg"
                    val requestFile = imageBytes.toRequestBody(mimeType.toMediaTypeOrNull())
                    val fileName = "image_${UUID.randomUUID()}.${mimeType.split('/').lastOrNull() ?: "jpg"}"
                    val imagePart = MultipartBody.Part.createFormData("image", fileName, requestFile)
                    val animalType = _selectedAnimalType.value
                    val animalTypePart = animalType.toRequestBody("text/plain".toMediaTypeOrNull())

                    Log.i("MainViewModel", "Sending image for $animalType analysis...")
                    val response = apiService.predictSkinCondition(imagePart, animalTypePart)

                    if (response.isSuccessful && response.body() != null) {
                        val apiResponse = response.body()!!
                        Log.i("MainViewModel", "API Success: Received diagnosis - ${apiResponse.diagnosis}")

                        // Create the result object for the current diagnosis
                        val result = DiagnosisResult(
                            conditionName = apiResponse.diagnosis, // Store only the name
                            treatment = apiResponse.treatment_info,
                            timestamp = Date(), // Capture current time
                            imageUriString = imageUri.toString() // Store Uri as string
                        )

                        _currentDiagnosisResult.value = result // Set the current result
                        _currentScreen.value = Screen.Result // Navigate to Result Screen

                    } else {
                        val errorBody = response.errorBody()?.string() ?: "Unknown server error"
                        Log.e("MainViewModel", "API Error: ${response.code()} - $errorBody")
                        _errorMessage.value = "Analysis failed: Server error ${response.code()}. Please try again."
                        // Stay on Diagnosis screen on error
                    }

                } ?: run {
                    _errorMessage.value = "Error reading image file. Please select another image."
                    Log.e("MainViewModel", "Failed to open InputStream for URI: $imageUri")
                }

            } catch (e: Exception) {
                Log.e("MainViewModel", "Exception during analysis", e)
                _errorMessage.value = "Analysis failed: ${e.localizedMessage ?: "Check network connection"}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    // Function to save the *current* diagnosis to the history list
    fun saveCurrentDiagnosis() {
        viewModelScope.launch {
            _currentDiagnosisResult.value?.let { resultToSave ->
                // Add to the beginning of the list
                _diagnosisHistory.update { currentList ->
                    listOf(resultToSave) + currentList
                }
                Log.i("MainViewModel", "Diagnosis saved: ${resultToSave.conditionName}")
                _feedbackMessage.value = "Diagnosis Saved!"
                // Optional: Clear current result after saving to prevent accidental re-save?
                _currentDiagnosisResult.value = null
                navigateTo(Screen.History) // Navigate to history after saving
            } ?: run {
                Log.w("MainViewModel", "Attempted to save but no current diagnosis result exists.")
                _errorMessage.value = "No diagnosis result to save."
            }
        }
    }

    // Function to view details of a specific history item
    fun viewHistoryDetail(item: DiagnosisResult) {
        _selectedHistoryItem.value = item
        navigateTo(Screen.HistoryDetail)
    }

    // Function to clear the error message once shown
    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    // Function to clear the feedback message once shown
    fun clearFeedbackMessage() {
        _feedbackMessage.value = null
    }
}