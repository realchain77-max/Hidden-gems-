package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import kotlinx.coroutines.delay
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
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
import com.example.util.AntiFraudManager
import com.example.util.LiveCaptureProof
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
    val isHeatMapEnabled by viewModel.isHeatMapEnabled.collectAsState()

    // Search and Radius
    val searchQuery by viewModel.searchQuery.collectAsState()
    val radiusLimit by viewModel.radiusLimitInMeters.collectAsState()
    val verifiedFilter by viewModel.verifiedFilter.collectAsState()

    // Form inputs
    var newSpotTitle by remember { mutableStateOf("") }
    var newSpotDescription by remember { mutableStateOf("") }
    var newSpotCategory by remember { mutableStateOf("Scenic") }
    var capturedProof by remember { mutableStateOf<LiveCaptureProof?>(null) }

    // Live Camera Shutter & GPS Registration for new spot uploads (Anti-Fraud)
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val now = System.currentTimeMillis()
            val (gpsLat, gpsLng, acc) = AntiFraudManager.getLiveDeviceCoordinates(context, droppedLat, droppedLng)
            val distance = AntiFraudManager.calculateDistanceMeters(droppedLat, droppedLng, gpsLat, gpsLng)
            val isMatched = distance <= 1500.0
            val hash = AntiFraudManager.generateVerificationHash(gpsLat, gpsLng, now)
            val base64 = AntiFraudManager.bitmapToBase64(bitmap)
            capturedProof = LiveCaptureProof(
                bitmap = bitmap,
                base64 = base64,
                timestamp = now,
                captureLat = gpsLat,
                captureLng = gpsLng,
                accuracyMeters = acc,
                isProximityMatched = isMatched,
                verificationHash = hash
            )
            Toast.makeText(context, "Live Camera Shutter & Hardware GPS Registered!", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Camera permission required for live shutter proof", Toast.LENGTH_LONG).show()
        }
    }

    // Live Camera Shutter & GPS Verification for existing spots (On-Site Verification)
    val verifyExistingCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null && selectedGemId != null) {
            val now = System.currentTimeMillis()
            val currentGem = selectedGem
            val refLat = currentGem?.latitude ?: 0.0
            val refLng = currentGem?.longitude ?: 0.0
            val (gpsLat, gpsLng, acc) = AntiFraudManager.getLiveDeviceCoordinates(context, refLat, refLng)
            val base64 = AntiFraudManager.bitmapToBase64(bitmap)
            viewModel.verifySpotInPerson(
                gemId = selectedGemId!!,
                captureTimestamp = now,
                captureLat = gpsLat,
                captureLng = gpsLng,
                gpsAccuracyMeters = acc,
                photoBase64 = base64
            )
            Toast.makeText(context, "Spot Verified In-Person with Live Camera & GPS!", Toast.LENGTH_LONG).show()
        }
    }

    val verifyExistingPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            verifyExistingCameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Camera permission needed to verify spot on-site", Toast.LENGTH_LONG).show()
        }
    }

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
    var showPwaDialog by remember { mutableStateOf(false) }
    var showAiScoutSheet by remember { mutableStateOf(false) }
    var showAiItineraryDialog by remember { mutableStateOf(false) }

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
            var isTopBarVisible by remember { mutableStateOf(true) }

            // Auto-slide off top bar on app open up / app usage after 4.5 seconds for full map immersion
            LaunchedEffect(Unit) {
                delay(4500)
                isTopBarVisible = false
            }

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
                            if (isTopBarVisible) {
                                isTopBarVisible = false
                            }
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
                // Background terrain - Base Land with dynamic heat/vector dark theme
                val bgColor = if (isHeatMapEnabled) Color(0xFF080D0A) else SophisticatedBgBase
                drawRect(color = bgColor)

                // Draw a sophisticated grid overlay from the design theme
                val gridSize = 80f
                val gridColor = if (isHeatMapEnabled) Color(0x1500E676) else SophisticatedBorder.copy(alpha = 0.15f)
                for (x in 0..width.toInt() step gridSize.toInt()) {
                    drawLine(
                        color = gridColor,
                        start = Offset(x.toFloat(), 0f),
                        end = Offset(x.toFloat(), height),
                        strokeWidth = 1f
                    )
                }
                for (y in 0..height.toInt() step gridSize.toInt()) {
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y.toFloat()),
                        end = Offset(width, y.toFloat()),
                        strokeWidth = 1f
                    )
                }

                // Enhanced Vector Road Network (Arterial Transit Corridors)
                val roadStroke = if (isHeatMapEnabled) Color(0x224E6B56) else Color(0x386B8B73)
                drawLine(
                    color = roadStroke,
                    start = Offset(0f, height * 0.46f),
                    end = Offset(width, height * 0.54f),
                    strokeWidth = 2.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                )
                drawLine(
                    color = roadStroke,
                    start = Offset(width * 0.44f, 0f),
                    end = Offset(width * 0.56f, height),
                    strokeWidth = 2.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                )

                // Vector Waterway & Coastline paths
                val riverPath = Path().apply {
                    moveTo(0f, height * 0.36f)
                    quadraticBezierTo(width * 0.35f, height * 0.42f, width * 0.65f, height * 0.37f)
                    quadraticBezierTo(width * 0.85f, height * 0.30f, width, height * 0.38f)
                }
                drawPath(riverPath, color = Color(0x2A00B0FF), style = Stroke(width = 3.5f, cap = StrokeCap.Round))

                // Render Kenya Regional Parks & Forest reserves when viewing Kenya coordinates
                if (centerLat > -5.0 && centerLat < 1.0 && centerLng > 34.0 && centerLng < 42.0) {
                    // Karura Forest (Nairobi)
                    val kPath = Path()
                    val kCoords = listOf(-1.230 to 36.810, -1.230 to 36.840, -1.255 to 36.840, -1.255 to 36.810)
                    kPath.moveTo(width / 2f + ((kCoords[0].second - centerLng) * scaleFactor).toFloat(), height / 2f - ((kCoords[0].first - centerLat) * scaleFactor * 1.35f).toFloat())
                    for (i in 1..3) {
                        kPath.lineTo(width / 2f + ((kCoords[i].second - centerLng) * scaleFactor).toFloat(), height / 2f - ((kCoords[i].first - centerLat) * scaleFactor * 1.35f).toFloat())
                    }
                    kPath.close()
                    drawPath(path = kPath, color = if (isHeatMapEnabled) Color(0xFF141F17) else Color(0xFF1E2721))

                    // Nairobi National Park Sanctuary
                    val nnpPath = Path()
                    val nnpCoords = listOf(-1.320 to 36.800, -1.320 to 36.950, -1.400 to 36.950, -1.400 to 36.800)
                    nnpPath.moveTo(width / 2f + ((nnpCoords[0].second - centerLng) * scaleFactor).toFloat(), height / 2f - ((nnpCoords[0].first - centerLat) * scaleFactor * 1.35f).toFloat())
                    for (i in 1..3) {
                        nnpPath.lineTo(width / 2f + ((nnpCoords[i].second - centerLng) * scaleFactor).toFloat(), height / 2f - ((nnpCoords[i].first - centerLat) * scaleFactor * 1.35f).toFloat())
                    }
                    nnpPath.close()
                    drawPath(path = nnpPath, color = if (isHeatMapEnabled) Color(0xFF102015) else Color(0xFF1B2A1E))

                    // Watamu / Mida Creek Marine Reserve
                    val mcPath = Path()
                    val mcCoords = listOf(-3.320 to 39.950, -3.320 to 39.980, -3.370 to 39.980, -3.370 to 39.950)
                    mcPath.moveTo(width / 2f + ((mcCoords[0].second - centerLng) * scaleFactor).toFloat(), height / 2f - ((mcCoords[0].first - centerLat) * scaleFactor * 1.35f).toFloat())
                    for (i in 1..3) {
                        mcPath.lineTo(width / 2f + ((mcCoords[i].second - centerLng) * scaleFactor).toFloat(), height / 2f - ((mcCoords[i].first - centerLat) * scaleFactor * 1.35f).toFloat())
                    }
                    mcPath.close()
                    drawPath(path = mcPath, color = if (isHeatMapEnabled) Color(0xFF091D22) else Color(0xFF0F262A))
                }

                // -------------------------------------------------------------
                // RADIANT HEATMAP LAYER (Thermal Density & Activity Blooms)
                // -------------------------------------------------------------
                if (isHeatMapEnabled) {
                    // Thermal connecting isotherms between nearby spots
                    for (i in filteredGems.indices) {
                        val gemA = filteredGems[i]
                        val pxA = width / 2f + ((gemA.longitude - centerLng) * scaleFactor).toFloat()
                        val pyA = height / 2f - ((gemA.latitude - centerLat) * scaleFactor * 1.35f).toFloat()

                        for (j in i + 1 until filteredGems.size) {
                            val gemB = filteredGems[j]
                            val pxB = width / 2f + ((gemB.longitude - centerLng) * scaleFactor).toFloat()
                            val pyB = height / 2f - ((gemB.latitude - centerLat) * scaleFactor * 1.35f).toFloat()

                            val distSq = (pxA - pxB) * (pxA - pxB) + (pyA - pyB) * (pyA - pyB)
                            if (distSq < 220f * 220f) {
                                drawLine(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0x35FF5722), Color(0x35FFD600)),
                                        start = Offset(pxA, pyA),
                                        end = Offset(pxB, pyB)
                                    ),
                                    start = Offset(pxA, pyA),
                                    end = Offset(pxB, pyB),
                                    strokeWidth = 26f,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    }

                    // Multi-tier Gaussian radiant blooms for each spot
                    filteredGems.forEach { gem ->
                        val px = width / 2f + ((gem.longitude - centerLng) * scaleFactor).toFloat()
                        val py = height / 2f - ((gem.latitude - centerLat) * scaleFactor * 1.35f).toFloat()

                        if (px >= -150f && px <= width + 150f && py >= -150f && py <= height + 150f) {
                            val popularityWeight = (gem.upvotes.toFloat() / 140f).coerceIn(1.0f, 2.4f)
                            val baseR = 48f * popularityWeight * zoom.coerceIn(0.8f, 2.0f)

                            // Outer ambient thermal dispersion (Translucent Emerald)
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0x4400E676),
                                        Color(0x1600E676),
                                        Color.Transparent
                                    ),
                                    center = Offset(px, py),
                                    radius = baseR * 1.8f
                                ),
                                radius = baseR * 1.8f,
                                center = Offset(px, py)
                            )

                            // Mid-range activity heat (Amber / Gold)
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0x99FFD600),
                                        Color(0x44FF9100),
                                        Color.Transparent
                                    ),
                                    center = Offset(px, py),
                                    radius = baseR * 1.0f
                                ),
                                radius = baseR * 1.0f,
                                center = Offset(px, py)
                            )

                            // Core thermal hotspot (Radiant Coral / Crimson)
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFFFF3D00),
                                        Color(0xCCFF5722),
                                        Color.Transparent
                                    ),
                                    center = Offset(px, py),
                                    radius = baseR * 0.45f
                                ),
                                radius = baseR * 0.45f,
                                center = Offset(px, py)
                            )
                        }
                    }
                }

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

            // -------------------------------------------------------------
            // ON-SCREEN FLOATING MAP / HEATVIEW TOGGLE PILL
            // -------------------------------------------------------------
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xEE0B140F),
                border = BorderStroke(1.dp, Color(0xFF284838)),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 110.dp, end = 16.dp)
                    .testTag("map_mode_toggle_screen")
            ) {
                Row(
                    modifier = Modifier.padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Vector Map Segment
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (!isHeatMapEnabled) Emerald500 else Color.Transparent)
                            .clickable { viewModel.setHeatMapEnabled(false) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                tint = if (!isHeatMapEnabled) Color(0xFF060D09) else Color.LightGray,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Vector Map",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!isHeatMapEnabled) Color(0xFF060D09) else Color.LightGray
                            )
                        }
                    }

                    // Heat View Segment
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isHeatMapEnabled) Color(0xFFFF5722) else Color.Transparent)
                            .clickable { viewModel.setHeatMapEnabled(true) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Whatshot,
                                contentDescription = null,
                                tint = if (isHeatMapEnabled) Color.White else Color.LightGray,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Heat View 🔥",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isHeatMapEnabled) Color.White else Color.LightGray
                            )
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // FLOATING THERMAL DENSITY LEGEND (WHEN HEAT VIEW IS ACTIVE)
            // -------------------------------------------------------------
            if (isHeatMapEnabled) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xDD090F0C),
                    border = BorderStroke(1.dp, Color(0xFFFF5722).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(bottom = 120.dp, start = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Whatshot, null, tint = Color(0xFFFF5722), modifier = Modifier.size(12.dp))
                            Text("Thermal Density Heatmap", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("Low", color = Color(0xFF00E676), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .width(70.dp)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF00E676), Color(0xFFFFD600), Color(0xFFFF5722), Color(0xFFFF1744))
                                        )
                                    )
                            )
                            Text("Peak", color = Color(0xFFFF1744), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // 2. MAP BOTTOM OVERLAYS (ZOOM & RADIUS SLIDER)
        // -------------------------------------------------------------
        Box(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
                .padding(bottom = 120.dp, start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
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

                    // ---------------------------------------------------------
                    // ANTI-FRAUD LIVE CAMERA & GPS REGISTRATION
                    // ---------------------------------------------------------
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (capturedProof != null) Color(0xFF0A2216) else Color(0xFF141A17)
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (capturedProof != null) Color(0xFF00E676) else Color(0xFF284435)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = if (capturedProof != null) Color(0xFF00E676) else Color(0xFFF5D061),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Anti-Fraud Spot Verification",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }

                                if (capturedProof != null) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF00E676).copy(alpha = 0.2f),
                                        border = BorderStroke(1.dp, Color(0xFF00E676))
                                    ) {
                                        Text(
                                            text = "✓ GPS & TIME LOCKED",
                                            color = Color(0xFF00E676),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Live Shutter Only",
                                        color = Color(0xFFF5D061),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Text(
                                text = "Pre-taken gallery photos are disabled to prevent fake uploads. Proof must be captured live through the camera shutter with real-time GPS coordinates.",
                                color = Color(0xFFA6C5B3),
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )

                            if (capturedProof == null) {
                                Button(
                                    onClick = {
                                        cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF00E676),
                                        contentColor = Color(0xFF060D09)
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().height(44.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Snap Live Camera Proof (Time & GPS)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            } else {
                                // Display Captured Proof & Telemetry
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    capturedProof?.bitmap?.let { bmp ->
                                        Image(
                                            bitmap = bmp.asImageBitmap(),
                                            contentDescription = "Live Proof Photo",
                                            modifier = Modifier
                                                .size(70.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .border(1.dp, Color(0xFF00E676), RoundedCornerShape(10.dp)),
                                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                        )
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Text(
                                            text = "🕒 ${AntiFraudManager.formatTimestamp(capturedProof!!.timestamp)}",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "📍 GPS: ${"%.5f".format(capturedProof!!.captureLat)}, ${"%.5f".format(capturedProof!!.captureLng)}",
                                            color = Color(0xFF00E676),
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Text(
                                            text = "Accuracy: ±${"%.1f".format(capturedProof!!.accuracyMeters)}m • Hash: #${capturedProof!!.verificationHash.take(8)}",
                                            color = Color(0xFFA6C5B3),
                                            fontSize = 10.sp
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Retake",
                                            tint = Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Anti-Fraud Requirement notice
                    if (capturedProof == null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF261A0C),
                            border = BorderStroke(1.dp, Color(0xFFFFB74D).copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB74D),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Anti-Fraud Rule: A live camera shutter with registered hardware GPS is mandatory to upload. Pre-saved gallery uploads are prohibited.",
                                    color = Color(0xFFFFE0B2),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                capturedProof = null
                                viewModel.cancelPinDrop()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                            border = BorderStroke(1.dp, Slate700),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Discard")
                        }
                        Button(
                            onClick = {
                                if (newSpotTitle.isNotBlank() && capturedProof != null) {
                                    viewModel.addGem(
                                        title = newSpotTitle,
                                        description = newSpotDescription,
                                        category = newSpotCategory,
                                        captureTimestamp = capturedProof?.timestamp,
                                        captureLat = capturedProof?.captureLat,
                                        captureLng = capturedProof?.captureLng,
                                        gpsAccuracyMeters = capturedProof?.accuracyMeters,
                                        isLiveVerified = true,
                                        photoBase64 = capturedProof?.base64
                                    )
                                    capturedProof = null
                                }
                            },
                            enabled = newSpotTitle.isNotBlank() && capturedProof != null,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00E676),
                                disabledContainerColor = Slate800
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.5f)
                                .testTag("save_gem_button")
                        ) {
                            Text(
                                text = if (capturedProof != null) "Save Verified Spot 🛡️" else "Live Camera Required",
                                color = if (capturedProof != null) Color(0xFF060D09) else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

                                // Close Map sub-mode
                            }
                            "Discovery" -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                                    DiscoveryScreen(
                                        viewModel = viewModel,
                                        modifier = Modifier.widthIn(max = 840.dp),
                                        onOpenFullMap = { viewModel.setAppMode("Map") },
                                        onOpenPwa = { showPwaDialog = true },
                                        onOpenAiScout = { showAiScoutSheet = true },
                                        onOpenAiTour = { showAiItineraryDialog = true },
                                        onOpenUpload = { activeTab = "Upload" },
                                        onScrollPerused = { isScrollingDown -> isTopBarVisible = !isScrollingDown }
                                    )
                                }
                            }
                            "LocationSensor" -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                                    LocationSensorScreen(
                                        viewModel = viewModel,
                                        modifier = Modifier.widthIn(max = 840.dp),
                                        onScrollPerused = { isScrollingDown -> isTopBarVisible = !isScrollingDown }
                                    )
                                }
                            }
                        }

                        // UNIFIED MINIMALIST TOP HEADER (Bar + Search Interface)
                        AnimatedVisibility(
                            visible = isTopBarVisible && selectedGemId == null,
                            enter = slideInVertically(
                                initialOffsetY = { -it },
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = Spring.DampingRatioLowBouncy)
                            ) + fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)),
                            exit = slideOutVertically(
                                targetOffsetY = { -it },
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            ) + fadeOut(animationSpec = tween(180, easing = FastOutSlowInEasing)),
                            modifier = Modifier.align(Alignment.TopCenter)
                        ) {
                            MinimalistTopHeader(
                                viewModel = viewModel,
                                activeUser = activeUser,
                                searchQuery = searchQuery,
                                isHeatMapEnabled = isHeatMapEnabled,
                                onOpenAiScout = { showAiScoutSheet = true },
                                onOpenInstall = { showPwaDialog = true },
                                onSlideOff = { isTopBarVisible = false }
                            )
                        }

                        // Minimalist Pull-Down Pill when Top Bar & Search are slid off
                        AnimatedVisibility(
                            visible = !isTopBarVisible && selectedGemId == null,
                            enter = slideInVertically(
                                initialOffsetY = { -it },
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            ) + fadeIn(animationSpec = tween(200)),
                            exit = slideOutVertically(
                                targetOffsetY = { -it },
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            ) + fadeOut(animationSpec = tween(150)),
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .statusBarsPadding()
                                .padding(top = 8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xEE11151A),
                                border = BorderStroke(1.dp, Emerald500.copy(alpha = 0.5f)),
                                shadowElevation = 8.dp,
                                modifier = Modifier
                                    .clickable { isTopBarVisible = true }
                                    .testTag("show_top_bar_pill")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Show Search & Controls",
                                        tint = Emerald500,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "Search & Controls ▾",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                    "Favorites" -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                            FavoritesScreen(viewModel = viewModel, modifier = Modifier.widthIn(max = 840.dp))
                        }
                    }
                    "Upload" -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                            UploadGemScreen(
                                viewModel = viewModel,
                                modifier = Modifier.widthIn(max = 840.dp),
                                onSpotUploaded = { newGemId ->
                                    activeTab = "Home"
                                    viewModel.selectGem(newGemId)
                                }
                            )
                        }
                    }
                    "Profile" -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                            ProfileScreen(viewModel = viewModel, modifier = Modifier.widthIn(max = 840.dp))
                        }
                    }
                    "Settings" -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                            SettingsScreen(
                                viewModel = viewModel,
                                modifier = Modifier.widthIn(max = 840.dp),
                                onOpenPwa = { showPwaDialog = true }
                            )
                        }
                    }
                }

                // -------------------------------------------------------------
                // FLOATING SUB-MODE TABS FOR HOME (Airbnb style)
                // -------------------------------------------------------------
                if (activeTab == "Home" && selectedGemId == null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(bottom = 92.dp)
                            .widthIn(max = 560.dp)
                            .shadow(8.dp, RoundedCornerShape(20.dp))
                            .background(Color(0xEE12151A), RoundedCornerShape(20.dp))
                            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)), RoundedCornerShape(20.dp))
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
                // FLOATING GLASSMORPHIC BOTTOM BAR (PHONE, TABLET, DESKTOP UNIFIED)
                // -------------------------------------------------------------
                if (selectedGemId == null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
                            .widthIn(max = 560.dp)
                            .fillMaxWidth()
                            .height(64.dp)
                            .shadow(16.dp, RoundedCornerShape(32.dp))
                            .background(
                                Color(0xEE12151A), // Translucent dark
                                RoundedCornerShape(32.dp)
                            )
                            .border(
                                BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
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

                            // 4. Upload New Hidden Gem (Studio with Real-Time Camera Shutter, AI Mapping & Supabase Sync)
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (activeTab == "Upload") Emerald500
                                        else Color(0xFF14241B)
                                    )
                                    .border(
                                        BorderStroke(
                                            1.5.dp,
                                            if (activeTab == "Upload") Color.White else Emerald500.copy(alpha = 0.7f)
                                        ),
                                        CircleShape
                                    )
                                    .clickable { activeTab = "Upload" }
                                    .testTag("upload_gem_plus_btn"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Upload New Hidden Gem",
                                    tint = if (activeTab == "Upload") SophisticatedBgDark else Emerald500,
                                    modifier = Modifier.size(26.dp)
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
                        .widthIn(max = 720.dp)
                        .fillMaxWidth()
                        .fillMaxHeight(0.65f)
                        .shadow(16.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        // Drag Handle & Top controls
                        val context = LocalContext.current
                        val clipboard = LocalClipboardManager.current

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
                            Row(
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        val shareUrl = "https://hiddengems.app/spot/${gem.id}"
                                        val shareText = "Check out this spot on HiddenGems: ${gem.title} (${gem.category}) - $shareUrl"
                                        clipboard.setText(AnnotatedString(shareUrl))
                                        Toast.makeText(context, "Public spot link copied!", Toast.LENGTH_SHORT).show()
                                        
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                            putExtra(Intent.EXTRA_TITLE, gem.title)
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Share Public Spot Link"))
                                    }
                                ) {
                                    Icon(Icons.Default.Share, "Share Public Link", tint = Emerald500)
                                }

                                IconButton(
                                    onClick = { viewModel.selectGem(null) }
                                ) {
                                    Icon(Icons.Default.Close, "Close Detail", tint = Color.Gray)
                                }
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
                            // Spot Title, Share & Location
                            item {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.weight(1f)
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

                                    // Direct Share Action Button
                                    Button(
                                        onClick = {
                                            val shareUrl = "https://hiddengems.app/spot/${gem.id}"
                                            val shareText = "Check out this spot on HiddenGems: ${gem.title} (${gem.category}) - $shareUrl"
                                            clipboard.setText(AnnotatedString(shareUrl))
                                            Toast.makeText(context, "Public link copied to clipboard!", Toast.LENGTH_SHORT).show()
                                            
                                            val sendIntent = Intent().apply {
                                                action = Intent.ACTION_SEND
                                                putExtra(Intent.EXTRA_TEXT, shareText)
                                                putExtra(Intent.EXTRA_TITLE, gem.title)
                                                type = "text/plain"
                                            }
                                            context.startActivity(Intent.createChooser(sendIntent, "Share Public Spot Link"))
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Default.Share, null, tint = SophisticatedBgDark, modifier = Modifier.size(18.dp))
                                            Text("Share Public Spot Link", color = SophisticatedBgDark, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // -------------------------------------------------
                            // GEMINI AI INSIDER DOSSIER & SECRET LORE
                            // -------------------------------------------------
                            item {
                                AiSpotDossierSection(
                                    gem = gem,
                                    viewModel = viewModel
                                )
                            }

                            // -------------------------------------------------
                            // GEMINI AI CROWD & ATMOSPHERE RADAR
                            // -------------------------------------------------
                            item {
                                AiVibeRadarCard(
                                    gem = gem,
                                    reviews = selectedGemReviews,
                                    viewModel = viewModel
                                )
                            }

                            // -------------------------------------------------
                            // ANTI-FRAUD AUTHENTICITY CERTIFICATE
                            // -------------------------------------------------
                            item {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (gem.isLiveVerified) Color(0xFF0C2417) else Color(0xFF161F1A)
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        if (gem.isLiveVerified) Color(0xFF00E676).copy(alpha = 0.8f) else Color(0xFF2E4637)
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.VerifiedUser,
                                                    contentDescription = null,
                                                    tint = if (gem.isLiveVerified) Color(0xFF00E676) else Color(0xFFF5D061),
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Column {
                                                    Text(
                                                        text = if (gem.isLiveVerified) "Anti-Fraud Authenticated Spot" else "Unverified Community Spot",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                    Text(
                                                        text = if (gem.isLiveVerified) "Live camera shutter & hardware GPS verified" else "Awaiting in-person shutter verification",
                                                        color = Color(0xFFA6C5B3),
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (gem.isLiveVerified) Color(0xFF00E676).copy(alpha = 0.2f) else Color(0xFFF5D061).copy(alpha = 0.2f),
                                                border = BorderStroke(1.dp, if (gem.isLiveVerified) Color(0xFF00E676) else Color(0xFFF5D061))
                                            ) {
                                                Text(
                                                    text = if (gem.isLiveVerified) "LIVE PROOF ✓" else "UNVERIFIED",
                                                    color = if (gem.isLiveVerified) Color(0xFF00E676) else Color(0xFFF5D061),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }

                                        // Photo proof preview if exists
                                        val proofBitmap = remember(gem.photoBase64) {
                                            AntiFraudManager.base64ToBitmap(gem.photoBase64)
                                        }
                                        if (proofBitmap != null) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Image(
                                                    bitmap = proofBitmap.asImageBitmap(),
                                                    contentDescription = "Live Shutter Proof",
                                                    modifier = Modifier
                                                        .size(76.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .border(1.dp, Color(0xFF00E676), RoundedCornerShape(12.dp)),
                                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                                )
                                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                                    Text(
                                                        text = "Live Shutter Photo Taken On-Site",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 11.sp
                                                    )
                                                    Text(
                                                        text = "Gallery upload blocked by security policy",
                                                        color = Color(0xFFA6C5B3),
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        }

                                        // Telemetry Details Grid
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFF06140D),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(10.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("🕒 Capture Time:", color = Color.Gray, fontSize = 10.sp)
                                                    Text(
                                                        text = AntiFraudManager.formatTimestamp(gem.captureTimestamp ?: gem.createdAt),
                                                        color = Color.White,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("📍 Registered GPS:", color = Color.Gray, fontSize = 10.sp)
                                                    Text(
                                                        text = "${"%.5f".format(gem.captureLat ?: gem.latitude)}, ${"%.5f".format(gem.captureLng ?: gem.longitude)}",
                                                        color = Color(0xFF00E676),
                                                        fontSize = 10.sp,
                                                        fontFamily = FontFamily.Monospace,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("🛡️ Anti-Spoofing Audit:", color = Color.Gray, fontSize = 10.sp)
                                                    Text(
                                                        text = if (gem.isLiveVerified) "PASSED (In-Person Hardware GPS)" else "PENDING SHUTTER AUDIT",
                                                        color = if (gem.isLiveVerified) Color(0xFF00E676) else Color(0xFFF5D061),
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }

                                        // Action to verify on-site if not yet verified
                                        if (!gem.isLiveVerified) {
                                            OutlinedButton(
                                                onClick = {
                                                    verifyExistingPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                                },
                                                border = BorderStroke(1.dp, Color(0xFF00E676)),
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E676)),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.fillMaxWidth().height(38.dp)
                                            ) {
                                                Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("I'm Here! Verify with Live Camera", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
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

        // Dedicated PWA Quick-Install & Download Widget Modal
        if (showPwaDialog) {
            PwaInstallDialog(
                onDismissRequest = { showPwaDialog = false },
                pwaUrl = DEFAULT_PWA_URL,
                viewModel = viewModel
            )
        }

        // Gemini AI Local Scout Bottom Sheet
        if (showAiScoutSheet) {
            AiScoutBottomSheet(
                viewModel = viewModel,
                onDismiss = { showAiScoutSheet = false }
            )
        }

        // Gemini AI Day-Trip Itinerary Dialog
        if (showAiItineraryDialog) {
            AiItineraryDialog(
                viewModel = viewModel,
                onDismiss = { showAiItineraryDialog = false }
            )
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

/**
 * Minimalist Top Header: Combines a slim glassmorphic App Bar and single-line Search Capsule.
 * Automatically slides off as the user peruses locations across Discovery, Map, and Proximity Radar.
 */
@Composable
fun MinimalistTopHeader(
    viewModel: GemViewModel,
    activeUser: com.example.data.User,
    searchQuery: String,
    isHeatMapEnabled: Boolean,
    onOpenAiScout: () -> Unit,
    onOpenInstall: () -> Unit,
    onSlideOff: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // Row 1: Minimalist Top Bar (Sleek 36dp glassmorphic pill)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xEE11151A),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.09f)),
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Identity pill: tap to toggle Explorer / Business
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { viewModel.toggleUserRole() }
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                        .testTag("minimal_role_toggle")
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                if (activeUser.role == "explorer") Emerald500 else Violet500,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (activeUser.role == "explorer") Icons.Default.Explore else Icons.Default.Store,
                            contentDescription = null,
                            tint = SophisticatedBgDark,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = activeUser.username,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = (if (activeUser.role == "explorer") Emerald500 else Violet500).copy(alpha = 0.16f),
                        border = BorderStroke(0.5.dp, if (activeUser.role == "explorer") Emerald500 else Violet500)
                    ) {
                        Text(
                            text = if (activeUser.role == "explorer") "Explorer" else "Business",
                            color = if (activeUser.role == "explorer") Emerald500 else Violet500,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }

                // Minimal Action Icons Cluster
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Thermal Heat Toggle
                    IconButton(
                        onClick = { viewModel.toggleHeatMap() },
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(if (isHeatMapEnabled) Color(0x33FF5722) else Color.Transparent)
                            .testTag("minimal_heat_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Whatshot,
                            contentDescription = "Thermal Heatmap",
                            tint = if (isHeatMapEnabled) Color(0xFFFF5722) else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // AI Scout Sparkle
                    IconButton(
                        onClick = onOpenAiScout,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .testTag("minimal_scout_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Scout",
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Instant App / Shortcut Install
                    IconButton(
                        onClick = onOpenInstall,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .testTag("minimal_install_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.InstallMobile,
                            contentDescription = "Install App",
                            tint = Color.LightGray,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    // Slide Off Chevron
                    IconButton(
                        onClick = onSlideOff,
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .testTag("minimal_slide_off_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Slide Off Top Bar",
                            tint = Emerald500,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Row 2: Minimalist Search Capsule (Single-line slim bar)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xEE11151A),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
            shadowElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Emerald500,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = { viewModel.updateSearchQuery(it) },
                    placeholder = {
                        Text(
                            text = "Search hidden gems, cafes, waterfalls...",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    },
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
                        .testTag("minimal_search_input"),
                    singleLine = true
                )
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { viewModel.updateSearchQuery("") },
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.Gray, modifier = Modifier.size(13.dp))
                    }
                }
            }
        }
    }
}
