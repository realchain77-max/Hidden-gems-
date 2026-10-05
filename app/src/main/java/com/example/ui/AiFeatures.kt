package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.HiddenGem
import com.example.data.GemReview
import com.example.ui.theme.*

/**
 * Gemini AI Local Scout Bottom Sheet / Modal
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiScoutBottomSheet(
    viewModel: GemViewModel,
    onDismiss: () -> Unit
) {
    val aiResponse by viewModel.aiScoutResponse.collectAsState()
    val isLoading by viewModel.isAiScoutLoading.collectAsState()
    var userQuery by remember { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    val quickPrompts = listOf(
        "☕ Secret work & coffee spots",
        "🌅 Best secluded sunset viewpoints",
        "🌿 Peaceful forest and nature walks",
        "🍲 Authentic street food & artisanal bites",
        "🗺️ Plan a 1-day hidden gem tour"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F1713),
        contentColor = Color.White,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = Emerald500.copy(alpha = 0.5f))
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with Gemini Branding & Sparkle
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
                            .size(38.dp)
                            .background(
                                Brush.linearGradient(listOf(Emerald500, Color(0xFF00E676))),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Sparkle",
                            tint = Color(0xFF060D09),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Gemini Local Scout",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = "Your AI companion for authentic secrets",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFA6C5B3)
                            )
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.White.copy(alpha = 0.08f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.LightGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Quick Prompt Chips
            Text(
                text = "EXPLORE BY TOPIC",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Emerald400,
                letterSpacing = 1.sp
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickPrompts) { prompt ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF16251D),
                        border = BorderStroke(1.dp, Color(0xFF284838)),
                        modifier = Modifier.clickable {
                            userQuery = prompt
                            viewModel.askAiScout(prompt)
                        }
                    ) {
                        Text(
                            text = prompt,
                            fontSize = 12.sp,
                            color = Color(0xFFD6F5E3),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            // Freeform Input Field
            OutlinedTextField(
                value = userQuery,
                onValueChange = { userQuery = it },
                placeholder = {
                    Text("Ask anything (e.g. 'Quiet rooftop with wifi in Kilimani')", color = Color.Gray, fontSize = 13.sp)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_scout_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Emerald500,
                    unfocusedBorderColor = Color(0xFF284838),
                    focusedContainerColor = Color(0xFF121E18),
                    unfocusedContainerColor = Color(0xFF121E18),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                trailingIcon = {
                    IconButton(
                        onClick = {
                            if (userQuery.isNotBlank() && !isLoading) {
                                viewModel.askAiScout(userQuery)
                            }
                        },
                        enabled = userQuery.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .size(36.dp)
                            .background(if (userQuery.isNotBlank()) Emerald500 else Color.DarkGray, CircleShape)
                            .testTag("ai_scout_submit_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Ask Scout",
                            tint = if (userQuery.isNotBlank()) Color(0xFF060D09) else Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = {
                    if (userQuery.isNotBlank() && !isLoading) {
                        viewModel.askAiScout(userQuery)
                    }
                }),
                singleLine = true
            )

            // Loading Indicator
            AnimatedVisibility(visible = isLoading) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF15261E)),
                    border = BorderStroke(1.dp, Emerald500.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(
                            color = Emerald500,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                        Column {
                            Text(
                                text = "Gemini AI is analyzing authentic local spots...",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Scanning verified GPS coordinates & community reviews",
                                color = Color(0xFFA6C5B3),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // AI Response Display
            AnimatedVisibility(visible = !isLoading && aiResponse != null) {
                aiResponse?.let { responseText ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF14221A)),
                        border = BorderStroke(1.dp, Emerald500.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = Emerald500,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Scout Intel",
                                        fontWeight = FontWeight.Bold,
                                        color = Emerald400,
                                        fontSize = 12.sp
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = {
                                            clipboard.setText(AnnotatedString(responseText))
                                            android.widget.Toast.makeText(context, "Scout tip copied!", android.widget.Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = Color.LightGray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = responseText,
                                color = Color(0xFFE2F1E8),
                                fontSize = 13.sp,
                                lineHeight = 20.sp
                            )

                            // Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onDismiss,
                                    colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(38.dp)
                                ) {
                                    Icon(Icons.Default.Map, null, tint = Color(0xFF060D09), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Explore on Map", color = Color(0xFF060D09), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = { viewModel.clearAiScout() },
                                    border = BorderStroke(1.dp, Color(0xFF284838)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.LightGray),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.height(38.dp)
                                ) {
                                    Text("Clear", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

/**
 * AI Spot Dossier & Insider Lore Section (Inside Gem Detail Sheet)
 */
@Composable
fun AiSpotDossierSection(
    gem: HiddenGem,
    viewModel: GemViewModel
) {
    val dossiers by viewModel.spotDossiers.collectAsState()
    val isGenerating by viewModel.isGeneratingDossier.collectAsState()
    val dossier = dossiers[gem.id]

    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF132018)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.5f)),
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
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color(0xFF00E676).copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF00E676),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Gemini AI Dossier & Secret Lore",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Historical secrets, photo angles & optimal timing",
                            fontSize = 10.sp,
                            color = Color(0xFFA6C5B3)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF00E676).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.5f))
                ) {
                    Text(
                        text = "AI INTEL",
                        color = Color(0xFF00E676),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            if (dossier == null) {
                Button(
                    onClick = { viewModel.generateSpotDossier(gem) },
                    enabled = !isGenerating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E676),
                        contentColor = Color(0xFF060D09)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().height(38.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            color = Color(0xFF060D09),
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Deciphering Local Lore...", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    } else {
                        Icon(Icons.Default.MenuBook, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Generate AI Insider Dossier", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF09140E),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = dossier,
                        color = Color(0xFFE0EFE6),
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}

/**
 * AI Crowd & Atmosphere Vibe Radar Card
 */
@Composable
fun AiVibeRadarCard(
    gem: HiddenGem,
    reviews: List<GemReview>,
    viewModel: GemViewModel
) {
    val vibeSummaries by viewModel.spotVibeSummaries.collectAsState()
    val isGenerating by viewModel.isGeneratingVibeSummary.collectAsState()
    val summary = vibeSummaries[gem.id]

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161F1A)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF2E4637)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = Emerald400,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "AI Atmosphere & Crowd Radar",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }

                if (summary == null) {
                    TextButton(
                        onClick = { viewModel.generateSpotVibeSummary(gem, reviews) },
                        enabled = !isGenerating,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isGenerating) "Analyzing..." else "Analyze Vibe",
                            color = Emerald400,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (summary != null) {
                Text(
                    text = summary,
                    color = Color(0xFFD6F5E3),
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            } else {
                Text(
                    text = "Tap 'Analyze Vibe' to synthesize crowd density and explorer reviews with Gemini AI.",
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }
    }
}

/**
 * AI Day-Trip Itinerary Dialog
 */
@Composable
fun AiItineraryDialog(
    viewModel: GemViewModel,
    onDismiss: () -> Unit
) {
    val itinerary by viewModel.aiItinerary.collectAsState()
    val isGenerating by viewModel.isGeneratingItinerary.collectAsState()
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1713)),
            border = BorderStroke(1.dp, Emerald500.copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
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
                            imageVector = Icons.Default.DirectionsTransit,
                            contentDescription = null,
                            tint = Emerald500,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "AI Secret Day Tour",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Close, null, tint = Color.LightGray, modifier = Modifier.size(18.dp))
                    }
                }

                if (isGenerating) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(color = Emerald500, strokeWidth = 3.dp)
                            Text("Gemini is curating your secret itinerary...", color = Color.LightGray, fontSize = 12.sp)
                        }
                    }
                } else if (itinerary != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF14241B),
                        border = BorderStroke(1.dp, Color(0xFF284838)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = itinerary!!,
                            color = Color(0xFFE2F1E8),
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(14.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                clipboard.setText(AnnotatedString(itinerary!!))
                                android.widget.Toast.makeText(context, "Itinerary copied!", android.widget.Toast.LENGTH_SHORT).show()
                            },
                            border = BorderStroke(1.dp, Emerald500),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Emerald500),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Tour", fontSize = 12.sp)
                        }

                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Start Tour", color = Color(0xFF060D09), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                } else {
                    Button(
                        onClick = { viewModel.generateDayTripItinerary() },
                        colors = ButtonDefaults.buttonColors(containerColor = Emerald500),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(44.dp)
                    ) {
                        Icon(Icons.Default.Route, null, tint = Color(0xFF060D09))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generate 1-Day Secret Tour", color = Color(0xFF060D09), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
