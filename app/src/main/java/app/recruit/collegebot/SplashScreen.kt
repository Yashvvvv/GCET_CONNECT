package com.example.collegebot

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 5000)
    )
    val scaleAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.5f,
        animationSpec = tween(durationMillis = 2000)
    )
    
    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(6000L)
        navController.navigate("chat") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E3D59), // Deep professional blue
                        Color(0xFF2D5F8B)  // Lighter complementary blue
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Animated background circles
        Box(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.1f)
        ) {
            repeat(3) { index ->
                AnimatedCircle(index)
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scaleAnim.value)
                .alpha(alphaAnim.value)
        ) {
            // Original Lottie animation
            val composition by rememberLottieComposition(
                LottieCompositionSpec.RawRes(R.raw.splash_animation)
            )
            val progress by animateLottieCompositionAsState(
                composition = composition,
                iterations = LottieConstants.IterateForever
            )
            
            LottieAnimation(
                composition = composition,
                progress = progress,
                modifier = Modifier.size(250.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "GCET Connect",
                style = MaterialTheme.typography.headlineLarge.copy(
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.25f),
                        offset = Offset(2f, 2f),
                        blurRadius = 4f
                    )
                ),
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF5F5F5)  // Almost white for better contrast
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your College Companion",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFE0E7FF),  // Light blue-ish white
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Loading animation
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.alpha(alphaAnim.value)
            ) {
                repeat(3) { index ->
                    LoadingDot(index = index)
                }
            }
        }

        // College logo at bottom with enhanced styling
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
                .alpha(alphaAnim.value)
        ) {
            Image(
                painter = painterResource(id = R.drawable.gcet_logo),
                contentDescription = "GCET Logo",
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.95f),
                                Color.White.copy(alpha = 0.85f)
                            )
                        )
                    ),
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun AnimatedCircle(index: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(index * 1500)
        ), label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .scale(scale)
            .alpha(0.1f)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.2f),
                        Color.Transparent
                    )
                )
            )
    )
}

@Composable
private fun LoadingDot(index: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(index * 500)
        ), label = ""
    )

    Box(
        modifier = Modifier
            .size(8.dp)
            .scale(1f + (scale * 0.5f))
            .clip(CircleShape)
            .background(Color(0xFFE0E7FF).copy(alpha = 0.8f))
    )
}





