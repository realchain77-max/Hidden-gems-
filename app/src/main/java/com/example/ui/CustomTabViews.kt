package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HiddenGem
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

// =============================================================================
// CATEGORY ILLUSTRATIONS (HIGH FIDELITY CUSTOM REAL-TIME VECTOR DRAWINGS)
// =============================================================================
@Composable
fun CategoryIllustrationCanvas(category: String, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Rich vertical gradient depending on spot category
        val gradient = when (category.lowercase()) {
            "beaches" -> Brush.verticalGradient(listOf(Color(0xFF0F3A5F), Color(0xFF2874A6)))
            "parks" -> Brush.verticalGradient(listOf(Color(0xFF145A32), Color(0xFF27AE60)))
            "scenic" -> Brush.verticalGradient(listOf(Color(0xFF4A235A), Color(0xFF8E44AD)))
            "historic" -> Brush.verticalGradient(listOf(Color(0xFF78281F), Color(0xFFC0392B)))
            "cafes" -> Brush.verticalGradient(listOf(Color(0xFF5C3A21), Color(0xFF935116)))
            "arts" -> Brush.verticalGradient(listOf(Color(0xFF7D6608), Color(0xFFD4AC0D)))
            else -> Brush.verticalGradient(listOf(Color(0xFF2E4053), Color(0xFF5D6D7E)))
        }

        drawRect(brush = gradient)

        // Draw illustrative vector layers for beautiful, responsive texture
        when (category.lowercase()) {
            "beaches" -> {
                // Glowing Sun & Wave sweeps
                drawCircle(
                    color = Color(0xFFF39C12).copy(alpha = 0.8f),
                    radius = height * 0.28f,
                    center = Offset(width * 0.75f, height * 0.35f)
                )
                val wavePath = Path().apply {
                    moveTo(0f, height * 0.65f)
                    quadraticTo(width * 0.25f, height * 0.55f, width * 0.5f, height * 0.65f)
                    quadraticTo(width * 0.75f, height * 0.75f, width, height * 0.65f)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }
                drawPath(wavePath, color = Color(0xFF1B4F72).copy(alpha = 0.85f))
                
                val wavePath2 = Path().apply {
                    moveTo(0f, height * 0.78f)
                    quadraticTo(width * 0.3f, height * 0.85f, width * 0.6f, height * 0.75f)
                    quadraticTo(width * 0.85f, height * 0.68f, width, height * 0.82f)
                    lineTo(width, height)
                    lineTo(0f, height)
                    close()
                }
                drawPath(wavePath2, color = Color(0xFF5DADE2).copy(alpha = 0.3f))
            }
            "parks" -> {
                // Golden sun peaking through stylized pine silhouettes
                drawCircle(
                    color = Color(0xFFF1C40F).copy(alpha = 0.5f),
                    radius = height * 0.25f,
                    center = Offset(width * 0.3f, height * 0.35f)
                )
                // Evergreen 1
                val tree1 = Path().apply {
                    moveTo(width * 0.55f, height * 0.25f)
                    lineTo(width * 0.44f, height * 0.85f)
                    lineTo(width * 0.66f, height * 0.85f)
                    close()
                }
                drawPath(tree1, color = Color(0xFF0E3A20))
                // Evergreen 2
                val tree2 = Path().apply {
                    moveTo(width * 0.72f, height * 0.38f)
                    lineTo(width * 0.64f, height * 0.9f)
                    lineTo(width * 0.8f, height * 0.9f)
                    close()
                }
                drawPath(tree2, color = Color(0xFF196F3D))
            }
            "scenic" -> {
                // Mountain Range & Crescent Moon
                drawCircle(
                    color = Color(0xFFF5EEF8).copy(alpha = 0.8f),
                    radius = height * 0.15f,
                    center = Offset(width * 0.8f, height * 0.3f)
                )
                val mt1 = Path().apply {
                    moveTo(width * 0.2f, height)
                    lineTo(width * 0.55f, height * 0.42f)
                    lineTo(width * 0.9f, height)
                    close()
                }
                drawPath(mt1, color = Color(0xFF2E1A47))
                val mt2 = Path().apply {
                    moveTo(0f, height)
                    lineTo(width * 0.32f, height * 0.55f)
                    lineTo(width * 0.65f, height)
                    close()
                }
                drawPath(mt2, color = Color(0xFF3B1E5C).copy(alpha = 0.85f))
            }
            "historic" -> {
                // Classic Roman Arch silhouette
                drawRect(
                    color = Color(0xFF5E2F0D),
                    topLeft = Offset(width * 0.35f, height * 0.35f),
                    size = Size(width * 0.3f, height * 0.55f)
                )
                drawCircle(
                    color = Color(0xFF78281F), // Arch cutout
                    radius = width * 0.1f,
                    center = Offset(width * 0.5f, height * 0.55f)
                )
                drawRect(
                    color = Color(0xFF78281F),
                    topLeft = Offset(width * 0.4f, height * 0.55f),
                    size = Size(width * 0.2f, height * 0.35f)
                )
            }
            "cafes" -> {
                // Steaming Mug shape with glowing heat rings
                drawCircle(
                    color = Color(0xFFF5CBA7).copy(alpha = 0.15f),
                    radius = height * 0.3f,
                    center = Offset(width * 0.5f, height * 0.5f)
                )
                // Mug body
                drawRoundRect(
                    color = Color(0xFFE59866),
                    topLeft = Offset(width * 0.4f, height * 0.48f),
                    size = Size(width * 0.2f, height * 0.38f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                )
                // Mug Handle
                val handle = Path().apply {
                    moveTo(width * 0.6f, height * 0.54f)
                    quadraticTo(width * 0.68f, height * 0.67f, width * 0.6f, height * 0.8f)
                }
                drawPath(handle, color = Color(0xFFE59866), style = Stroke(width = 8f))
                // Steam
                val steam1 = Path().apply {
                    moveTo(width * 0.45f, height * 0.42f)
                    quadraticTo(width * 0.47f, height * 0.32f, width * 0.45f, height * 0.22f)
                }
                drawPath(steam1, color = Color.White.copy(alpha = 0.6f), style = Stroke(width = 5f))
                val steam2 = Path().apply {
                    moveTo(width * 0.55f, height * 0.42f)
                    quadraticTo(width * 0.57f, height * 0.32f, width * 0.55f, height * 0.22f)
                }
                drawPath(steam2, color = Color.White.copy(alpha = 0.6f), style = Stroke(width = 5f))
            }
            "arts" -> {
                // Modern abstract color splashes (RGB overlapping layout)
                drawCircle(
                    color = Color(0xFFE74C3C).copy(alpha = 0.75f),
                    radius = height * 0.26f,
                    center = Offset(width * 0.42f, height * 0.48f)
                )
                drawCircle(
                    color = Color(0xFF3498DB).copy(alpha = 0.75f),
                    radius = height * 0.24f,
                    center = Offset(width * 0.58f, height * 0.44f)
                )
                drawCircle(
                    color = Color(0xFF2ECC71).copy(alpha = 0.7f),
                    radius = height * 0.22f,
                    center = Offset(width * 0.5f, height * 0.62f)
                )
            }
            else -> {
                // Retro maritime compass card
                drawCircle(
                    color = Color.White.copy(alpha = 0.12f),
                    radius = height * 0.35f,
                    center = Offset(width * 0.5f, height * 0.5f)
                )
                val compass = Path().apply {
                    moveTo(width * 0.5f, height * 0.22f)
                    lineTo(width * 0.54f, height * 0.48f)
                    lineTo(width * 0.78f, height * 0.5f)
                    lineTo(width * 0.54f, height * 0.52f)
                    lineTo(width * 0.5f, height * 0.78f)
                    lineTo(width * 0.46f, height * 0.52f)
                    lineTo(width * 0.22f, height * 0.5f)
                    lineTo(width * 0.46f, height * 0.48f)
                    close()
                }
                drawPath(compass, color = Color(0xFFEBEDEF))
            }
        }
    }
}

// =============================================================================
// 1. AIRBNB-STYLE DISCOVERY SCREEN
// =============================================================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DiscoveryScreen(
    viewModel: GemViewModel,
    modifier: Modifier = Modifier
) {
    val filteredGems by viewModel.filteredGems.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val favoriteGemIds by viewModel.favoriteGemIds.collectAsState()
    val votedGems by viewModel.votedGems.collectAsState()

    val categories = listOf("All", "Scenic", "Parks", "Historic", "Beaches", "Cafes", "Arts")

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        // Upper search and title block
        Text(
            text = "Explore Secrets",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = FontFamily.Serif
            ),
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
        )
        Text(
            text = "Discover quiet, unvisited spots in San Francisco",
            style = MaterialTheme.typography.bodyMedium.copy(color = SophisticatedTextMuted),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Integrated Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            placeholder = { Text("Search by name, category, or description...", color = SophisticatedTextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Emerald500) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.White)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Emerald500,
                unfocusedBorderColor = SophisticatedBorder,
                focusedContainerColor = SophisticatedBgDark,
                unfocusedContainerColor = SophisticatedBgDark
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // Airbnb Floating categories row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val isSelected = cat == selectedCategory
                
                // Fetch relevant icons
                val icon = when (cat.lowercase()) {
                    "scenic" -> Icons.Default.Landscape
                    "parks" -> Icons.Default.Nature
                    "historic" -> Icons.Default.AccountBalance
                    "beaches" -> Icons.Default.Water
                    "cafes" -> Icons.Default.LocalCafe
                    "arts" -> Icons.Default.Palette
                    else -> Icons.Default.Apps
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Emerald500 else SophisticatedSurface)
                        .clickable { viewModel.setCategory(cat) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) SophisticatedBgDark else Emerald500,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) SophisticatedBgDark else Color.White
                            )
                        )
                    }
                }
            }
        }

        // Lazy column displaying lists of spots
        if (filteredGems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = SophisticatedBorder,
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "No Hidden Gems found",
                        style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
                    )
                    Text(
                        text = "Try clearing search filters or switching categories.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = SophisticatedTextMuted),
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = {
                            viewModel.updateSearchQuery("")
                            viewModel.setCategory("All")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500)
                    ) {
                        Text("Reset Filters", color = SophisticatedBgDark)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredGems, key = { it.id }) { gem ->
                    val isFavorite = favoriteGemIds.contains(gem.id)
                    val userVote = votedGems[gem.id]

                    Card(
                        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                        border = BorderStroke(1.dp, SophisticatedBorder),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectGem(gem.id) }
                    ) {
                        Column {
                            // Procedural Canvas Banner based on category
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(170.dp)
                            ) {
                                CategoryIllustrationCanvas(
                                    category = gem.category,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Verified green badge
                                if (gem.isVerified) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(12.dp)
                                            .background(
                                                Color(0xCC111417),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Emerald500,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text(
                                                text = "Verified Business",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            )
                                        }
                                    }
                                }

                                // Category text capsule
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                        .background(
                                            Color(0xCC111417),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = gem.category,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Emerald500,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                            }

                            // Info details
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = gem.title,
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )

                                    // Favorites heart button
                                    IconButton(
                                        onClick = { viewModel.toggleFavorite(gem.id) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = null,
                                            tint = if (isFavorite) Color.Red else Color.White
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = gem.description,
                                    style = MaterialTheme.typography.bodyMedium.copy(color = SophisticatedTextMuted),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Ratings and upvotes/downvotes
                                Divider(color = SophisticatedBorder, modifier = Modifier.padding(vertical = 4.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Thumbs Up/Down Voting Controls
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Thumbs Up Row
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { viewModel.upvoteGem(gem.id) }
                                                .padding(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ThumbUp,
                                                contentDescription = null,
                                                tint = if (userVote == "up") Emerald500 else SophisticatedTextMuted,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "${gem.upvotes}",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (userVote == "up") Emerald500 else Color.White
                                                )
                                            )
                                        }

                                        // Thumbs Down Row
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { viewModel.downvoteGem(gem.id) }
                                                .padding(6.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ThumbDown,
                                                contentDescription = null,
                                                tint = if (userVote == "down") Color(0xFFE74C3C) else SophisticatedTextMuted,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "${gem.downvotes}",
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (userVote == "down") Color(0xFFE74C3C) else Color.White
                                                )
                                            )
                                        }
                                    }

                                    // View detail action
                                    Text(
                                        text = "View Details →",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Emerald500
                                        )
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

// =============================================================================
// 2. LOCATION PROXIMITY SENSOR SCREEN
// =============================================================================
@Composable
fun LocationSensorScreen(
    viewModel: GemViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allGems by viewModel.filteredGems.collectAsState()
    val userLat by viewModel.userLocationLat.collectAsState()
    val userLng by viewModel.userLocationLng.collectAsState()
    val isSensorActive by viewModel.isSensorRunning.collectAsState()

    // Sort spots by Haversine distance
    val sortedGems = remember(allGems, userLat, userLng) {
        allGems.map { gem ->
            val dist = calculateDistanceInMeters(userLat, userLng, gem.latitude, gem.longitude)
            gem to dist
        }.sortedBy { it.second }
    }

    // Sonar animation states
    val infiniteTransition = rememberInfiniteTransition(label = "sonar_transition")
    val sonarAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sonar_sweep"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "sonar_pulse"
    )

    // Pre-defined SF spots for easy location simulation
    val simulatedLocations = listOf(
        SimulatedLoc("Sutro Coastline", 37.7796, -122.5137),
        SimulatedLoc("Castro District", 37.7578, -122.4398),
        SimulatedLoc("Russian Hill Lane", 37.8002, -122.4172),
        SimulatedLoc("Marina Harbor", 37.8085, -122.4367),
        SimulatedLoc("Golden Gate", 37.8155, -122.5295)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "GPS Proximity Sensor",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = FontFamily.Serif
            ),
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
        )
        Text(
            text = "Scan for local hidden sanctuaries nearest you",
            style = MaterialTheme.typography.bodyMedium.copy(color = SophisticatedTextMuted),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Simulated locations selector
        Text(
            text = "Search / Simulate My Location:",
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                color = Emerald500
            ),
            modifier = Modifier.padding(vertical = 4.dp)
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(simulatedLocations) { loc ->
                val isCurrent = Math.abs(loc.lat - userLat) < 0.001 && Math.abs(loc.lng - userLng) < 0.001
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isCurrent) Emerald500 else SophisticatedSurface)
                        .border(
                            1.dp,
                            if (isCurrent) Emerald500 else SophisticatedBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            viewModel.setUserLocation(loc.lat, loc.lng)
                            viewModel.toggleLocationSensor(true)
                            Toast
                                .makeText(
                                    context,
                                    "Location updated: ${loc.name}",
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = loc.name,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrent) SophisticatedBgDark else Color.White
                        )
                    )
                }
            }
        }

        // Radar/Sonar Sweep Widget
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(SophisticatedBgDark)
                .border(1.dp, SophisticatedBorder, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val maxRadius = Math.min(size.width, size.height) * 0.44f

                // Concentric circles
                drawCircle(color = Emerald500.copy(alpha = 0.06f), radius = maxRadius, center = Offset(cx, cy))
                drawCircle(color = Emerald500.copy(alpha = 0.09f), radius = maxRadius * 0.66f, center = Offset(cx, cy))
                drawCircle(color = Emerald500.copy(alpha = 0.12f), radius = maxRadius * 0.33f, center = Offset(cx, cy))

                // Pulsing sweep ring
                if (isSensorActive) {
                    drawCircle(
                        color = Emerald500.copy(alpha = (1.0f - pulseScale) * 0.15f),
                        radius = maxRadius * pulseScale,
                        center = Offset(cx, cy),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }

                // Grid lines (Crosshairs)
                drawLine(
                    color = SophisticatedBorder.copy(alpha = 0.5f),
                    start = Offset(cx - maxRadius, cy),
                    end = Offset(cx + maxRadius, cy),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = SophisticatedBorder.copy(alpha = 0.5f),
                    start = Offset(cx, cy - maxRadius),
                    end = Offset(cx, cy + maxRadius),
                    strokeWidth = 1.dp.toPx()
                )

                // Rotating radar beam line
                if (isSensorActive) {
                    val angleRad = Math.toRadians(sonarAngle.toDouble())
                    val rx = cx + maxRadius * cos(angleRad).toFloat()
                    val ry = cy + maxRadius * sin(angleRad).toFloat()
                    drawLine(
                        brush = Brush.sweepGradient(
                            listOf(Color.Transparent, Emerald500.copy(alpha = 0.7f)),
                            center = Offset(cx, cy)
                        ),
                        start = Offset(cx, cy),
                        end = Offset(rx, ry),
                        strokeWidth = 3.dp.toPx()
                    )
                } else {
                    // Static inactive beam
                    drawLine(
                        color = SophisticatedBorder.copy(alpha = 0.3f),
                        start = Offset(cx, cy),
                        end = Offset(cx + maxRadius * 0.707f, cy - maxRadius * 0.707f),
                        strokeWidth = 2.dp.toPx()
                    )
                }

                // Nearby spots blips
                if (isSensorActive) {
                    sortedGems.take(5).forEachIndexed { i, pair ->
                        val gem = pair.first
                        val dist = pair.second
                        
                        // Map distance to radar radius
                        val normalizedDist = (dist / 15000.0).coerceAtMost(1.0)
                        val angle = (45 + i * 65) % 360
                        val angleRad = Math.toRadians(angle.toDouble())
                        
                        val blipRadius = maxRadius * normalizedDist
                        val bx = cx + blipRadius * cos(angleRad).toFloat()
                        val by = cy + blipRadius * sin(angleRad).toFloat()

                        // Check if the sonar sweep is close to the blip angle to flash it!
                        val diff = Math.abs((sonarAngle - angle + 360) % 360)
                        val flashAlpha = if (diff < 40f) 0.9f else 0.25f

                        drawCircle(
                            color = Emerald500.copy(alpha = flashAlpha * 0.3f),
                            radius = 12.dp.toPx(),
                            center = Offset(bx.toFloat(), by.toFloat())
                        )
                        drawCircle(
                            color = if (gem.isVerified) Violet500 else Emerald500,
                            radius = 5.dp.toPx(),
                            center = Offset(bx.toFloat(), by.toFloat())
                        )
                    }
                }

                // Center node representing user
                drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(cx, cy))
                drawCircle(
                    color = Emerald500,
                    radius = 10.dp.toPx(),
                    center = Offset(cx, cy),
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            // Radar status text overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isSensorActive) Emerald500 else Color.Red)
                    )
                    Text(
                        text = if (isSensorActive) "RADAR ACTIVE (DISCOVERING CLOSEST)" else "RADAR STANDBY",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Proximity list
        Text(
            text = "Spots sorted by distance:",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(sortedGems) { pair ->
                val gem = pair.first
                val distance = pair.second

                Card(
                    colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                    border = BorderStroke(1.dp, SophisticatedBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectGem(gem.id) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                CategoryIllustrationCanvas(
                                    category = gem.category,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Column {
                                Text(
                                    text = gem.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = gem.category,
                                    style = MaterialTheme.typography.bodySmall.copy(color = Emerald500),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Distance visual indicator
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = formatDistance(distance),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "distance away",
                                style = MaterialTheme.typography.bodySmall.copy(color = SophisticatedTextMuted)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper structures for simulation
data class SimulatedLoc(val name: String, val lat: Double, val lng: Double)

fun calculateDistanceInMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371000.0 // Earth's radius in meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return r * c
}

fun formatDistance(meters: Double): String {
    return if (meters < 1000.0) {
        "${meters.toInt()} m"
    } else {
        String.format("%.1f km", meters / 1000.0)
    }
}

// =============================================================================
// 3. FAVORITES GALLERY SCREEN
// =============================================================================
@Composable
fun FavoritesScreen(
    viewModel: GemViewModel,
    modifier: Modifier = Modifier
) {
    val allGems by viewModel.filteredGems.collectAsState()
    val favoriteGemIds by viewModel.favoriteGemIds.collectAsState()

    val favGems = remember(allGems, favoriteGemIds) {
        allGems.filter { favoriteGemIds.contains(it.id) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "My Saved Sanctuaries",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = FontFamily.Serif
            ),
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
        )
        Text(
            text = "Your private portfolio of quiet, peaceful spots",
            style = MaterialTheme.typography.bodyMedium.copy(color = SophisticatedTextMuted),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (favGems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = SophisticatedBorder,
                        modifier = Modifier.size(72.dp)
                    )
                    Text(
                        text = "Your collection is empty",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Heart spots in the Discovery list or on the Map to save them permanently in your custom diary.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = SophisticatedTextMuted),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 100.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(favGems.size) { index ->
                    val gem = favGems[index]

                    Card(
                        colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
                        border = BorderStroke(1.dp, SophisticatedBorder),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectGem(gem.id) }
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(110.dp)
                            ) {
                                CategoryIllustrationCanvas(
                                    category = gem.category,
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Favorite Quick Unsave
                                IconButton(
                                    onClick = { viewModel.toggleFavorite(gem.id) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(32.dp)
                                        .background(
                                            Color.Black.copy(alpha = 0.5f),
                                            CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = Color.Red,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = gem.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    ),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = gem.category,
                                    style = MaterialTheme.typography.bodySmall.copy(color = Emerald500),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// 4. SUPPORT & CRYPTO DONATIONS PORTAL
// =============================================================================
@Composable
fun QRCodeGeneratorView(
    data: String,
    coinColor: Color,
    modifier: Modifier = Modifier,
    sizeDp: androidx.compose.ui.unit.Dp = 200.dp
) {
    val sizePx = with(androidx.compose.ui.platform.LocalDensity.current) { sizeDp.roundToPx() }
    val bitMatrix = remember(data, sizePx) {
        try {
            val writer = com.google.zxing.qrcode.QRCodeWriter()
            writer.encode(data, com.google.zxing.BarcodeFormat.QR_CODE, sizePx, sizePx)
        } catch (e: Exception) {
            null
        }
    }

    Box(
        modifier = modifier
            .size(sizeDp)
            .shadow(16.dp, RoundedCornerShape(24.dp))
            .background(Color.White, RoundedCornerShape(24.dp))
            .border(BorderStroke(3.dp, coinColor), RoundedCornerShape(24.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (bitMatrix != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = bitMatrix.width
                val height = bitMatrix.height
                val cellWidth = size.width / width
                val cellHeight = size.height / height
                for (x in 0 until width) {
                    for (y in 0 until height) {
                        if (bitMatrix.get(x, y)) {
                            drawRect(
                                color = Color(0xFF12151A), // Dark color for QR dots
                                topLeft = Offset(x * cellWidth, y * cellHeight),
                                size = Size(cellWidth, cellHeight)
                            )
                        }
                    }
                }
            }
            
            // Draw a sophisticated center badge representing the cryptocurrency or "GEM"
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(coinColor)
                    .border(BorderStroke(2.dp, Color.White), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "💎",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp)
                )
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = coinColor)
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SupportDonationScreen(
    viewModel: GemViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    // Simulated transaction states
    var isSimulatingTransaction by remember { mutableStateOf(false) }
    var simulationProgress by remember { mutableStateOf(0f) }
    var simulationStepText by remember { mutableStateOf("") }
    var showConfettiReward by remember { mutableStateOf(false) }

    // Selected cryptos
    val cryptos = listOf(
        CryptoCoin("Bitcoin", "BTC", "bc1q8gems972402452088f6a9ec4c8cjungle", Color(0xFFF39C12)),
        CryptoCoin("Ethereum", "ETH", "0x78f6a9ec4c8c465a7f3f86679901e75jungle", Color(0xFF8E44AD)),
        CryptoCoin("Solana", "SOL", "GemsSOL78f6a9ec4c8c465a7f3f86679901e75", Color(0xFF1ABC9C)),
        CryptoCoin("Dogecoin", "DOGE", "Dgems78f6a9ec4c8c465a7f3f86679901e75", Color(0xFFF1C40F))
    )

    // Interactive states
    var selectedCoin by remember { mutableStateOf(cryptos[0]) }
    var donationAmount by remember { mutableStateOf("0.005") }
    var isAddressCopied by remember { mutableStateOf(false) }

    // Calculate dynamic preset amounts based on the coin
    val presetAmounts = when (selectedCoin.symbol) {
        "BTC" -> listOf("0.001", "0.005", "0.01", "0.05")
        "ETH" -> listOf("0.01", "0.05", "0.1", "0.5")
        "SOL" -> listOf("0.5", "1.0", "5.0", "10.0")
        "DOGE" -> listOf("50", "100", "500", "1000")
        else -> listOf("1", "5", "10", "50")
    }

    // Dynamic QR payload encoding standard cryptocurrency URI schemes (BIP-21 etc.)
    val qrData = remember(selectedCoin, donationAmount) {
        val symbolStr = selectedCoin.symbol
        val addressStr = selectedCoin.address
        val cleanAmount = donationAmount.trim().toDoubleOrNull()
        val scheme = when (symbolStr) {
            "BTC" -> "bitcoin"
            "ETH" -> "ethereum"
            "SOL" -> "solana"
            "DOGE" -> "dogecoin"
            else -> "crypto"
        }
        if (cleanAmount != null && cleanAmount > 0) {
            "$scheme:$addressStr?amount=$donationAmount"
        } else {
            "$scheme:$addressStr"
        }
    }

    val copyAddress = {
        clipboard.setText(AnnotatedString(selectedCoin.address))
        isAddressCopied = true
        Toast.makeText(context, "${selectedCoin.name} address copied!", Toast.LENGTH_SHORT).show()
        coroutineScope.launch {
            delay(2000)
            isAddressCopied = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Sanctuary Support",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = FontFamily.Serif
            ),
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
        )
        Text(
            text = "Help us scale server infrastructure and expand our botanical hidden networks. Select a coin below to generate a dynamic donation address.",
            style = MaterialTheme.typography.bodyMedium.copy(color = SophisticatedTextMuted),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // 1. Transaction Simulation Screen Overlay
        if (isSimulatingTransaction) {
            Card(
                colors = CardDefaults.cardColors(containerColor = SophisticatedBgDark),
                border = BorderStroke(1.dp, Emerald500),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "MOCK BLOCKCHAIN SETTLEMENT ENGINE",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Emerald500,
                            letterSpacing = 1.sp
                        )
                    )
                    CircularProgressIndicator(
                        progress = simulationProgress,
                        color = Emerald500,
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(56.dp)
                    )
                    Text(
                        text = simulationStepText,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        textAlign = TextAlign.Center
                    )
                    LinearProgressIndicator(
                        progress = simulationProgress,
                        color = Emerald500,
                        trackColor = SophisticatedBorder,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )
                    Text(
                        text = "This simulates a real ledger broadcast to test the platform's reactive response.",
                        style = MaterialTheme.typography.bodySmall.copy(color = SophisticatedTextMuted),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Confetti Success Card
        AnimatedVisibility(
            visible = showConfettiReward,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F3A20)),
                border = BorderStroke(2.dp, Amber500),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🎉 LEDGER TRANSACTION SETTLED! 🎉",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Amber500,
                            letterSpacing = 2.sp
                        )
                    )
                    Text(
                        text = "Thank you! Your simulated support was verified on the decentralized ledger. Your profile has received the legendary 'Ledger Legend' achievement award badge!",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                        textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { showConfettiReward = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Amber500, contentColor = SophisticatedBgDark)
                    ) {
                        Text("Accept Blessing")
                    }
                }
            }
        }

        // 2. Interactive Wallet Selector Bar (Card grid of supported coins)
        Text(
            text = "Select Cryptocurrency Wallet:",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            cryptos.forEach { coin ->
                val isSelected = selectedCoin.symbol == coin.symbol
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) coin.color.copy(alpha = 0.15f) else SophisticatedSurface
                    ),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) coin.color else SophisticatedBorder
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            selectedCoin = coin
                            donationAmount = when (coin.symbol) {
                                "BTC" -> "0.005"
                                "ETH" -> "0.05"
                                "SOL" -> "1.0"
                                "DOGE" -> "100.0"
                                else -> "1.0"
                            }
                            isAddressCopied = false
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(coin.color.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = coin.symbol,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = coin.color,
                                    fontSize = 11.sp
                                )
                            )
                        }
                        Text(
                            text = coin.name,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else SophisticatedTextMuted,
                                fontSize = 11.sp
                            ),
                            textAlign = TextAlign.Center,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Central Interactive QR Code & Address Display Panel
        Card(
            colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
            border = BorderStroke(1.dp, SophisticatedBorder),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header details within panel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(selectedCoin.color)
                        )
                        Text(
                            text = "${selectedCoin.name} Network",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(selectedCoin.color.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = selectedCoin.symbol,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = selectedCoin.color
                            )
                        )
                    }
                }

                // Dynamically Rendered Real-Time QR Code Generator
                QRCodeGeneratorView(
                    data = qrData,
                    coinColor = selectedCoin.color,
                    sizeDp = 220.dp
                )

                // Subtitle with instructions and selected address text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Interactive QR Code",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        text = "Encodes BIP-21 dynamic URI with set amounts",
                        style = MaterialTheme.typography.bodySmall.copy(color = SophisticatedTextMuted),
                        textAlign = TextAlign.Center
                    )
                }

                // Interactive Address Display Block
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SophisticatedSecondarySurface)
                        .border(BorderStroke(1.dp, SophisticatedBorder), RoundedCornerShape(12.dp))
                        .clickable { copyAddress() }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "DEPOSIT ADDRESS",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = selectedCoin.color,
                                    letterSpacing = 1.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = selectedCoin.address,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.White
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = if (isAddressCopied) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                            contentDescription = "Copy address",
                            tint = if (isAddressCopied) Color.Green else selectedCoin.color,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Dynamic amount input fields and presets
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = donationAmount,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.matches(Regex("""^\d*\.?\d*$"""))) {
                                donationAmount = newValue
                            }
                        },
                        label = { Text("Donation Amount", color = selectedCoin.color) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = selectedCoin.color,
                            unfocusedBorderColor = SophisticatedBorder,
                            focusedLabelColor = selectedCoin.color,
                            unfocusedLabelColor = SophisticatedTextMuted
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            Text(
                                text = selectedCoin.symbol,
                                color = selectedCoin.color,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        }
                    )

                    // Preset Quick-Select Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetAmounts.forEach { amount ->
                            val isSelected = donationAmount == amount
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) selectedCoin.color else SophisticatedSecondarySurface)
                                    .border(
                                        BorderStroke(1.dp, if (isSelected) Color.White else SophisticatedBorder),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { donationAmount = amount }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = amount,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) SophisticatedBgDark else Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Action Buttons: Copy Address & Simulate Ledger Broadcast
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { copyAddress() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAddressCopied) Color(0xFF1B5E20) else SophisticatedSecondarySurface,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (isAddressCopied) Color.Green else SophisticatedBorder),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (isAddressCopied) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                                contentDescription = null,
                                tint = if (isAddressCopied) Color.White else selectedCoin.color,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isAddressCopied) "Copied!" else "Copy Address",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (isSimulatingTransaction) return@Button
                            val finalAmount = donationAmount.trim().toDoubleOrNull() ?: 0.0
                            coroutineScope.launch {
                                isSimulatingTransaction = true
                                showConfettiReward = false
                                
                                simulationStepText = "Connecting to decentralized ${selectedCoin.name} RPC node..."
                                simulationProgress = 0.15f
                                delay(1200)
                                
                                simulationStepText = "Preparing payload: transfer of $finalAmount ${selectedCoin.symbol}..."
                                simulationProgress = 0.35f
                                delay(1200)
                                
                                simulationStepText = "Broadcasting cryptographic transaction hash to peers..."
                                simulationProgress = 0.6f
                                delay(1400)
                                
                                simulationStepText = "Mining block with Proof-of-Work consensus..."
                                simulationProgress = 0.85f
                                delay(1200)
                                
                                simulationStepText = "Ledger verified! Donation successful."
                                simulationProgress = 1.0f
                                delay(800)
                                
                                isSimulatingTransaction = false
                                showConfettiReward = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = selectedCoin.color,
                            contentColor = SophisticatedBgDark
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1.2f),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Simulate Ledger",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}

data class CryptoCoin(val name: String, val symbol: String, val address: String, val color: Color)

// =============================================================================
// 5. EXPLORER PROFILE SCREEN
// =============================================================================
@Composable
fun ProfileScreen(
    viewModel: GemViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allGems by viewModel.filteredGems.collectAsState()
    val favoriteGemIds by viewModel.favoriteGemIds.collectAsState()
    val votedGems by viewModel.votedGems.collectAsState()

    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Explorer Sanctuary",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = FontFamily.Serif
            ),
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
        )
        Text(
            text = "Your reputation and exploration statistics",
            style = MaterialTheme.typography.bodyMedium.copy(color = SophisticatedTextMuted),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Account Header Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
            border = BorderStroke(1.dp, SophisticatedBorder),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Large Avatar
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Emerald500),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = SophisticatedBgDark,
                        modifier = Modifier.size(48.dp)
                    )
                }

                Text(
                    text = currentUser?.username ?: "ExplorerJess",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Text(
                    text = currentUser?.email ?: "lopezjessie891@gmail.com",
                    style = MaterialTheme.typography.bodyMedium.copy(color = SophisticatedTextMuted)
                )

                // Role tag
                Box(
                    modifier = Modifier
                        .background(
                            if (currentUser?.role == "business") Violet500.copy(alpha = 0.2f) else Emerald500.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (currentUser?.role == "business") "BUSINESS REPRESENTATIVE" else "EXPLORER GUIDE",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (currentUser?.role == "business") Violet500 else Emerald500
                        )
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Role Switcher
                Button(
                    onClick = {
                        viewModel.toggleUserRole()
                        Toast
                            .makeText(
                                context,
                                "Role switched successfully!",
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentUser?.role == "business") Emerald500 else Violet500,
                        contentColor = SophisticatedBgDark
                    )
                ) {
                    Text(
                        text = if (currentUser?.role == "business") "Switch to Explorer Workflow" else "Switch to Business Owner Workflow",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Stats summary cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val totalSpots = allGems.size
            val favoriteCount = favoriteGemIds.size
            val upvotedCount = votedGems.filter { it.value == "up" }.size

            StatBlock("Spots", "$totalSpots", Modifier.weight(1f))
            StatBlock("Saved", "$favoriteCount", Modifier.weight(1f))
            StatBlock("Upvotes", "$upvotedCount", Modifier.weight(1f))
        }

        // Achievement Badges Card
        Card(
            colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
            border = BorderStroke(1.dp, SophisticatedBorder),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Achievement Badges",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                // List custom badges
                BadgeRow(
                    badgeName = "First Blip",
                    desc = "Signed into the app successfully.",
                    unlocked = true,
                    icon = Icons.Default.Launch
                )

                BadgeRow(
                    badgeName = "Seward Slider",
                    desc = "Upvote the Seward Street Slides.",
                    unlocked = votedGems.containsKey(2),
                    icon = Icons.Default.ThumbUp
                )

                BadgeRow(
                    badgeName = "Ledger Legend",
                    desc = "Pledge blockchain support to server channels.",
                    unlocked = false, // Managed by donation session memory, will toggle
                    icon = Icons.Default.Savings
                )
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
fun StatBlock(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SophisticatedBgDark),
        border = BorderStroke(1.dp, SophisticatedBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Emerald500
                )
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(color = SophisticatedTextMuted)
            )
        }
    }
}

@Composable
fun BadgeRow(badgeName: String, desc: String, unlocked: Boolean, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (unlocked) Emerald500 else SophisticatedBorder),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (unlocked) SophisticatedBgDark else SophisticatedTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = badgeName,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (unlocked) Color.White else SophisticatedTextMuted
                )
            )
            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall.copy(color = SophisticatedTextMuted)
            )
        }

        if (unlocked) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Emerald500,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// =============================================================================
// 6. SETTINGS SCREEN
// =============================================================================
@Composable
fun SettingsScreen(
    viewModel: GemViewModel,
    modifier: Modifier = Modifier
) {
    val radiusLimit by viewModel.radiusLimitInMeters.collectAsState()
    val verifiedFilter by viewModel.verifiedFilter.collectAsState()
    val minRatingFilter by viewModel.minRatingFilter.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontFamily = FontFamily.Serif
            ),
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
        )
        Text(
            text = "Control spatial radiuses and system preferences",
            style = MaterialTheme.typography.bodyMedium.copy(color = SophisticatedTextMuted),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Radius constraint card
        Card(
            colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
            border = BorderStroke(1.dp, SophisticatedBorder),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Proximity Search Boundary",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
                Text(
                    text = "Current boundary: ${radiusLimit.toInt() / 1000} km",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Emerald500, fontWeight = FontWeight.Bold)
                )
                Slider(
                    value = radiusLimit.toFloat(),
                    onValueChange = { viewModel.updateRadiusLimit(it.toDouble()) },
                    valueRange = 1000f..30000f,
                    colors = SliderDefaults.colors(
                        thumbColor = Emerald500,
                        activeTrackColor = Emerald500,
                        inactiveTrackColor = SophisticatedBorder
                    )
                )
                Text(
                    text = "Gems outside this geographic boundary will be excluded from the feeds and sensor radar sweeps.",
                    style = MaterialTheme.typography.bodySmall.copy(color = SophisticatedTextMuted)
                )
            }
        }

        // Filters card
        Card(
            colors = CardDefaults.cardColors(containerColor = SophisticatedSurface),
            border = BorderStroke(1.dp, SophisticatedBorder),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Content Verification",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Show only Verified Businesses",
                        style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                    )
                    Switch(
                        checked = verifiedFilter == true,
                        onCheckedChange = { viewModel.updateVerifiedFilter(if (it) true else null) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Emerald500,
                            checkedTrackColor = Emerald500.copy(alpha = 0.5f),
                            uncheckedThumbColor = SophisticatedTextMuted,
                            uncheckedTrackColor = SophisticatedBorder
                        )
                    )
                }

                Divider(color = SophisticatedBorder)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Minimum Stars Required",
                        style = MaterialTheme.typography.bodyLarge.copy(color = Color.White)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf(0, 3, 4, 5).forEach { stars ->
                            val isSelected = stars == minRatingFilter
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(if (isSelected) Emerald500 else SophisticatedBorder)
                                    .clickable { viewModel.updateMinRatingFilter(stars) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (stars == 0) "Any" else "$stars★",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) SophisticatedBgDark else Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}
