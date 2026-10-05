package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.*

/**
 * Mesmerizing, high-fidelity Landing Screen featuring the "Eye of Discovery"
 * with smooth organic animations, interactive gaze tracking, lifelike ocular depth,
 * luminous iris striations, compass runes, and cinematic shutter exit.
 */
@Composable
fun LandingScreen(viewModel: GemViewModel) {
    // Luxury Obsidian & Emerald Palette
    val darkObsidian = Color(0xFF060D09)
    val deepJungle = Color(0xFF0A1E14)
    val emeraldMid = Color(0xFF134E35)
    val emeraldBright = Color(0xFF00E676)
    val cyanGlow = Color(0xFF00E5FF)
    val goldLustre = Color(0xFFF5D061)
    val goldDeep = Color(0xFFC5A038)
    val ivoryWhite = Color(0xFFFAFBF9)

    // Interaction & Animation States
    val scope = rememberCoroutineScope()
    var isDismissing by remember { mutableStateOf(false) }
    val shutterProgress = remember { Animatable(0f) }
    val irisPulse = remember { Animatable(1f) }

    // Interactive Gaze Tracking: Eye pupil follows touch / drag with smooth spring physics
    var targetGazeX by remember { mutableStateOf(0f) }
    var targetGazeY by remember { mutableStateOf(0f) }
    val animatedGazeX = remember { Animatable(0f) }
    val animatedGazeY = remember { Animatable(0f) }

    // Ambient micro-drift when not touched
    val infiniteTransition = rememberInfiniteTransition(label = "eye_ambient")

    // Ambient blink cycle (every 4.2 seconds)
    val ambientBlink by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4200
                1.0f at 0
                1.0f at 3400 // Fully open for 3.4s
                0.05f at 3550 // Swift close
                0.05f at 3650 // Brief hold shut
                1.0f at 3820 // Smooth reopen
                1.0f at 4200
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "ambient_blink"
    )

    // Slow celestial rune ring rotation
    val compassRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(32000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "compass_rotation"
    )

    // Counter-rotating outer ticks
    val outerRingRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(48000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_ring_rotation"
    )

    // Breathing pupil dilation
    val breathingPupil by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_pupil"
    )

    // Glowing aura pulse
    val auraGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_glow"
    )

    // Ambient subtle drift when user is not touching
    LaunchedEffect(Unit) {
        while (true) {
            if (targetGazeX == 0f && targetGazeY == 0f && !isDismissing) {
                // Occasional curious eye glance
                val driftX = ((-15..15).random()).toFloat()
                val driftY = ((-10..10).random()).toFloat()
                animatedGazeX.animateTo(driftX, tween(900, easing = FastOutSlowInEasing))
                animatedGazeY.animateTo(driftY, tween(900, easing = FastOutSlowInEasing))
                delay(2500)
                animatedGazeX.animateTo(0f, tween(1100, easing = FastOutSlowInEasing))
                animatedGazeY.animateTo(0f, tween(1100, easing = FastOutSlowInEasing))
            }
            delay(2000)
        }
    }

    // Trigger hypnotic exit on tap
    fun triggerAwakening() {
        if (isDismissing) return
        isDismissing = true
        scope.launch {
            // 1. Iris dilation & radiant pulse
            launch {
                irisPulse.animateTo(1.4f, tween(300, easing = FastOutSlowInEasing))
            }
            // 2. Cinematic eyelid shutter closing smoothly
            shutterProgress.animateTo(1f, tween(550, easing = FastOutSlowInEasing))
            delay(120)
            viewModel.dismissLandingPage()
        }
    }

    // Effective eyelid openness: 1.0 = wide open, 0.0 = completely shut
    val effectiveOpenness = if (isDismissing) {
        (1f - shutterProgress.value).coerceAtLeast(0f)
    } else {
        ambientBlink
    }

    // Stardust floating particles
    val stardustParticles = remember {
        List(22) {
            Triple(
                (50..950).random().toFloat() / 1000f,
                (40..960).random().toFloat() / 1000f,
                (2..6).random().toFloat()
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0F3220),
                        deepJungle,
                        darkObsidian
                    ),
                    center = Offset.Unspecified,
                    radius = 1200f
                )
            )
            .pointerInput(isDismissing) {
                if (!isDismissing) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            targetGazeX = (targetGazeX + dragAmount.x * 0.4f).coerceIn(-40f, 40f)
                            targetGazeY = (targetGazeY + dragAmount.y * 0.4f).coerceIn(-25f, 25f)
                            scope.launch {
                                animatedGazeX.snapTo(targetGazeX)
                                animatedGazeY.snapTo(targetGazeY)
                            }
                        },
                        onDragEnd = {
                            targetGazeX = 0f
                            targetGazeY = 0f
                            scope.launch {
                                animatedGazeX.animateTo(0f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow))
                                animatedGazeY.animateTo(0f, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow))
                            }
                        }
                    )
                }
            }
    ) {
        // -------------------------------------------------------------
        // BACKGROUND PARTICLES & TOPOGRAPHIC RAYS
        // -------------------------------------------------------------
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Floating luminous motes
            stardustParticles.forEachIndexed { i, p ->
                val px = p.first * w
                val py = p.second * h
                val floatOffset = sin(compassRotation * 0.05f + i) * 12f
                val glowAlpha = (0.25f + 0.55f * sin(auraGlow * 3.14f + i)).coerceIn(0.1f, 0.9f)

                drawCircle(
                    color = emeraldBright.copy(alpha = glowAlpha * 0.15f),
                    radius = p.third * 2.8f,
                    center = Offset(px, py + floatOffset)
                )
                drawCircle(
                    color = goldLustre.copy(alpha = glowAlpha),
                    radius = p.third * 0.9f,
                    center = Offset(px, py + floatOffset)
                )
            }

            // Top-left and bottom-right subtle organic explorer lines
            val cornerStroke = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f))
            drawCircle(
                color = goldDeep.copy(alpha = 0.08f),
                radius = w * 0.45f,
                center = Offset(0f, 0f),
                style = cornerStroke
            )
            drawCircle(
                color = emeraldBright.copy(alpha = 0.06f),
                radius = w * 0.5f,
                center = Offset(w, h),
                style = cornerStroke
            )
        }

        // -------------------------------------------------------------
        // MAIN CONTENT COLUMN (RESPONSIVE & CENTERED ON ALL SCREENS)
        // -------------------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP BRANDING & BADGE
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.widthIn(max = 600.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Explorer Guild Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = emeraldMid.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, goldLustre.copy(alpha = 0.4f)),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "✦",
                            color = goldLustre,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ALL-SEEING COMPASS",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.5.sp,
                                color = goldLustre
                            )
                        )
                        Text(
                            text = "✦",
                            color = goldLustre,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = "HIDDEN GEMS",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 6.sp,
                        color = ivoryWhite,
                        fontFamily = FontFamily.Serif
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Awaken the secret map. Discover unseen spots, verified crowds & authentic local gems.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFFB0C9BC),
                        letterSpacing = 0.5.sp,
                        lineHeight = 20.sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // -------------------------------------------------------------
            // THE EYE OF DISCOVERY (MASTER ARTWORK & INTERACTION)
            // -------------------------------------------------------------
            Box(
                modifier = Modifier
                    .size(290.dp)
                    .clip(CircleShape)
                    .pointerInput(isDismissing) {
                        detectTapGestures(
                            onTap = {
                                triggerAwakening()
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val eyeW = size.width * 0.82f
                    val eyeH = size.height * 0.48f

                    // 1. CELESTIAL RUNIC COMPASS RING (Slowly rotating outer disc)
                    rotate(compassRotation, pivot = Offset(cx, cy)) {
                        drawCircle(
                            color = goldDeep.copy(alpha = 0.18f),
                            radius = size.width * 0.44f,
                            center = Offset(cx, cy),
                            style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 8f), 0f))
                        )
                        // Cardinal compass points
                        val cardinalDist = size.width * 0.44f
                        listOf(0f, 90f, 180f, 270f).forEach { angle ->
                            val rad = Math.toRadians(angle.toDouble())
                            val markX = cx + cardinalDist * cos(rad).toFloat()
                            val markY = cy + cardinalDist * sin(rad).toFloat()
                            drawCircle(
                                color = goldLustre.copy(alpha = 0.7f),
                                radius = 2.5f,
                                center = Offset(markX, markY)
                            )
                        }
                    }

                    // Counter-rotating geometric tick aura
                    rotate(outerRingRotation, pivot = Offset(cx, cy)) {
                        drawCircle(
                            color = emeraldBright.copy(alpha = 0.12f * auraGlow),
                            radius = size.width * 0.39f,
                            center = Offset(cx, cy),
                            style = Stroke(width = 1f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 14f), 0f))
                        )
                    }

                    // Radiant Golden Halo under Sclera
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                emeraldBright.copy(alpha = 0.28f * auraGlow),
                                goldLustre.copy(alpha = 0.15f * auraGlow),
                                Color.Transparent
                            ),
                            center = Offset(cx, cy),
                            radius = eyeW * 0.65f
                        ),
                        radius = eyeW * 0.65f,
                        center = Offset(cx, cy)
                    )

                    // ---------------------------------------------------------
                    // 2. SCLERA PATH (EYELID CURVATURE WITH VOLUMETRIC DEPTH)
                    // ---------------------------------------------------------
                    val startX = cx - eyeW / 2f
                    val endX = cx + eyeW / 2f

                    // Organic asymmetric eyelid curvature: upper lid opens 65%, lower lid opens 35%
                    val topControlY = cy - (eyeH * 0.65f) * effectiveOpenness
                    val bottomControlY = cy + (eyeH * 0.35f) * effectiveOpenness

                    val scleraPath = Path().apply {
                        moveTo(startX, cy)
                        quadraticTo(cx, topControlY, endX, cy)
                        quadraticTo(cx, bottomControlY, startX, cy)
                        close()
                    }

                    // Sclera Clip & Volumetric 3D Spherical Shading
                    drawPath(
                        path = scleraPath,
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFF9FDFB),
                                Color(0xFFE4EDE7),
                                Color(0xFFB8CCC1),
                                Color(0xFF2A4235)
                            ),
                            center = Offset(cx, cy),
                            radius = eyeW * 0.45f
                        )
                    )

                    // Ambient occlusion shadow under upper eyelid
                    val upperShadowPath = Path().apply {
                        moveTo(startX, cy)
                        quadraticTo(cx, topControlY, endX, cy)
                        quadraticTo(cx, topControlY + (16.dp.toPx() * effectiveOpenness), startX, cy)
                        close()
                    }
                    drawPath(
                        path = upperShadowPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x99000000),
                                Color.Transparent
                            ),
                            startY = topControlY,
                            endY = topControlY + (16.dp.toPx() * effectiveOpenness)
                        )
                    )

                    // ---------------------------------------------------------
                    // 3. IRIS & PUPIL WITH MESMERIZING STRIATIONS & GLOW
                    // ---------------------------------------------------------
                    if (effectiveOpenness > 0.04f) {
                        // Gaze offset with natural parallax
                        val gazeX = animatedGazeX.value
                        val gazeY = animatedGazeY.value
                        val irisCenterX = cx + gazeX
                        val irisCenterY = cy + gazeY

                        val baseIrisRadius = (eyeH * 0.46f) * (0.8f + 0.2f * effectiveOpenness)
                        val irisRadius = baseIrisRadius * irisPulse.value

                        // A. Outer Limbal Ring (Dark, crisp, glowing edge)
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF032214).copy(alpha = 0.85f),
                                    Color(0xFF02130B)
                                ),
                                center = Offset(irisCenterX, irisCenterY),
                                radius = irisRadius
                            ),
                            radius = irisRadius,
                            center = Offset(irisCenterX, irisCenterY)
                        )

                        // B. Emerald & Golden Jewel Iris Gradient
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    goldLustre,
                                    emeraldBright,
                                    Color(0xFF0B663F),
                                    Color(0xFF073822),
                                    Color(0xFF02170D)
                                ),
                                center = Offset(irisCenterX, irisCenterY),
                                radius = irisRadius * 0.95f
                            ),
                            radius = irisRadius * 0.95f,
                            center = Offset(irisCenterX, irisCenterY)
                        )

                        // C. Radiant Iris Fibers / Striations (36 intricate rays)
                        for (ray in 0 until 36) {
                            val angleRad = (ray * 10) * (PI / 180.0)
                            val rayLenRatio = 0.5f + (ray % 5) * 0.1f
                            val innerR = irisRadius * 0.25f
                            val outerR = irisRadius * 0.90f * rayLenRatio.toFloat()

                            val rx1 = irisCenterX + (cos(angleRad) * innerR).toFloat()
                            val ry1 = irisCenterY + (sin(angleRad) * innerR).toFloat()
                            val rx2 = irisCenterX + (cos(angleRad) * outerR).toFloat()
                            val ry2 = irisCenterY + (sin(angleRad) * outerR).toFloat()

                            val rayColor = if (ray % 3 == 0) goldLustre.copy(alpha = 0.65f) else cyanGlow.copy(alpha = 0.45f)
                            drawLine(
                                color = rayColor,
                                start = Offset(rx1, ry1),
                                end = Offset(rx2, ry2),
                                strokeWidth = if (ray % 2 == 0) 1.5f else 1.0f
                            )
                        }

                        // D. Concentric Glowing Collarette Ring
                        drawCircle(
                            color = goldLustre.copy(alpha = 0.5f * auraGlow),
                            radius = irisRadius * 0.52f,
                            center = Offset(irisCenterX, irisCenterY),
                            style = Stroke(width = 1.2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f))
                        )

                        // E. Dynamic Breathing Pupil
                        val pupilRadius = (irisRadius * 0.38f * breathingPupil) * if (isDismissing) irisPulse.value else 1.0f
                        drawCircle(
                            color = Color(0xFF040605),
                            radius = pupilRadius,
                            center = Offset(irisCenterX, irisCenterY)
                        )

                        // F. Dual Prismatic Specular Highlights (Studio Curve + Star Sparkle)
                        // Crescent Primary Reflection
                        val glintPath = Path().apply {
                            val gRadius = pupilRadius * 0.55f
                            val gx = irisCenterX - pupilRadius * 0.42f
                            val gy = irisCenterY - pupilRadius * 0.42f
                            moveTo(gx - gRadius, gy)
                            quadraticTo(gx, gy - gRadius, gx + gRadius, gy)
                            quadraticTo(gx, gy - gRadius * 0.3f, gx - gRadius, gy)
                            close()
                        }
                        drawPath(
                            path = glintPath,
                            color = Color.White.copy(alpha = 0.92f)
                        )

                        // Crisp Pinpoint Secondary Glint
                        drawCircle(
                            color = Color.White.copy(alpha = 0.85f),
                            radius = 2.5.dp.toPx(),
                            center = Offset(irisCenterX + pupilRadius * 0.35f, irisCenterY + pupilRadius * 0.35f)
                        )
                        drawCircle(
                            color = cyanGlow.copy(alpha = 0.6f),
                            radius = 1.2.dp.toPx(),
                            center = Offset(irisCenterX + pupilRadius * 0.35f, irisCenterY + pupilRadius * 0.35f)
                        )
                    }

                    // ---------------------------------------------------------
                    // 4. GOLDEN EYELID OUTLINES & RADIANT COMPASS LASHES
                    // ---------------------------------------------------------
                    val upperLidPath = Path().apply {
                        moveTo(startX, cy)
                        quadraticTo(cx, topControlY, endX, cy)
                    }
                    val lowerLidPath = Path().apply {
                        moveTo(startX, cy)
                        quadraticTo(cx, bottomControlY, endX, cy)
                    }

                    // Upper Lid Crease (Anatomical depth)
                    val upperCreaseY = topControlY - (14.dp.toPx() * effectiveOpenness)
                    val creasePath = Path().apply {
                        moveTo(startX + 20f, cy - 4f)
                        quadraticTo(cx, upperCreaseY, endX - 20f, cy - 4f)
                    }
                    drawPath(
                        path = creasePath,
                        color = goldDeep.copy(alpha = 0.35f * effectiveOpenness),
                        style = Stroke(width = 1.2f)
                    )

                    // Main Gold Eyeliner Contours
                    drawPath(
                        path = upperLidPath,
                        brush = Brush.horizontalGradient(
                            colors = listOf(goldDeep.copy(alpha = 0.4f), goldLustre, goldDeep.copy(alpha = 0.4f)),
                            startX = startX,
                            endX = endX
                        ),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawPath(
                        path = lowerLidPath,
                        brush = Brush.horizontalGradient(
                            colors = listOf(goldDeep.copy(alpha = 0.3f), emeraldBright.copy(alpha = 0.8f), goldDeep.copy(alpha = 0.3f)),
                            startX = startX,
                            endX = endX
                        ),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )

                    // Compass Ticks on Eyelids (Cardinal Marks at Corners)
                    drawCircle(color = goldLustre, radius = 3.dp.toPx(), center = Offset(startX, cy))
                    drawCircle(color = goldLustre, radius = 3.dp.toPx(), center = Offset(endX, cy))
                }

                // Eye Center Touch Hint Overlay
                if (!isDismissing) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.Transparent),
                        contentAlignment = Alignment.Center
                    ) {
                        // Gentle pulsating touch target ring
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = goldLustre.copy(alpha = 0.25f * (1f - auraGlow)),
                                radius = size.width * 0.45f * auraGlow,
                                style = Stroke(width = 1.5f)
                            )
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // BOTTOM CALL TO ACTION & PILL HIGHLIGHTS
            // -------------------------------------------------------------
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
            ) {
                // Interactive Hint
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {
                            triggerAwakening()
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = goldLustre,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Touch the Eye to Awaken the Secrets",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = goldLustre,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Feature Highlights Pills Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LandingFeatureChip(icon = Icons.Default.Map, label = "Vector Radar")
                    LandingFeatureChip(icon = Icons.Default.People, label = "Crowd Density")
                    LandingFeatureChip(icon = Icons.Default.Verified, label = "Verified Gems")
                }

                Spacer(modifier = Modifier.height(18.dp))

                // PRIMARY ACTION BUTTON (SHIMMERING LUXURY CTA)
                Button(
                    onClick = { triggerAwakening() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = emeraldBright,
                        contentColor = darkObsidian
                    ),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .shadow(16.dp, RoundedCornerShape(28.dp), spotColor = emeraldBright.copy(alpha = 0.5f)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "ENTER HIDDEN GEMS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun LandingFeatureChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0E2519),
        border = BorderStroke(1.dp, Color(0xFF1E4631)),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF2ECC71),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFFD6E4DC),
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}
