package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.GemActivity
import com.example.data.GemReview
import com.example.data.HiddenGem
import com.example.ui.theme.*
import kotlin.math.sqrt

// Standalone types for geographic clustering
sealed class ClusterItem {
    data class Single(val gem: HiddenGem) : ClusterItem()
    data class Cluster(val id: Int, val latitude: Double, val longitude: Double, val items: List<HiddenGem>) : ClusterItem()
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MainScreen(viewModel: GemViewModel = viewModel()) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val filteredGems by viewModel.filteredGems.collectAsState()
    val selectedGemId by viewModel.selectedGemId.collectAsState()
    val selectedGem by viewModel.selectedGem.collectAsState()
    val selectedGemActivities by viewModel.selectedGemActivities.collectAsState()
    val selectedGemReviews by viewModel.selectedGemReviews.collectAsState()

    // Map gesture states
    val centerLat by viewModel.mapCenterLat.collectAsState()
    val centerLng by viewModel.mapCenterLng.collectAsState()
    var zoom by remember { mutableStateOf(1.0f) }

    // Drop Pin flow states
    val isDroppingPin by viewModel.isDroppingPin.collectAsState()
    val droppedLat by viewModel.droppedLat.collectAsState()
    val droppedLng by viewModel.droppedLng.collectAsState()
    val droppedCity by viewModel.droppedCity.collectAsState()
    val isGeocoding by viewModel.isGeocoding.collectAsState()
    val aiDescription by viewModel.aiDescription.collectAsState()
    val isGeneratingAI by viewModel.isGeneratingAI.collectAsState()

    // Search and Radius
    val searchQuery by viewModel.searchQuery.collectAsState()
    val radiusLimit by viewModel.radiusLimitInMeters.collectAsState()
    val verifiedFilter by viewModel.verifiedFilter.collectAsState()

    // Form inputs
    var newSpotTitle by remember { mutableStateOf("") }
    var newSpotDescription by remember { mutableStateOf("") }

    var reviewRating by remember { mutableStateOf(5) }
    var reviewCrowdDensity by remember { mutableStateOf(2) } // 1=Empty, 2=Mod, 3=Crowded
    var reviewComment by remember { mutableStateOf("") }

    var activityName by remember { mutableStateOf("") }
    var activityDesc by remember { mutableStateOf("") }
    var activitySchedule by remember { mutableStateOf("") }
    var activityPrice by remember { mutableStateOf(2) }

    var replyText by remember { mutableStateOf("") }
    var replyingToReviewId by remember { mutableStateOf<Int?>(null) }

    // Panel sheet visible flags
    var isWritingReview by remember { mutableStateOf(false) }
    var isAddingActivity by remember { mutableStateOf(false) }

    // Project geographic coordinates onto screen pixels
    val baseScale = 55000f
    val scaleFactor = baseScale * zoom

    val activeUser = currentUser
    val showLandingPage by viewModel.showLandingPage.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Gray950)
    ) {
        if (showLandingPage) {
            LandingScreen(viewModel = viewModel)
        } else if (activeUser == null) {
            AuthScreen(viewModel = viewModel)
        } else {
            // Local state to track the active bottom tab
            var activeTab by remember { mutableStateOf("Home") }
            val appMode by viewModel.appMode.collectAsState()

            // -------------------------------------------------------------
            // SCREEN CONTENT SWITCHER (BASED ON ACTIVE TAB & APP MODE)
            // -------------------------------------------------------------
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                when (activeTab) {
                    "Home" -> {
                        when (appMode) {
                            "Map" -> {
                                // -------------------------------------------------------------
                                // 1. DYNAMIC VECTOR GEOGRAPHIC MAP INTERFACE
                                // -------------------------------------------------------------
                                BoxWithConstraints(
                                    modifier = Modifier.fillMaxSize()
                                ) {
            val width = constraints.maxWidth.toFloat()
            val height = constraints.maxHeight.toFloat()

            // Coastline & parks map drawn dynamically via Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(centerLat, centerLng, zoom) {
                        // Drag to Pan
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val dLng = -dragAmount.x / scaleFactor
                            val dLat = dragAmount.y / (scaleFactor * 1.35f)
                            viewModel.setMapCenter(centerLat + dLat, centerLng + dLng)
                        }
                    }
                    .pointerInput(filteredGems, centerLat, centerLng, zoom) {
                        // Tap to select, Long-press to Drop Pin
                        detectTapGestures(
                            onTap = { offset ->
                                // Project screen offset to Lat/Lng to find clicked pin
                                val clusteredItems = clusterGems(filteredGems, centerLat, centerLng, scaleFactor, width, height)
                                val clicked = findClickedItem(offset, clusteredItems, centerLat, centerLng, scaleFactor, width, height)
                                when (clicked) {
                                    is ClusterItem.Single -> {
                                        viewModel.selectGem(clicked.gem.id)
                                        isWritingReview = false
                                        isAddingActivity = false
                                        replyingToReviewId = null
                                    }
                                    is ClusterItem.Cluster -> {
                                        // Zoom in on cluster
                                        viewModel.setMapCenter(clicked.latitude, clicked.longitude)
                                        zoom = (zoom * 1.5f).coerceAtMost(4.5f)
                                    }
                                    null -> {
                                        viewModel.selectGem(null)
                                        isWritingReview = false
                                        isAddingActivity = false
                                        replyingToReviewId = null
                                    }
                                }
                            },
                            onLongPress = { offset ->
                                val clickedLng = centerLng + (offset.x - width / 2f) / scaleFactor
                                val clickedLat = centerLat - (offset.y - height / 2f) / (scaleFactor * 1.35f)
                                viewModel.startPinDrop(clickedLat, clickedLng)
                                newSpotTitle = ""
                                newSpotDescription = ""
                            }
                        )
                    }
            ) {
                // Background water - SophisticatedBgDark (#121416)
                drawRect(color = SophisticatedBgDark)

                // Draw a sophisticated grid overlay from the design theme
                val gridSize = 80f
                for (x in 0..width.toInt() step gridSize.toInt()) {
                    drawLine(
                        color = SophisticatedBorder.copy(alpha = 0.15f),
                        start = Offset(x.toFloat(), 0f),
                        end = Offset(x.toFloat(), height),
                        strokeWidth = 1f
                    )
                }
                for (y in 0..height.toInt() step gridSize.toInt()) {
                    drawLine(
                        color = SophisticatedBorder.copy(alpha = 0.15f),
                        start = Offset(0f, y.toFloat()),
                        end = Offset(width, y.toFloat()),
                        strokeWidth = 1f
                    )
                }

                // Coastline polygon definition
                val landPath = Path()
                val landCoords = listOf(
                    37.70 to -122.52,
                    37.73 to -122.51,
                    37.77 to -122.515, // Land's End
                    37.80 to -122.48, // Presidio
                    37.812 to -122.472, // Fort Point
                    37.808 to -122.44, // Marina
                    37.806 to -122.41, // Pier 39
                    37.795 to -122.39, // Ferry Bldg
                    37.76 to -122.38, // Potrero
                    37.71 to -122.39, // Hunters Point
                    37.70 to -122.40,
                    37.70 to -122.52
                )

                // Render Land peninsula - SophisticatedBgBase (#1A1C1E)
                val fX = width / 2f + ((landCoords[0].second - centerLng) * scaleFactor).toFloat()
                val fY = height / 2f - ((landCoords[0].first - centerLat) * scaleFactor * 1.35f).toFloat()
                landPath.moveTo(fX, fY)
                for (i in 1 until landCoords.size) {
                    val px = width / 2f + ((landCoords[i].second - centerLng) * scaleFactor).toFloat()
                    val py = height / 2f - ((landCoords[i].first - centerLat) * scaleFactor * 1.35f).toFloat()
                    landPath.lineTo(px, py)
                }
                landPath.close()
                drawPath(path = landPath, color = SophisticatedBgBase)

                // Render Golden Gate Park (sage green rectangle, desaturated dark green)
                val ggpPath = Path()
                val ggpCoords = listOf(
                    37.773 to -122.51,
                    37.773 to -122.458,
                    37.765 to -122.458,
                    37.765 to -122.51
                )
                ggpPath.moveTo(width / 2f + ((ggpCoords[0].second - centerLng) * scaleFactor).toFloat(), height / 2f - ((ggpCoords[0].first - centerLat) * scaleFactor * 1.35f).toFloat())
                for (i in 1..3) {
                    ggpPath.lineTo(width / 2f + ((ggpCoords[i].second - centerLng) * scaleFactor).toFloat(), height / 2f - ((ggpCoords[i].first - centerLat) * scaleFactor * 1.35f).toFloat())
                }
                ggpPath.close()
                drawPath(path = ggpPath, color = Color(0xFF1E2721))

                // Render Presidio National Park Forest
                val presPath = Path()
                val presCoords = listOf(
                    37.810 to -122.481,
                    37.804 to -122.448,
                    37.791 to -122.451,
                    37.788 to -122.478
                )
                presPath.moveTo(width / 2f + ((presCoords[0].second - centerLng) * scaleFactor).toFloat(), height / 2f - ((presCoords[0].first - centerLat) * scaleFactor * 1.35f).toFloat())
                for (i in 1..3) {
                    presPath.lineTo(width / 2f + ((presCoords[i].second - centerLng) * scaleFactor).toFloat(), height / 2f - ((presCoords[i].first - centerLat) * scaleFactor * 1.35f).toFloat())
                }
                presPath.close()
                drawPath(path = presPath, color = Color(0xFF1E2721))

                // Render primary roads/arteries for visual orientation
                // Geary Blvd
                drawLine(
                    color = SophisticatedBorder.copy(alpha = 0.4f),
                    start = Offset(width / 2f + ((-122.51 - centerLng) * scaleFactor).toFloat(), height / 2f - ((37.781 - centerLat) * scaleFactor * 1.35f).toFloat()),
                    end = Offset(width / 2f + ((-122.40 - centerLng) * scaleFactor).toFloat(), height / 2f - ((37.781 - centerLat) * scaleFactor * 1.35f).toFloat()),
                    strokeWidth = 3f
                )
                // Market Street
                drawLine(
                    color = SophisticatedBorder.copy(alpha = 0.4f),
                    start = Offset(width / 2f + ((-122.445 - centerLng) * scaleFactor).toFloat(), height / 2f - ((37.752 - centerLat) * scaleFactor * 1.35f).toFloat()),
                    end = Offset(width / 2f + ((-122.392 - centerLng) * scaleFactor).toFloat(), height / 2f - ((37.793 - centerLat) * scaleFactor * 1.35f).toFloat()),
                    strokeWidth = 4f
                )

                // Golden Gate Bridge - drawn in primary theme color (Lavender)
                drawLine(
                    color = SophisticatedPrimary,
                    start = Offset(width / 2f + ((-122.478 - centerLng) * scaleFactor).toFloat(), height / 2f - ((37.811 - centerLat) * scaleFactor * 1.35f).toFloat()),
                    end = Offset(width / 2f + ((-122.483 - centerLng) * scaleFactor).toFloat(), height / 2f - ((37.838 - centerLat) * scaleFactor * 1.35f).toFloat()),
                    strokeWidth = 5f
                )

                // 2. ST_DWithin spatial radius ring visual overlay using SophisticatedSecondary (Sage Green)
                val circleRadiusPx = (radiusLimit / (6371000.0 * 2.0 * Math.PI) * 360.0 * scaleFactor).toFloat()
                drawCircle(
                    color = SophisticatedSecondary.copy(alpha = 0.08f), // semi-transparent glow
                    radius = circleRadiusPx,
                    center = Offset(width / 2f, height / 2f),
                )
                drawCircle(
                    color = SophisticatedSecondary.copy(alpha = 0.35f), // solid outline
                    radius = circleRadiusPx,
                    center = Offset(width / 2f, height / 2f),
                    style = Stroke(width = 3f)
                )
            }

            // -------------------------------------------------------------
            // Marker Rendering Layer (using absolute Compose Offset positioning)
            // -------------------------------------------------------------
            val clusteredGems = clusterGems(filteredGems, centerLat, centerLng, scaleFactor, width, height)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RectangleShape)
            ) {
                clusteredGems.forEach { item ->
                    val (lat, lng) = when (item) {
                        is ClusterItem.Single -> item.gem.latitude to item.gem.longitude
                        is ClusterItem.Cluster -> item.latitude to item.longitude
                    }

                    val px = width / 2f + ((lng - centerLng) * scaleFactor).toFloat()
                    val py = height / 2f - ((lat - centerLat) * scaleFactor * 1.35f).toFloat()

                    // Only draw inside screen bounds
                    if (px >= 0 && px <= width && py >= 0 && py <= height) {
                        when (item) {
                            is ClusterItem.Single -> {
                                val gem = item.gem
                                val isSelected = gem.id == selectedGemId
                                Box(
                                    modifier = Modifier
                                        .offset {
                                            IntOffset(
                                                (px - 24.dp.toPx()).toInt(),
                                                (py - 48.dp.toPx()).toInt()
                                            )
                                        }
                                        .size(48.dp, 48.dp)
                                        .testTag("gem_marker_${gem.id}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Glow or Pulse Ring for selected pins
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .drawBehind {
                                                    drawCircle(
                                                        color = if (gem.isVerified) Color(0x668B5CF6) else Color(0x6610B981),
                                                        radius = size.minDimension / 1.5f
                                                    )
                                                }
                                        )
                                    }

                                    // Marker shape
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = gem.title,
                                        tint = if (gem.isVerified) Violet500 else Emerald500,
                                        modifier = Modifier.size(if (isSelected) 42.dp else 34.dp)
                                    )

                                    // Inner Icon
                                    Icon(
                                        imageVector = if (gem.isVerified) Icons.Default.Verified else Icons.Default.Explore,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier
                                            .padding(bottom = 12.dp)
                                            .size(if (isSelected) 14.dp else 11.dp)
                                    )
                                }
                            }
                            is ClusterItem.Cluster -> {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Amber600),
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .offset {
                                            IntOffset(
                                                (px - 22.dp.toPx()).toInt(),
                                                (py - 22.dp.toPx()).toInt()
                                            )
                                        }
                                        .size(44.dp)
                                        .shadow(6.dp, CircleShape)
                                        .clickable {
                                            // Click zooms in
                                            viewModel.setMapCenter(item.latitude, item.longitude)
                                            zoom = (zoom * 1.5f).coerceAtMost(4.5f)
                                        }
                                        .testTag("cluster_marker_${item.id}"),
                                    border = BorderStroke(2.dp, Color(0xFF0F172A))
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item.items.size.toString(),
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Dropped orange pin visual indicator
                if (isDroppingPin) {
                    val px = width / 2f + ((droppedLng - centerLng) * scaleFactor).toFloat()
                    val py = height / 2f - ((droppedLat - centerLat) * scaleFactor * 1.35f).toFloat()

                    if (px >= 0 && px <= width && py >= 0 && py <= height) {
                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        (px - 26.dp.toPx()).toInt(),
                                        (py - 52.dp.toPx()).toInt()
                                    )
                                }
                                .size(52.dp, 52.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Pulsating red/orange rings
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .drawBehind {
                                        drawCircle(
                                            color = Color(0x66F97316),
                                            radius = size.minDimension / 1.3f
                                        )
                                    }
                            )

                            Icon(
                                imageVector = Icons.Default.PinDrop,
                                contentDescription = "Dropped Pin Location",
                                tint = Color(0xFFF97316),
                                modifier = Modifier.size(42.dp)
                            )
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // 2. FLOATING CONTROL OVERLAYS & SEARCH
        // -------------------------------------------------------------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Controls: Profile Toggle Bar & Search
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Floating Profile Selector bar (The Portal switch)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.92f)),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Slate700.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (activeUser.role == "explorer") Emerald500 else Violet500,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (activeUser.role == "explorer") Icons.Default.Explore else Icons.Default.Store,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = activeUser.username,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = if (activeUser.role == "explorer") "Standard Explorer" else "Business Portal",
                                        color = if (activeUser.role == "explorer") Emerald500 else Violet500,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "•",
                                        color = Color.Gray,
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = "Passkey 🔑",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .clickable { viewModel.registerPasskey(context, activeUser.username) }
                                            .testTag("register_passkey_btn")
                                    )
                                }
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Button(
                                onClick = { viewModel.toggleUserRole() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (activeUser.role == "explorer") Violet500 else Emerald500
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("portal_toggle_button")
                            ) {
                                Text(
                                    text = if (activeUser.role == "explorer") "Go Business" else "Go Explorer",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            IconButton(
                                onClick = { viewModel.signOut() },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
                                    .testTag("sign_out_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = "Sign Out",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Search Bar Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Gray900.copy(alpha = 0.94f)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Slate800),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        TextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            placeholder = { Text("Search off-beaten locations...", color = Color.Gray, fontSize = 14.sp) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("search_input"),
                            singleLine = true
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray)
                            }
                        }
                    }
                }

                // Filter Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = verifiedFilter == null,
                        onClick = { viewModel.updateVerifiedFilter(null) },
                        label = { Text("All Spots", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Slate700,
                            selectedLabelColor = Color.White,
                            containerColor = Slate900.copy(alpha = 0.8f),
                            labelColor = Color.Gray
                        ),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = verifiedFilter == null, borderColor = Slate800)
                    )
                    FilterChip(
                        selected = verifiedFilter == true,
                        onClick = { viewModel.updateVerifiedFilter(true) },
                        label = { Text("Verified (Business)", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Verified, null, modifier = Modifier.size(12.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Violet700,
                            selectedLabelColor = Color.White,
                            containerColor = Slate900.copy(alpha = 0.8f),
                            labelColor = Color.Gray
                        ),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = verifiedFilter == true, borderColor = Slate800)
                    )
                    FilterChip(
                        selected = verifiedFilter == false,
                        onClick = { viewModel.updateVerifiedFilter(false) },
                        label = { Text("Explorer Spots", fontSize = 11.sp) },
                        leadingIcon = { Icon(Icons.Default.Explore, null, modifier = Modifier.size(12.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Emerald700,
                            selectedLabelColor = Color.White,
                            containerColor = Slate900.copy(alpha = 0.8f),
                            labelColor = Color.Gray
                        ),
                        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = verifiedFilter == false, borderColor = Slate800)
                    )
                }
            }

            // Bottom Controls: Map Zoom + Radius Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Search radius slider (ST_DWithin visualizer)
                Card(
                    colors = CardDefaults.cardColors(containerColor = Slate900.copy(alpha = 0.94f)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Slate800),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                        .shadow(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Search Radius (ST_DWithin)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = "${(radiusLimit / 1000).toInt()} km",
                                color = Emerald500,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        Slider(
                            value = radiusLimit.toFloat(),
                            onValueChange = { viewModel.updateRadiusLimit(it.toDouble()) },
                            valueRange = 1000f..20000f,
                            colors = SliderDefaults.colors(
                                thumbColor = Emerald500,
                                activeTrackColor = Emerald500,
                                inactiveTrackColor = Slate800
                            ),
                            modifier = Modifier.height(24.dp)
                        )
                        Text(
                            text = "Limits spots matching GIS radius search",
                            color = Color.Gray,
                            fontSize = 9.sp
                        )
                    }
                }

                // Vertical Zoom Buttons
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Recenter on default SF Center
                    IconButton(
                        onClick = {
                            viewModel.setMapCenter(37.7850, -122.4600)
                            zoom = 1.0f
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Slate900, CircleShape)
                            .border(1.dp, Slate700, CircleShape)
                    ) {
                        Icon(Icons.Default.MyLocation, "Recenter Map", tint = Color.White, modifier = Modifier.size(18.dp))
                    }

                    // Zoom In
                    IconButton(
                        onClick = { zoom = (zoom * 1.3f).coerceAtMost(4.5f) },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Slate900, CircleShape)
                            .border(1.dp, Slate700, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, "Zoom In", tint = Color.White, modifier = Modifier.size(20.dp))
                    }

                    // Zoom Out
                    IconButton(
                        onClick = { zoom = (zoom / 1.3f).coerceAtLeast(0.4f) },
                        modifier = Modifier
                            .size(44.dp)
                            .background(Slate900, CircleShape)
                            .border(1.dp, Slate700, CircleShape)
                    ) {
                        Icon(Icons.Default.Remove, "Zoom Out", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // 3. WORKFLOW A: "DROP A PIN" BOTTOM FORM OVERLAY
        // -------------------------------------------------------------
        AnimatedVisibility(
            visible = isDroppingPin,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Gray900),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                border = BorderStroke(1.dp, Slate700),
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .shadow(16.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header Drag Bar
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .size(40.dp, 4.dp)
                            .background(Slate700, CircleShape)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.PinDrop, "Drop Spot", tint = Color(0xFFF97316), modifier = Modifier.size(24.dp))
                            Text(
                                text = "Crowdsource a Spot",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                fontSize = 20.sp
                            )
                        }
                        IconButton(onClick = { viewModel.cancelPinDrop() }) {
                            Icon(Icons.Default.Close, "Cancel", tint = Color.Gray)
                        }
                    }

                    // Geographic Coordinate metadata & reverse geocoding indicator
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Slate900),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Location Coordinates", color = Color.Gray, fontSize = 11.sp)
                                Text(
                                    text = String.format("%.5f, %.5f", droppedLat, droppedLng),
                                    color = Color.LightGray,
                                    fontSize = 12.sp,
                                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Resolved City/Zone", color = Color.Gray, fontSize = 11.sp)
                                if (isGeocoding) {
                                    CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.dp)
                                } else {
                                    Text(
                                        text = droppedCity,
                                        color = Emerald500,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    // Form Fields
                    OutlinedTextField(
                        value = newSpotTitle,
                        onValueChange = { newSpotTitle = it },
                        label = { Text("Spot Title", color = Color.Gray) },
                        placeholder = { Text("e.g. Whispering Eucalyptus Grove", color = Color.DarkGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Emerald500,
                            unfocusedBorderColor = Slate700
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("new_gem_title_input"),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = newSpotDescription,
                        onValueChange = { newSpotDescription = it },
                        label = { Text("Review Notes / Description", color = Color.Gray) },
                        placeholder = { Text("What makes this location off-the-beaten-path? Any hidden trails or best times to visit?", color = Color.DarkGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = Emerald500,
                            unfocusedBorderColor = Slate700
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("new_gem_desc_input"),
                        maxLines = 4
                    )

                    // Gemini AI Assistant Backstory generator
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1221)),
                        border = BorderStroke(1.dp, Violet500.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.AutoAwesome, "AI", tint = Violet500, modifier = Modifier.size(16.dp))
                                    Text(
                                        text = "Gemini Lore Assistant",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                                if (isGeneratingAI) {
                                    CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 1.5.dp, color = Violet500)
                                } else {
                                    Text(
                                        text = "Active",
                                        color = Violet500,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = aiDescription.ifEmpty { "Drop pin anywhere on map to auto-write a fascinating description of this sector with Gemini AI." },
                                color = if (aiDescription.isNotEmpty()) Color.LightGray else Color.Gray,
                                fontSize = 11.sp,
                                fontStyle = if (aiDescription.isEmpty()) androidx.compose.ui.text.font.FontStyle.Italic else androidx.compose.ui.text.font.FontStyle.Normal,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (aiDescription.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                TextButton(
                                    onClick = { newSpotDescription = aiDescription },
                                    colors = ButtonDefaults.textButtonColors(contentColor = Violet500),
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Apply AI Generated Lore to Form", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.cancelPinDrop() },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                            border = BorderStroke(1.dp, Slate700),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Discard")
                        }
                        Button(
                            onClick = {
                                if (newSpotTitle.isNotBlank()) {
                                    viewModel.addGem(newSpotTitle, newSpotDescription)
                                }
                            },
                            enabled = newSpotTitle.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Emerald500,
                                disabledContainerColor = Slate800
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("save_gem_button")
                        ) {
                            Text("Save Location", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

                                // Close Map sub-mode
                            }
                            "Discovery" -> {
                                DiscoveryScreen(viewModel = viewModel)
                            }
                            "LocationSensor" -> {
                                LocationSensorScreen(viewModel = viewModel)
                            }
                        }
                    }
                    "Favorites" -> {
                        FavoritesScreen(viewModel = viewModel)
                    }
                    "Support" -> {
                        SupportDonationScreen(viewModel = viewModel)
                    }
                    "Profile" -> {
                        ProfileScreen(viewModel = viewModel)
                    }
                    "Settings" -> {
                        SettingsScreen(viewModel = viewModel)
                    }
                }

                // -------------------------------------------------------------
                // FLOATING SUB-MODE TABS FOR HOME (Airbnb style)
                // -------------------------------------------------------------
                if (activeTab == "Home" && selectedGemId == null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 96.dp)
                            .shadow(8.dp, RoundedCornerShape(20.dp))
                            .background(Color(0xDD12151A), RoundedCornerShape(20.dp))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)), RoundedCornerShape(20.dp))
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf("Discovery", "Map", "LocationSensor").forEach { mode ->
                                val isSelected = appMode == mode
                                val label = when (mode) {
                                    "Discovery" -> "Discovery Mode"
                                    "Map" -> "Vector Map"
                                    "LocationSensor" -> "Proximity Radar"
                                    else -> mode
                                }
                                val icon = when (mode) {
                                    "Discovery" -> Icons.Default.Explore
                                    "Map" -> Icons.Default.Map
                                    "LocationSensor" -> Icons.Default.MyLocation
                                    else -> Icons.Default.Explore
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (isSelected) Emerald500 else Color.Transparent)
                                        .clickable { viewModel.setAppMode(mode) }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
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
                                            text = label,
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

                // -------------------------------------------------------------
                // FLOATING GLASSMORPHIC BOTTOM BAR
                // -------------------------------------------------------------
                if (selectedGemId == null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 20.dp, start = 16.dp, end = 16.dp)
                            .fillMaxWidth()
                            .height(64.dp)
                            .shadow(12.dp, RoundedCornerShape(32.dp))
                            .background(
                                Color(0xDD12151A), // Translucent dark
                                RoundedCornerShape(32.dp)
                            )
                            .border(
                                BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                                RoundedCornerShape(32.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Settings (far left)
                            IconButton(onClick = { activeTab = "Settings" }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = if (activeTab == "Settings") Emerald500 else Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // 2. Favorites
                            IconButton(onClick = { activeTab = "Favorites" }) {
                                Icon(
                                    imageVector = if (activeTab == "Favorites") Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorites",
                                    tint = if (activeTab == "Favorites") Color.Red else Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // 3. Home (Middle prominent floating action)
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(if (activeTab == "Home") Emerald500 else SophisticatedSecondarySurface)
                                    .clickable { activeTab = "Home" },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Home",
                                    tint = if (activeTab == "Home") SophisticatedBgDark else Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            // 4. Support / Donation
                            IconButton(onClick = { activeTab = "Support" }) {
                                Icon(
                                    imageVector = Icons.Default.VolunteerActivism,
                                    contentDescription = "Support Donations",
                                    tint = if (activeTab == "Support") Amber500 else Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // 5. Profile (far right)
                            IconButton(onClick = { activeTab = "Profile" }) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Profile",
                                    tint = if (activeTab == "Profile") Emerald500 else Color.White.copy(alpha = 0.5f),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }

        // -------------------------------------------------------------
        // 4. WORKFLOW B: BUSINESS ENGAGEMENT PANEL / DETAILED SPOT VIEW
        // -------------------------------------------------------------
        AnimatedVisibility(
            visible = selectedGemId != null && !isDroppingPin,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            selectedGem?.let { gem ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Gray900),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    border = BorderStroke(1.dp, Slate700),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.65f)
                        .shadow(16.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        // Drag Handle & Top controls
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(36.dp, 4.dp)
                                    .background(Slate700, CircleShape)
                            )
                            IconButton(
                                onClick = { viewModel.selectGem(null) },
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 12.dp)
                            ) {
                                Icon(Icons.Default.Close, "Close Detail", tint = Color.Gray)
                            }
                        }

                        // Scrollable content body
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Spot Title & Location
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = gem.title,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Serif,
                                            fontSize = 24.sp,
                                            modifier = Modifier.testTag("selected_gem_title")
                                        )
                                        if (gem.isVerified) {
                                            Icon(
                                                imageVector = Icons.Default.Verified,
                                                contentDescription = "Verified Local Business",
                                                tint = Violet500,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (gem.isVerified) "Verified Business Experience Spot" else "Explorer Crowdsourced Spot",
                                        color = if (gem.isVerified) Violet500 else Emerald500,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = gem.description,
                                        color = Color.LightGray,
                                        fontSize = 14.sp,
                                        lineHeight = 20.sp
                                    )
                                }
                            }

                            // CLAIM MODULE FOR UNVERIFIED SPOTS
                            if (!gem.isVerified && activeUser.role == "business") {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Violet700.copy(alpha = 0.15f)),
                                        border = BorderStroke(1.dp, Violet500.copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = "Own this Local Gem?",
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = "Claim and verify this location to add experiences, schedules, and reply to reviews.",
                                                    color = Color.LightGray,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Button(
                                                onClick = { viewModel.claimSpot(gem.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = Violet500),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.padding(start = 10.dp)
                                            ) {
                                                Text("Claim Spot", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }

                            // Rating summary & Crowd Level Indexes
                            item {
                                val averageRating = if (selectedGemReviews.isEmpty()) 0.0 else selectedGemReviews.map { it.rating }.average()
                                val avgCrowd = if (selectedGemReviews.isEmpty()) 0 else selectedGemReviews.map { it.crowdDensity }.average().toInt()

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Slate900),
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(text = "Rating", color = Color.Gray, fontSize = 11.sp)
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Icon(Icons.Default.Star, null, tint = Amber500, modifier = Modifier.size(16.dp))
                                                Text(
                                                    text = if (averageRating == 0.0) "N/A" else String.format("%.1f / 5", averageRating),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                            }
                                        }
                                    }

                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Slate900),
                                        modifier = Modifier.weight(1.2f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(text = "Crowd Density", color = Color.Gray, fontSize = 11.sp)
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                val (crowdText, crowdColor) = when (avgCrowd) {
                                                    1 -> "Quiet / Peaceful" to Emerald500
                                                    2 -> "Moderate" to Amber500
                                                    3 -> "Crowded / Busy" to Color.Red
                                                    else -> "Unknown" to Color.Gray
                                                }
                                                Icon(Icons.Default.Groups, null, tint = crowdColor, modifier = Modifier.size(16.dp))
                                                Text(
                                                    text = crowdText,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // ACTIVE EXPERIENCES & SCHEDULES LISTING (Verified Business Spots)
                            if (gem.isVerified) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Icon(Icons.Default.LocalActivity, null, tint = Violet500, modifier = Modifier.size(18.dp))
                                            Text(
                                                text = "Business Experiences",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Serif,
                                                fontSize = 16.sp
                                            )
                                        }

                                        // Only actual owner (Uploader business user) can add activities
                                        if (activeUser.role == "business" && gem.uploaderId == activeUser.id) {
                                            TextButton(
                                                onClick = {
                                                    isAddingActivity = !isAddingActivity
                                                    activityName = ""
                                                    activityDesc = ""
                                                    activitySchedule = ""
                                                    activityPrice = 2
                                                },
                                                colors = ButtonDefaults.textButtonColors(contentColor = Violet500)
                                            ) {
                                                Icon(
                                                    imageVector = if (isAddingActivity) Icons.Default.RemoveCircleOutline else Icons.Default.AddCircleOutline,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(if (isAddingActivity) "Close" else "Add Experience", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                // Interactive Create Activity Form
                                if (isAddingActivity) {
                                    item {
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = CardGray),
                                            border = BorderStroke(1.dp, Violet500.copy(alpha = 0.4f)),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(12.dp),
                                                verticalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Text("New Promotional Activity", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                OutlinedTextField(
                                                    value = activityName,
                                                    onValueChange = { activityName = it },
                                                    label = { Text("Activity Name", fontSize = 11.sp, color = Color.Gray) },
                                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Violet500, unfocusedBorderColor = Slate700),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    singleLine = true
                                                )
                                                OutlinedTextField(
                                                    value = activityDesc,
                                                    onValueChange = { activityDesc = it },
                                                    label = { Text("Short Description", fontSize = 11.sp, color = Color.Gray) },
                                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Violet500, unfocusedBorderColor = Slate700),
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                                OutlinedTextField(
                                                    value = activitySchedule,
                                                    onValueChange = { activitySchedule = it },
                                                    label = { Text("Schedule (e.g. Saturdays at 5 PM)", fontSize = 11.sp, color = Color.Gray) },
                                                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Violet500, unfocusedBorderColor = Slate700),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    singleLine = true
                                                )
                                                Column {
                                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                        Text("Price Level", color = Color.Gray, fontSize = 11.sp)
                                                        Text("$".repeat(activityPrice), color = Violet500, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                    }
                                                    Slider(
                                                        value = activityPrice.toFloat(),
                                                        onValueChange = { activityPrice = it.toInt() },
                                                        valueRange = 1f..4f,
                                                        steps = 2,
                                                        colors = SliderDefaults.colors(thumbColor = Violet500, activeTrackColor = Violet500)
                                                    )
                                                }
                                                Button(
                                                    onClick = {
                                                        if (activityName.isNotBlank() && activitySchedule.isNotBlank()) {
                                                            viewModel.addActivity(gem.id, activityName, activityDesc, activitySchedule, activityPrice)
                                                            isAddingActivity = false
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Violet500),
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(8.dp)
                                                ) {
                                                    Text("List Experience", fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }

                                if (selectedGemActivities.isEmpty()) {
                                    item {
                                        Text(
                                            text = "No active experiences listed yet by this business.",
                                            color = Color.Gray,
                                            fontSize = 12.sp,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                    }
                                } else {
                                    // Use absolute index items count mapping to guarantee stability in scopes
                                    items(count = selectedGemActivities.size) { index ->
                                        val activity = selectedGemActivities[index]
                                        Card(
                                            colors = CardDefaults.cardColors(containerColor = Slate900),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = activity.activityName,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp
                                                    )
                                                    Text(
                                                        text = "$".repeat(activity.priceLevel),
                                                        color = Violet500,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp
                                                    )
                                                }
                                                Text(text = activity.description, color = Color.LightGray, fontSize = 12.sp)
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(Icons.Default.Schedule, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                                                    Text(text = activity.schedule, color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // REVIEWS & REPLIES
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.Comment, null, tint = Emerald500, modifier = Modifier.size(18.dp))
                                        Text(
                                            text = "Community Reviews (${selectedGemReviews.size})",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Serif,
                                            fontSize = 16.sp
                                        )
                                    }

                                    // Explorers can write reviews
                                    if (activeUser.role == "explorer") {
                                        TextButton(
                                            onClick = {
                                                isWritingReview = !isWritingReview
                                                reviewComment = ""
                                                reviewRating = 5
                                                reviewCrowdDensity = 2
                                            },
                                            colors = ButtonDefaults.textButtonColors(contentColor = Emerald500)
                                        ) {
                                            Icon(
                                                imageVector = if (isWritingReview) Icons.Default.RemoveCircleOutline else Icons.Default.AddComment,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(if (isWritingReview) "Close" else "Write Review", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Write Review Form Panel
                            if (isWritingReview) {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = CardGray),
                                        border = BorderStroke(1.dp, Emerald500.copy(alpha = 0.4f)),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Text("Add Spot Review", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                                            // Rating Stars Selector
                                            Column {
                                                Text("Your Rating", color = Color.Gray, fontSize = 11.sp)
                                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    for (r in 1..5) {
                                                        IconButton(
                                                            onClick = { reviewRating = r },
                                                            modifier = Modifier.size(24.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Star,
                                                                contentDescription = "$r Stars",
                                                                tint = if (r <= reviewRating) Amber500 else Color.DarkGray,
                                                                modifier = Modifier.size(20.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            // Crowd Density Toggle
                                            Column {
                                                Text("Crowd Levels at Spot", color = Color.Gray, fontSize = 11.sp)
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    listOf(1 to "Quiet", 2 to "Moderate", 3 to "Crowded").forEach { (density, text) ->
                                                        Button(
                                                            onClick = { reviewCrowdDensity = density },
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = if (reviewCrowdDensity == density) Emerald500 else Slate900
                                                            ),
                                                            contentPadding = PaddingValues(horizontal = 8.dp),
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .height(32.dp),
                                                            shape = RoundedCornerShape(8.dp)
                                                        ) {
                                                            Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                        }
                                                    }
                                                }
                                            }

                                            OutlinedTextField(
                                                value = reviewComment,
                                                onValueChange = { reviewComment = it },
                                                label = { Text("Comment", fontSize = 11.sp, color = Color.Gray) },
                                                placeholder = { Text("Describe crowd levels, parking options, or overall feel...", color = Color.DarkGray) },
                                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Emerald500, unfocusedBorderColor = Slate700),
                                                modifier = Modifier.fillMaxWidth()
                                            )

                                            Button(
                                                onClick = {
                                                    if (reviewComment.isNotBlank()) {
                                                        viewModel.addReview(gem.id, reviewRating, reviewCrowdDensity, reviewComment)
                                                        isWritingReview = false
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                                                modifier = Modifier.fillMaxWidth(),
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Text("Submit Review", fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }

                            if (selectedGemReviews.isEmpty()) {
                                item {
                                    Text(
                                        text = "No reviews yet. Be the first to review this secret gem!",
                                        color = Color.Gray,
                                        fontSize = 12.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            } else {
                                // Use absolute count index for LazyColumn list items to avoid scope clash
                                items(count = selectedGemReviews.size) { index ->
                                    val review = selectedGemReviews[index]
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Slate900),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .background(Color.Gray.copy(alpha = 0.3f), CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(review.username.take(1), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                    Text(
                                                        text = review.username,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp
                                                    )
                                                }

                                                // Stars row & crowd density
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Row {
                                                        for (r in 1..5) {
                                                            Icon(
                                                                imageVector = Icons.Default.Star,
                                                                contentDescription = null,
                                                                tint = if (r <= review.rating) Amber500 else Color.DarkGray,
                                                                modifier = Modifier.size(12.dp)
                                                            )
                                                        }
                                                    }
                                                    Text(
                                                        text = when (review.crowdDensity) {
                                                            1 -> "Quiet"
                                                            2 -> "Mod"
                                                            else -> "Busy"
                                                        },
                                                        color = when (review.crowdDensity) {
                                                            1 -> Emerald500
                                                            2 -> Amber500
                                                            else -> Color.Red
                                                        },
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }

                                            Text(text = review.comment, color = Color.LightGray, fontSize = 13.sp)

                                            // Reply nested display
                                            if (review.businessReply != null) {
                                                Card(
                                                    colors = CardDefaults.cardColors(containerColor = CardGray),
                                                    shape = RoundedCornerShape(8.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(top = 4.dp)
                                                ) {
                                                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                        ) {
                                                            Icon(Icons.Default.Storefront, null, tint = Violet500, modifier = Modifier.size(14.dp))
                                                            Text("Business Owner Reply", color = Violet500, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                        }
                                                        Text(text = review.businessReply, color = Color.LightGray, fontSize = 12.sp)
                                                    }
                                                }
                                            } else {
                                                // If business role and owns this spot, allow replying
                                                if (activeUser.role == "business" && gem.uploaderId == activeUser.id) {
                                                    if (replyingToReviewId == review.id) {
                                                        Column(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(top = 6.dp),
                                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            OutlinedTextField(
                                                                value = replyText,
                                                                onValueChange = { replyText = it },
                                                                label = { Text("Write response reply...", fontSize = 11.sp, color = Color.Gray) },
                                                                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedBorderColor = Violet500, unfocusedBorderColor = Slate700),
                                                                modifier = Modifier.fillMaxWidth()
                                                            )
                                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                                TextButton(onClick = { replyingToReviewId = null }) {
                                                                    Text("Cancel", color = Color.Gray)
                                                                }
                                                                Button(
                                                                    onClick = {
                                                                        if (replyText.isNotBlank()) {
                                                                            viewModel.submitReply(review.id, replyText)
                                                                            replyingToReviewId = null
                                                                            replyText = ""
                                                                        }
                                                                    },
                                                                    colors = ButtonDefaults.buttonColors(containerColor = Violet500),
                                                                    shape = RoundedCornerShape(6.dp),
                                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                                    modifier = Modifier.height(30.dp)
                                                                ) {
                                                                    Text("Submit Reply", fontSize = 11.sp)
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        TextButton(
                                                            onClick = {
                                                                replyingToReviewId = review.id
                                                                replyText = ""
                                                            },
                                                            colors = ButtonDefaults.textButtonColors(contentColor = Violet500),
                                                            contentPadding = PaddingValues(0.dp),
                                                            modifier = Modifier.height(28.dp)
                                                        ) {
                                                            Icon(Icons.Default.Reply, null, modifier = Modifier.size(12.dp))
                                                            Spacer(modifier = Modifier.width(4.dp))
                                                            Text("Reply to review", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        }
    }
}

// Coordinate based Grid Marker Clustering Algorithm
fun clusterGems(
    gems: List<HiddenGem>,
    centerLat: Double,
    centerLng: Double,
    scale: Float,
    w: Float,
    h: Float,
    clusterDistancePx: Float = 60f
): List<ClusterItem> {
    val result = mutableListOf<ClusterItem>()
    val visited = BooleanArray(gems.size)

    fun getCanvasX(lng: Double, centerLng: Double, scale: Float, width: Float): Float {
        return width / 2f + ((lng - centerLng) * scale).toFloat()
    }

    fun getCanvasY(lat: Double, centerLat: Double, scale: Float, height: Float): Float {
        return height / 2f - ((lat - centerLat) * scale * 1.35f).toFloat()
    }

    for (i in gems.indices) {
        if (visited[i]) continue
        val gem = gems[i]
        val px = getCanvasX(gem.longitude, centerLng, scale, w)
        val py = getCanvasY(gem.latitude, centerLat, scale, h)

        val currentCluster = mutableListOf<HiddenGem>()
        currentCluster.add(gem)
        visited[i] = true

        for (j in (i + 1) until gems.size) {
            if (visited[j]) continue
            val other = gems[j]
            val opx = getCanvasX(other.longitude, centerLng, scale, w)
            val opy = getCanvasY(other.latitude, centerLat, scale, h)

            val distance = sqrt((px - opx) * (px - opx) + (py - opy) * (py - opy))
            if (distance <= clusterDistancePx) {
                currentCluster.add(other)
                visited[j] = true
            }
        }

        if (currentCluster.size > 1) {
            // Group together
            val avgLat = currentCluster.map { it.latitude }.average()
            val avgLng = currentCluster.map { it.longitude }.average()
            result.add(
                ClusterItem.Cluster(
                    id = -i - 1000,
                    latitude = avgLat,
                    longitude = avgLng,
                    items = currentCluster
                )
            )
        } else {
            result.add(ClusterItem.Single(gem))
        }
    }
    return result
}

// Calculate which clustered item was tapped
fun findClickedItem(
    tapOffset: Offset,
    clusteredItems: List<ClusterItem>,
    centerLat: Double,
    centerLng: Double,
    scale: Float,
    w: Float,
    h: Float
): ClusterItem? {
    val clickRadiusPx = 35f

    fun getCanvasX(lng: Double, centerLng: Double, scale: Float, width: Float): Float {
        return width / 2f + ((lng - centerLng) * scale).toFloat()
    }

    fun getCanvasY(lat: Double, centerLat: Double, scale: Float, height: Float): Float {
        return height / 2f - ((lat - centerLat) * scale * 1.35f).toFloat()
    }

    return clusteredItems.firstOrNull { item ->
        val (lat, lng) = when (item) {
            is ClusterItem.Single -> item.gem.latitude to item.gem.longitude
            is ClusterItem.Cluster -> item.latitude to item.longitude
        }
        val px = getCanvasX(lng, centerLng, scale, w)
        val py = getCanvasY(lat, centerLat, scale, h)
        val distance = sqrt((tapOffset.x - px) * (tapOffset.x - px) + (tapOffset.y - py) * (tapOffset.y - py))
        distance <= clickRadiusPx
    }
}
