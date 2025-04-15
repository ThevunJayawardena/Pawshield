// Inside your ui/screens package, or a dedicated splash package
package com.example.pawshield.ui.screens

import androidx.compose.animation.core.* // Import necessary animation core functions, including spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.pawshield.R // Your R file import
import kotlinx.coroutines.delay

@Composable
fun PawShieldSplashScreen(onTimeout: () -> Unit) {
    val splashDurationMillis = 2500L
    val animationDuration = 1500 // Duration for the alpha fade

    var startAnimation by remember { mutableStateOf(false) }

    // Alpha animation (fade-in using tween) - Tween is fine for simple fades
    val alphaAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = animationDuration),
        label = "splash_alpha"
    )

    // Scale animation (zoom-in effect using spring for overshoot)
    val scaleAnim by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f, // Start smaller
        // Use spring for the overshoot effect
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy, // Creates a noticeable overshoot/bounce
            stiffness = Spring.StiffnessLow           // Adjust stiffness for speed (Low, Medium, High)
            // You can also provide specific float values for dampingRatio and stiffness
            // dampingRatio = 0.4f, // Values < 1.0 cause overshoot
            // stiffness = 200f
        ),
        label = "splash_scale"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(splashDurationMillis)
        onTimeout()
    }

    // --- Your Splash Screen UI (Remains the same) ---
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alphaAnim) // Apply fade-in
        ) {
            Image(
                painter = painterResource(id = R.drawable.pawshield_logo),
                contentDescription = "PawShield Logo",
                modifier = Modifier
                    .size(150.dp)
                    .scale(scaleAnim) // Apply spring-based scale animation
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "PawShield",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}