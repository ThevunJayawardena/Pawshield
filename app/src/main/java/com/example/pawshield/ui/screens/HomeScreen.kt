package com.example.pawshield.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pawshield.R
import com.example.pawshield.viewmodel.MainViewModel
import com.example.pawshield.ui.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PawShield") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary // Color for action icons
                ),
                actions = {
                    IconButton(onClick = { viewModel.navigateTo(Screen.About) }) {
                        Icon(
                            imageVector = Icons.Filled.Info, // Info icon for About
                            contentDescription = "About App"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp) // Add spacing between cards
        ) {
            // Hero Image Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.pawshield_logo),
                        contentDescription = "Pet Health",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Overlay text (optional, consider if readable over image)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)) // Dark overlay for text visibility
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Bottom,
                        horizontalAlignment = Alignment.Start // Align text left
                    ) {
                        Text(
                            "Protect Your Pet's Skin",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White, // Ensure text is visible
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Use our AI scanner for a preliminary check",
                            style = MaterialTheme.typography.bodyLarge, // Slightly larger body text
                            color = Color.White // Ensure text is visible
                        )
                    }
                }
            }

            // Scan Card
            ActionCard(
                icon = Icons.Default.CameraAlt,
                title = "Scan Skin Condition",
                subtitle = "Upload or take a photo for analysis",
                onClick = { viewModel.navigateTo(Screen.Diagnosis) }
            )

            // History Card
            ActionCard(
                icon = Icons.Default.History,
                title = "Diagnosis History",
                subtitle = "View your past scan results",
                onClick = { viewModel.navigateTo(Screen.History) }
            )

            Spacer(modifier = Modifier.weight(1f)) // Push content up if screen is tall
        }
    }
}

// Reusable Card for Scan and History actions
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp), // Slightly less rounded corners
        elevation = CardDefaults.cardElevation(2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp), // More vertical padding
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant // Slightly muted color
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowForwardIos, // More conventional arrow
                contentDescription = "Go",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}