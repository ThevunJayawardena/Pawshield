package com.example.pawshield.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.pawshield.viewmodel.MainViewModel
import com.example.pawshield.ui.Screen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(viewModel: MainViewModel) {
    val selectedItem by viewModel.selectedHistoryItem.collectAsState()
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd 'at' HH:mm", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diagnosis Detail") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.History) }) { // Go back to History list
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back to History")
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
        selectedItem?.let { item ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Display saved image
                item.imageUriString?.let { uriString ->
                    Uri.parse(uriString)?.let { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(model = uri),
                            contentDescription = "Saved pet image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp) // Make image a bit larger
                                .clip(RoundedCornerShape(12.dp))
                                .padding(bottom = 16.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Display Timestamp
                Text(
                    text = "Diagnosis Recorded: ${dateFormat.format(item.timestamp)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )


                // Diagnosis Card (Similar to ResultScreen but without confidence)
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
                            text = "Diagnosis",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val formattedName = item.conditionName
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
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Treatment Info Card (if available)
                item.treatment?.let { treatment ->
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
                                    containerColor = when (treatment.veterinaryVisit.lowercase()) {
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
                                        tint = when (treatment.veterinaryVisit.lowercase()) {
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
                                        )
                                    )
                                }
                            } // End Vet Visit Card
                        } // End Treatment Column
                    } // End Treatment Card
                } ?: run {
                    Text(
                        "No specific treatment information was saved for this diagnosis.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                } // End Treatment let/run

                Spacer(modifier = Modifier.height(16.dp))

            } // End Main Column
        } ?: run {
            // Display if selectedItem is null (e.g., navigated directly)
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No history item selected.")
            }
        }
    } // End Scaffold
}