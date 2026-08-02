package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import kotlinx.coroutines.delay
import kotlin.math.sin

@Composable
fun LandingScreen(viewModel: GemViewModel) {
    // Elegant Jungle Nature Colors
    val jungleDeep = Color(0xFF071F11)
    val jungleDarkGreen = Color(0xFF0D3D22)
    val jungleEmerald = Color(0xFF1B633E)
    val jungleLightGreen = Color(0xFF2ECC71)
    val goldAccent = Color(0xFFF1C40F)
    val ivoryText = Color(0xFFF5F6FA)

    // Eye opening & closing infinite transition
    val infiniteTransition = rememberInfiniteTransition(label = "eye_transition")
    val eyeBlinkOpening by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                1.0f at 0 // Open
                1.0f at 3200 // Stay open
                0.0f at 3400 // Fast close
                0.0f at 3600 // Keep closed (blink)
                1.0f at 3800 // Re-open
                1.0f at 4000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "eye_opening"
    )

    // Dynamic state for click trigger to close eye completely
    var isClicked by remember { mutableStateOf(false) }
    val clickCloseProgress = remember { Animatable(1f) }

    LaunchedEffect(isClicked) {
        if (isClicked) {
            // Animate eye closing smoothly over 500ms
            clickCloseProgress.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
            )
            delay(100)
            viewModel.dismissLandingPage()
        }
    }

    // Effective eyeOpening (ambient blinking when not clicked, forces shut on click)
    val eyeOpening = if (isClicked) clickCloseProgress.value else eyeBlinkOpening

    // Fireflies / Floating spots animation
    val fireflyOffsets = remember {
        List(15) {
            Offset(
                x = (20..980).random().toFloat() / 1000f,
                y = (10..950).random().toFloat() / 1000f
            )
        }
    }
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "firefly_pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(jungleDeep, jungleDarkGreen, Color(0xFF031008))
                )
            )
    ) {
        // 1. Draw glowing fireflies (Hidden spots) & jungle elements in canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Fireflies
            fireflyOffsets.forEachIndexed { index, normOffset ->
                val px = normOffset.x * width
                val py = normOffset.y * height
                val sizePulse = (3.dp.toPx() + (index % 5) * 2.dp.toPx()) * (pulseAnim + 0.2f)
                val colorAlpha = (0.4f + 0.6f * (sin(pulseAnim * (index + 1)) + 1f) / 2f)
                
                drawCircle(
                    color = jungleLightGreen.copy(alpha = colorAlpha * 0.15f),
                    radius = sizePulse * 2.5f,
                    center = Offset(px, py)
                )
                drawCircle(
                    color = goldAccent.copy(alpha = colorAlpha),
                    radius = sizePulse * 0.8f,
                    center = Offset(px, py)
                )
            }

            // Draw illustrative organic background vines / leaves on edges
            val leafPath = Path().apply {
                moveTo(0f, 0f)
                quadraticTo(width * 0.2f, height * 0.05f, width * 0.1f, height * 0.15f)
                quadraticTo(width * 0.05f, height * 0.1f, 0f, 0f)
            }
            drawPath(leafPath, color = jungleEmerald.copy(alpha = 0.3f))

            val rightLeafPath = Path().apply {
                moveTo(width, height)
                quadraticTo(width * 0.8f, height * 0.95f, width * 0.9f, height * 0.85f)
                quadraticTo(width * 0.95f, height * 0.9f, width, height)
            }
            drawPath(rightLeafPath, color = jungleEmerald.copy(alpha = 0.3f))
        }

        // 2. Central Jungle Interactive Opening Eye and Title
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header spacing
            Spacer(modifier = Modifier.height(16.dp))

            // Brand/App Title Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "HIDDEN GEMS",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 8.sp,
                        color = ivoryText,
                        fontFamily = FontFamily.Serif
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Unveil the city's best-kept secrets",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = jungleLightGreen,
                        letterSpacing = 2.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center
                )
            }

            // 3. The Opening/Closing Eye (Jungle Eye)
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(RoundedCornerShape(120.dp))
                    .background(Color.Black.copy(alpha = 0.3f))
                    .clickable(enabled = !isClicked) {
                        isClicked = true
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val eyeWidth = size.width * 0.75f
                    val eyeHeight = size.height * 0.45f

                    // Sclera / Eye White container path (clipped by eye shape)
                    val scleraPath = Path().apply {
                        val startX = cx - eyeWidth / 2f
                        val endX = cx + eyeWidth / 2f
                        moveTo(startX, cy)
                        // Top lid curve (modulated by eyeOpening)
                        val topControlY = cy - (eyeHeight / 2f) * eyeOpening
                        quadraticTo(cx, topControlY, endX, cy)
                        // Bottom lid curve
                        val bottomControlY = cy + (eyeHeight / 2f) * eyeOpening
                        quadraticTo(cx, bottomControlY, startX, cy)
                        close()
                    }

                    // Draw Sclera
                    drawPath(
                        path = scleraPath,
                        color = Color(0xFFF9F9FB)
                    )

                    // Draw Iris & Pupil (only when eye is open, or scale with opening)
                    if (eyeOpening > 0.05f) {
                        val irisRadius = (eyeHeight * 0.42f) * (0.6f + 0.4f * eyeOpening)
                        
                        // Emerald / Gold Gradient for Jungle Eye Iris
                        val irisBrush = Brush.radialGradient(
                            colors = listOf(jungleLightGreen, jungleEmerald, Color(0xFF0B1F13)),
                            center = Offset(cx, cy),
                            radius = irisRadius
                        )
                        
                        // Draw Iris inside sclera by clipping, or just draw it normally if eyeHeight is scaled
                        drawCircle(
                            brush = irisBrush,
                            radius = irisRadius,
                            center = Offset(cx, cy)
                        )

                        // Pupil (Black hole in center)
                        val pupilRadius = irisRadius * 0.45f
                        drawCircle(
                            color = Color(0xFF080C0A),
                            radius = pupilRadius,
                            center = Offset(cx, cy)
                        )

                        // Light Reflection / Sparkle
                        drawCircle(
                            color = Color.White.copy(alpha = 0.8f),
                            radius = pupilRadius * 0.3f,
                            center = Offset(cx - pupilRadius * 0.35f, cy - pupilRadius * 0.35f)
                        )
                    }

                    // Eyelid borders (Contours / Outlines)
                    val outlinePath = Path().apply {
                        val startX = cx - eyeWidth / 2f
                        val endX = cx + eyeWidth / 2f
                        
                        // Top outline
                        moveTo(startX, cy)
                        val topControlY = cy - (eyeHeight / 2f) * eyeOpening
                        quadraticTo(cx, topControlY, endX, cy)
                        
                        // Bottom outline
                        val bottomControlY = cy + (eyeHeight / 2f) * eyeOpening
                        quadraticTo(cx, bottomControlY, startX, cy)
                    }

                    drawPath(
                        path = outlinePath,
                        color = goldAccent,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }

                // Small center overlay icon for extra touch of mystery
                Icon(
                    imageVector = Icons.Default.Visibility,
                    contentDescription = null,
                    tint = jungleLightGreen.copy(alpha = 0.1f),
                    modifier = Modifier.size(120.dp)
                )
            }

            Text(
                text = "— Tap the Eye to Enter —",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = goldAccent.copy(alpha = 0.85f),
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // 4. Entering Jungle call-to-action button
            Button(
                onClick = {
                    if (!isClicked) {
                        isClicked = true
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = jungleLightGreen,
                    contentColor = jungleDeep
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = "DISCOVER HIDDEN GEMS",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                )
            }
        }
    }
}
