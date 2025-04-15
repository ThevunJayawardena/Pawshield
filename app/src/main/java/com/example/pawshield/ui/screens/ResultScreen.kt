package com.example.pawshield.ui.screens

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle // For save confirmation
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Save // For save button
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource // Keep if you use placeholder drawables
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.pawshield.R // If you have string resources or drawables like pet_hero
import com.example.pawshield.viewmodel.MainViewModel
import com.example.pawshield.ui.Screen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(viewModel: MainViewModel) {
    // Observe the necessary states
    val diagnosisResult by viewModel.currentDiagnosisResult.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val feedbackMessage by viewModel.feedbackMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    // val context = LocalContext.current // Only needed if you load resources like strings/drawables directly

    // Show error or feedback messages
    LaunchedEffect(errorMessage, feedbackMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Long)
            viewModel.clearErrorMessage()
        }
        feedbackMessage?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
            viewModel.clearFeedbackMessage()
        }
    }

    // This effect handles navigating away if the result becomes null (e.g., after saving)
    // It doesn't need to know the *current* screen, just that the data it relies on is gone.
    LaunchedEffect(diagnosisResult) {
        if (diagnosisResult == null) {
            Log.d("ResultScreen", "Diagnosis result became null, navigating to Diagnosis.")

            viewModel.navigateTo(Screen.Home)
        }
    }

    val result = diagnosisResult // Use the state value

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Diagnosis Result") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.Home) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
        if (result == null) {
            // Displayed while result is null (e.g., navigating away after save)
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                // Optionally show a brief message or just let it navigate away
                // CircularProgressIndicator()
                Text("Loading...") // Or empty
            }
        } else {
            // Main content column when result is available
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Display captured image
                result.imageUriString?.let { uriString ->
                    Uri.parse(uriString)?.let { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(model = uri),
                            contentDescription = "Analyzed pet image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .padding(bottom = 16.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Diagnosis Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Preliminary Diagnosis",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val formattedName = result.conditionName
                            .replace("_", " ")
                            .split(" ")
                            .joinToString(" ") { word ->
                                word.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                            }

                        Text(
                            text = formattedName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        // Confidence removed
                    }
                } // End Diagnosis Card

                Spacer(modifier = Modifier.height(20.dp))

                // Treatment Info Card (only if treatment is not null)
                result.treatment?.let { treatment ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Information & Suggestions",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Description
                            Text(
                                text = "Description",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = treatment.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            // Symptoms
                            if (treatment.symptoms.isNotEmpty() && treatment.symptoms.first() != "Consult a veterinarian for accurate symptoms.") {
                                Text(
                                    text = "Common Symptoms",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                treatment.symptoms.forEach { symptom ->
                                    Row(modifier = Modifier.padding(bottom = 4.dp), verticalAlignment = Alignment.Top) {
                                        Text("• ", style = MaterialTheme.typography.bodyMedium)
                                        Text(symptom, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // Treatments
                            if (treatment.treatments.isNotEmpty() && treatment.treatments.first() != "Please consult with a veterinarian for diagnosis and treatment options.") {
                                Text(
                                    text = "Potential Treatments / Management",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                treatment.treatments.forEach { treatmentItem ->
                                    Row(modifier = Modifier.padding(bottom = 4.dp), verticalAlignment = Alignment.Top) {
                                        Text("• ", style = MaterialTheme.typography.bodyMedium)
                                        Text(treatmentItem, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                            }

                            // Veterinary visit advice Card
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = when (treatment.veterinaryVisit.lowercase(Locale.ROOT)) { // Use Locale.ROOT for consistency
                                        "recommended" -> MaterialTheme.colorScheme.secondaryContainer
                                        "urgent", "seek immediately" -> MaterialTheme.colorScheme.errorContainer
                                        else -> MaterialTheme.colorScheme.tertiaryContainer
                                    }
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Veterinary Advice",
                                        tint = when (treatment.veterinaryVisit.lowercase(Locale.ROOT)) {
                                            "recommended" -> MaterialTheme.colorScheme.onSecondaryContainer
                                            "urgent", "seek immediately" -> MaterialTheme.colorScheme.onErrorContainer
                                            else -> MaterialTheme.colorScheme.onTertiaryContainer
                                        },
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Veterinary Visit: ${treatment.veterinaryVisit}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Medium
                                            // Color set implicitly by container color
                                        )
                                    )
                                }
                            } // End Vet Visit Card
                        } // End Treatment Column
                    } // End Treatment Card
                } ?: run {
                    // Display if treatment info is null
                    Text(
                        "No specific treatment information available in the database for this condition.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } // End Treatment let/run

                Spacer(modifier = Modifier.height(24.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Save Button
                    OutlinedButton(
                        onClick = { viewModel.saveCurrentDiagnosis() },
                        modifier = Modifier.weight(1f),
                        // Enabled state is implicitly handled because `result` is non-null here
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save", Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Save Diagnosis") // Updated Text
                    }

                    // Home Button
                    Button(
                        onClick = { viewModel.navigateTo(Screen.Home) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Home, contentDescription = "Home", Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Home")
                    }
                } // End Action Buttons Row

                Spacer(modifier = Modifier.height(24.dp))

                // Disclaimer
                Text(
                    text = "Disclaimer: This AI provides preliminary guidance only and is not a substitute for professional veterinary diagnosis or treatment. Always consult a qualified veterinarian for any health concerns regarding your pet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp)) // Padding at the bottom

            } // End Main Column
        } // End Else (result != null)
    } // End Scaffold
} // End ResultScreen Composable