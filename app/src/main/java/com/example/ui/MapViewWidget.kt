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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HiddenGem
import com.example.ui.theme.*
import kotlin.math.sqrt

/**
 * Updated Interactive Map View Widget showing Hidden Gems
 * Supports smooth pan, zoom, category filtering, pin selection, and detail preview card.
 */
@Composable
fun MapViewWidget(
    viewModel: GemViewModel,
    modifier: Modifier = Modifier,
    onExpandToFullMap: (() -> Unit)? = null,
    onSelectGem: ((Int) -> Unit)? = null
) {
    val gems by viewModel.filteredGems.collectAsState()
    val centerLat by viewModel.mapCenterLat.collectAsState()
    val centerLng by viewModel.mapCenterLng.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var localZoom by remember { mutableStateOf(1.3f) }
    var selectedPinGem by remember { mutableStateOf<HiddenGem?>(null) }

    // Pulsing beacon animation for pins
    val infiniteTransition = rememberInfiniteTransition(label = "map_widget_pulse")
    val pinPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pin_pulse"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
            .shadow(16.dp, RoundedCornerShape(24.dp))
            .border(BorderStroke(1.dp, Color(0xFF1E3A29)), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF09140E)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val width = constraints.maxWidth.toFloat()
            val height = constraints.maxHeight.toFloat()
            val baseScale = 850.0
            val effectiveScale = (baseScale * localZoom).toFloat()

            // 1. Interactive Dynamic Map Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(centerLat, centerLng, localZoom) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val dLng = -dragAmount.x / effectiveScale
                            val dLat = dragAmount.y / (effectiveScale * 1.35f)
                            viewModel.setMapCenter(centerLat + dLat, centerLng + dLng)
                        }
                    }
                    .pointerInput(gems, centerLat, centerLng, localZoom) {
                        detectTapGestures(
                            onTap = { tapOffset ->
                                // Find nearest pin to tap
                                var clicked: HiddenGem? = null
                                var minDistance = 32.dp.toPx()

                                gems.forEach { gem ->
                                    val px = width / 2f + ((gem.longitude - centerLng) * effectiveScale).toFloat()
                                    val py = height / 2f - ((gem.latitude - centerLat) * effectiveScale * 1.35f).toFloat()
                                    val dx = tapOffset.x - px
                                    val dy = tapOffset.y - py
                                    val dist = sqrt(dx * dx + dy * dy)
                                    if (dist < minDistance) {
                                        minDistance = dist
                                        clicked = gem
                                    }
                                }

                                selectedPinGem = clicked
                                if (clicked != null) {
                                    viewModel.selectGem(clicked?.id)
                                }
                            }
                        )
                    }
            ) {
                // Background dark land
                drawRect(color = Color(0xFF0C1B13))

                // Subtle coordinate grid overlay
                val stepSize = 50f
                for (x in 0..width.toInt() step stepSize.toInt()) {
                    drawLine(
                        color = Color(0xFF132B1F),
                        start = Offset(x.toFloat(), 0f),
                        end = Offset(x.toFloat(), height),
                        strokeWidth = 1f
                    )
                }
                for (y in 0..height.toInt() step stepSize.toInt()) {
                    drawLine(
                        color = Color(0xFF132B1F),
                        start = Offset(0f, y.toFloat()),
                        end = Offset(width, y.toFloat()),
                        strokeWidth = 1f
                    )
                }

                // Coastline / Water / Reserve accent paths (Kenya Rift Valley & Indian Ocean coastline)
                // Kenya Coastline water block
                val oceanLeftX = width / 2f + ((39.5 - centerLng) * effectiveScale).toFloat()
                if (oceanLeftX < width) {
                    drawRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF071926), Color(0xFF04101A)),
                            startX = oceanLeftX.coerceAtLeast(0f),
                            endX = width
                        ),
                        topLeft = Offset(oceanLeftX.coerceAtLeast(0f), 0f),
                        size = Size(width - oceanLeftX.coerceAtLeast(0f), height)
                    )
                }

                // Forest & National Reserves (Karura, Nairobi NP)
                val nnpX = width / 2f + ((36.88 - centerLng) * effectiveScale).toFloat()
                val nnpY = height / 2f - ((-1.35 - centerLat) * effectiveScale * 1.35f).toFloat()
                drawCircle(
                    color = Color(0xFF143321),
                    radius = 24.dp.toPx() * localZoom,
                    center = Offset(nnpX, nnpY)
                )

                // ST_DWithin search radius circle overlay around map center
                drawCircle(
                    color = Color(0xFF00E676).copy(alpha = 0.08f),
                    radius = 80.dp.toPx() * localZoom,
                    center = Offset(width / 2f, height / 2f)
                )
                drawCircle(
                    color = Color(0xFF00E676).copy(alpha = 0.25f),
                    radius = 80.dp.toPx() * localZoom,
                    center = Offset(width / 2f, height / 2f),
                    style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f))
                )

                // 2. Render all Hidden Gem Pins on the map
                gems.forEach { gem ->
                    val px = width / 2f + ((gem.longitude - centerLng) * effectiveScale).toFloat()
                    val py = height / 2f - ((gem.latitude - centerLat) * effectiveScale * 1.35f).toFloat()

                    val isSelected = selectedPinGem?.id == gem.id

                    // Avoid drawing if far offscreen
                    if (px in -40f..(width + 40f) && py in -40f..(height + 40f)) {
                        val pinColor = when (gem.category.lowercase()) {
                            "scenic", "nature" -> Color(0xFF00E676)
                            "parks" -> Color(0xFF2ECC71)
                            "cafes", "food" -> Color(0xFFF39C12)
                            "beaches" -> Color(0xFF00BCD4)
                            "historic", "culture" -> Color(0xFFE91E63)
                            else -> Color(0xFFF5D061)
                        }

                        // Outer pulsing aura for selected or verified pins
                        if (isSelected || gem.isVerified) {
                            drawCircle(
                                color = pinColor.copy(alpha = if (isSelected) 0.35f else 0.18f),
                                radius = (14.dp.toPx() * if (isSelected) pinPulse else 1.1f),
                                center = Offset(px, py)
                            )
                        }

                        // Pin Base Drop Shadow
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.5f),
                            radius = 8.dp.toPx(),
                            center = Offset(px, py + 2f)
                        )

                        // Outer Border Ring
                        drawCircle(
                            color = if (isSelected) Color.White else Color(0xFF060D09),
                            radius = (8.dp.toPx() * if (isSelected) 1.25f else 1.0f),
                            center = Offset(px, py)
                        )

                        // Main Vibrant Core
                        drawCircle(
                            color = pinColor,
                            radius = (6.dp.toPx() * if (isSelected) 1.25f else 1.0f),
                            center = Offset(px, py)
                        )

                        // Inner Sparkle dot
                        drawCircle(
                            color = Color.White.copy(alpha = 0.9f),
                            radius = 2.dp.toPx(),
                            center = Offset(px - 1.5f, py - 1.5f)
                        )
                    }
                }
            }

            // ---------------------------------------------------------
            // OVERLAY: TOP CONTROLS BAR (TITLE + STATS + EXPAND BUTTON)
            // ---------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xDD09140E),
                    border = BorderStroke(1.dp, Color(0xFF1E3A29))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Live Map Radar",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF1B3B28))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${gems.size} Gems",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color(0xFF00E676),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                if (onExpandToFullMap != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xDD09140E),
                        border = BorderStroke(1.dp, Color(0xFF1E3A29)),
                        modifier = Modifier.clickable { onExpandToFullMap() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Full Map",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF5D061)
                                )
                            )
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Expand Full Map",
                                tint = Color(0xFFF5D061),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // ---------------------------------------------------------
            // OVERLAY: FLOATING ZOOM & RECENTER CONTROLS (RIGHT SIDE)
            // ---------------------------------------------------------
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Zoom In
                Surface(
                    shape = CircleShape,
                    color = Color(0xDD09140E),
                    border = BorderStroke(1.dp, Color(0xFF1E3A29)),
                    modifier = Modifier
                        .size(34.dp)
                        .clickable { localZoom = (localZoom * 1.3f).coerceAtMost(3.5f) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Zoom In",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Zoom Out
                Surface(
                    shape = CircleShape,
                    color = Color(0xDD09140E),
                    border = BorderStroke(1.dp, Color(0xFF1E3A29)),
                    modifier = Modifier
                        .size(34.dp)
                        .clickable { localZoom = (localZoom / 1.3f).coerceAtLeast(0.6f) }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Zoom Out",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Recenter
                Surface(
                    shape = CircleShape,
                    color = Color(0xDD09140E),
                    border = BorderStroke(1.dp, Color(0xFF1E3A29)),
                    modifier = Modifier
                        .size(34.dp)
                        .clickable {
                            viewModel.setMapCenter(-1.286389, 36.817223)
                            localZoom = 1.3f
                            selectedPinGem = null
                        }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Recenter",
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // ---------------------------------------------------------
            // OVERLAY: SELECTED PIN POPUP PREVIEW CARD (BOTTOM CENTER)
            // ---------------------------------------------------------
            AnimatedVisibility(
                visible = selectedPinGem != null,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp, start = 12.dp, end = 12.dp)
            ) {
                selectedPinGem?.let { gem ->
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xF00D1F15),
                        border = BorderStroke(1.dp, Color(0xFF2E6342)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSelectGem?.invoke(gem.id)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF163825)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = null,
                                        tint = Color(0xFF00E676),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = gem.title,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (gem.isVerified) {
                                            Icon(
                                                imageVector = Icons.Default.Verified,
                                                contentDescription = "Verified",
                                                tint = Color(0xFF00E676),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "${gem.category} • ${if (gem.isVerified) "Verified Spot" else "Community Secret"}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFFA6C5B3)
                                            )
                                        )
                                        Text(
                                            text = "▲ ${gem.upvotes} Upvotes",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFFF5D061),
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                    }
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Button(
                                    onClick = { onSelectGem?.invoke(gem.id) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF00E676),
                                        contentColor = Color(0xFF060D09)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(
                                        text = "View",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                IconButton(
                                    onClick = { selectedPinGem = null },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close preview",
                                        tint = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
