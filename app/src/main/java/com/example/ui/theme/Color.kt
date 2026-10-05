package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Sophisticated Dark Design Palette
val SophisticatedBgBase = Color(0xFF1A1C1E)      // Base background #1A1C1E
val SophisticatedBgDark = Color(0xFF121416)      // Darker under-layer #121416
val SophisticatedSurface = Color(0xFF2F3033)     // Component surface #2F3033
val SophisticatedSecondarySurface = Color(0xFF3D4758) // Secondary Container #3D4758
val SophisticatedPrimary = Color(0xFFD0BCFF)     // Primary Lavender #D0BCFF
val SophisticatedSecondary = Color(0xFFA1C9A1)   // Sage Green #A1C9A1
val SophisticatedTextPrimary = Color(0xFFE2E2E6) // Text Primary #E2E2E6
val SophisticatedTextMuted = Color(0xFFC6C6CA)   // Muted Text #C6C6CA
val SophisticatedBorder = Color(0xFF44474E)      // Borders & Gridlines #44474E

// Map existing variables so references in other files don't break,
// but resolve to the new elegant colors of the Sophisticated Dark theme!
val Slate900 = Color(0xFF1A1C1E) // Maps to BgBase
val Slate800 = Color(0xFF3D4758) // Maps to SecondarySurface
val Slate700 = Color(0xFF44474E) // Maps to Border
val Emerald500 = SophisticatedSecondary // Sage Green for standard explorer pins/labels
val Emerald400 = Color(0xFF34D399)
val Emerald700 = Color(0xFF8BB58B)
val Violet500 = SophisticatedPrimary   // Lavender for business pins/labels
val Violet700 = Color(0xFFB59FFA)
val Amber500 = Color(0xFFE6C100) // Beautiful Gold Star from HTML
val Amber600 = Color(0xFFCCAC00)
val Gray950 = SophisticatedBgBase
val Gray900 = SophisticatedBgDark
val CardGray = SophisticatedSurface

