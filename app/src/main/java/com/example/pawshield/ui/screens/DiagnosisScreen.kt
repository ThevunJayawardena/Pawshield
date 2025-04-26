package com.example.pawshield.ui.screens

import android.Manifest // Import Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat // Import ContextCompat
import androidx.core.content.FileProvider // Import FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.pawshield.viewmodel.MainViewModel
import com.example.pawshield.ui.Screen
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

// Helper function to create a URI for the camera to save the image
fun createImageUri(context: Context): Uri? {
    return try {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = context.cacheDir // Use cache dir for temporary storage
        val file = File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
        // Use your FileProvider authority defined in AndroidManifest.xml
        val authority = "${context.packageName}.provider"
        FileProvider.getUriForFile(
            Objects.requireNonNull(context),
            authority,
            file
        )
    } catch (ex: Exception) {
        Log.e("DiagnosisScreen", "Error creating image URI", ex)
        null
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosisScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val capturedImageUri by viewModel.capturedImageUri.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val selectedAnimalType by viewModel.selectedAnimalType.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // State to hold the URI generated *before* launching the camera
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // --- Camera Logic ---
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            Log.d("DiagnosisScreen", "Camera picture taken successfully. URI: $tempCameraUri")
            tempCameraUri?.let { uri ->
                viewModel.setCapturedImageUri(uri) // Update ViewModel with the final URI
            }
        } else {
            Log.d("DiagnosisScreen", "Camera picture cancelled or failed.")
            // Optionally handle cancellation (e.g., show a message)
            // Clear the temp URI if capture failed/cancelled
            tempCameraUri = null
        }
    }

    // --- Permission Logic ---
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, launch camera
            Log.d("DiagnosisScreen", "Camera permission granted.")
            val uri = createImageUri(context)
            if (uri != null) {
                tempCameraUri = uri // Store the URI before launching
                cameraLauncher.launch(uri)
            } else {
                viewModel.setErrorMessage("Could not create file for photo.")
            }
        } else {
            // Permission denied
            Log.d("DiagnosisScreen", "Camera permission denied.")
            viewModel.setErrorMessage("Camera permission is required to take photos.")
        }
    }

    // --- Gallery Logic ---
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            Log.d("DiagnosisScreen", "Image selected from gallery: $it")
            viewModel.setCapturedImageUri(it)
        }
    }

    // Show error messages in a Snackbar
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            viewModel.clearErrorMessage() // Clear the error after showing
        }
    }


    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("New Diagnosis") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.Home) }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Take or select a clear photo of your pet's skin condition",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Image preview/placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (capturedImageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(capturedImageUri), // Use Coil
                        contentDescription = "Selected pet image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Image Placeholder",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Image selection buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        Log.d("DiagnosisScreen", "Gallery button clicked")
                        galleryLauncher.launch("image/*")
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Gallery")
                }

                Button(
                    onClick = {
                        Log.d("DiagnosisScreen", "Take Photo button clicked")
                        // Check for permission before launching camera
                        when (PackageManager.PERMISSION_GRANTED) {
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) -> {
                                // Permission already granted
                                Log.d("DiagnosisScreen", "Camera permission already granted.")
                                val uri = createImageUri(context)
                                if (uri != null) {
                                    tempCameraUri = uri // Store URI before launch
                                    cameraLauncher.launch(uri)
                                } else {
                                    viewModel.setErrorMessage("Could not create file for photo.")
                                }
                            }
                            else -> {
                                // Request permission
                                Log.d("DiagnosisScreen", "Requesting camera permission.")
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Take Photo")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pet type selector (Using Toggle Buttons for better UX)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center // Center the buttons
            ) {
                FilterChip(
                    selected = selectedAnimalType == "Dog", // Use "Dog" for consistency
                    onClick = { viewModel.setAnimalType("Dog") }, // Use "Dog"
                    label = { Text("Dog") },
                    modifier = Modifier.padding(horizontal = 8.dp) // Add padding
                )

                FilterChip(
                    selected = selectedAnimalType == "Cat", // Use "Cat" for consistency
                    onClick = { viewModel.setAnimalType("Cat") }, // Use "Cat"
                    label = { Text("Cat") },
                    modifier = Modifier.padding(horizontal = 8.dp) // Add padding
                )
            }


            Spacer(modifier = Modifier.weight(1f)) // Pushes elements below to the bottom

            // Tips section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f) // Use secondary container
                ),
                shape = RoundedCornerShape(8.dp) // Adjust shape
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Tips for a Clear Photo",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold // Make title bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "• Ensure good lighting (avoid shadows)\n" +
                                "• Focus directly on the affected skin area\n" +
                                "• Hold the camera steady\n" +
                                "• Include some surrounding healthy skin if possible",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Analyze button
            Button(
                onClick = {
                    capturedImageUri?.let { uri ->
                        viewModel.analyzeImage(uri) // Pass the URI
                    } ?: run {
                        viewModel.setErrorMessage("Please select or take a photo first.")
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp), // Standard button height
                enabled = capturedImageUri != null && !isAnalyzing && selectedAnimalType != null, // Also check if animal type is selected
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isAnalyzing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary, // White on primary
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analyzing...")
                } else {
                    Text("Analyze")
                }
            }
        }
    }
}

// Ensure clearErrorMessage exists in ViewModel
fun MainViewModel.clearErrorMessage() {
    // Assuming _errorMessage is MutableStateFlow<String?>
    (this.javaClass.getDeclaredField("_errorMessage").apply { isAccessible = true }
        .get(this) as MutableStateFlow<String?>).value = null
}

// Ensure setErrorMessage exists in ViewModel (if not already)
fun MainViewModel.setErrorMessage(message: String) {
    (this.javaClass.getDeclaredField("_errorMessage").apply { isAccessible = true }
        .get(this) as MutableStateFlow<String?>).value = message
}